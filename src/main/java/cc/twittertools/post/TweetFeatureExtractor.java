package cc.twittertools.post;

import java.nio.file.Path;

import org.joda.time.DateTime;

/**
 * Extracts paired features from tweets: one references the "text", one references the "event"
 * 
 * The text features will include normal words, and optionally addressees and hash-tags. Hashtags
 * can be encoded as normal words (i.e. with the hash marker removed) or as special words (i.e
 * retaining the hash-marker).
 * 
 * The event features contain the user ID, the post date, and optionally whether it was a retweet,
 * the addressee identifier and [TODO the list of people the user follows.]
 * @author bryanfeeney
 *
 */
public class TweetFeatureExtractor
{
  
  /** Remove all addressee tokens. Otherwise they get encoded in the BOW vector like any word (albeit with the addressee marker still included) */
  private boolean stripAddresseesFromText = false;
  
  /** If true remove all hashtags from text */
  private boolean stripHashTagsFromText   = false;
  
  /** If true remove all retweet markers from text */
  private boolean stripRtMarkersFromText  = false;
  
  /** If true, don't include retweets in the output - skip them entirely */
  private boolean stripRetweets           = false;
  
  /** If true, strip the hash tag marker from hashtags before tokenizing. Note hashtags, left alone, will not be stemed */
  private boolean treatHashTagsAsWords    = false;
  
  /** If true create separate pairs of event and text matrices per author (e.g. for author topic modelling) */
  private boolean aggregateByAuthor       = false;
  
  /** The minimum (inclusive) date for tweets. Tweets before this date are not included in the output. */
  private DateTime minDateIncl;
  
  /** The maximum (exclusive) date for tweets. Tweets on or after this date are not included in the output */
  private DateTime maxDateExcl;
  
  /** Directory of input files. Each file is the name of the author, optionally suffixed with a dot and a number */
  private final Path inputDir;
  
  /** Directory of output files. At a minimum there are two, the event features matrix and the text features matrix. If aggregateByAuthor is true, there will be a matrix pair for every matrix. */
  private final Path outputDir;
  
  
  public TweetFeatureExtractor(Path inputDir, Path outputDir) {
    super();
    this.inputDir = inputDir;
    this.outputDir = outputDir;
  }


  public boolean isStripAddresseesFromText() {
    return stripAddresseesFromText;
  }

  public void setStripAddresseesFromText(boolean stripAddresseesFromText) {
    this.stripAddresseesFromText = stripAddresseesFromText;
  }

  public boolean isStripHashTagsFromText() {
    return stripHashTagsFromText;
  }

  public void setStripHashTagsFromText(boolean stripHashTagsFromText) {
    this.stripHashTagsFromText = stripHashTagsFromText;
  }

  public boolean isStripRtMarkersFromText() {
    return stripRtMarkersFromText;
  }

  public void setStripRtMarkersFromText(boolean stripRtMarkersFromText) {
    this.stripRtMarkersFromText = stripRtMarkersFromText;
  }

  public boolean isStripRetweets() {
    return stripRetweets;
  }

  public void setStripRetweets(boolean stripRetweets) {
    this.stripRetweets = stripRetweets;
  }

  public boolean isTreatHashTagsAsWords() {
    return treatHashTagsAsWords;
  }

  public void setTreatHashTagsAsWords(boolean treatHashTagsAsWords) {
    this.treatHashTagsAsWords = treatHashTagsAsWords;
  }

  public boolean isAggregateByAuthor() {
    return aggregateByAuthor;
  }

  public void setAggregateByAuthor(boolean aggregateByAuthor) {
    this.aggregateByAuthor = aggregateByAuthor;
  }

  public Path getInputDir() {
    return inputDir;
  }

  public Path getOutputDir() {
    return outputDir;
  }

  public DateTime getMinDateIncl() {
    return minDateIncl;
  }

  public void setMinDateIncl(DateTime minDateIncl) {
    this.minDateIncl = minDateIncl;
  }

  public DateTime getMaxDateExcl() {
    return maxDateExcl;
  }

  public void setMaxDateExcl(DateTime maxDateExcl) {
    this.maxDateExcl = maxDateExcl;
  }
  
  
  
}
