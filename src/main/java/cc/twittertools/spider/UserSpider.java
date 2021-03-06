package cc.twittertools.spider;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Given a map of categories to lists of "seed" users, fetch
 * those user's followers in turn in order to build up an exhaustive
 * list of users.
 * @author bfeeney
 */
public class UserSpider 
{
  private static final DateTime SEED_USER_CREAT_DATE = new DateTime(2012, 01, 01, 00, 01);
  private static final int STD_USER_COUNT_PER_RESPONSE = 20;
  private static final Charset UTF8 = Charset.forName("UTF-8");

  private final static int MAX_ERROR_COUNT = 50;
  private final static int MAX_USERS_PER_CATEGORY = 1500;
  
  private final static Logger log = Logger.getLogger(UserSpider.class);
  
  /* pkg */ final static String ISO8601_FMT_STR = "yyyy-MM-dd'T'HH:mm:ssZ";
  private final SimpleDateFormat ISO8601_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  private final Map<String, List<String>> seedUsersByCategory = new HashMap<>();
  private final Map<String, Long> cursorsPerUser = new HashMap<>();
  private final List<FetchedUser> aggregatedFetchedUsers = new LinkedList<>();
  private final Path outputPath;
  private final Path skippedUsersPath;
  private int numSeedUsers;
  
  public UserSpider(Path outputPath, Path skippedUsersPath)
  { this.outputPath        = outputPath;
    this.skippedUsersPath  = skippedUsersPath;
  }
  
  public void init (Path file) throws IOException
  { 
    try (
      BufferedReader rdr = Files.newBufferedReader(file, UTF8)
    )
    { String line = null;
      while ((line = rdr.readLine()) != null)
      { String[] words = StringUtils.split(line, '\t');
        List<String> seedUsers = Arrays.asList((String[]) ArrayUtils.subarray(words, 1, words.length));
        seedUsersByCategory.put (words[0], seedUsers);
        for (String userName : seedUsers)
          aggregatedFetchedUsers.add (new FetchedUser (words[0], userName, SEED_USER_CREAT_DATE.toDate()));
      }
    }
    numSeedUsers = aggregatedFetchedUsers.size();
  }
  
  public void fetchUsers() throws IOException
  { BlockingRequestCounter reqCounter = new BlockingRequestCounter();
    int errorCount = MAX_ERROR_COUNT;
    int accessErrorCount = numSeedUsers; // access error happens if an account is suspended / deleted
                                         // or if our credentials fail. So if it occurs more than the
                                         // number of accounts, then it's our credentials that are broken
    // We fill up each category, one by one
    //  - To do this we cycle through each users, asking each for 20 users
    //  - Then cycle through users again asking for another 20 users each
    //  - Until we reach the end.
    //  - So no one user dominates the list of followees
    Twitter twitter = createTwitterConnection();
    for (Map.Entry<String, List<String>> catUsers : seedUsersByCategory.entrySet())
    { 
      String       catgy               = catUsers.getKey();
      List<String> seedUsers           = catUsers.getValue();
      Set<String>  exhaustedUsers      = new HashSet<>(catUsers.getValue().size());
      int          numFolloweesFetched = 0;
      
      clearCursors();
      writeFolloweesToFile();
    
      while (numFolloweesFetched < MAX_USERS_PER_CATEGORY && seedUsers.size() != exhaustedUsers.size())
      {
        seedUserLoop:for (String seedUser : seedUsers)
        { if (exhaustedUsers.contains (seedUser))
            continue;
          
          try 
          { 
            try
            { 
              long cursor = getCursor (seedUser);
              reqCounter.incAndWait();
              
              PagableResponseList<User> fetchedUsers 
                = twitter.getFriendsList(seedUser, cursor);
              
              log.info ("Fetched " + fetchedUsers.size() + " followees for user " + seedUser + " in category " + catgy + " to add to the existing " + aggregatedFetchedUsers.size());
              
              if (fetchedUsers.size() < STD_USER_COUNT_PER_RESPONSE)
                exhaustedUsers.add(seedUser);
              setCursor (seedUser, fetchedUsers.getNextCursor());
              
              for (User fetchedUser : fetchedUsers)
              { ++numFolloweesFetched;
                aggregatedFetchedUsers.add (
                  new FetchedUser(
                    catgy,
                    fetchedUser.getScreenName(),
                    fetchedUser.getCreatedAt(),
                    Arrays.asList (new String[] { seedUser, fetchedUser.getScreenName() }),
                    cursor
                  )
                );
              }   
            }
            catch (TwitterException te)
            { if (te.getStatusCode() == 401)
              { --accessErrorCount;
                if (accessErrorCount < 0)
                  throw new RuntimeException("Too many access errors (" + numSeedUsers + ") have occurred, quitting.");
                log.error("Access error occurred downloading followers, skipping user " + seedUser + " : " + te.getMessage(), te);
                logSkippedUser (seedUser);
                exhaustedUsers.add (seedUser);
                continue seedUserLoop;
              }
              throw te;
            }
          }
          catch (Exception te)
          { --errorCount;
            if (errorCount < 0)
              throw new RuntimeException("Too many errors (" + MAX_ERROR_COUNT + ") have occurred, quitting.");
            log.error("Error occurred downloading followers, skipping user " + seedUser + " : " + te.getMessage(), te);
            
            tryToSleepMins(30);
            logSkippedUser (seedUser);
            exhaustedUsers.add (seedUser);
            continue seedUserLoop;
          }         
        }
      }
    }

    writeFolloweesToFile();
  }
  
