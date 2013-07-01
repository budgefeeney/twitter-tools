package cc.twittertools.spider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.j256.simplejmx.server.JmxServer;

/**
 * Given a ranked list of users, grouped by category
 */
public class UserTweetsSpider
{
  private static final Logger LOG = Logger.getLogger(UserTweetsSpider.class);
  
  private static final int THREAD_COUNT = 20;
  private static final int EXPECTED_CAT_COUNT = 70;
  private static final int EXPECTED_USER_COUNT_IN_CAT = 1100;
  
  final Path inputPath;
  final Path excludedCatsPath;
  final Path chosenUsersPath;
  final Path outputDirectoryPath;  
  final Map<String, List<TwitterUser>> users;
  final ExecutorService executor;
  
  
  public UserTweetsSpider(Path inputFile, Path excludedCatsPath ,Path chosenUsersFile, Path outputDirectory)
  { this.inputPath           = inputFile;
    this.excludedCatsPath    = excludedCatsPath;
    this.chosenUsersPath     = chosenUsersFile;
    this.outputDirectoryPath = outputDirectory;
    this.users    = new HashMap<>(EXPECTED_CAT_COUNT);
    this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
  }
  
  public UserTweetsSpider init() throws IOException
  {
    // Make sure the output directory exists
    Files.createDirectories(outputDirectoryPath);
    
    // Read in all the categories we want to exclude
    // (e.g. they've already been read)
    Set<String> excludedCats = excludedCatsPath == null
      ? Collections.<String>emptySet()
      : new HashSet<>(Files.readAllLines(excludedCatsPath, Charsets.UTF_8));
    
    // Read in all the users, but skip those users who have never
    // posted any tweets (users whose inter-tweet time is Long.MAX_VALUE)
    try (
      BufferedReader rdr = Files.newBufferedReader (inputPath, Charsets.UTF_8)
    )
    { String line = null;
      while ((line = rdr.readLine()) != null)
      { if ((line = line.trim()).isEmpty())
          continue;
        
        TwitterUser user = new TwitterUser(line);
        if (excludedCats.contains (user.getCategory()))
        { LOG.info("Skipping user " + user + " as they're in one of the excluded categories " + user.getCategory());
          continue;
        }
        
        if (user.getRecent20TweetInterval().getMillis() == Long.MAX_VALUE)
        { LOG.warn("User " + user.getName() + " in category " + user.getCategory() + " has never posted any tweets");
          continue;
        }
        
        List<TwitterUser> catUsers = users.get(user.getCategory());
        if (catUsers == null)
        { catUsers = new ArrayList<>(EXPECTED_USER_COUNT_IN_CAT);
          users.put (user.getCategory(), catUsers);
        }
        catUsers.add (new TwitterUser (line));
      }
    }
   
    
    // Write out those users we've retained for logging purposes.
    try (
      BufferedWriter wtr = Files.newBufferedWriter(chosenUsersPath, Charsets.UTF_8);
    )
    { for (List<TwitterUser> catUsers : users.values())
        for (TwitterUser user : catUsers)
          wtr.write (user.toTabDelimLine());
    }
    
    
    return this;
  }
  
  public void call() throws JMException, InterruptedException
  { 
    JmxServer jmxServer = new JmxServer(12345);
    jmxServer.start();
    
    Throttle throttle = new Throttle();
    ProgressMonitor progress = new ProgressMonitor();
    
    jmxServer.register(throttle);
    jmxServer.register(progress);
    
    for (Map.Entry<String, List<TwitterUser>> entry : users.entrySet())
    { List<String> userNames = Lists.transform(entry.getValue(), new Function<TwitterUser, String>() {
        @Override public String apply(TwitterUser input){
          return input.getName();
        }
      });
      
      IndividualUserTweetsSpider task = 
        newIndividualSpider(throttle, progress, entry, userNames);
    
      jmxServer.register (task);
      executor.submit(task);
      tryToWait(750, TimeUnit.MILLISECONDS);
    }
    executor.shutdown();
    executor.awaitTermination(31, TimeUnit.DAYS);
    jmxServer.stop();
  }

  protected IndividualUserTweetsSpider newIndividualSpider(Throttle throttle,
      ProgressMonitor progress, Map.Entry<String, List<TwitterUser>> entry, List<String> userNames) {
    return new IndividualUserTweetsSpider(
      throttle,
      progress,
      entry.getKey(),
      userNames,
      outputDirectoryPath
    );
  }

  private void tryToWait(long waitTime, TimeUnit units) {
    try
    { Thread.sleep(units.toMillis(waitTime));
    }
    catch (InterruptedException ie)
    { LOG.warn("Got interrupted while sleeping");
    }
  }
  
  public static void main (String[] args) throws JMException, InterruptedException, IOException
  { BasicConfigurator.configure();
    
    Path inputPath           = Paths.get(args.length > 0 ? args[0] : "/home/bfeeney/Workspace/twitter-tools/src/test/resources/ranked.csv");
    Path chosenUsersPath     = Paths.get(args.length > 1 ? args[1] : "/home/bfeeney/Workspace/twitter-tools/src/test/resources/selectedUserList.csv");
    Path outputDirectoryPath = Paths.get(args.length > 2 ? args[2] : "/home/bfeeney/Workspace/twitter-tools/src/test/resources/spider/");   
    Path excludedCatsPath    = args.length > 3 ? Paths.get(args[3]) : null;
    
    new UserTweetsSpider(
      inputPath,
      excludedCatsPath,
      chosenUsersPath,
      outputDirectoryPath
    ).init().call();
  }
}
