package cc.twittertools.post;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Given a directory of (optionally GZipped) JSON files, reads in tweets
 * one by one (as they occur in the file) file by file (files sorted alphanumerically).
 * <p>
 * GZipping is detected simply by the presence of a ".gz" suffix.
 */
public class TweetReader implements Iterator<String>
{
  private final JsonParser       parser;
  private final Iterator<String> lines;
  private       Tweet            nextTweet;
  private       Exception        nextError;
  
  /**
   * Creates a new JsonTweetReader
   * @param path a path to a particular file, or a path to a directory 
   * full of files containing the ".json" or the ".json.gz" prefix 
   * (not case-sensitive)
   * @throws IOException 
   */
  public TweetReader (Path path) throws IOException
  { parser = new JsonParser();
    lines  = Iterators.filter (
      new LineReader(path),
      new Predicate<String>() {
        @Override public boolean apply(@Nullable String arg) {
          return ! StringUtils.isBlank(arg);
        }
      }
    );
  }
  
  /**
   * Creates a new JsonTweetReader
   * @param path a path to a particular file, or a path to a directory 
   * full of files containing the ".json" or the ".json.gz" prefix 
   * (not case-sensitive)
   * @throws IOException 
   */
  public TweetReader (String path) throws IOException
  { this (Paths.get(path));
  }

  /**
   * Unsupported
   * @throws UnsupportedOperationException on every call, as it's not supported
   */
  @Override
  public void remove()
  { throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNext()
  { return lines.hasNext();
  }

  @Override
  public Tweet next()
  { String line = lines.next().trim();
    JsonObject json = (JsonObject) parser.parse(line);
    return 
  }
}
