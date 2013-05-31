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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
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
  private final long eveningInterRequestWaitMs = 250; // TimeUnit.SECONDS.toMillis(2);
  private final long dayTimeInterRequestWaitMs = 250; // TimeUnit.SECONDS.toMillis(20);
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
    String responseBody;
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
        
        responseBody = resp.get().getResponseBody();
        List<Tweet> tweets = htmlParser.parse (responseBody);
        Tweet lastTweet    = removeLastAuthoredTweet(user, tweets);
         
        // continue reading until we've gone far enough back in time or we've
        // run out of tweets from the current user.
        while (! tweets.isEmpty() && lastTweet != null && lastTweet.getLocalTime().isAfter(oldestTweet))
        { pauseBetweenRequests();
          
          ++page;
          aggregateTweets.addAll(tweets);
          LOG.debug("Have accumulated " + aggregateTweets.size() + " tweets for user " + user + " after processing page " + page);
          
          resp = httpClient.prepareGet(jsonTweetsUrl(user, lastTweet.getId()))
                  .addHeader("Accept-Charset", "utf-8")
                  .addHeader("Accept-Language", "en-US")
                  .execute();
          
          responseBody = resp.get().getResponseBody();
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
   * Pause for some time to stop saturating the network. The 
   * duration depends on whether we're currently active 
   * during working hours or not.
   * @throws InterruptedException
   */
  private void pauseBetweenRequests() throws InterruptedException
  { DateTime now = new DateTime();
    if (now.getDayOfWeek() < 6 && now.getHourOfDay() >= 8 && now.getHourOfDay() < 19)
      Thread.sleep(dayTimeInterRequestWaitMs);
    else
      Thread.sleep(eveningInterRequestWaitMs);
  }
  
  /**
   * Write all the tweets to a file.
   * @param user the user we're currently considering, determines the 
   * filename
   * @param tweets the user's tweets.
   * @throws IOException
   */
  private void writeTweets(String user, List<Tweet> tweets) throws IOException
  { Path userOutputPath = outputDirectory.resolve(user);
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
        Collections.singletonList("susiebubble"),
        outputDir
    );
    tweetsSpider.call();
  }
}
