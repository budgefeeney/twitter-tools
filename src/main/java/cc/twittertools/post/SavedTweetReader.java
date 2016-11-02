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
  
  // this breaks the LineReader encapsulation, but we need access to the current
  // filename to know the current user account, as the file contents just
  // contains authors (see Tweet class for more on this)
  private       Path       currentFile;
  private       String     currentAccount;
  
  private       Tweet      nextTweet = null;
  private       Exception  nextError = null;
  
  public SavedTweetReader (Iterator<Path> files) throws IOException
  { lines = new LineReader (files);
  }
  
  public SavedTweetReader (Path directory, String username) throws IOException
  { this (new TweetsFileIterator(directory, username));
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
      	
      	if (currentFile != lines.getCurrentFile())
      	{	currentFile    = lines.getCurrentFile();
      		currentAccount = cc.twittertools.post.old.Tweet.userNameFromFile(currentFile);
      	}
      
        if (line != null)
          nextTweet = Tweet.WRITER.fromTabDelimStr(line);
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
