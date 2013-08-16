package cc.twittertools.post;

import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.twittertools.numpy.CsrShortMatrixBuilder;
import cc.twittertools.util.FilesInFoldersIterator;
import cc.twittertools.words.Dictionary;
import cc.twittertools.words.LookupDictionary;
import cc.twittertools.words.Vectorizer;

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
public class TweetFeatureExtractor implements Callable<Integer>
{
  private static final int MAX_WORDS_PER_TWEET = 70;
  private static final int MAX_USERS = 21000;
	private static final int MAX_EXTRA_ADDRESSEES = 39000;
	
	private static final Pattern ENDS_WITH_DIGITS = Pattern.compile("\\.\\d+$");
	
	private final static Logger LOG = LoggerFactory.getLogger(TweetFeatureExtractor.class);

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
  
  /** the full list of input files to process */
  private final Path inputDir;
  
  /** Directory of output files. At a minimum there are two, the event features matrix and the text features matrix. If aggregateByAuthor is true, there will be a matrix pair for every matrix. */
  private final Path outputDir;
  
  /** The text tokenizer */
  private final Vectorizer vectorizer;
  
  /** The specification of which features to encode */
  private final FeatureSpecification featSpec;
  
  /** dictionary of users (including addressees if we're using them) */
  private final Dictionary userDict;
  
  /**
   * Creates a new {@link TweetFeatureExtractor}
   * @param inputDir the directory from which the raw tweets are read. This directory
   * should contain many sub-directory, each sub-directory should contain several
   * files (and only files), and all the files in the all the sub-directories should
   * be lists of tweets.
   * @param outputDir the directory to which the resulting matrices should be
   * written.
   * @param vectorizer a pre-configured vectorizer used to convert text to
   * a feature vector.
   * @param featureSpecification the side-information features which should be encoded
   * and included in the output.
   * @throws IOException 
   */
  public TweetFeatureExtractor(Path inputDir, Path outputDir, Vectorizer vectorizer, FeatureSpecification featureSpecification) throws IOException {
    this.inputDir   = inputDir;
    this.outputDir  = outputDir;
    this.vectorizer = vectorizer;
    this.featSpec   = featureSpecification;
    
    int numUsers =
    	   featSpec.isAddresseeInFeatures() ? MAX_USERS + MAX_EXTRA_ADDRESSEES
    	 : featSpec.isAuthorInFeatures() ? MAX_USERS
    	 : 0;
    
    userDict = numUsers == 0 ? null : new LookupDictionary (numUsers);
  }
  


  /**
   * Loads in all tweets, extracts features, encodes them, and writes the encoded
   * features to disk. Returns the number of tweets processed.
   */
  public Integer call() throws Exception
  {	int tweetCount = 0;
  	
  	if (aggregateByAuthor)
  	{	Map<String, List<Path>> filesByUser = groupFilesByUser (inputDir);
  		for (Map.Entry<String, List<Path>> entry : filesByUser.entrySet())
  		{	String user = entry.getKey();
  			tweetCount += extractAndWriteFeatures (
  				entry.getValue().iterator(),
  				outputDir.resolve(user + "-words"),
  				outputDir.resolve(user + "-side")
  			);
  		}
  	}
  	else
  	{ try (FilesInFoldersIterator tweetFiles = new FilesInFoldersIterator(inputDir); )
	  	{ tweetCount = extractAndWriteFeatures(
	  			tweetFiles,
	  			outputDir.resolve("words"),
	  			outputDir.resolve("side")
	  		);
	  	}
  	}
  	
  	return tweetCount;
  }
  
  /**
   * Read all files into memory, then group them by user, using the files' names
   * to detect the username.
   * @throws Exception 
   * @throws IOException 
   */
  private Map<String, List<Path>> groupFilesByUser(Path inputDir) throws IOException, Exception
	{	Map<String, List<Path>> map = new HashMap<>();
		try (FilesInFoldersIterator files = new FilesInFoldersIterator(inputDir); )
		{	while (files.hasNext())
			{	Path   file     = files.next();
				String userName = userNameFromFile (file);
				addToMultimap (map, userName, file);
			}
		}
		
		return map;
	}

