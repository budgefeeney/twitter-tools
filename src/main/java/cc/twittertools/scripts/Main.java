package cc.twittertools.scripts;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import cc.twittertools.post.FeatureSpecification;
import cc.twittertools.post.TweetFeatureExtractor;
import cc.twittertools.words.Vectorizer;
import cc.twittertools.words.Vectorizer.InputType;
import cc.twittertools.words.dict.CompoundTokenDictionary;
import cc.twittertools.words.dict.Dictionary;
import cc.twittertools.words.dict.LookupDictionary;
import cc.twittertools.words.dict.NullDictionary;
import cc.twittertools.words.dict.SigilStrippingDictionary;

import com.twitter.common.text.token.attribute.TokenType;

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
 * TODO It turns out the latest version of args4j can decorate fields instead of getters
 * and setters, which means the getters and setters could be deleted....
 * @author bryanfeeney
 * 
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
  private boolean showHelp = false;
  
  private Command command;
  private String inPath;
  private String outPath;
  
  // Options for processing lists of tweets - uses objects as default values are
  // defined in the class itself.
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
  
  // These dictionary fields are overloaded. If parseable as an int, then they
  // specify the maximum capacity of a new dictionary to be created. Otherwise
  // they specify the path to a file from which the dict should be loaded.
	private String addresseeDict = "100000";
	private String urlsDict      = "100000";
	private String wordsDict     = "50000";
	private String stocksDict    = "50000";
	private String emoticonsDict = "500";
	private String hashTagsDict  = "50000";
  
  // Options for encoding of non-text features
  FeatureSpecification featSpec = new FeatureSpecification();
  
  @Argument
  private List<String> arguments = new ArrayList<String>();
  
  private final DateTimeFormatter dateFormat = ISODateTimeFormat.basicDate();
  
  /**
   * Program entry point.
   */
  public static void main (String[] args) throws Exception
  {	Main main = new Main();
  	main.parseArguments(args);
  	main.call();
  }
  
  /**
   * Called after arguments have been parsed, this delegate to the appropriate
   * method for the given command.
   */
  public Integer call() throws Exception
  {
	switch (command)
	{
	case spider_trec: throw new UnsupportedOperationException("Code for new TREC spidering CLI not yet ready");
	case spider_users: throw new UnsupportedOperationException("Code for new user spidering CLI not yet ready");
	case encode:
	  doEncode();
	  break;
	default: throw new IllegalStateException("The command " + command + " is unknown. This is a programmer error");
	}
    return 0;
  }
   
  /** Parses the arguments */
  private void parseArguments(String[] args)
  {	CmdLineParser parser = null;
  	try
  	{ parser = new CmdLineParser(this);
  	  parser.parseArgument(args);
  	
  	  if (showHelp)
  	  { System.out.println("Help for this command:");
  		showHelp (System.out, parser);
  		System.exit(0);
  	  }
  	  
  	  if (arguments.isEmpty())
  	    die ("You must specify a command. Commands are " + Arrays.toString (Command.values()));
  	  
  	  if (arguments.size() > 1)
  		die ("You should only specify one command. You specified many: " + arguments.toString());
  	}
  	catch (CmdLineException e)
  	{ System.err.println (e.getMessage());
  	  showHelp (System.err, parser);
  	}
	  
  }
  
  /** Shows the help message to the given stream. Needs the parser object to say what the options are. */
  private void showHelp(PrintStream out, CmdLineParser parser)
  {	out.println ("Usage: java -jar JARNAME.jar <command> <options>");
	out.println ("       where <command> is one of " + Arrays.toString (Command.values()));
	if (parser != null)
	  parser.printUsage(out);
  }
  
  /** Prints the given messsage to stderr and quits the app */
  private void die(String msg)
  { System.err.println (msg);
	System.exit(-1);
  }

  
  /**
   * Launches the job for encoding tweets and their associated features into vectors bundled up
   * into matrices.
   * @throws IOException 
   */
  private void doEncode() throws Exception
  {
  	TweetFeatureExtractor tfe = newTweetFeatExtractor();
		tfe.call();
  }

	public TweetFeatureExtractor newTweetFeatExtractor() throws IOException
	{
		Vectorizer vec = newVectorizer();
		TweetFeatureExtractor tfe = new TweetFeatureExtractor(Paths.get(inPath), Paths.get(outPath), vec, featSpec);
		
		tfe.setMinDateIncl(minDateIncl);
		tfe.setMaxDateExcl(maxDateExcl);
		
		tfe.setStripRetweets(stripRetweets);
		tfe.setStripRtMarkersFromText(stripRtMarkersFromText);
		
		
		return tfe;
	}

	public Vectorizer newVectorizer() throws IOException
	{
		CompoundTokenDictionary dict = tokenDictionary();
		Vectorizer vec = new Vectorizer(dict);
		vec.setStemEnabled(stem);
		vec.setStopElimEnabled(elimStopWords);
		vec.setMinWordLength(minWordLen);
		vec.setMaxWordLength(maxWordLen);
		vec.setMinWordCount(minWordCount);
		vec.setNumbersAllowed(numbersAllowed);
		vec.setInputType(InputType.TWITTER);
		return vec;
	}

	private CompoundTokenDictionary tokenDictionary() throws IOException
	{
		CompoundTokenDictionary dict = new CompoundTokenDictionary(null);
		
		if (treatHashTagsAsWords)
		{	if ("0".equals (wordsDict))
				throw new IllegalStateException ("Cannot set dict-words = 0 and also require hashtags to be treated as words.");
			if (! "0".equals (hashTagsDict))
				throw new IllegalStateException ("Must set dict-tags = 0 and dict-words to a non-zero value when you treat hashtags as words");
		}
		
		Dictionary wordDict = dictionary(wordsDict);	
		dict.addDictionary(TokenType.USERNAME, dictionary(addresseeDict));
		dict.addDictionary(TokenType.URL,      dictionary(urlsDict));
		dict.addDictionary(TokenType.TOKEN,    wordDict);
		dict.addDictionary(TokenType.STOCK,    dictionary(stocksDict));
		dict.addDictionary(TokenType.EMOTICON, dictionary(emoticonsDict));
		dict.addDictionary(TokenType.HASHTAG,  treatHashTagsAsWords
			? new SigilStrippingDictionary('#', wordDict)
			: dictionary(hashTagsDict));
		
		return dict;
	}
  
  private final Dictionary dictionary(String dict) throws IOException
  {	if (isDigitSequence (dict))
  	{	int size = Integer.parseInt(dict);
  		return size == 0 ? NullDictionary.INSTANCE : new LookupDictionary(size);
  	}
	  else
	  {	return LookupDictionary.fromFile(Paths.get(dict), minWordCount);
	  }
  }
  
  /** Is the given string entirely a sequence of integers only */
  private boolean isDigitSequence(String dict)
	{	for (int i = 0; i < dict.length(); i++)
			if (! Character.isDigit (dict.charAt(i)))
				return false;
		return true;
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
  
  public String getMinDateIncl() {
    return dateFormat.print(minDateIncl);
  }
  
  @Option(name="--start-date", usage="Only tweets on or after this date will be processed and included in the output", metaVar=" ")
  public void setMinDateIncl(String minDateIncl) {
    this.minDateIncl = dateFormat.parseDateTime(minDateIncl);
  }
  
  public String getMaxDateExcl() {
    return dateFormat.print(maxDateExcl);
  }
  
  @Option(name="--end-date", usage="Only tweets before this date will be processed and included in the output", metaVar=" ")
  public void setMaxDateExcl(String maxDateExcl) {
    this.maxDateExcl = dateFormat.parseDateTime(maxDateExcl);
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

  public boolean isAuthorInFeatures() {
    return featSpec.isAuthorInFeatures();
  }

  @Option(name="--feat-author", usage="Use author as a feature in the side-information.", metaVar=" ")
  public void setAuthorInFeatures(boolean authorInFeatures) {
    featSpec.setAuthorInFeatures(authorInFeatures);
  }

  public boolean isDayOfWeekInFeatures() {
    return featSpec.isDayOfWeekInFeatures();
  }

  @Option(name="--feat-dow", usage="Use day of week as a feature in the side-information.", metaVar=" ")
  public void setDayOfWeekInFeatures(boolean dayOfWeekInFeatures) {
    featSpec.setDayOfWeekInFeatures(dayOfWeekInFeatures);
  }

  public boolean isHourOfDayInFeatures() {
    return featSpec.isHourOfDayInFeatures();
  }

  @Option(name="--feat-hod", usage="Use hour of day as a feature in the side-information.", metaVar=" ")
  public void setHourOfDayInFeatures(boolean hourOfDayInFeatures) {
    featSpec.setHourOfDayInFeatures(hourOfDayInFeatures);
  }

  public boolean isDayHourOfWeekInFeatures() {
    return featSpec.isDayHourOfWeekInFeatures();
  }

  @Option(name="--feat-how", usage="Use hour of week as a feature in the side-information.", metaVar=" ")
  public void setDayHourOfWeekInFeatures(boolean dayHourOfWeekInFeatures) {
    featSpec.setDayHourOfWeekInFeatures(dayHourOfWeekInFeatures);
  }

  public boolean isDayOfYearInFeatures() {
    return featSpec.isDayOfYearInFeatures();
  }

  @Option(name="--feat-doy", usage="Use day of year as a feature in the side-information.", metaVar=" ")
  public void setDayOfYearInFeatures(boolean dayOfYearInFeatures) {
    featSpec.setDayOfYearInFeatures(dayOfYearInFeatures);
  }

  public boolean getWeekOfYearInFeatures() {
    return featSpec.isWeekOfYearInFeatures();
  }

  @Option(name="--feat-woy", usage="Use week of year as a feature in the side-information.", metaVar=" ")
  public void setWeekOfYearInFeatures(boolean weekOfYearInFeatures) {
    featSpec.setWeekOfYearInFeatures(weekOfYearInFeatures);
  }

  public boolean isMonthOfYearInFeatures() {
    return featSpec.isMonthOfYearInFeatures();
  }

  @Option(name="--feat-moy", usage="Use month of year as a feature in the side-information.", metaVar=" ")
  public void setMonthOfYearInFeatures(boolean monthOfYearInFeatures) {
    featSpec.setMonthOfYearInFeatures(monthOfYearInFeatures);
  }

  public boolean isAddresseeInFeatures() {
    return featSpec.isAddresseeInFeatures();
  }

  @Option(name="--feat-addressee", usage="Use addressee as a feature in the side-information.", metaVar=" ")
  public void setAddresseeInFeatures(boolean addresseeInFeatures) {
    featSpec.setAddresseeInFeatures(addresseeInFeatures);
  }

  public boolean isRtInFeatures() {
    return featSpec.isRtInFeatures();
  }

  @Option(name="--feat-rt", usage="Use whether a tweet is an original or retweet as a feature in the side-information.", metaVar=" ")
  public void setRtInFeatures(boolean rtInFeatures) {
    featSpec.setRtInFeatures(rtInFeatures);
  }

  public boolean isIntercepInFeatures() {
    return featSpec.isInterceptInFeatures();
  }

  @Option(name="--feat-intercept", usage="Include an always-one intercept feature in the side-information.", metaVar=" ")
  public void setIntercepInFeatures(boolean intercepInFeatures) {
    featSpec.setInterceptInFeatures(intercepInFeatures);
  }

  public boolean isShowHelp() {
  	return showHelp;
  }

  @Option(name="-h", aliases="--help", usage="Show this help message.", metaVar=" ")
  public void setShowHelp(boolean showHelp) {
  	this.showHelp = showHelp;
  }

	public String getAddresseeDict()
	{	return addresseeDict;
	}

	@Option(name="--dict-addrs", aliases="--help", usage="Maximum number of addressees in dictionary, all subsequent words are dropped.", metaVar=" ")
	public void setAddresseeDict(String dict)
	{	this.addresseeDict = dict;
	}

	public String getUrlsDict()
	{	return urlsDict;
	}

	@Option(name="--dict-urls", aliases="--help", usage="Maximum number of URLs to add to dictionary, or path to dictionary to be loaded.", metaVar=" ")
	public void setUrlsDict(String dict)
	{	this.urlsDict = dict;
	}

	public String getWordsDict()
	{	return wordsDict;
	}

	@Option(name="--dict-words", aliases="--help", usage="Maximum number of words to add to dictionary, or path to dictionary to be loaded.", metaVar=" ")
	public void setWordsDict(String dict)
	{	this.wordsDict = dict;
	}

	public String getStocksDict()
	{	return stocksDict;
	}

	@Option(name="--dict-stocks", aliases="--help", usage="Maximum number of stocks to add to dictionary, or path to dictionary to be loaded.", metaVar=" ")
	public void setStocksDict(String dict)
	{	this.stocksDict = dict;
	}

	public String getEmoticonsDict()
	{	return emoticonsDict;
	}

	@Option(name="--dict-smileys", aliases="--help", usage="Maximum number of emoticons (\"smileys\") to add to dictionary, or path to dictionary to be loaded.", metaVar=" ")
	public void setEmoticonsDict(String dict)
	{	this.emoticonsDict = dict;
	}

	public String getHashTagsDict()
	{	return hashTagsDict;
	}

	@Option(name="--dict-tags", aliases="--help", usage="Maximum number of hashtags to add to dictionary, or path to dictionary to be loaded.", metaVar=" ")
	public void setHashTagsDict(String dict)
	{	this.hashTagsDict = dict;
	}
}
