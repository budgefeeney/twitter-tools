package cc.twittertools.spider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import cc.twittertools.post.Tweet;

/**
 * Very similar to the superclass, {@link IndividualUserTweetsSpider} except that this
 * assumes we've already downloaded some tweets, and we just continue downloading
 * tweets until we've hit the last tweet downloaded.
 * @author bryanfeeney
 *
 */
public class IndividualUserTweetsUpdater extends IndividualUserTweetsSpider
{

  public IndividualUserTweetsUpdater(Throttle throttle, ProgressMonitor progress, String category,
      List<String> users, Path outputDirectory) {
    super(throttle, progress, category, users, outputDirectory);
    // TODO Auto-generated constructor stub
  }
  
  /**
   * Removes tweets we don't want. In this implementation, this does nothing.
   * @return
   */
  protected List<Tweet> removeUndesireableTweets(List<Tweet> tweets, long lastTweetId) {
    return tweets;
  }
  

  /**
   * Should we download user's tweets or not. In the case of this method this will only return
   * true if we've failed either to download tweets from the minimum number of users, or failed
   * to download the minimum number of tweets thus far.
   */
  protected boolean shouldDownloadUsersTweets(String user) {
    return Files.exists(path, options)
  }
}