  /**
   * Given a tweets file determins what the username should be.
   * @param file
   * @return
   */
  private String userNameFromFile(Path file)
	{	String fileName = file.getFileName().toString();
		Matcher m = ENDS_WITH_DIGITS.matcher(fileName);
		return m.find()
		 ? fileName.substring (0, m.start())
		 : fileName;
	}



	/**
   * A MultiMap is a map associating a list of values to a single key. This
   * does the tedious job of creating the list if necessary before adding
   * the value to that list.
   */
  private <K, V> void addToMultimap(Map<K, List<V>> multimap, K key, V value) {
    List<V> valueList = multimap.get(key);
    if (valueList == null)
    { valueList = new ArrayList<>();
      multimap.put (key, valueList);
    }
    valueList.add(value);
  }


	/**
   * Given an iterator of files, and the names of two output files, extracts information
   * relating to all possible tweets, and 
   * @throws Exception 
   */
  private int extractAndWriteFeatures (Iterator<Path> tweetFiles, Path wordsFile, Path eventsFile) throws Exception
  {	Interval interval = new Interval(minDateIncl, maxDateExcl);
  	FeatureDimension dim = featSpec.dimensionality(userDict, interval);
    	
  	CsrShortMatrixBuilder wordMatrix
  		= new CsrShortMatrixBuilder(vectorizer.getDict().capacity());
  	CsrShortMatrixBuilder eventMatrix 
  		= new CsrShortMatrixBuilder(dim.getTotal());
  	
  	Int2ShortMap wordFeatures  = new Int2ShortOpenHashMap(MAX_WORDS_PER_TWEET);
  	Int2ShortMap eventFeatures = new Int2ShortOpenHashMap(featSpec.maxNonZeroFeatures());
  	
  	wordFeatures.defaultReturnValue((short) 0);
  	eventFeatures.defaultReturnValue((short) 0);
  	int tweetCount = 0;
  	
  	while (tweetFiles.hasNext())
	  {	
			try (SavedTweetReader rdr = new SavedTweetReader(tweetFiles.next()); )
			{	
				while (rdr.hasNext())
		  	{	Tweet tweet = rdr.next();
		  		
		  		// Do we include this tweet, or do we skip it.
		  		if (stripRetweets && tweet.isRetweetFromId() || tweet.isRetweetFromMsg())
		  			continue;
		  		if (tweet.getLocalTime().isBefore(minDateIncl))
		  			continue;
		  		if (maxDateExcl.isBefore(tweet.getLocalTime()))
		  			continue;
		  		
		  		// TODO need some sort of "last-date" idea for when we have no date.
		  	
		  		++tweetCount;
		  		extractFeatures (tweet, dim, wordFeatures, eventFeatures);
		  		
		  		wordMatrix.addRow(wordFeatures);
		  		eventMatrix.addRow(eventFeatures);
		  	}
			}
	  }
	
		wordMatrix.writeToFile(wordsFile);
		LOG.info ("Wrote tweet text features to " + wordsFile);
		
		eventMatrix.writeToFile(eventsFile);
		LOG.info ("Wrote tweet side features to " + eventsFile);
  	
  	return tweetCount;
  }


  /**
   * Clears the given maps, extracts word and event features, encodes them, and
   * then populates the maps with the encoded features. This is essentially a
   * sparse vector representation.
   * @param tweet the tweet from which featurse should be extracted
   * @param dim the dimension of each of the side-information features.
   * @param wordFeatures the features extracted from the text of the tweet
   * @param eventFeatures the features extracted from other information about
   * the tweet, see {@link FeatureSpecification} for more on these.
   */
	private void extractFeatures(Tweet tweet, FeatureDimension dim, Int2ShortMap wordFeatures, Int2ShortMap eventFeatures)
	{	List<String> addressees = extractWordFeatures(tweet, wordFeatures);
		extractEventFeatures(tweet, dim, addressees, eventFeatures);
	}

