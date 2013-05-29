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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.ISODateTimeFormat;

import cc.twittertools.post.Tweet;

import com.google.common.base.Charsets;
import com.google.common.collect.ComparisonChain;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
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
  private final static int UPDATE_OUTPUT_INTERVAL = 500;
  
  /**
   * All the information we need about a Twitter user, with a
   * constructor to read in the data written out by {@link UserSpider}.
   * <p>
   * Comparable implementation is first by category, then by creationMonthYear
   * and finally by the recent20TweetInterval
   */
  private final static class TwitterUser implements Comparable<TwitterUser>
  { private final String category;
    private final String name;
    private final String cursor;
    private final List<String> ancestry;
    private final DateTime creationDate;
    private final int ageInMonths;
    private Duration recent20TweetInterval = new Duration(Long.MAX_VALUE);
    
    public TwitterUser (String line)
    { String[] fields = StringUtils.split(line, '\t');
      
      category     = fields[0];      
      name         = fields[1];
      creationDate = ISODateTimeFormat.basicDateTime().parseDateTime(fields[2]);
      cursor       = fields[3];
      
      List<String> anc = new ArrayList<String>(Math.min(1, 4 - fields.length));
      for (int i = 4; i < fields.length; i++)
        if (! StringUtils.isBlank(fields[i]))
          anc.add(fields[i]);
      if (anc.isEmpty())
        anc.add(name);
      
      ancestry = Collections.unmodifiableList(anc);
      
      Interval age = new Interval(creationDate, new DateTime());
      ageInMonths  = age.toPeriod().getMonths();
    }
    
    /**
     * Return the twitter user as a tab delimited line terminated
     * by a newline.
     */
    public String toTabDelimLine()
    { 
      StringBuilder sb = new StringBuilder (150)
        .append (category)
        .append ('\t')
        .append (name)
        .append ('\t')
        .append (ISODateTimeFormat.basicDateTime().print(creationDate))
        .append ('\t')
        .append (recent20TweetInterval.getMillis());
      
      for (String ancestor : ancestry)
        sb.append ('\t').append (ancestor);
      sb.append ('\n');
      
      return sb.toString();
    }

    @Override
    public int compareTo(TwitterUser that)
    { int thisIsOlderThanSixMonths = this.ageInMonths >= 6 ? 0 : 1;
      int thatIsOlderThanSixMonths = that.ageInMonths >= 6 ? 0 : 1;
      
      return ComparisonChain.start()
        .compare(this.category, that.category)
        .compare(thisIsOlderThanSixMonths, thatIsOlderThanSixMonths)
        .compare(this.recent20TweetInterval, that.recent20TweetInterval)
        .result();
    }

    public Duration getRecent20TweetInterval() {
      return recent20TweetInterval;
    }

    public void setRecent20TweetInterval(Duration recent20TweetInterval) {
      this.recent20TweetInterval = recent20TweetInterval;
    }

    public String getCategory() {
      return category;
    }

    public String getName() {
      return name;
    }

    public List<String> getAncestry() {
      return ancestry;
    }

    public DateTime getCreationDate() {
      return creationDate;
    }

    public int getAgeInMonths() {
      return ageInMonths;
    }
  }
  
  
  private final Path inputFile;
  private final Path outputFile;
  private final TweetsHtmlParser htmlParser;
  private final Set<String> distinctUsers;
  private final List<TwitterUser> twitterUsers;
  private       List<TwitterUser> sortedUsers;
  private       long interRequestWaitMs = TimeUnit.SECONDS.toMillis(5);
  
  public UserRanker(Path inputFile, Path outputFile) {
    super();
    this.inputFile     = inputFile;
    this.outputFile    = outputFile;
    this.twitterUsers  = new LinkedList<>();
    this.htmlParser    = new TweetsHtmlParser();
    this.distinctUsers = new HashSet<>();
  }
  
  public void init() throws IOException
  { try (
      BufferedReader rdr = Files.newBufferedReader(inputFile, Charsets.UTF_8);
    )
    { String line = null;
      while ((line = rdr.readLine()) != null)
      { TwitterUser user = new TwitterUser (line);
        if (distinctUsers.contains(user.getName()))
          continue; // many seed users may have followed the one fetched user
                    // this is the point where we weed those dupes out.
        twitterUsers.add (user);
        distinctUsers.add (user.getName());
      }
      sortedUsers = new ArrayList<TwitterUser>(twitterUsers.size());
    }
  }
  
  public void call() throws IOException
  { AsyncHttpClient  client = createHttpClient();
    int userCount = 0;
    for (TwitterUser user : twitterUsers)
    { ++userCount;
      try
      {
        Future<Response> resp 
          = client.prepareGet("http://twitter.com/" + user.getName())
                  .addHeader("Accept-Charset", "utf-8")
                  .addHeader("Accept-Language", "en-US")
                  .execute();
        
        List<Tweet> tweets = htmlParser.parse(user.getName(), resp.get().getResponseBody());
        
        // Time period covered by the most recent 20 tweets
        Duration interTweetDuration = tweets.size() < 20
            ? new Duration(Long.MAX_VALUE)
            : new Duration(tweets.get(0).getTime(), tweets.get(19).getTime());
            
        user.setRecent20TweetInterval(interTweetDuration);
        Thread.sleep(interRequestWaitMs);
        
        if (userCount % UPDATE_OUTPUT_INTERVAL == 0)
        { Collections.sort(twitterUsers);
          writeUsersToFile();
        }
      }
      catch (Exception e)
      { LOG.error ("Could not download tweets page for user " + user + " : " + e.getMessage(), e);
      }
    }
    
    // Re-order by category, then age of user, then user posting intensity
    Collections.sort(twitterUsers);
    writeUsersToFile();
  }
  
  private void writeUsersToFile() throws IOException
  {
    try (
      BufferedWriter wtr = Files.newBufferedWriter(outputFile, Charsets.UTF_8, CREATE, APPEND);
    )
    { for (TwitterUser user : twitterUsers)
      { wtr.write(user.toTabDelimLine());
      }
    }
  }

  private AsyncHttpClient createHttpClient() 
  { AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
      .addRequestFilter(new ThrottleRequestFilter(MAX_CONNECTIONS))
      .setConnectionTimeoutInMs(CONNECTION_TIMEOUT)
      .setIdleConnectionInPoolTimeoutInMs(IDLE_CONNECTION_TIMEOUT)
      .setRequestTimeoutInMs(REQUEST_TIMEOUT)
      .setMaxRequestRetry(0)
      .setProxyServer(new ProxyServer ("cornillon.grenoble.xrce.xerox.com", 8000))
      .build();
    return new AsyncHttpClient(config);
  }
}
