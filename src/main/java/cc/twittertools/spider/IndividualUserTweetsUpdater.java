package cc.twittertools.spider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.twittertools.post.Tweet;

import com.j256.simplejmx.common.JmxResource;

/**
 * Very similar to the superclass, {@link IndividualUserTweetsSpider} except that this
 * assumes we've already downloaded some tweets, and we just continue downloading
 * tweets until we've hit the last tweet downloaded.
 * @author bryanfeeney
 *
 */
@JmxResource(description = "Category tweet update", domainName = "cc.twittertools.spider", folderNames={ "spiders" })
public class IndividualUserTweetsUpdater extends IndividualUserTweetsSpider
{
  private final static Logger LOG = LoggerFactory.getLogger(IndividualUserTweetsUpdater.class);
  
  public IndividualUserTweetsUpdater(Throttle throttle, ProgressMonitor progress, String category,
      List<String> users, Path outputDirectory) {
    super(throttle, progress, category, users, outputDirectory);
  }
  
  /**
   * Removes tweets we don't want. In this implementation, should we detect a 
   * tweet matching the ID of the last tweet stored on file (<tt>lastTweetId</tt>)
   * we remove it and all subsequent tweets. We assume tweets are ordered in 
   * chronological display order, the most recent first.
   */
  @Override
  protected List<Tweet> removeUndesireableTweets(List<Tweet> tweets, long lastTweetId) {
    // Search for tweet matching lastTweetId. Assume tweets are ordered f
    int numTweetsBeforeTweetWithLastID = 0;
    for (Tweet tweet : tweets)
      if (tweet.getId() != lastTweetId)
        ++numTweetsBeforeTweetWithLastID;
      else
        break;
    
    // Delete all tweets after lastTweetId
    while (tweets.size() > numTweetsBeforeTweetWithLastID)
      tweets.remove(tweets.size() - 1);
    
    return tweets;
  }
  

  /**
   * Should we download user's tweets or not. In the case of this method this will only return
   * true if we've failed either to download tweets from the minimum number of users, or failed
   * to download the minimum number of tweets thus far.
   * <p>
   * Note that if the files are user.1, user.2, user.3, user.4 and files 3 and 4 are empty,
   * then {@link #newestTweetsFile(String, StandardOpenOption)} will return user.2 as the
   * last most recent tweets file. Thus the limit is really that there was at least one run 
   * where we  succeeded in downloading tweets and writing them to a file.
   */
  @Override
  protected boolean shouldDownloadUsersTweets(String user) {
    try
    { Path mostRecentlyWrittenFile = newestTweetsFile(user, StandardOpenOption.READ);
      if (Files.exists(mostRecentlyWrittenFile)
          && Files.size(mostRecentlyWrittenFile) >= 10L) // Quick check that a file is not full of blanks or empty
      { return true;
      }
      else
      { 
//      	LOG.info("Won't download new tweets for user " + user);
//        return false;
      	return true;
      }
    }
    catch (IOException ioe)
    { LOG.error("Can't determine most recent saved tweets file for user \"" + user + "\" - " + ioe.getMessage(), ioe);
      
      // can't read the file, so can't proceed with this user
      return false;
    }
  }
}