	/**
	 * Extracts and encodes features from the given tweet according to this
	 * class's configuration. The given map is cleared and filled with the
	 * encoded features.
	 */
	private void extractEventFeatures(Tweet tweet, FeatureDimension dim, List<String> addressees, Int2ShortMap eventFeatures)
	{	eventFeatures.clear();
		short one = (short) 1;
		
		int step = 0;
		if (featSpec.isAddresseeInFeatures())
		{	for (String addressee : addressees)
			{	int userId = userDict.toInt(addressee);
				inc (eventFeatures, step + userId);
			}
			step += dim.getAddresseeDim();
		}
		
		if (featSpec.isAuthorInFeatures())
		{	eventFeatures.put (step + userDict.toInt(tweet.getAccount()), one);
			step += dim.getAuthorDim();
		}
		
		int dayOfWeek = tweet.getLocalTime().getDayOfWeek() - 1;
		int hourOfDay = tweet.getLocalTime().getHourOfDay();
		
		if (featSpec.isDayHourOfWeekInFeatures())
		{	eventFeatures.put (step + (dayOfWeek * 24) + hourOfDay, one);
			step += dim.getDayHourOfWeekDim();
		}
		
		if (featSpec.isDayOfWeekInFeatures())
		{	eventFeatures.put (step + dayOfWeek, one);
			step += dim.getDayOfWeekDim();
		}
		
		Interval interval = new Interval (minDateIncl, tweet.getLocalTime());
		int days   = (int) TimeUnit.MILLISECONDS.toDays(interval.toDurationMillis());
		int weeks  = days / 7;
		int months = interval.toPeriod().getMonths();
		
		if (featSpec.isDayOfYearInFeatures())
		{	eventFeatures.put (step + dayOfWeek, one);
			step += dim.getDayOfWeekDim();
		}
		
		if (featSpec.isHourOfDayInFeatures())
		{	eventFeatures.put (step + tweet.getLocalTime().getHourOfDay(), one);
			step += dim.getHourOfDayDim();
		}
		
		if (featSpec.isMonthOfYearInFeatures())
		{	eventFeatures.put (step + months, one);
			step += dim.getMonthOfYearDim();
		}
		
		if (featSpec.isRtInFeatures())
		{	if (tweet.isRetweetFromId() || tweet.isRetweetFromMsg())
				eventFeatures.put (step, one);
			step += dim.getRtDim();
		}
		
		if (featSpec.isInterceptInFeatures())
		{	eventFeatures.put (step, one);
			step += dim.getInterceptDim();
		}
	}



	/**
	 * Tidies the tweet as specified in this class's configuration then encodes
	 * it into word features. The given map is cleared and filled with the
	 * encoded features.
	 * 
	 * @return the list of addressees
	 */
	private List<String> extractWordFeatures(Tweet tweet, Int2ShortMap wordFeatures)
	{ wordFeatures.clear();
		
		String text = tweet.getMsg();
		Pair<String, List<String>> textAndAddressees =
				Sigil.ADDRESSEE.extractSigils(text);
		
		if (stripAddresseesFromText)
			text = textAndAddressees.getLeft();
		if (stripHashTagsFromText)
			text = Sigil.HASH_TAG.stripFromMsg(text);
		if (stripRtMarkersFromText)
			text = Sigil.RETWEET.stripFromMsg(null);
		
		if (treatHashTagsAsWords)
			text = text.replace ('#', ' ');
		
		// TODO Test whether lucene analyzer will strip the hashes from hash
		// tags, in which case we have to do this backwards by replacing
		// # with HASH_TAG etc.
		
		for (int wordId : vectorizer.toInts(text))
		{	inc(wordFeatures, wordId);
		}
		
		return textAndAddressees.getRight();
	}

	/**
	 * Increments the value associated with the given key. If the key doesn't
	 * exist, it's assumed the value to be incremented is zero, so we just
	 * put 1.
	 */
	private void inc(Int2ShortMap map, int key)
	{	map.put (key, (short) (map.get(key) + 1));
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

	public Path getOutputDir()
	{ return outputDir;
	}

	public Vectorizer getVectorizer()
	{ return vectorizer;
	}

	public FeatureSpecification getFeatSpec()
	{ return featSpec;
	}

	public Dictionary getUserDict()
	{ return userDict;
	}
}