  /**
   * Appends the user name to the file given by {@link #skippedUsersPath} on a new line.
   * Swallows all exceptions, and logs them.
   * @param user
   */
  private final void logSkippedUser (String user)
  { try (
      BufferedWriter wtr = Files.newBufferedWriter(skippedUsersPath, UTF8, CREATE, APPEND);
    )
    {
      wtr.write(user + '\n');
    }
    catch (Exception e)
    { log.error ("LOGFAIL Could not write out skipped user " + user + " to skipped users file " + skippedUsersPath + " : " + e.getMessage(), e);
    }
    
  }

  /**
   * Try to sleep for the given number of minutes, returning silently if an
   * {@link InterruptedException} is caught.
   */
  private void tryToSleepMins(long sleepTimeMins)
  {
    try
    { Thread.sleep (TimeUnit.MINUTES.toMillis(sleepTimeMins));
    }
    catch (InterruptedException ie)
    { ;
    }
  }

  private Twitter createTwitterConnection() {
    ConfigurationBuilder cb = new ConfigurationBuilder();

    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("JT9JsiJthOpMeKUXa7z9jnKkZ")
      .setOAuthConsumerSecret("ep7POBlAV5HrBWX2qP4ZbVOFf1gtLEIIXWcGxhDiDnti8TBJ6Q")
      .setOAuthAccessToken("139781931-TjdloWfynXg589JdK2tD7y1lrbV4Hr4ekKteK086")
      .setOAuthAccessTokenSecret("yAuB8R1vipw1qpXXNnC4lRo91Nffttkde4Q1oDQXr6QC8")
      .setUseSSL(true);


    
    Twitter twitter = new TwitterFactory(cb.build()).getInstance();
    return twitter;
  }
  
  private long getCursor (String username)
  { Long cursor = cursorsPerUser.get(username);
    return cursor == null ? -1 : cursor.longValue();
  }
  
  private void setCursor (String username, long cursor)
  { cursorsPerUser.put (username, cursor);
  }
  
  private void clearCursors()
  { cursorsPerUser.clear();
  }
  
  private void writeFolloweesToFile() throws IOException
  { 
    try (
      BufferedWriter wtr = Files.newBufferedWriter(outputPath, UTF8)
    )
    {
      for (FetchedUser user : aggregatedFetchedUsers)
      { 
        wtr.write(
          user.getCategory()   + '\t' +
          user.getScreenName() + '\t' +
          ISO8601_FMT.format(user.getSignupDate()) + '\t' +
          user.getCursor()     + '\t' +
          StringUtils.join(user.getAntecedents(), '\t') + '\n'
        );
      }
    }
    
    log.info("Wrote out " + aggregatedFetchedUsers.size() + " users to the file " + outputPath);
  }
  
  /* history failed */
  public static void main (String[] args) throws Exception
  { 
    Path input  = Paths.get(args.length >= 1 ? args[0] : "/Users/bryanfeeney/Desktop/seedusers.csv");
    Path output = Paths.get(args.length >= 2 ? args[1] : "/Users/bryanfeeney/Desktop/fetchedusers.csv");
    Path skips  = Paths.get(args.length >= 3 ? args[2] : "/Users/bryanfeeney/Desktop/skippedusers.csv");
    
    UserSpider spider = new UserSpider (output, skips);
    spider.init(input);
    spider.fetchUsers();
  }
}
