package cc.twittertools.spider;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        { String category = categoryDir.getFileName().toString();
          try (
            DirectoryStream<Path> us = Files.newDirectoryStream(categoryDir)
          )
          {
            for (Path userFile : us)
            { if (! Files.isRegularFile(userFile))
                continue;
              
            }
          }
        }
      }
    }
    
    
    return this;
  }
}
