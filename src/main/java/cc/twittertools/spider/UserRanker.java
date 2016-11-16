package cc.twittertools.spider;

import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.CONNECTION_TIMEOUT;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.IDLE_CONNECTION_TIMEOUT;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.MAX_CONNECTIONS;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.REQUEST_TIMEOUT;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import cc.twittertools.post.Tweet;

import com.google.common.base.Charsets;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ThrottleRequestFilter;

/**
 * Takes in a list of users by category. Re-ranks users within 
 * that category based firstly on how old their account is (measured
 * in months) the on whether their account is more than six months
 * old (higher ranked) or not (lower ranked) and then on the gap between
 * their more recent and 20th most recent tweets (i.e. what span
 * of time is covered by their initial tweets webpage). 
 * @author bfeeney
 *
 */
public class UserRanker
{
  private final static Logger LOG = Logger.getLogger(UserRanker.class);
  
  public static final int STD_TWEETS_PER_PAGE = 20;
  private final static int UPDATE_OUTPUT_INTERVAL = 100;
  
  private final Path inputFile;
  private final Path outputFile;
  private final TweetsHtmlParser htmlParser;
  private final Set<String> distinctUsers;
  private final List<TwitterUser> seedUsers;
  private       List<TwitterUser> annotatedUsers;
  private       long interRequestWaitMs = TimeUnit.SECONDS.toMillis(1);
  
  public UserRanker(Path inputFile, Path outputFile) {
    super();
    this.inputFile     = inputFile;
    this.outputFile    = outputFile;
    this.seedUsers = new LinkedList<>();
    this.annotatedUsers = new LinkedList<>();
    this.htmlParser    = new TweetsHtmlParser();
    this.distinctUsers = new HashSet<>();
  }
  
  public void init() throws IOException
  { // First look at the output file to see which users we've already worked on.
    if (Files.exists(outputFile))
    { try (
        BufferedReader rdr = Files.newBufferedReader(outputFile, Charsets.UTF_8);
      )
      { String line = null;
        while ((line = rdr.readLine()) != null)
        { if ((line = line.trim()).isEmpty() || isHeaderRow (line))
            continue;
        
          TwitterUser user = new TwitterUser (line);
          if (distinctUsers.contains(user.getName()))
            continue; // many seed users may have followed the one fetched user
                      // this is the point where we weed those dupes out.
          annotatedUsers.add(user);
          distinctUsers.add (user.getName());
        }
      }
    }
    
    try (
      BufferedReader rdr = Files.newBufferedReader(inputFile, Charsets.UTF_8);
    )
    { String line = null;
      while ((line = rdr.readLine()) != null)
      { if ((line = line.trim()).isEmpty() || isHeaderRow (line))
          continue;
        
        TwitterUser user = new TwitterUser (line);
        if (distinctUsers.contains(user.getName()))
          continue; // many seed users may have followed the one fetched user
                    // this is the point where we weed those dupes out.
                    // This is also where we avoid making requests for already processed users.
        seedUsers.add (user);
        distinctUsers.add (user.getName());
      }
    }
  }
  
  /* pkg */ final static boolean isHeaderRow (String line)
  { return line.trim().startsWith("Topic\tUsers");
  }
  
  public void call() throws IOException
  { AsyncHttpClient  client = createHttpClient();
    int userCount = 0;
    for (TwitterUser user : seedUsers)
    { ++userCount;
      try
      {
        Future<Response> resp 
          = client.prepareGet("https://twitter.com/" + user.getName())
                  .addHeader("Accept-Charset", "utf-8")
                  .addHeader("Accept-Language", "en-US")
                  .execute();
        
        String htmlBody = resp.get().getResponseBody();
        List<Tweet> tweets = htmlParser.parse(user.getName(), htmlBody);
        
        // Time period covered by the most recent 20 tweets
        DateTime now = DateTime.now();
        Duration interTweetDuration = tweets.size() < (STD_TWEETS_PER_PAGE  - 2) // allow footer and header.
            ? new Duration(Long.MAX_VALUE)
            : new Duration(tweets.get(tweets.size() - 1).getLocalTime(),
                           tweets.get(1).getLocalTime());
            
        user.setRecent20TweetInterval(interTweetDuration);
        annotatedUsers.add (user);
        Thread.sleep(interRequestWaitMs);
        
        LOG.info ("User " + user.getName() + " is " + user.getAgeInMonths() + " months old and has posted 20 tweets in " + interTweetDuration.getStandardHours());
        
        if (userCount % UPDATE_OUTPUT_INTERVAL == 0)
        { Collections.sort(annotatedUsers);
          writeUsersToFile();
        }
      }
      catch (Exception e)
      { LOG.error ("Could not download tweets page for user " + user + " : " + e.getMessage(), e);
      }
    }
    
    Collections.sort(annotatedUsers);
    writeUsersToFile();
  }
  
  private void writeUsersToFile() throws IOException
  {
    try (
      BufferedWriter wtr = Files.newBufferedWriter(outputFile, Charsets.UTF_8, CREATE, APPEND);
    )
    { for (TwitterUser user : annotatedUsers)
      { wtr.write(user.toTabDelimLine());
      }
    }
  }

  /* pkg */ static AsyncHttpClient createHttpClient() 
  { AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
      .addRequestFilter(new ThrottleRequestFilter(MAX_CONNECTIONS))
      .setConnectionTimeoutInMs(CONNECTION_TIMEOUT)
      .setIdleConnectionInPoolTimeoutInMs(IDLE_CONNECTION_TIMEOUT)
      .setRequestTimeoutInMs(REQUEST_TIMEOUT)
      .setMaxRequestRetry(0)
//      .setProxyServer(new ProxyServer ("cornillon.grenoble.xrce.xerox.com", 8000))
      .setFollowRedirects(true)
      .build();
    return new AsyncHttpClient(config);
  }
  
  public static void main (String[] args) throws IOException
  {
    BasicConfigurator.configure();
    String inputFile  = args.length > 0 ? args[0] : "/Users/bryanfeeney/Dropbox/Seeds2016/fetchedusers-trump.csv";
    String outputFile = args.length > 1 ? args[1] : "/Users/bryanfeeney/Dropbox/Seeds2016/fetchedusers-trump-ranked.csv";
    
    UserRanker ranker = new UserRanker(Paths.get(inputFile), Paths.get(outputFile));
    ranker.init();
    ranker.call();
  }
}
