package cc.twittertools.post;

import static cc.twittertools.corpus.data.Status.DATETIME;
import static cc.twittertools.corpus.data.Status.ID;
import static cc.twittertools.corpus.data.Status.MESSAGE;
import static cc.twittertools.corpus.data.Status.REQUESTED_ID;
import static cc.twittertools.corpus.data.Status.TIMESTAMP;
import static cc.twittertools.corpus.data.Status.USER;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Given a directory of (optionally GZipped) JSON files, reads in tweets
 * one by one (as they occur in the file) file by file (files sorted alphanumerically).
 * <p>
 * GZipping is detected simply by the presence of a ".gz" suffix.
 */
public class TweetReader implements Iterator<Tweet>
{
  /* Sample is 3:37 PM - 24 Jan 11 */
  private final DateTimeFormatter TWITTER_FMT =
      DateTimeFormat.forPattern("h:m a - d MMM yy");
  
  private final JsonParser       parser;
  private final Iterator<String> lines;
  
  /**
   * Creates a new JsonTweetReader reading JSON elements from the
   * given iterator. Blank strings are skipped.
   */
  public TweetReader (Iterator<String> jsonIter) throws IOException
  { parser = new JsonParser();
    lines  = Iterators.filter (
      jsonIter,
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
  public TweetReader (Path path) throws IOException
  { this (new LineReader (path));
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
    
    String msg   = json.get(MESSAGE).getAsString();
    String user  = json.get(USER).getAsString();
    String date  = json.get(DATETIME).getAsString();
    long   stamp = json.get(TIMESTAMP).getAsLong(); // relative time since it was downloaded...
    
    Pair<String, List<String>> hashTags   = Sigil.HASH_TAG.extractSigils(msg);
    Pair<String, List<String>> addressees = Sigil.ADDRESSEE.extractSigils(msg);
    Pair<String, List<String>> retweets   = Sigil.RETWEET.extractSigils(msg);
    
    DateTime time = TWITTER_FMT.parseDateTime(date);
    
    return new Tweet(
      /* hashTags = */     Sets.newHashSet(hashTags.getRight()),
      /* user = */         user,
      /* msg = */          msg,
      /* addressees = */   Sets.newHashSet(addressees.getRight()),
      /* id = */           json.get(ID).getAsLong(),
      /* requestedId = */  json.get(REQUESTED_ID).getAsLong(),
      /* isRetweetFromMsg = */ ! retweets.getRight().isEmpty(),
      /* time = */         time
    );
  }
}
