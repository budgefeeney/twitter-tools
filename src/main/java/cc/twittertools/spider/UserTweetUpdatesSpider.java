package cc.twittertools.spider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;

import org.apache.log4j.BasicConfigurator;

import com.google.common.base.Charsets;

/**
 * Reads in the currently tracked users from a directory of their historic tweets,
 * and downloads their latest tweets.
 */
public class UserTweetUpdatesSpider extends UserTweetsSpider
{

  
  public UserTweetUpdatesSpider(Path inputFile, Path excludedCatsPath, Path chosenUsersFile,
      Path outputDirectory) {
    super(inputFile, excludedCatsPath, chosenUsersFile, outputDirectory);
  }

  @Override
  public UserTweetUpdatesSpider init() throws IOException
  { 
    // Read in all the categories we want to exclude
    // (e.g. they've already been updated)
    Set<String> excludedCats = excludedCatsPath == null
      ? Collections.<String>emptySet()
      : new HashSet<>(Files.readAllLines(excludedCatsPath, Charsets.UTF_8));
    
    try (
      DirectoryStream<Path> ds = Files.newDirectoryStream(outputDirectoryPath);
    )
    {
      for (Path categoryDir : ds)
      { if (Files.isDirectory(categoryDir))
        { String category = extractUserName(categoryDir);
          if (excludedCats.contains(category))
            continue; // skip "excluded" categories
          
          try (
            DirectoryStream<Path> us = Files.newDirectoryStream(categoryDir)
          )
          {
            for (Path userFile : us)
            { if (! Files.isRegularFile(userFile))
                continue;
              
              String userName = extractUserName(userFile);
              TwitterUser userAccount = new TwitterUser (userName, category);
              
              addToMultimap (users, category, userAccount);
            }
          }
        }
      }
    }
    
    // Write out those users we've retained for logging purposes.
    if (chosenUsersPath != null)
    { try (
        BufferedWriter wtr = Files.newBufferedWriter(chosenUsersPath, Charsets.UTF_8);
      )
      { for (List<TwitterUser> catUsers : users.values())
          for (TwitterUser user : catUsers)
            wtr.write (user.toTabDelimLine());
      }
    }
    
    return this;
  }

  /**
   * A MultiMap is a map associating a list of values to a single key. This
   * does the tedious job of creating the list if necessary before adding
   * the value to that list.
   */
  private <K, V> void addToMultimap(Map<K, List<V>> multimap, K key, V value) {
    List<V> valueList = multimap.get(value);
    if (valueList == null)
    { valueList = new ArrayList<>();
      multimap.put (key, valueList);
    }
    valueList.add(value);
  }

  /**
   * Given the full path to a file containing a user's tweets, return the
   * username of the user whose tweets are in that file.
   */
  private String extractUserName(Path userFile) {
    return userFile.getFileName().toString().replaceAll("\\.\\d+$", "");
  }
  
  public static void main (String[] args) throws JMException, InterruptedException, IOException
  { BasicConfigurator.configure();
    
    Path inputPath           = Paths.get(args.length > 0 ? args[0] : "/home/bfeeney/Workspace/twitter-tools/src/test/resources/ranked.csv");
    Path chosenUsersPath     = Paths.get(args.length > 1 ? args[1] : "/home/bfeeney/Workspace/twitter-tools/src/test/resources/selectedUserList.csv");
    Path outputDirectoryPath = Paths.get(args.length > 2 ? args[2] : "/home/bfeeney/Workspace/twitter-tools/src/test/resources/spider/");   
    Path excludedCatsPath    = args.length > 3 ? Paths.get(args[3]) : null;
    
    new UserTweetUpdatesSpider(
      inputPath,
      excludedCatsPath,
      chosenUsersPath,
      outputDirectoryPath
    ).init().call();
  }
}
