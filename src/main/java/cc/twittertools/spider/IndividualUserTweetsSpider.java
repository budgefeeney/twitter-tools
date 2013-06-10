package cc.twittertools.spider;

import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.CONNECTION_TIMEOUT;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;

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
 */
@JmxResource(description = "Category tweet download", domainName = "cc.twittertools.spider", folderNames={ "spiders" })
public class IndividualUserTweetsSpider 
extends BaseJmxSelfNaming
implements JmxSelfNaming, Callable<Integer> {
  
  private static final int HTTP_200_OK = 200;

  private final static Logger LOG = Logger.getLogger(IndividualUserTweetsSpider.class);
  
  private final static int MIN_USERS_SPIDERED  = 200;
  private final static int MIN_TWEETS_PER_USER = 1000;
  private final static int MIN_TWEETS_SPIDERED = MIN_USERS_SPIDERED * MIN_TWEETS_PER_USER;
  
  private final static int TIME_LIMIT_MONTHS     = 6;
  private final static int DAYS_PER_MONTH        = 31;
  private final static int AVG_TWEETS_PER_DAY    = 50;
  
  private final static int ESTIMATED_TWEET_COUNT 
    = AVG_TWEETS_PER_DAY * DAYS_PER_MONTH * TIME_LIMIT_MONTHS;
  
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
    { if (spideredUsers >= MIN_USERS_SPIDERED && tweetsDownloaded >= MIN_TWEETS_SPIDERED)
        break;
      
      page = 1;
      try
      { // We may be paused during working hours to avoid saturating the
        // network
        while (paused)
          wait();

        final String pageUrl = "https://twitter.com/" + user;
        responseBody = makeHttpRequest(pageUrl);
        List<Tweet> tweets = htmlParser.parse (responseBody);
        Tweet lastTweet    = removeLastAuthoredTweet(user, tweets);
         
        // continue reading until we've gone far enough back in time or we've
        // run out of tweets from the current user.
        while (! tweets.isEmpty() && lastTweet != null && lastTweet.getLocalTime().isAfter(oldestTweet))
        { throttle.pause();
          
          ++page;
          aggregateTweets.addAll(tweets);
          LOG.debug("Have accumulated " + aggregateTweets.size() + " tweets for user " + user + " after processing page " + page);
          
          responseBody = makeHttpRequest (jsonTweetsUrl(user, lastTweet.getId()), pageUrl);
          tweets = jsonParser.parse(responseBody);
          if (tweets.size() != UserRanker.STD_TWEETS_PER_PAGE)
          { LOG.warn ("Only got " + tweets.size() + " tweets for the most recent request for user " + user + " on page " + page + " with ID " + lastTweet.getId());
            //System.err.println (resp.get().getResponseBody());
          }
          lastTweet = removeLastAuthoredTweet(user, tweets);
          
          if (page % 10 == 0)
            writeTweets (user, aggregateTweets);
        }
        
        ++page;
        aggregateTweets.addAll(tweets);
        writeTweets (user, aggregateTweets);
        
        LOG.info("Finished fetching tweets for user " + user);
        //System.err.println ("Final response body was " + responseBody);
      }
      catch (Exception e)
      { e.printStackTrace();
        LOG.error("Error downloading tweets on page " + page + " for user " + user + " : " + e.getMessage(), e);
        try
        { writeTweets (user, aggregateTweets);
        }
        catch (Exception eio)
        { LOG.error("Error writing tweets for user " + user + " while recovering from previous error : " + eio.getMessage(), eio);
        }
      }
      finally
      { ++spideredUsers;
        tweetsDownloaded += aggregateTweets.size();
        aggregateTweets.clear();
      }
    }
    
    completed = true;
    progress.markCompleted(category, tweetsDownloaded);
    return spideredUsers;
  }

  private String makeHttpRequest(String url) throws IOException, HttpException {
    return makeHttpRequest(url, null);
  }
    
  private String makeHttpRequest(String url, String refUrl) throws IOException, HttpException {
    String responseBody;
    GetMethod req = new GetMethod(url);
    req.addRequestHeader(new Header("Accept-Charset", "utf-8"));
    req.addRequestHeader(new Header("Accept-Language", "en-US,en;q=0.8"));
    req.addRequestHeader(new Header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
    req.addRequestHeader(new Header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/536.30.1 (KHTML, like Gecko) Version/6.0.5 Safari/536.30.1"));
    if (! StringUtils.isBlank(refUrl))
      req.addRequestHeader(new Header("Referer", refUrl));
    req.setFollowRedirects(true);
    
    int respStatusCode = httpClient.executeMethod(req);
    if (respStatusCode != HTTP_200_OK)
      throw new IOException ("Failed to download page, received HTTP response code " + respStatusCode);
            
    responseBody = req.getResponseBodyAsString();
    return responseBody;
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
   * @param user the user we're currently considering, determines the 
   * filename
   * @param tweets the user's tweets.
   * @throws IOException
   */
  private void writeTweets(String user, List<Tweet> tweets) throws IOException
  { Path catOutputDir   = outputDirectory.resolve(category);
    if (! Files.exists(catOutputDir))
      Files.createDirectories(catOutputDir);    
    Path userOutputPath = catOutputDir.resolve(user);
    
    try (
      BufferedWriter wtr = Files.newBufferedWriter(userOutputPath, Charsets.UTF_8);
    )
    { for (Tweet tweet : tweets)
      { wtr.write(
          tweet.getAuthor()
          + '\t' + tweet.getId()
          + '\t' + tweet.getRequestedId()
          + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(tweet.getUtcTime())
          + '\t' + ISODateTimeFormat.dateTimeNoMillis().print(tweet.getLocalTime())
          + '\t' + toTimeZoneString (new Period (tweet.getUtcTime(), tweet.getLocalTime()))
          + '\t' + tweet.getMsg()
          + '\n'
        );
      }
    }
  }
  
  private HttpClient createHttpClient()
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
    final String FMT = "https://twitter.com/i/profiles/show/%1$s/timeline/with_replies?include_available_features=1&include_entities=1&max_id=%2$d";
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

  public static final String toTimeZoneString (Period period)
  {
    
    int hours = period.getDays() * 24 + period.getHours();
    int mins  = period.getMinutes();
    
    // round minutes to the nearest 15min interval
    mins = ((mins + 2) / 15) * 15;
    
    return String.format ("%+03d:%02d", hours, mins);
  }
  

  public static void main (String[] args)
  {    
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.DEBUG);
    Path outputDir = Paths.get("/home/bfeeney/Desktop");
    IndividualUserTweetsSpider tweetsSpider = 
      new IndividualUserTweetsSpider (
        new Throttle(),
        new ProgressMonitor(),
        "misc",
        Collections.singletonList("rtraister"),
        outputDir
    );
    tweetsSpider.call();
  }
}
