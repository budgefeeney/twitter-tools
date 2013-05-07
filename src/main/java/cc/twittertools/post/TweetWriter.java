package cc.twittertools.post;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

/**
 * Writes out tweets in a tab-delimited format. Compound values,
 * such as hash-tags etc, are written out in a comma-delimited
 * format. The text-encoding is UTF-8.
 * <p>
 * So essentially we have a tab-delimited row of comma-delimited
 * fields.
 * <p>
 * Dates are written out in the standard ISO8601 format
 * @author bfeeney
 *
 */
public class TweetWriter implements AutoCloseable
{
  private final BufferedWriter writer;
  private final Joiner tab   = Joiner.on ('\t');
  private final Joiner comma = Joiner.on (',');
  
  public TweetWriter (BufferedWriter writer)
  { this.writer = writer;
  }
  
  public TweetWriter (File file) throws IOException
  { this (Paths.get(file.getAbsolutePath()));
  }
  
  public TweetWriter (Path path) throws IOException
  { this.writer = Files.newBufferedWriter(path, Charsets.UTF_8);
  }
  
  public void writeTweet (Tweet tweet) throws IOException
  { String hashTags   = comma.join(tweet.getHashTags());
    String addressees = comma.join(tweet.getAddressees());
    
    String line = tab.join(
        Long.toString(tweet.getId()),
        Long.toString(tweet.getRequestedId()),
        tweet.getTime(),
        tweet.getUser(),
        tweet.getMsg(),
        hashTags,
        addressees
    );
    
    writer.write(line);
    writer.write('\n');
  }

  @Override
  public void close() throws Exception
  { if (writer != null)
      writer.close();
  }

}
