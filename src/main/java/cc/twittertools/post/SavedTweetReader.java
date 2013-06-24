package cc.twittertools.post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import cc.twittertools.spider.IndividualUserTweetsSpider;

/**
 * Reads in tweets stored by {@link IndividualUserTweetsSpider} and its subclasses.
 * This is a slightly different format to that used by {@link TweetWriter}, and
 * also definitely different to the JSON format parsed by {@link TweetReader}
 * @author bryanfeeney
 *
 */
public class SavedTweetReader implements AutoCloseable, Iterator<Tweet>
{
  private static final class TweetsFileIterator implements Iterator<Path>
  { private final Path dir;
    private final String user;
    
    private int count = 0;
    private Path nextPath;

    public TweetsFileIterator(Path dir, String user)
    { super();
      this.dir = dir;
      this.user = user;
      
      nextPath = dir.resolve(user);
    }

    @Override
    public boolean hasNext()
    { return Files.exists(nextPath);
    }

    @Override
    public Path next()
    { Path returnValue = nextPath;
      ++count;
      nextPath = dir.resolve(user + '.' + count);
      
      return returnValue;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
  }
  
  private final LineReader lines;
  private       Tweet      nextTweet = null;
  private       Exception  nextError = null;
  
  public SavedTweetReader (Path directory, String username) throws IOException
  { lines = new LineReader (new TweetsFileIterator(directory, username));
  }

  public SavedTweetReader (Path tweetsFile) throws IOException
  { lines = new LineReader (tweetsFile);
  }
  
  
  @Override
  public void close() throws IOException
  { lines.close();
  }

  @Override
  public boolean hasNext()
  { 
    try
    { while (nextError == null && nextTweet == null && lines.hasNext())
      { String line = lines.next();
        if (line != null)
          nextTweet = Tweet.fromShortTabDelimString(line);
      }
    
      return nextTweet != null;
    }
    catch (Exception e)
    { nextError = e;
      return true;
    }
  }

  @Override
  public Tweet next() {
    if (nextError != null)
    { Exception errVal = nextError;
      nextError        = null;
      throw new RuntimeException ("Error fetching next tweet : " + errVal.getMessage(), errVal);
    }
    
    Tweet retVal = nextTweet;
    nextTweet    = null;
    return retVal;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  
}
