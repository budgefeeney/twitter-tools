package cc.twittertools.spider;

import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.CONNECTION_TIMEOUT;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.IDLE_CONNECTION_TIMEOUT;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.MAX_CONNECTIONS;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.REQUEST_TIMEOUT;
import static cc.twittertools.spider.UserRanker.toTwitterUserList;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import cc.twittertools.sink.FileSink;
import cc.twittertools.sink.Sink;
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
import twitter4j.Twitter;

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
public class UserRanker implements Callable<Integer>
{
  private final static Logger LOG = Logger.getLogger(UserRanker.class);
  
  public static final int STD_TWEETS_PER_PAGE = 20;
  private final static int UPDATE_OUTPUT_INTERVAL = 100;
  
  private final Iterable<TwitterUser> inputs;
  private final Sink<TwitterUser> outputSink;
  private final TweetsHtmlParser htmlParser;
  private final Set<String> distinctUsers;
  private       long interRequestWaitMs = TimeUnit.SECONDS.toMillis(1);
  
  public UserRanker(Iterable<TwitterUser> input, Sink<TwitterUser> output) throws IOException {
    super();
    this.inputs        = input;
    this.outputSink    = output;
    this.htmlParser    = new TweetsHtmlParser();
    this.distinctUsers = new HashSet<>();
  }

  public UserRanker(Path inputFile, Path outputFile) throws IOException {
    this (toTwitterUserList(inputFile),
          new FileSink<>(outputFile, TwitterUser::toTabDelimLine, /* flushOnPut = */ true));
  }

  private final static List<TwitterUser> toTwitterUserList(Path inputFile) throws IOException {
    List<TwitterUser> inputs = new ArrayList<>();
    try (BufferedReader rdr = Files.newBufferedReader(inputFile)) {
      String line;
      while ((line = rdr.readLine()) != null) {
        if (! (line = line.trim()).isEmpty() && ! isHeaderRow(line)) {
          inputs.add(new TwitterUser(line));
        }
      }
    }
    return inputs;
  }

  
  /* pkg */ final static boolean isHeaderRow (String line)
  { return line.trim().startsWith("Topic\tUsers");
  }

  @Override
  public Integer call() throws IOException
  { AsyncHttpClient  client = createHttpClient();

    int userCount = 0;
    for (TwitterUser user : inputs)
    { // avoid redoing the same person twice
      if (distinctUsers.contains(user.getName())) {
        continue;
      }
      distinctUsers.add(user.getName());

      // Get the user's webpage
      ++userCount;
      try
      {
        Future<Response> resp 
          = client.prepareGet("https://twitter.com/" + user.getName())
                  .addHeader("Accept-Charset", "utf-8")
                  .addHeader("Accept-Language", "en-US")
                  .execute();
        
        String htmlBody = resp.get().getResponseBody();
        List<Tweet> tweets = htmlParser.parse(user.getName(), htmlBody);
        
        // Determine period covered by the most recent 20 tweets
        DateTime now = DateTime.now();
        Duration interTweetDuration = tweets.size() < (STD_TWEETS_PER_PAGE  - 2) // allow footer and header.
            ? new Duration(Long.MAX_VALUE)
            : new Duration(tweets.get(tweets.size() - 1).getLocalTime(),
                           tweets.get(1).getLocalTime());
        user.setRecent20TweetInterval(interTweetDuration);

        // Write the user out to file
        LOG.info ("User " + user.getName() + " (#" + userCount + ") is " + user.getAgeInMonths() + " months old and has posted 20 tweets in " + interTweetDuration.getStandardHours());
        outputSink.put(user);


        // Sleep a bit before hitting twitter.com again, so we don't get blocked
        Thread.sleep(interRequestWaitMs);
        
      }
      catch (Exception e)
      { LOG.error ("Could not download tweets page for user " + user + " : " + e.getMessage(), e);
      }
    }

    outputSink.close();
    return userCount;
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
  
  public static void main (String[] args) throws Exception
  {
    BasicConfigurator.configure();

    // Read in the configuration, then the inputs.
    String inputFile  = args.length > 0 ? args[0] : "/Users/bryanfeeney/Dropbox/Seeds2016/fetchedusers-trump.csv";
    String outputFile = args.length > 1 ? args[1] : "/Users/bryanfeeney/Dropbox/Seeds2016/fetchedusers-trump-ranked.csv";
    int jobCount      = args.length > 2 ? Integer.parseInt(args[2]) : 4;

    List<TwitterUser> users = toTwitterUserList(Paths.get(inputFile));
    Sink<TwitterUser> output = new FileSink<>(Paths.get(outputFile), TwitterUser::toTabDelimLine, true);

    // Prepare to execute things in parallel
    ExecutorService exec = Executors.newFixedThreadPool(jobCount);
    List<List<TwitterUser>> lists = splitIntoSubLists(users, jobCount);
    List<Future<Boolean>> tasks = new ArrayList<>(jobCount);
    for (int j = 0; j < jobCount; j++) {
      exec.submit(new UserRanker(lists.get(j), output));
    }

    // Wait for them all to finish
    for (int j = 0; j < jobCount; j++) {
      System.out.println ("Job #" + j + " processed " + tasks.get(j).get() + " users out of a total of " + lists.get(j).size());
    }
  }

  private static <T> List<List<T>> splitIntoSubLists(List<T> list, int count) {
    List<List<T>> lists = new ArrayList<>(count);
    int segmentSize = list.size() / count;
    for (int i = 0; i < count - 1; i++) {
      int offset = i * segmentSize;
      lists.add (list.subList(offset, offset + segmentSize));
    }
    lists.add(list.subList((count - 1) * segmentSize, list.size()));
    return lists;
  }
}
