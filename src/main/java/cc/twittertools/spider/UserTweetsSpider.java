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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Given a ranked list of users, grouped by category
 */
public class UserTweetsSpider
{
  private static final Logger LOG = Logger.getLogger(UserTweetsSpider.class);
  
  private static final int THREAD_COUNT = 20;
  private static final int EXPECTED_CAT_COUNT = 70;
  private static final int EXPECTED_USER_COUNT_IN_CAT = 1100;
  private static final int DESIRED_USER_COUNT_IN_CAT  = 100;
  
  private final Path inputPath;
  private final Path chosenUsersPath;
  private final Path outputDirectoryPath;  
  private final Map<String, List<TwitterUser>> users;
  private final ExecutorService executor;
  
  
  public UserTweetsSpider(Path inputFile, Path chosenUsersFile, Path outputDirectory)
  { this.inputPath           = inputFile;
    this.chosenUsersPath     = chosenUsersFile;
    this.outputDirectoryPath = outputDirectory;
    this.users    = new HashMap<>(EXPECTED_CAT_COUNT);
    this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
  }
  
  public void init() throws IOException
  {
    // Make sure the output directory exists
    Files.createDirectories(outputDirectoryPath);
    
    // Read in all the users
    try (
      BufferedReader rdr = Files.newBufferedReader (inputPath, Charsets.UTF_8)
    )
    { String line = null;
      while ((line = rdr.readLine()) != null)
      { if ((line = line.trim()).isEmpty())
          continue;
        
        TwitterUser user = new TwitterUser(line);
        List<TwitterUser> catUsers = users.get(user.getCategory());
        if (catUsers == null)
        { catUsers = new ArrayList<>(EXPECTED_USER_COUNT_IN_CAT);
          users.put (user.getCategory(), catUsers);
        }
        catUsers.add (new TwitterUser (line));
      }
    }
    
    // For each category, only keep the users more than 6 months old with the most tweets
    for (Map.Entry<String, List<TwitterUser>> entry : users.entrySet())
    { List<TwitterUser> catUsers = entry.getValue();
      
      Collections.sort(catUsers);
      while (catUsers.size() > DESIRED_USER_COUNT_IN_CAT)
        catUsers.remove (catUsers.size() - 1);
      
      if (catUsers.size() < DESIRED_USER_COUNT_IN_CAT)
        LOG.warn("Only " + catUsers.size() + " users in the category " + entry.getKey());
    }
    
    // Write out those users we've retained for logging purposes.
    try (
      BufferedWriter wtr = Files.newBufferedWriter(chosenUsersPath, Charsets.UTF_8);
    )
    { for (List<TwitterUser> catUsers : users.values())
        for (TwitterUser user : catUsers)
          wtr.write (user.toTabDelimLine());
    }
  }
  
  public void call()
  {
    for (Map.Entry<String, List<TwitterUser>> entry : users.entrySet())
    { List<String> userNames = Lists.transform(entry.getValue(), new Function<TwitterUser, String>() {
        @Override public String apply(TwitterUser input){
          return input.getName();
        }
      });
      
      executor.submit(new IndividualUserTweetsSpider(userNames, outputDirectoryPath));
    }
  }
  
  public final static void main (String[] args)
  {
    Path inputPath           = Paths.get(args.length > 0 ? args[0] : "/home/bfeeney/Workspace/twitter-tools/src/test/resouces/rankedCandidateUserList.csv");
    Path chosenUsersPath     = Paths.get(args.length > 0 ? args[0] : "/home/bfeeney/Workspace/twitter-tools/src/test/resouces/selectedUserList.csv");
    Path outputDirectoryPath = Paths.get(args.length > 0 ? args[0] : "/home/bfeeney/Workspace/twitter-tools/src/test/resouces/spider/");   
    
    new UserTweetsSpider(inputPath, chosenUsersPath, outputDirectoryPath).call();
  }
}
