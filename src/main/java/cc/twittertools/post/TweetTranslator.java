package cc.twittertools.post;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Simple script to read in JSON formatted tweets and write
 * out tab-delimited tweets (with addressees and hash-tags
 * extracted out for easy analysis. Also writes out tables of
 * incidence of authors and hash-tags. 
 * 
 *
 * @author bfeeney
 * TODO add basic language detection to this via lc4j
 */
public class TweetTranslator implements Callable<Integer>
{
  private final Path inputPath;
  private final Path outputPath;
  
  private final Path authorStatsPath;
  private final Path hashtagStatsPath;
  
  
  public TweetTranslator(Path inputPath, Path outputPath, Path authorStatsPath,
      Path hashtagStatsPath) {
    super();
    this.inputPath = inputPath;
    this.outputPath = outputPath;
    this.authorStatsPath = authorStatsPath;
    this.hashtagStatsPath = hashtagStatsPath;
  }



  /**
   * Translates tweets and writes out tables of stats on
   * tweets per author / per hashtag
   */
  public Integer call() throws Exception 
  { Counts<String> userCounts    = new Counts<>();
    Counts<String> hashTagCounts = new Counts<>();
    int count = 0;
    
    try (
        LineReader  rdr  = new LineReader (inputPath);
        TweetWriter wrtr = new TweetWriter (outputPath);
    )
    {
      Iterator<Tweet> tweets = new TweetReader(rdr);
      
      while (tweets.hasNext())
      { Tweet tweet = tweets.next();
        ++count;
        userCounts.inc(tweet.getUser());
        for (String hashTag : tweet.getHashTags())
          hashTagCounts.inc(hashTag);
        //wrtr.writeTweet(tweet);
      }
      
      userCounts.write(authorStatsPath);
      hashTagCounts.write(hashtagStatsPath);
    }
    return count;
  }
  
  public final static void main (String[] args)
  { Path dataDir  = Paths.get("/local/datasets/twitter/TREC/crawl");
    Path outDir   = Paths.get("/local/datasets/twitter/TREC/crawl.fmt");
    Path userFile = Paths.get("/local/datasets/twitter/TREC/crawl.authors.csv");
    Path hashFile = Paths.get("/local/datasets/twitter/TREC/crawl.hashtags.csv");
    
    TweetTranslator trans = new TweetTranslator(dataDir, outDir, userFile, hashFile);
  }
}
