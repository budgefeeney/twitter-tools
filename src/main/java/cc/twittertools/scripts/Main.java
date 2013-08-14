package cc.twittertools.scripts;

import java.util.concurrent.Callable;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

/**
 * Main entry-point to the application. Parses paramaters and (statically) hands over to the
 * other scripts bundled in the app
 * <p>
 * TODO Need to remove all other entry points, ideally by just removing the GNU GetOpt 
 * dependency and deleting all the affected code.
 * <p>
 * TODO This will require defining instance variables and flags for almost all other
 * scripts, which will be messy.
 * <p>
 * TODO The use of objects will fail horrible if the getters are called, the getters are
 * mainly there to help args4j infer the datatype.
 * @author bryanfeeney
 *
 */
public class Main implements Callable<Integer>
{
  private enum Command {
    spider_users,
    spider_trec,
    encode
  };
  
  // The command to be executed and the files where the input should be found
  // and the output written.
  private Command command;
  private String inPath;
  private String outPath;
  
  // Options for processing lists of tweets - uses objects as default values are
  // defined in the class itself.
  private boolean stripAddresseesFromText = false;
  private boolean stripHashTagsFromText   = false;
  private boolean stripRtMarkersFromText  = false;
  private boolean stripRetweets           = false;
  private boolean treatHashTagsAsWords    = false;
  private boolean aggregateByAuthor       = false;

  private DateTime minDateIncl = new DateTime(1900,01,01,00,01,01, ISOChronology.getInstance());
  private DateTime maxDateExcl = DateTime.now();
  
  // Options for encoding text as features vectors - uses objects as default
  // values are defined in the class itself.
  private boolean stem           = false;
  private boolean elimStopWords  = false;
  private int     minWordLen     = 2;
  private int     maxWordLen     = 80; // to strip e.g. emails etc.
  private boolean numbersAllowed = false;
  private int     minWordCount   = 5; // words occuring less often than this will be skipped
  
  // Options for encoding of non-text features
  private boolean authorInFeatures        = false;
  private boolean dayOfWeekInFeatures     = false;
  private boolean hourOfDayInFeatures     = false;
  private boolean dayHourOfWeekInFeatures = false;
  private boolean dayOfYearInFeatures     = false;
  private boolean weekOfYearInFeatures    = false;
  private boolean monthOfYearInFeatures   = false;
  private boolean addresseeInFeatures     = false;
  private boolean rtInFeatures            = false;
  private boolean intercepInFeatures      = false ;
  
  
  public Integer call() throws Exception
  {
    return 0;
  }
  
  public Command getCommand() {
    return command;
  }
  
  public void setCommand(Command command) {
    this.command = command;
  }
  
  public String getInPath() {
    return inPath;
  }
  
  @Option(name="-i", aliases="--infile", usage="Input file or directory", metaVar=" ")
  public void setInPath(String inPath) {
    this.inPath = inPath;
  }
  
  public String getOutPath() {
    return outPath;
  }
  
  @Option(name="-o", aliases="--outfile", usage="Output file or directory", metaVar=" ")
  public void setOutPath(String outPath) {
    this.outPath = outPath;
  }
  
  public boolean isStripAddresseesFromText() {
    return stripAddresseesFromText;
  }
  
  @Option(name="--strip-addressees", usage="Remove all addressees from input tweets", metaVar=" ")
  public void setStripAddresseesFromText(boolean stripAddresseesFromText) {
    this.stripAddresseesFromText = stripAddresseesFromText;
  }
  
  public boolean isStripHashTagsFromText() {
    return stripHashTagsFromText;
  }
  
  @Option(name="--strip-hash-tags", usage="Remove all hashtags from input tweets", metaVar=" ")
  public void setStripHashTagsFromText(boolean stripHashTagsFromText) {
    this.stripHashTagsFromText = stripHashTagsFromText;
  }
  
  public boolean isStripRtMarkersFromText() {
    return stripRtMarkersFromText;
  }
  
  @Option(name="--strip-rt-marks", usage="Remove all retweet markers from input tweets", metaVar=" ")
  public void setStripRtMarkersFromText(boolean stripRtMarkersFromText) {
    this.stripRtMarkersFromText = stripRtMarkersFromText;
  }
  
  public boolean isStripRetweets() {
    return stripRetweets;
  }
  
  @Option(name="--skip-retweets", usage="Ignore retweets when processing a user's tweets", metaVar=" ")
  public void setStripRetweets(boolean stripRetweets) {
    this.stripRetweets = stripRetweets;
  }
  
  public boolean isTreatHashTagsAsWords() {
    return treatHashTagsAsWords;
  }
  
  @Option(name="--skip-retweets", usage="Strip the hash from hashtags so they're processed as normal words (including stemming etc.)", metaVar=" ")
  public void setTreatHashTagsAsWords(boolean treatHashTagsAsWords) {
    this.treatHashTagsAsWords = treatHashTagsAsWords;
  }
  
  public boolean isAggregateByAuthor() {
    return aggregateByAuthor;
  }
  
  @Option(name="--by-author", usage="Create multiple outputs, each corresponding to a single author, instead of one output among all authors", metaVar=" ")
  public void setAggregateByAuthor(boolean aggregateByAuthor) {
    this.aggregateByAuthor = aggregateByAuthor;
  }
  
  public DateTime getMinDateIncl() {
    return minDateIncl;
  }
  
  @Option(name="--start-date", usage="Only tweets on or after this date will be processed and included in the output", metaVar=" ")
  public void setMinDateIncl(DateTime minDateIncl) {
    this.minDateIncl = minDateIncl;
  }
  
  public DateTime getMaxDateExcl() {
    return maxDateExcl;
  }
  
