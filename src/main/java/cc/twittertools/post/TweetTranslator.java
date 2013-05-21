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
  
  private final Path hashAuthorStatsPath;
  
  
  public TweetTranslator(Path inputPath, Path outputPath, Path hashAuthorStatsPath) {
    super();
    this.inputPath = inputPath;
    this.outputPath = outputPath;
    this.hashAuthorStatsPath = hashAuthorStatsPath;
  }



  /**
   * Translates tweets and writes out tables of stats on
   * tweets per author / per hashtag
   */
  public Integer call() throws Exception 
  { Counts<String> hashUserStats = new Counts<>();
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
        printProgress (count);
          
        for (String hashTag : tweet.getHashTags())
          hashUserStats.inc(hashTag + '\t' + tweet.getUser());
        //wrtr.writeTweet(tweet);
      }
      
      hashUserStats.write(hashAuthorStatsPath);
    }
    return count;
  }
  
  private final static void printProgress (int count)
  { if (count == 0)
      return;
  
    if (count % 100000 == 0)
    { System.out.print (" " + count + "\n");
    }
    else
    { if (count % 1000 == 0)
      System.out.print ('.');
    }
    System.out.flush();
  }
  
  public final static void main (String[] args) throws Exception
  { Path dataDir  = Paths.get("/local/datasets/twitter/TREC/crawl");
    Path outDir   = Paths.get("/local/datasets/twitter/TREC/crawl.fmt.csv");
    Path userHashFile = Paths.get("/local/datasets/twitter/TREC/crawl.authors.hash.csv");
    
    TweetTranslator trans = new TweetTranslator(dataDir, outDir, userHashFile);
    System.out.println ("Processed " + trans.call() + " tweets");
  }
}
