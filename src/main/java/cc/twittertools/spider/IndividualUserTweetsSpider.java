package cc.twittertools.spider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import cc.twittertools.post.Tweet;

import com.google.common.base.Charsets;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * Takes a list of users, and one by one downloads their tweets, using
 * a synchronous HTTP client, with a 2 second delay between requests.
 * <p>
 * The individual part refers to the fact that we're only downloading
 * tweets for one user at a time, and downloading them serially.
 * <p>
 * See UserTweetsSpider for the code to set all this in motion.
 * @author bfeeney
 *
 */
public class IndividualUserTweetsSpider implements Callable<Integer> {
  
  private final static Logger LOG = Logger.getLogger(IndividualUserTweetsSpider.class);
  
  private final static int TIME_LIMIT_MONTHS     = 6;
  private final static int DAYS_PER_MONTH        = 31;
  private final static int AVG_TWEETS_PER_DAY    = 50;
  
  private final static int ESTIMATED_TWEET_COUNT 
    = AVG_TWEETS_PER_DAY * DAYS_PER_MONTH * TIME_LIMIT_MONTHS;
  
  private final Path outputDirectory;
  private final List<String> users;
  private final AsyncHttpClient httpClient;
  private final long eveningInterRequestWaitMs = TimeUnit.SECONDS.toMillis(2);
  private final long dayTimeInterRequestWaitMs = TimeUnit.SECONDS.toMillis(20);
  private final TweetsHtmlParser htmlParser;
  private final TweetsJsonParser jsonParser;
  private final DateTime oldestTweet;
  
  private       boolean paused = false;
  private       int spideredUsers = 0;
  
  public IndividualUserTweetsSpider(List<String> users, Path outputDirectory)
  { super();
    this.outputDirectory = outputDirectory;
    this.users           = users;
    this.httpClient      = UserRanker.createHttpClient();
    this.htmlParser      = new TweetsHtmlParser();
    this.jsonParser      = new TweetsJsonParser(htmlParser);
    this.oldestTweet     = new DateTime().minusMonths(TIME_LIMIT_MONTHS);
  }
  
  public synchronized Integer call()
  { List<Tweet> aggregateTweets = new ArrayList<>(ESTIMATED_TWEET_COUNT);
    int page;
    for (String user : users)
    { 
      page = 1;
      try
      { // We may be paused during working hours to avoid saturating the
        // network
        while (paused)
          wait();
      
        Future<Response> resp 
          = httpClient.prepareGet("https://twitter.com/" + user)
                  .addHeader("Accept-Charset", "utf-8")
                  .addHeader("Accept-Language", "en-US")
                  .execute();
        
        String htmlBody = resp.get().getResponseBody();
        List<Tweet> tweets = htmlParser.parse (user, htmlBody);
        Tweet lastTweet    = tweets.isEmpty() ? null : tweets.remove(tweets.size() - 1);
         
        // continue reading until we've gone far enough back in time or we've
        // run out of tweets from the current user.
        while (tweets.size() == UserRanker.STD_TWEETS_PER_PAGE
            && lastTweet.getTime().isAfter(oldestTweet))
        { pauseBetweenRequests();
          ++page;
          aggregateTweets.addAll(tweets);
          resp = httpClient.prepareGet(jsonTweetsUrl(user, lastTweet.getId()))
                  .addHeader("Accept-Charset", "utf-8")
                  .addHeader("Accept-Language", "en-US")
                  .execute();
          
          tweets    = jsonParser.parse(user, resp.get().getResponseBody());
          lastTweet = tweets.isEmpty() ? null : tweets.remove(tweets.size() - 1);
        }
        
        aggregateTweets.addAll(tweets);
        writeTweets (aggregateTweets);
      }
      catch (Exception e)
      { LOG.error("Error downloading tweets on page " + page + " for user " + user + " : " + e.getMessage(), e);
        try
        { writeTweets (aggregateTweets);
        }
        catch (Exception eio)
        { LOG.error("Error writting tweets for user " + user + " while recovering from previous error : " + eio.getMessage(), eio);
        }
      }
      finally
      { ++spideredUsers;
        aggregateTweets.clear();
      }
    }
    return spideredUsers;
  }
  
  private void pauseBetweenRequests() throws InterruptedException
  { DateTime now = new DateTime();
    if (now.getDayOfWeek() < 6 && now.getHourOfDay() >= 8 && now.getHourOfDay() < 19)
      Thread.sleep(dayTimeInterRequestWaitMs);
    else
      Thread.sleep(eveningInterRequestWaitMs);
  }
  
  private void writeTweets(List<Tweet> tweets) throws IOException
  { Path userOutputPath = outputDirectory.resolve(tweets.get(0).getUser());
    try (
      BufferedWriter wtr = Files.newBufferedWriter(userOutputPath, Charsets.UTF_8);
    )
    { for (Tweet tweet : tweets)
      { wtr.write(
          tweet.getUser()
          + '\t' + tweet.getId()
          + '\t' + tweet.getRequestedId()
          + '\t' + ISODateTimeFormat.basicDateTimeNoMillis().print(tweet.getTime())
          + '\t' + tweet.getMsg()
        );
      }
    }
  }
  
  private final static String jsonTweetsUrl (String user, long id)
  {
    final String FMT = "https://twitter.com/i/profiles/show/%1$s/timeline/with_replies?include_available_features=1&include_entities=1&max_id=%2$d";
    return String.format(FMT, user, id);
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
  { BasicConfigurator.configure();
    Path outputDir = Paths.get("/home/bfeeney/Desktop");
    IndividualUserTweetsSpider tweetsSpider = 
      new IndividualUserTweetsSpider (
        Collections.singletonList("charlie_whiting"),
        outputDir
    );
    tweetsSpider.call();
  }
  
  
}
