package cc.twittertools.spider;

import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.CONNECTION_TIMEOUT;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.twittertools.post.SavedTweetReader;
import cc.twittertools.post.Tweet;

import com.google.common.base.Charsets;
import com.j256.simplejmx.common.BaseJmxSelfNaming;
import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;
import com.j256.simplejmx.common.JmxSelfNaming;

/**
 * Takes a list of users, and one by one downloads their tweets, using
 * a synchronous HTTP client, with a 2 second delay between requests.
 * <p>
 * The individual part refers to the fact that we're only downloading
 * tweets for one user at a time, and downloading them serially.
 * <p>
 * See UserTweetsSpider for the code to set all this in motion.
 * <p>
 * We continue downloading tweets until we've accumulated a minimum
 * number of tweets in total from a minimum number of users (i.e. one
 * of these minima will likely be exceeded to meet the other).
 * <p>
 * Tweets written out by this should be read in using 
 * {@link SavedTweetReader}
 */
@JmxResource(description = "Category tweet download", domainName = "cc.twittertools.spider", folderNames={ "spiders" })
public class IndividualUserTweetsSpider 
extends BaseJmxSelfNaming
implements JmxSelfNaming, Callable<Integer> {
  
  public static final int HTTP_200_OK = 200;

  private final static Logger LOG = LoggerFactory.getLogger(IndividualUserTweetsSpider.class);
  
  private final static int MIN_USERS_SPIDERED  = 200;
  private final static int MIN_TWEETS_PER_USER = 1000;
  private final static int MIN_TWEETS_SPIDERED = MIN_USERS_SPIDERED * MIN_TWEETS_PER_USER;
  
  private final static int TIME_LIMIT_MONTHS     = 6;
  private final static int DAYS_PER_MONTH        = 31;
  private final static int AVG_TWEETS_PER_DAY    = 50;
  
  private final static int ESTIMATED_TWEET_COUNT 
    = AVG_TWEETS_PER_DAY * DAYS_PER_MONTH * TIME_LIMIT_MONTHS;
  
  private final static long DOWNLOAD_ALL_AVAILABLE_TWEETS = -1;

  public final static Map<String, String> DEFAULT_HEADERS;
  public static final int HTTP_429_TOO_MANY_REQUESTS = 429;
  public static final int RETRY_ATTEMPT_COUNT = 4;

  static {
    DEFAULT_HEADERS = new HashMap<>();
    DEFAULT_HEADERS.put("Accept-Charset", "utf-8");
    DEFAULT_HEADERS.put("Accept-Language", "en-US,en;q=0.8");
    DEFAULT_HEADERS.put("Accept", "Accept\ttext/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    DEFAULT_HEADERS.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/601.7.8 (KHTML, like Gecko) Version/9.1.3 Safari/601.7.8");
  }
  
  private final List<String> users;
  private final HttpClient httpClient;
  private final TweetsHtmlParser htmlParser;
  private final TweetsJsonParser jsonParser;
  private final DateTime oldestTweet;
  
  @JmxAttributeField(description = "Actively spidering users", isWritable = false)
  private       boolean running = false;
  
  @JmxAttributeField(description = "Finished spidering users", isWritable = false)
  private       boolean completed = false;

  @JmxAttributeField(description = "Users Processed", isWritable = false)
  private final Path outputDirectory;
  
  @JmxAttributeField(description = "Users Processed", isWritable = false)
  private final String category;
  
  @JmxAttributeField(description = "Paused", isWritable = false)
  private       boolean paused = false;
  
  @JmxAttributeField(description = "Users Processed", isWritable = false)
  private       int spideredUsers = 0;
  
  @JmxAttributeField(description = "Tweets Downloaded", isWritable = false)
  private       int tweetsDownloaded = 0;
  
  @JmxAttributeField(description = "Users in Category", isWritable = false)
  private       int userCount = 0;
  
  private final Throttle throttle;
  private final ProgressMonitor progress;


  public IndividualUserTweetsSpider(Throttle throttle, ProgressMonitor progress, String category, List<String> users, Path outputDirectory)
  { super();
    this.category        = category;
    this.outputDirectory = outputDirectory;
    this.users           = users;
    this.httpClient      = createHttpClient();
    this.htmlParser      = new TweetsHtmlParser();
    this.jsonParser      = new TweetsJsonParser(htmlParser);
    this.oldestTweet     = new DateTime().minusMonths(TIME_LIMIT_MONTHS);
    this.userCount       = users.size();
    this.throttle        = throttle;
    this.progress        = progress;
    
    this.progress.markPending(category);
  }
  
  public synchronized Integer call()
  { running = true;
    progress.markActive(category);
    
    List<Tweet> aggregateTweets = new ArrayList<>(ESTIMATED_TWEET_COUNT);
    int page;
    String responseBody;
    for (String user : users)
    { Path userOutputPath = null;
      page = 1;
      try
      { LOG.info ("Launching spider for user " + user + " in category " + category);
        userOutputPath = newestTweetsFile(user, StandardOpenOption.CREATE);
        long lastTweetId = readLastTweetId(user);
        LOG.debug("Last tweet ID for user " + user + " in category " + category + " is " + lastTweetId);
        if (! shouldDownloadUsersTweets(user))
          continue;
      
        // We may be paused during working hours to avoid saturating the
        // network
        while (paused)
          wait();

        List<Tweet> tweets;
        final String pageUrl = "https://twitter.com/" + user;
        responseBody = IndividualUserTweetsSpider.requestHttpContent(httpClient, pageUrl);
        tweets = htmlParser.parse (user, responseBody);
        tweets = removeUndesireableTweets (tweets, lastTweetId);
        Tweet lastTweet = removeLastAuthoredTweet (user, tweets);
         
        // continue reading until we've gone far enough back in time or we've
        // run out of tweets from the current user.
        while (! tweets.isEmpty() && lastTweet != null && lastTweet.getLocalTime().isAfter(oldestTweet))
        { throttle.pause();
          
          ++page;
          aggregateTweets.addAll(tweets);
          LOG.debug("Have accumulated " + aggregateTweets.size() + " tweets for user " + user + " in category " + category + " after processing page " + page);

          // Code first, real second
          // https://twitter.com/i/profiles/show/rtraister/timeline/tweets?composed_count=0&include_available_features=1&include_entities=1&include_new_items_bar=true&interval=30000&latent_count=0&min_position=793275015328329728
          // https://twitter.com/i/profiles/show/rtraister/timeline/tweets?include_available_features=1&include_entities=1&max_position=793210916678541312&reset_error_state=false
          String jsonUrl = jsonTweetsUrl(user, lastTweet.getId());
          responseBody = requestHttpContent(httpClient, jsonUrl, pageUrl);
          tweets = jsonParser.parse(user, responseBody);
          tweets = removeUndesireableTweets(tweets, lastTweetId);
          if (tweets.size() != UserRanker.STD_TWEETS_PER_PAGE)
          { LOG.warn ("Only got " + tweets.size() + " tweets for the most recent request for user " + user + " in category " + category + " on page " + page + " with ID " + lastTweet.getId());
            //System.err.println (resp.get().getResponseBody());
          }
          lastTweet = removeLastAuthoredTweet(user, tweets);
          
          if (page % 10 == 0)
            writeTweets (userOutputPath, aggregateTweets);
        }
        
        ++page;
        aggregateTweets.addAll(tweets);
        
        LOG.info("Finished fetching tweets for user " + user + " in category " + category);
        //System.err.println ("Final response body was " + responseBody);
      }
      catch (Exception e)
      { e.printStackTrace();
        LOG.error("Error downloading tweets on page " + page + " for user " + user + " in category " + category + " : " + e.getMessage(), e);
      }
      finally
      { ++spideredUsers;
        tweetsDownloaded += aggregateTweets.size();

        try
        { if (! aggregateTweets.isEmpty() && userOutputPath != null)
            writeTweets (userOutputPath, aggregateTweets);
          if (aggregateTweets.isEmpty())
            LOG.warn("Found no tweets for user " + user + " in category " + category);
        }
        catch (Exception eio)
        { LOG.error("Error writing tweets for user " + user + " in category " + category + " while recovering from previous error : " + eio.getMessage(), eio);
        }
        aggregateTweets.clear();
      }
    }
    
    completed = true;
    progress.markCompleted(category, tweetsDownloaded);
    return spideredUsers;
  }

  /**
   * Removes tweets we don't want. In this implementation, this does nothing.
   * @return
   */
  protected List<Tweet> removeUndesireableTweets(List<Tweet> tweets, long lastTweetId) {
    return tweets;
  }

  /**
   * Should we download user's tweets or not. In the case of this method this will only return
   * true if we've failed either to download tweets from the minimum number of users, or failed
   * to download the minimum number of tweets thus far.
   */
  protected boolean shouldDownloadUsersTweets(String user) {
    return spideredUsers < MIN_USERS_SPIDERED || tweetsDownloaded < MIN_TWEETS_SPIDERED;
  }

  public static String requestHttpContent (HttpClient httpClient, String url) throws IOException {
    return requestHttpContent(httpClient, url, null);
  }

  public static GetMethod requestHttpGet(HttpClient httpClient, String url, String referrerUrl) throws IOException {
    return requestHttpGet(httpClient, url, referrerUrl, 0);
  }

  public static GetMethod requestHttpGet(HttpClient httpClient, String url, String referrerUrl, long attempt) throws IOException {
    GetMethod req = prepareGetRequest (url, referrerUrl);
    int respStatusCode = httpClient.executeMethod(req);
    if (respStatusCode == HTTP_429_TOO_MANY_REQUESTS) {
      if (attempt >= RETRY_ATTEMPT_COUNT) {
        throw new IOException("Failed to download page, received HTTP response code 429 after " + RETRY_ATTEMPT_COUNT + " attempts");
      } else {
        try {
          Thread.sleep(TimeUnit.MINUTES.toMillis(16));
          return requestHttpGet(httpClient, url, referrerUrl, attempt + 1);
        } catch (InterruptedException e) {
          throw new IOException("Failed to download page, interrupted when backing out on the " + attempt + "-th attempt");
        }
      }
    }
    if (respStatusCode != HTTP_200_OK)
      throw new IOException("Failed to download page, received HTTP response code " + respStatusCode + ". Page was " + req.getURI().toString());

    return req;
  }

  public static GetMethod prepareGetRequest (String url, String referrerUrl) throws IOException {
    GetMethod req = new GetMethod(url);
    DEFAULT_HEADERS.entrySet().forEach(
      header -> req.addRequestHeader(new Header(header.getKey(), header.getValue()))
    );
    if (!StringUtils.isBlank(referrerUrl))
      req.addRequestHeader(new Header("Referer", referrerUrl));
    req.setFollowRedirects(true);
    req.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

    return req;
  }

  public static String requestHttpContent(HttpClient httpClient, String url, String referrerUrl) throws IOException {
    return requestHttpGet(httpClient, url, referrerUrl).getResponseBodyAsString();
  }

  /**
   * Working from the final tweet, removes all tweets not authored
   * by the given user. Then removes one tweet, at the end, authored
   * by the given user, and returns it. This is to facilitate the
   * creation of URLs to fetch more tweets.
   * @param author the author whose tweet we search for at the end
   * of the given list.
   * @param tweets the list of tweets that is <strong>TRUNCATED</strong>
   * by this method
   * @return the last tweet originally (but no longer) in the list of
   * tweets to be authored by the given author
   */
  private Tweet removeLastAuthoredTweet(String author, List<Tweet> tweets)
  { Tweet lastTweet = null;
    while (! tweets.isEmpty())
    { lastTweet = tweets.remove (tweets.size() - 1);
      if (lastTweet.getAuthor().equals (author))
        return lastTweet;
    }
    
    return lastTweet;
  }

  
  /**
   * Write all the tweets to a file.
   * @param tweets the user's tweets.
   * @throws IOException
   */
  private void writeTweets(Path userOutputPath, List<Tweet> tweets) throws IOException
  { try (
      BufferedWriter wtr = Files.newBufferedWriter(userOutputPath, Charsets.UTF_8);
    )
    { Tweet.WRITER.writeAllAsTabDelim(wtr, tweets.iterator());
      LOG.info("Wrote " + tweets.size() + " tweets to file " + userOutputPath.toFile().getName() + " in directory " + userOutputPath.toFile().getParentFile().getName());
    }
  }
  
  /**
   * If it exists, read the most recent user file, and find the ID of their most recent
   * tweet.
   * @throws IOException 
   */
  private long readLastTweetId(String user) throws IOException
  { Path path = newestTweetsFile (user, StandardOpenOption.READ);
    if (! Files.exists (path))
      return DOWNLOAD_ALL_AVAILABLE_TWEETS;
    
    try (
      SavedTweetReader rdr = new SavedTweetReader (path);
    )
    { while (rdr.hasNext())
      { long id = rdr.next().getId();
        if (id != TweetsHtmlParser.SUSPECTED_ADVERT_TWEET_ID) {
          return id;
        }
      }
      return DOWNLOAD_ALL_AVAILABLE_TWEETS;
    }
  }
  
  /**
   * Returns the path from which we can either read the user's most recent
   * tweets, or to which we can write the tweets we've just downloaded,
   * the action specified by the third {@link OpenOption} parameter which
   * can be READ, APPEND, or CREATE. For CREATE we create a new file, based
   * on the old file name, with a period and then a number appended to the
   * end of the name.
   * @param user the user whose tweets are being read or written.
   * @param openOption whether the file should alreadsy exists (READ or 
   * APPEND) or whether we should create a new file (CREATE). New files are
   * named with number suffixes so they never overwrite exting files.
   * @return the path to a file containing a users tweets.
   * @throws IOException 
   */
  protected Path newestTweetsFile(String user, StandardOpenOption openOption) throws IOException
  { return newestTweetsFile(user, category, outputDirectory, openOption);
  }
  
  /**
   * Returns the path from which we can either read the user's most recent
   * tweets, or to which we can write the tweets we've just downloaded,
   * the action specified by the third {@link OpenOption} parameter which
   * can be READ, APPEND, or CREATE. For CREATE we create a new file, based
   * on the old file name, with a period and then a number appended to the
   * end of the name.
   * @param user the user whose tweets are being read or written.
   * @param openOption whether the file should alreadsy exists (READ or 
   * APPEND) or whether we should create a new file (CREATE). New files are
   * named with number suffixes so they never overwrite exting files.
   * @return the path to a file containing a users tweets.
   * @throws IOException 
   */
  /* pkg */ static Path newestTweetsFile(String user, String category, Path outputDirectory, StandardOpenOption openOption) throws IOException 
  { Path catOutputDir = outputDirectory.resolve(category);
    if (! Files.exists(catOutputDir))
      Files.createDirectories(catOutputDir);   
    
    // Iterate until we've found the most recent pre-existing file, 
    // and a suitable path for the next new file to create
    //   In the special case of a first-time write existingUserPath
    // won't actually exist.
    Path newUserPath = catOutputDir.resolve(user);
    Path existingUserPath = null;
    int i = 0;
    do
    { existingUserPath = newUserPath;
      newUserPath = catOutputDir.resolve (user + '.' + (++i));
    }
    while (Files.exists(newUserPath) && Files.size(newUserPath) > 10); // basically the file shouldn't be empty, but there may be UTF markers and a stray newline there...
    
    // Return the appropriate file based on the open criteria
    switch (openOption)
    {
      case READ:
      case APPEND:
      { return existingUserPath;
      }
      case CREATE:
      { return newUserPath;
      }
      default:
        throw new IllegalArgumentException ("The only open options allowed are READ, APPEND and CREATE as defined in StandardOpenOption. You specified " + openOption);
    }
  }

  public static HttpClient createHttpClient()
  { HttpClientParams params = new HttpClientParams();
    params.setConnectionManagerTimeout(CONNECTION_TIMEOUT);
    params.setSoTimeout(CONNECTION_TIMEOUT);
    
    return new HttpClient (params);
  }
  
  @Override
  public String getJmxNameOfObject()
  { StringBuilder sb = new StringBuilder (category.length());
    for (int i = 0; i < category.length(); i++)
    { char c = category.charAt(i);
      if (Character.isJavaIdentifierPart(c))
        sb.append(c);
    }
    return this.getClass().getSimpleName() + '-' + sb.toString();
  }

  /**
   * Creates the URL from which the next batch of tweets can be
   * fetched. The returned results is in JSON.
   * @param user
   * @param id
   * @return
   */
  private final static String jsonTweetsUrl (String user, long id)
  {
    //final String FMT  = "https://twitter.com/i/profiles/show/%1$s/timeline/tweets?composed_count=0&include_available_features=1&include_entities=1&include_new_items_bar=true&interval=30000&latent_count=0&min_position=%2$d\n";
    final String FMT = "https://twitter.com/i/profiles/show/%1$s/timeline/tweets?include_available_features=1&include_entities=1&max_position=%2$d&reset_error_state=false";
    return String.format(FMT, user, id);
  }
  
  @JmxOperation(description = "Pause this downloader or vice versa")
  public synchronized void togglePaused()
  { paused = ! paused;
    notifyAll();
  }
  
  
  public synchronized boolean isPaused()
  { return paused;
  }

  
  public synchronized void setPaused(boolean paused)
  { this.paused = paused;
    notifyAll();
  }
  
  public synchronized boolean isCompleted()
  { return spideredUsers == users.size();
  }

  

  public static void main (String[] args)
  {    
    BasicConfigurator.configure();
    org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
    Path outputDir = Paths.get("/Users/bryanfeeney/Desktop");
    IndividualUserTweetsSpider tweetsSpider = 
      new IndividualUserTweetsSpider (
        new Throttle(),
        new ProgressMonitor(),
        "misc",
        //Collections.singletonList("charlie_whiting"),
        Collections.singletonList("rtraister"),
        outputDir
    );
    tweetsSpider.call();
  }
}