  @Option(name="--end-date", usage="Only tweets before this date will be processed and included in the output", metaVar=" ")
  public void setMaxDateExcl(DateTime maxDateExcl) {
    this.maxDateExcl = maxDateExcl;
  }

  public Boolean getStripAddresseesFromText() {
    return stripAddresseesFromText;
  }

  @Option(name="--strip-addressees", usage="Only tweets on or after this date will be processed and included in the output", metaVar=" ")
  public void setStripAddresseesFromText(Boolean stripAddresseesFromText) {
    this.stripAddresseesFromText = stripAddresseesFromText;
  }

  public boolean getStem() {
    return stem;
  }

  @Option(name="--enable-stemming", usage="Turns on stemming of words in the input.", metaVar=" ")
  public void setStem(boolean stem) {
    this.stem = stem;
  }

  public boolean getElimStopWords() {
    return elimStopWords;
  }

  @Option(name="--elim-stopwords", usage="Eliminate all stop-words from the input", metaVar=" ")
  public void setElimStopWords(boolean elimStopWords) {
    this.elimStopWords = elimStopWords;
  }

  public int getMinWordLen() {
    return minWordLen;
  }

  @Option(name="--min-word-len", usage="Minimum word length - terms shorter than this are skipped", metaVar=" ")
  public void setMinWordLen(int minWordLen) {
    this.minWordLen = minWordLen;
  }

  public int getMaxWordLen() {
    return maxWordLen;
  }

  @Option(name="--max-word-len", usage="Maximum word length - terms longer than this are skipped", metaVar=" ")
  public void setMaxWordLen(int maxWordLen) {
    this.maxWordLen = maxWordLen;
  }

  public boolean getNumbersAllowed() {
    return numbersAllowed;
  }

  @Option(name="--incl-numbers", usage="Treat numbers as words: add them to the dictionary and encode.", metaVar=" ")
  public void setNumbersAllowed(boolean numbersAllowed) {
    this.numbersAllowed = numbersAllowed;
  }

  public int getMinWordCount() {
    return minWordCount;
  }

  @Option(name="--min-word-count", usage="Words occurring less frequently than this amount in the corpus are skipped.", metaVar=" ")
  public void setMinWordCount(int minWordCount) {
    this.minWordCount = minWordCount;
  }

  public boolean getAuthorInFeatures() {
    return authorInFeatures;
  }

  @Option(name="--feat-author", usage="Use author as a feature in the side-information.", metaVar=" ")
  public void setAuthorInFeatures(boolean authorInFeatures) {
    this.authorInFeatures = authorInFeatures;
  }

  public boolean getDayOfWeekInFeatures() {
    return dayOfWeekInFeatures;
  }

  @Option(name="--feat-dow", usage="Use day of week as a feature in the side-information.", metaVar=" ")
  public void setDayOfWeekInFeatures(boolean dayOfWeekInFeatures) {
    this.dayOfWeekInFeatures = dayOfWeekInFeatures;
  }

  public boolean getHourOfDayInFeatures() {
    return hourOfDayInFeatures;
  }

  @Option(name="--feat-hod", usage="Use hour of day as a feature in the side-information.", metaVar=" ")
  public void setHourOfDayInFeatures(boolean hourOfDayInFeatures) {
    this.hourOfDayInFeatures = hourOfDayInFeatures;
  }

  public boolean getDayHourOfWeekInFeatures() {
    return dayHourOfWeekInFeatures;
  }

  @Option(name="--feat-how", usage="Use hour of week as a feature in the side-information.", metaVar=" ")
  public void setDayHourOfWeekInFeatures(boolean dayHourOfWeekInFeatures) {
    this.dayHourOfWeekInFeatures = dayHourOfWeekInFeatures;
  }

  public boolean getDayOfYearInFeatures() {
    return dayOfYearInFeatures;
  }

  @Option(name="--feat-doy", usage="Use day of year as a feature in the side-information.", metaVar=" ")
  public void setDayOfYearInFeatures(boolean dayOfYearInFeatures) {
    this.dayOfYearInFeatures = dayOfYearInFeatures;
  }

  public boolean getWeekOfYearInFeatures() {
    return weekOfYearInFeatures;
  }

  @Option(name="--feat-woy", usage="Use week of year as a feature in the side-information.", metaVar=" ")
  public void setWeekOfYearInFeatures(boolean weekOfYearInFeatures) {
    this.weekOfYearInFeatures = weekOfYearInFeatures;
  }

  public boolean getMonthOfYearInFeatures() {
    return monthOfYearInFeatures;
  }

  @Option(name="--feat-moy", usage="Use month of year as a feature in the side-information.", metaVar=" ")
  public void setMonthOfYearInFeatures(boolean monthOfYearInFeatures) {
    this.monthOfYearInFeatures = monthOfYearInFeatures;
  }

  public boolean getAddresseeInFeatures() {
    return addresseeInFeatures;
  }

  @Option(name="--feat-addressee", usage="Use addressee as a feature in the side-information.", metaVar=" ")
  public void setAddresseeInFeatures(boolean addresseeInFeatures) {
    this.addresseeInFeatures = addresseeInFeatures;
  }

  public boolean getRtInFeatures() {
    return rtInFeatures;
  }

  @Option(name="--feat-rt", usage="Use whether a tweet is an original or retweet as a feature in the side-information.", metaVar=" ")
  public void setRtInFeatures(boolean rtInFeatures) {
    this.rtInFeatures = rtInFeatures;
  }

  public boolean getIntercepInFeatures() {
    return intercepInFeatures;
  }

  @Option(name="--feat-intercept", usage="Include an always-one intercept feature in the side-information.", metaVar=" ")
  public void setIntercepInFeatures(boolean intercepInFeatures) {
    this.intercepInFeatures = intercepInFeatures;
  } 
}
