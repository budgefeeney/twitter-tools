package cc.twittertools.scripts;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.twittertools.post.SavedTweetReader;
import cc.twittertools.post.Tweet;
import cc.twittertools.util.FilesInFoldersIterator;
import cc.twittertools.util.PathUtils;
import cc.twittertools.words.Vectorizer;

import com.twitter.common.text.token.attribute.TokenType;

/**
 * Quick script to determine a few useful statistics
 * <ul>
 * <li>The date of each author's first tweet
 * <li>How many tweets exist given from each starting point
 * <li>Statistics on the inter-post time in minutes
 * <li>Statistics on how often authors retweet. There are two kinds of retweet:
 *     the built-in kind, and one where a message is quoted via "RT"
 * </ul>
 * @author bryanfeeney
 *
 */
public class TwitterStats implements Callable<Integer>
{
	//	private static final int MAX_INTER_TWEET_TIME_MINS = 31 * 24 * 60; // basically we expect users to tweet at least once a month
	private static final int NUM_USERS_IN_DATASET = 21_000;
//	private static final int DATASET_LENGTH_IN_DAYS = 5_000;
//	private static final int EXPECTED_HASH_TAG_COUNT = 8_000_000;
//	private static final int EXPECTED_URL_COUNT = 10_000_000;
//	private static final int EXPECTED_WORD_COUNT = 5_000_000;
	
	private static final int MAX_CORRUPTED_TWEETS_PER_FILE = 5;

	private final static Logger LOG = LoggerFactory.getLogger(TwitterStats.class);
	
	private final Int2IntMap            postsSinceDay;
	private final Map<String, DateTime> lastPostByUser;
	private final Map<String, DateTime> firstPostByUser;
	private final Object2IntMap<String> firstPostByUserAsDay;
	private final Int2IntMap            interPostTimeMins;
	private final Object2IntMap<String> retweetsByUser;
	private final Object2IntMap<String> rtRetweetsByUser;
	private final Object2IntMap<String> tweetsPerUser;
	private final Object2IntMap<String> hashTagCounts;
	private final Object2IntMap<String> smileyCounts;
	private final Object2IntMap<String> addresseeCounts;
	private final Object2IntMap<String> urlCounts;
	private final Object2IntMap<String> wordCounts;
	private final Int2IntMap            tweetsPerWeek;
	private final Int2IntMap            wordsPerTweet;
	private final Int2IntMap            urlsPerTweet;
	private final Int2IntMap            hashTagsPerTweet;
	private final Int2IntMap            smileysPerTweet;
	private final Int2IntMap            addrsPerTweet;
	private final Int2IntMap            tokensPerTweet;

	
	private final Set<String> excludedUsers = new HashSet<>();
	private       DateTime startDateIncl = new DateTime (1900, 01, 01, 00, 00, 01);
	
	private final Path datasetDirectory;
	private final Path outputDir;
	
	
	
	public TwitterStats(Path datasetDirectory, Path outputDir)
	{
		super();
		this.datasetDirectory  = datasetDirectory;
		this.outputDir         = outputDir;
		
		postsSinceDay        = new Int2IntAVLTreeMap();
		lastPostByUser       = new HashMap<>(NUM_USERS_IN_DATASET);
		firstPostByUser      = new HashMap<>(NUM_USERS_IN_DATASET);
		firstPostByUserAsDay = new Object2IntAVLTreeMap<>();
		interPostTimeMins    = new Int2IntAVLTreeMap();
		retweetsByUser       = new Object2IntAVLTreeMap<>();
		rtRetweetsByUser     = new Object2IntAVLTreeMap<>();
		tweetsPerUser        = new Object2IntAVLTreeMap<>();
		hashTagCounts        = new Object2IntAVLTreeMap<>();
		smileyCounts         = new Object2IntAVLTreeMap<>();
		addresseeCounts      = new Object2IntAVLTreeMap<>();
		urlCounts            = new Object2IntAVLTreeMap<>();
		
		wordCounts           = new Object2IntAVLTreeMap<>();
		tweetsPerWeek        = new Int2IntAVLTreeMap();
		wordsPerTweet        = new Int2IntAVLTreeMap();
		urlsPerTweet         = new Int2IntAVLTreeMap();
		hashTagsPerTweet     = new Int2IntAVLTreeMap();
		smileysPerTweet      = new Int2IntAVLTreeMap();
		addrsPerTweet        = new Int2IntAVLTreeMap();
		tokensPerTweet       = new Int2IntAVLTreeMap();
		
		postsSinceDay.defaultReturnValue(0);
		firstPostByUserAsDay.defaultReturnValue(0);
		interPostTimeMins.defaultReturnValue(0);
		retweetsByUser.defaultReturnValue(0);
		rtRetweetsByUser.defaultReturnValue(0);
		tweetsPerUser.defaultReturnValue(0);
		hashTagCounts.defaultReturnValue(0);
		smileyCounts.defaultReturnValue(0);
		addresseeCounts.defaultReturnValue(0);
		urlCounts.defaultReturnValue(0);
		wordCounts.defaultReturnValue(0);
		tweetsPerWeek.defaultReturnValue(0);
		wordsPerTweet.defaultReturnValue(0);
		urlsPerTweet.defaultReturnValue(0);
		hashTagsPerTweet.defaultReturnValue(0);
		smileysPerTweet.defaultReturnValue(0);
		addrsPerTweet.defaultReturnValue(0);
		tokensPerTweet.defaultReturnValue(0);
	}
	
	public Integer call() throws Exception
	{	final DateTime firstDay = new DateTime (2000, 01, 01, 00, 00, 01);
		Main main = new Main();
		Vectorizer vec = main.newVectorizer();
		
		int tweetCount = 0;
		String currentAccount = null;
		DateTime lastDate     = null;
		try (FilesInFoldersIterator tweetFiles = new FilesInFoldersIterator(datasetDirectory); )
		{	
			filesLoop:while (tweetFiles.hasNext())
	  	{	
	  		int corruptedTweetCount = 0;
	  		Path currentFile = tweetFiles.next();
	  		LOG.info ("Processing tweets in file: " + currentFile);
	  		
				try (SavedTweetReader rdr = new SavedTweetReader(currentFile); )
				{	
					while (rdr.hasNext())
					{	
						try
						{	Tweet    tweet     = rdr.next();
			  			String   account   = tweet.getAccount();
			  			DateTime tweetDate = tweet.getLocalTime();
			  			
			  			if (excludedUsers.contains(tidyStringKey(account))
						   || tweetDate.isBefore(startDateIncl))
			  				continue;
			  			
			  			++tweetCount;
			  			
			  			// Retweet statistics
				  		if (! tweet.getAccount().equals (tweet.getAuthor()))
				  			inc (retweetsByUser, account);
				  		else if (tweet.isRetweetFromMsg())
				  			inc (rtRetweetsByUser, account);
				  		else
				  			inc (tweetsPerUser, account);
				  		
				  		// Inter-post time statistics
				  		if (account.equals (currentAccount) && lastDate != null)
				  		{	int interTweetTimeMins = (int) TimeUnit.MILLISECONDS.toMinutes(
				  				lastDate.isBefore(tweetDate)
				  				? new Interval (lastDate, tweetDate).toDurationMillis()
				  				:	new Interval (tweetDate, lastDate).toDurationMillis()
				  			);
				  			inc (interPostTimeMins, interTweetTimeMins);
				  		}
				  		currentAccount = account;
				  		lastDate       = tweetDate;
				  		
				  		// Total posts by date range (then until now)
				  		Interval interval = new Interval (firstDay, tweetDate);
				  		int dayOfTweet = (int) TimeUnit.MILLISECONDS.toDays(interval.toDurationMillis());
				  		inc (postsSinceDay, dayOfTweet);
				  		
				  		// tweets per week
				  		int year = tweetDate.getWeekyear(); // Jodatime Javadoc explains why this makes
				  		int week = tweetDate.getWeekOfWeekyear(); // sense even if it looks wrong
				  		int time = year * 100 + week;
				  		inc (tweetsPerWeek, time);
				  		
				  		// Dates of each user's first posts
				  		DateTime accountsFirstPost = firstPostByUser.get(account);
				  		if (accountsFirstPost == null || tweetDate.isBefore(accountsFirstPost))
				  		{	firstPostByUser.put (account, tweetDate);
				  			firstPostByUserAsDay.put (tidyStringKey(account), dayOfTweet);
				  		}
				  		DateTime accountsLastPost = lastPostByUser.get(account);
				  		if (accountsLastPost == null || tweetDate.isAfter(accountsLastPost))
				  		{	lastPostByUser.put (account, tweetDate);
				  		}
				  		
				  		// Content statistics	
				  		int wordCount   = 0;
				  		int urlCount    = 0;
				  		int hashCount   = 0;
				  		int smileyCount = 0;
				  		int addrsCount  = 0;
				  		
				  		Iterator<Pair<TokenType, String>> iter = vec.toWords(tweet.getMsg());
				  		while (iter.hasNext())
				  		{	Pair<TokenType, String> tokenValue = iter.next();
				  			switch (tokenValue.getKey())
				  			{	case URL:
				  					inc (urlCounts, tokenValue.getValue());
				  					++urlCount;
				  					break;
				  				case USERNAME:
				  					inc (addresseeCounts, tokenValue.getValue());
				  					++addrsCount;
				  					break;
				  				case HASHTAG:
				  					incTagCount (tweetDate, tokenValue.getValue());
				  					++hashCount;
				  					break;
				  				case EMOTICON:
				  					incSmileyCount (tweetDate, tokenValue.getValue());
				  					++smileyCount;
				  					break;
				  				case TOKEN:
				  					inc (wordCounts, tokenValue.getValue());
				  					++wordCount;
				  					if (tokenValue.getValue().length() == 1)
				  						LOG.warn ("Tweet " + String.format("%5d", tweetCount) + ": Single character word '" + tokenValue.getValue() + "' dervied from message '" + tweet.getMsg() + "'");
				  				default:
				  					break;
				  			}
				  		}
				  		
				  		inc (wordsPerTweet,    wordCount);
				  		inc (urlsPerTweet,     urlCount);
				  		inc (hashTagsPerTweet, hashCount);
				  		inc (smileysPerTweet,  smileyCount);
				  		inc (addrsPerTweet,    addrsCount);
				  		inc (tokensPerTweet,   wordCount + urlCount + hashCount + smileyCount + addrsCount);
						}
						catch (Exception e)
						{	LOG.warn ("Error processing tweet from file " + currentFile + " : " + e.getMessage(), e);
							if (++corruptedTweetCount >= MAX_CORRUPTED_TWEETS_PER_FILE)
							{	LOG.warn ("Encountered " + corruptedTweetCount + " corrupted tweets in the current file, so skipping it. The current file is " + currentFile);
								continue filesLoop; // skip this file.
							}
						}
					}
				
					LOG.info ("Total tweets processed thus far : " + tweetCount);
				}
		  }
		}
		finally
		{	writeStatistics();
		}
		
		return tweetCount;
	}
	
	
	
	
	private void writeStatistics() throws IOException
	{	// User statistics
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("userStats.txt"), Charsets.UTF_8))
		{
			List<String> users = new ArrayList<String>(firstPostByUser.keySet());
			Collections.sort(users);
			
			for (String user : users)
				writeSafely (wtr, "user-statistics",
					user                             + '\t' +
					firstPostByUser.get(user)        + '\t' +
					get (firstPostByUserAsDay, user) + '\t' +
					lastPostByUser.get(user)         + '\t' +
					get (tweetsPerUser, user)        + '\t' +
					get (retweetsByUser, user)       + '\t' +
					get (rtRetweetsByUser, user)     + '\n'
				);
		}
		catch (Exception ioe)
		{	LOG.error("Error writing out user statistics to file " + ioe.getMessage(), ioe);
		}
		
		// Inter post time statistics
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("interPostStats.txt"), Charsets.UTF_8))
		{	for (Int2IntMap.Entry entry : interPostTimeMins.int2IntEntrySet())
			{	writeSafely(wtr, "inter-post-time", entry.getKey().toString() + '\t' + entry.getIntValue() + '\n');
			}
		}
		catch (Exception ioe)
		{	LOG.error("Error writing out inter-post time statistics to file " + ioe.getMessage(), ioe);
		}
		
		// Tweets since date counts
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("postsSinceDay.txt"), Charsets.UTF_8))
		{	Set<Integer> set = postsSinceDay.keySet();
			int    start = min (set);
			int    end   = max (set);
			
			int cumulative = 0;
			for (int day = end; day >= start; day--)
			{	cumulative += postsSinceDay.get(day);
				writeSafely(wtr, "posts-since-day", Integer.toString (day) + '\t' + Integer.toString (cumulative) + '\n');
			}
		}
		catch (Exception ioe)
		{	LOG.error("Error writing out counts of tweets since dates to file " + ioe.getMessage(), ioe);
		}
		
		// The dictionary of words
		writeMapToFile (outputDir.resolve("dictionary"), Charsets.UTF_8, wordCounts, "word-counts");
		
		// The list of addressees
		writeMapToFile (outputDir.resolve("addressees"), Charsets.UTF_8, addresseeCounts, "addressee-counts");
		
		// The list of hashtags
		writeMapToFile (outputDir.resolve("hashtags"), Charsets.UTF_8, hashTagCounts, "hashtag-counts");	
		
		// The list of smileys
		writeMapToFile (outputDir.resolve("smileys"), Charsets.UTF_8, smileyCounts, "smiley-counts");	
		
		// Dictionary of URLs
		writeMapToFile(outputDir.resolve("urls"), Charsets.UTF_8, urlCounts, "url-counts");
		
		// Tweets per week
		writeMapsToFile(outputDir.resolve("tweets-per-week"), Charsets.UTF_8, tweetsPerWeek, "tweets");
		
		// Empirical distributions over the number of words, urls, hashtags etc. in each individual tweet.
		writeMapsToFile (outputDir.resolve ("tokencounts"), Charsets.UTF_8,
			wordsPerTweet,    "words",
			urlsPerTweet,     "urls",
			hashTagsPerTweet, "hashtags",
			smileysPerTweet,  "smileys",
			addrsPerTweet,    "addrs",
			tokensPerTweet,   "tokens"
		);
	}
	
	/**
	 * Writes the given line to the given {@link Writer}. If a {@link MalformedInputException}
	 * is thrown, indicating a charset issue, it's logged, and we return normally. 
	 * @param wtr
	 * @param fileDes
	 * @param text
	 * @throws IOException 
	 */
	private final static void writeSafely (BufferedWriter wtr, String fileDes, String text) throws IOException
	{	try
		{	wtr.write(text);
		}
		catch (MalformedInputException mie)
		{	LOG.error("Can't write out the following line to the " + fileDes + " file due to a charset issue " + mie.getMessage() + "\n\t" + text, mie);
		}
	}
	

	/**
	 * Writes a map out to a file. Keys are delimited from values by tabs, and key-value
	 * pairs are delimited from one another by newlines.
	 */
	private final static void writeMapToFile(Path file, Charset charset, Object2IntMap<String> urlCounts, String... mapName)
	{	Path singles = PathUtils.appendFileNameSuffix(file, "-sgls.txt");
		Path many    = PathUtils.appendFileNameSuffix(file, ".txt");
		
		String fileName = mapName.length == 0 ? "" : " " + mapName[0];
		try (BufferedWriter sgl = Files.newBufferedWriter(singles,charset);
				 BufferedWriter mny = Files.newBufferedWriter(many, charset))
		{	for (Object2IntMap.Entry<String> entry : urlCounts.object2IntEntrySet())
			{	writeSafely(
					entry.getIntValue() == 1 ? sgl : mny,
					fileName,
					entry.getKey().toString() + '\t' + String.valueOf (entry.getIntValue()) + '\n'
				);
			}
		}
		catch (Exception ioe)
		{	LOG.error("Error writing out the" + (mapName.length > 0 ? mapName[0] : "") + " map to file " + ioe.getMessage(), ioe);
		}
	}
	
	/**
	 * Writes a list of int-to-int maps to a file. The var-args parameter is a list
	 * of Int2IntMap and their (String) names, hence the Object type
	 * @throws IOException 
	 */
	private final static void writeMapsToFile (Path file, Charset charset, Object... mapsAndMapNames) throws IOException
	{	file = PathUtils.appendFileNameSuffix(file, ".txt");
		String name = "no-open-map";
	
		try (BufferedWriter wtr = Files.newBufferedWriter (file, charset);)
		{	for (int i = 0; i < mapsAndMapNames.length; i += 2)
			{	name = (String) mapsAndMapNames[i + 1];
				Int2IntMap map  = (Int2IntMap) mapsAndMapNames[i];
				
				for (Int2IntMap.Entry e : map.int2IntEntrySet())
					wtr.write (name + '\t' + e.getIntKey() + '\t' + e.getIntValue() + '\n');
			}
		}
		catch (Exception ioe)
		{	LOG.error("Error writing out the" + name + " map to file " + ioe.getMessage(), ioe);
		}
	}
	
	private int min(Set<Integer> set)
	{	int min = Integer.MAX_VALUE;
		Iterator<Integer> iter = set.iterator();
				
		while (iter.hasNext())
		{	min = Math.min (min, iter.next());
		}
		
		return min;
	}
	
	private int max(Set<Integer> set)
	{	int max = Integer.MIN_VALUE;
		Iterator<Integer> iter = set.iterator();
				
		while (iter.hasNext())
		{	max = Math.max (max, iter.next());
		}
		
		return max;
	}
	
	private static void inc (Object2IntMap<String> counts, String key)
	{	key = tidyStringKey(key);
		counts.put (key, counts.getInt(key) + 1);
	}

	/** Tidies string keys in hashmaps and sets - trimmed and to lower-case */
	private static String tidyStringKey(String key)
	{	return StringUtils.trimToEmpty(key).toLowerCase();
	}

	private static void inc (Int2IntMap counts, int key)
	{	counts.put (key, counts.get(key) + 1);
	}
	
	private static int get (Object2IntMap<String> counts, String key)
	{	key = tidyStringKey(key);
		return counts.getInt(key);
	}
	
	private final void incTagCount (DateTime tweetDate, String hashTag)
	{	inc (hashTagCounts, /* tweetDate,*/ hashTag.intern());
	}
	
	private final void incSmileyCount (DateTime tweetDate, String smiley)
	{	inc (smileyCounts, /*tweetDate,*/ smiley);
	}
	
	public Set<String> getExcludedUsers()
	{	return excludedUsers;
	}
	
	public void setExcludedUsers (Collection<String> excludedUsers)
	{	for (String user : excludedUsers)
			this.excludedUsers.add (tidyStringKey(user));
	}

	public DateTime getStartDateIncl()
	{	return startDateIncl;
	}

	public void setStartDateIncl(DateTime startDateIncl)
	{	this.startDateIncl = startDateIncl;
	}

	@SuppressWarnings("unused")
	private final void inc (Map<Integer, Object2IntMap<String>> counts, DateTime tweetDate, String hashTag)
	{
		int key = tweetDate.getYear() * 100 + tweetDate.getMonthOfYear();
		Object2IntMap<String> tagCounts = counts.get(key);
		if (tagCounts == null)
		{	tagCounts = new Object2IntAVLTreeMap<String>();
			tagCounts.defaultReturnValue(0);
			counts.put (key, tagCounts);
		}
		inc (tagCounts, hashTag.toLowerCase());
	}
	
	public static void main (String[] args) throws Exception
	{
		Path inputDir  = Paths.get("/Users/bryanfeeney/opt/twitter-tools-spider/src/test/resources/spider");
		Path outputDir = Paths.get("/Users/bryanfeeney/Desktop/DatasetStats");
		
		TwitterStats stats = new TwitterStats (inputDir, outputDir);
		stats.setExcludedUsers(DEFAULT_EXCLUDED_USERS);
		stats.setStartDateIncl(new DateTime (2013, 04, 01, 00, 00, 01, DateTimeZone.UTC));
		stats.call();
	}
	
	public final static Set<String> DEFAULT_EXCLUDED_USERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList (new String[] {
			"EconBrothers",
			"actioneconomics",
			"EatThisNotThat",
			"Fringer",
			"HendrixDesign",
			"PandoDaily",
			"billchenpoker",
			"cafreeland",
			"foreignoffice",
			"jmkarmstrong",
			"theDlasvegas",
			"theGrio",
			"BarbaraLedeen",
			"Black_Rose_ENT",
			"BuzzFeedAnimals",
			"CNETNews",
			"ElementaryForce",
			"GMA",
			"JonathanLanday",
			"arthurcrandon",
			"asteris",
			"bi_politics",
			"traychaney",
			"BookRiot",
			"DuncanBannatyne",
			"The_RobertEvans",
			"ZombyMusic",
			"brymell2",
			"DieWeiseRose",
			"EqualityDepot",
			"MrsPopsie",
			"PolitiBooks",
			"ritacolleen",
			"KristinaRibali",
			"LeakSourceNews",
			"debitking",
			"AmericanPapist",
			"DLYDJ",
			"MarketWatch",
			"TheOnion",
			"USATOpinion",
			"caleb_scharf",
			"iamcolinquinn",
			"nynjpaweather",
			"Barnsty72",
			"XantheClay",
			"jorcohen",
			"labourpress",
			"pamspaulding",
			"planetrockradio",
			"strangerslog",
			"tradingfloorcom",
			"1infanteriediv",
			"CBSNewYork",
			"FixedIncomeclub",
			"StealthMountain",
			"baseballpro",
			"jrf_uk",
			"peterleung",
			"sallykohn",
			"ELLEmagazine",
			"Fashionista_com",
			"HillaryPate",
			"Jerusalem_Post",
			"MattGoldstein26",
			"SUVFansclub",
			"SaveBradley",
			"TeodorFlonta",
			"gabrielarana",
			"legslonglength",
			"parva_x",
			"BritishRallyx",
			"CoffeeshopNews",
			"CongoRT",
			"RollingStones",
			"RugbyUnionNews",
			"ThomasMarzano",
			"anatotitan",
			"aotearoaxi",
			"bevysmith",
			"grahamruthven",
			"paleofuture",
			"shamyad",
			"AdrienneMarieP",
			"CronaEsler",
			"GreenPastures_",
			"HodderBooks",
			"PressSec",
			"RallyingUK",
			"Real_ESPNLeBrun",
			"afreedma",
			"economistmeg",
			"ericmetaxas",
			"hpshin",
			"mindthecam",
			"usNWSgov",
			"ADBClimate",
			"ArielFornari",
			"BODIESOFLIGHT",
			"BULLETTMedia",
			"BrooklynSpoke",
			"CSpillmann",
			"ChampionsSeries",
			"ClaireBerlinski",
			"ClimateCoLab",
			"HankCampbell",
			"MattCowleyBR",
			"MoTheBikeTD",
			"MuslimsRComing",
			"NASA_Astronauts",
			"OakvilleMayor",
			"OccupyAustin",
			"PLF2012",
			"PPSWOLauren",
			"Piscesboy69",
			"PracticalAction",
			"Randolph3John",
			"SHARKYontherun",
			"UNHCRUK",
			"WCVB",
			"WillWisbey",
			"YPLAC",
			"YorkshireRacing",
			"ZODIAC_MF",
			"chcktylr",
			"darealmaozedong",
			"evanwolfson",
			"howieinseattle",
			"indigogreenroom",
			"miningiq",
			"tommoracing",
			"15lovetennis",
			"AudioviSol15M",
			"AvrielleG",
			"BHarrisCountry",
			"CIFOR_forests",
			"HERMANOPRIMERO",
			"June4th",
			"KentuckyDerby",
			"LukeTully",
			"Marketingland",
			"SimonMarksFSN",
			"SleimanMichel",
			"UnWateredTruth",
			"WideAsleepNima",
			"acarvin",
			"asam_villaverde",
			"dandakich",
			"godolphin",
			"iainpope73",
			"josejoselimongi",
			"jsryding",
			"mranti",
			"saferreefer",
			"seaningle",
			"1RobBeasley",
			"AnneliePowell",
			"BookDesignGirl",
			"COEDMagazine",
			"CardiffBiz",
			"ESPN_BigTen",
			"Fadl_Shakker",
			"GoSemi",
			"GuildofBG",
			"Ian_from_TO",
			"LiveNation",
			"McKinsey_MGI",
			"SIFFnews",
			"alexisgoldstein",
			"bhaggs",
			"bycLIVE",
			"capntransit",
			"cricketingview",
			"dosomething",
			"duttypaul",
			"frdragonspouse",
			"janinezacharia",
			"mchui",
			"rhavers",
			"themaria",
			"thenation",
			"ChasnHodges",
			"DebMildenstein",
			"Edgecliffe",
			"FriedrichHayek",
			"GraceMurphy2",
			"HannahAllam",
			"JISEA1",
			"JamesPiotr",
			"JeffQuinton",
			"MSAUK",
			"MamaCre8s",
			"MarketplaceAPM",
			"MiddlehamPark",
			"PalestineToday",
			"PharmaceuticBen",
			"Quito113",
			"RedScareBot",
			"RobertBluey",
			"SenGillibrand",
			"TravWriters",
			"WLBSA",
			"WestchesterOcpy",
			"all4syria",
			"cliffordbennett",
			"franko1986",
			"hayfestival",
			"mitchjoel",
			"premrugby",
			"seiriol",
			"shaawasmund",
			"sportsfitnessnz",
			"Adamdbain",
			"BrianLehrer",
			"ChrisGorham",
			"Drudge_Siren",
			"GeorgeDobell1",
			"MattMcBradley",
			"MeganHustwaite",
			"SOIreland",
			"Sportingeye",
			"Tim_Stevens",
			"TinManBrewing",
			"_hatman",
			"britishrally",
			"championbeer",
			"danzarrella",
			"themoornextdoor",
			"yayayarndiva",
			"Hope_Smoke",
			"LupeFiasco",
			"MattChorley",
			"MauriceHamilton",
			"PokerScout1",
			"SeanQuinnracing",
			"Shoq",
			"Uniprecaria",
			"allahcentric",
			"openspacer",
			"sbnation",
			"CraftingANation",
			"DanPitchside",
			"Fact",
			"GrahamMotion",
			"KLM",
			"LibertyLndnGirl",
			"NNEASY",
			"NautilusMag",
			"RupertMyers",
			"TheSunNewspaper",
			"TicBot",
			"ddale8",
			"ibnezra",
			"johncabell",
			"peoplemag",
			"sacca",
			"tatts_ireland",
			"Aelkus",
			"Harryb22",
			"KevinNR",
			"TomRtweets",
			"da_kyky",
			"ewerickson",
			"indie_promotor",
			"jnovogratz",
			"lollypike",
			"smsaideman",
			"thewallgroup",
			"yurikageyama",
			"24Mattino",
			"Amparopolo",
			"BrownieScott",
			"Cllrwlaceyind",
			"EdgeofSports",
			"Guttmacher",
			"MADDSTER_7281",
			"MiguelDelaney",
			"PuckBuddys",
			"SeaTransitBlog",
			"Technologypr_eu",
			"YasserHashmi",
			"artwiz",
			"citizenrobert",
			"drivetimerte",
			"eu2013ie",
			"lkafle",
			"rjallain",
			"scanadu",
			"AcampadaLH",
			"AircrewBuzz",
			"AndyBoyBlue",
			"AnnalynKurtz",
			"BradLindenSoCal",
			"CatbytheC",
			"HuffingtonPost",
			"IntelTweet",
			"MediaPost",
			"NICKIMINAJ",
			"NakedWines",
			"PPLouisiana",
			"XanderArmstrong",
			"irevolt",
			"nahumg",
			"skenigsberg",
			"AdviceToWriters",
			"BresPolitico",
			"BusinessChannel",
			"CnservativePunk",
			"DukascopyFX",
			"FOX2News",
			"KevinBankston",
			"NavanRacecourse",
			"PPActionCA",
			"Phillip_Blond",
			"ProfessorNana",
			"RBPundit",
			"USArmy",
			"beermepodcast",
			"eye4style",
			"kameelpremhid",
			"missy2916",
			"palafo",
			"thekapman",
			"wbsustaindev",
			"BBC6Music",
			"BBCRadMac",
			"BarryPavel",
			"BarzzNet",
			"CColpetzer",
			"CuccinelliPress",
			"EC_Kosters",
			"HylandIan",
			"Jeremy_Newmark",
			"JohnitoBlog",
			"Rarebits",
			"SabrinaSiddiqui",
			"Xbox",
			"brooke",
			"dana1981",
			"evanchill",
			"jimmiebjr",
			"littlefluffycat",
			"mollygreene",
			"omarg",
			"usembassybeirut",
			"BLOWNMlND",
			"CFAinstitute",
			"FGW",
			"KeeleyMP",
			"MorgansDead",
			"NatSecWonk",
			"_Teb",
			"oss_romano",
			"paulkbiba",
			"sophos_anz",
			"stephsearle90",
			"EconomicsGuide",
			"FMR_Brussels",
			"JoshBooneMovies",
			"ScottBix",
			"WFMU",
			"alisonwillmore",
			"bobgourley",
			"freakPANDAboy",
			"licamp101",
			"GaryDStratton",
			"HealthyLiving20",
			"InStyle",
			"LccSy",
			"Nightline",
			"PostWorldNews",
			"TraceAdkins",
			"VictorRocha1",
			"WCL_Shawn",
			"keejayOV",
			"liamhighfield",
			"maggiepolitico",
			"sswinkgma",
			"washmonthly",
			"BADPtweets",
			"BloombergView",
			"CSLakin",
			"ChrandMANGMENT",
			"KIRO7Seattle",
			"Mktgeistcom",
			"RacingLight",
			"TheCriminalBar",
			"campbellsuz",
			"cebsilver",
			"freep",
			"harrismonkey",
			"helenczerski",
			"kelligrant",
			"spreadingJOY",
			"walshdominic",
			"willguyatt",
			"4pawsbrewing",
			"BloombergNews",
			"Cynegeticus",
			"EasyTigerATX",
			"GLOCKInc",
			"INETeconomics",
			"JustinaMusic",
			"MSF_france",
			"Moosefucker",
			"Paul71",
			"ScooterSchaefer",
			"SkyNewsBiz",
			"brewessentials",
			"hughhewitt",
			"janeriklarsson",
			"kmingis",
			"nickschmidle",
			"osynkem",
			"plepeltier",
			"susanslusser",
			"CGasparino",
			"CourtneyPFB",
			"FixWMATA",
			"HyperionVoice",
			"Loeb_Seb1",
			"RicCharts",
			"TheKouk",
			"TheWrap",
			"cindygallop",
			"cmcoop77",
			"dinodogan",
			"hyperallergic",
			"insidenu",
			"mcauz56",
			"muckrack",
			"rocketfuelinc",
			"savesouthwark",
			"scrippsnews",
			"skolkovo",
			"AANafgh",
			"SeekingAlpha",
			"SocialTimes",
			"TomxDoyle",
			"WeLoveDeephouse",
			"_KatherineBrown",
			"andrs_mr",
			"dmataconis",
			"ilyseh",
			"livesquawk",
			"michaelcshort",
			"owillis",
			"ryansholin",
			"shumpty77",
			"AlqassamBrigade",
			"IAMPHILLYCHASE",
			"NewsBreaker",
			"PMac21",
			"SkyRacingAU",
			"SteveBurtch",
			"TonyBarretTimes",
			"UPPastryPlate",
			"iowahawkblog",
			"jricole",
			"ppsworegon",
			"richiemclernon",
			"thegarance",
			"BIGKRIT",
			"CityWineryCHI",
			"HelpforHeroes",
			"KathyLLogan",
			"Marcelcomics",
			"PensInsideScoop",
			"RyanMaue",
			"Sergeant_Howie",
			"TheGoogleFactz",
			"al_matic",
			"fylderugby",
			"one_quus_one",
			"petermbenglish",
			"radiobubble",
			"Alea_",
			"Brown_Moses",
			"DRYmadrid",
			"DrPeggyDrexler",
			"JuliaRosien",
			"Oxford_News",
			"PattiParson",
			"TheDemocrats",
			"The_Paris_Angel",
			"WeAreRagbags",
			"WestSixth",
			"cbsboston",
			"ckwright",
			"grammaprete",
			"lindsaywise",
			"perlapell",
			"quebec_news",
			"HESherman",
			"LegendarySrat",
			"abeaujon",
			"expressandecho",
			"intermarketblog",
			"mamtabadkar",
			"susiebright",
			"theage",
			"Arab_News",
			"BritishMonarchy",
			"FindRugbyNow",
			"MParekh",
			"MrLix",
			"TPBderek",
			"TheBoiledEgg",
			"TheOrangeCone",
			"TheRealCurve",
			"csmonitor",
			"fondalo",
			"misstonic813",
			"mzemek",
			"projecteve1",
			"se4realhinton",
			"terrinakamura",
			"101greatgoals",
			"Acampadasbd",
			"CelesteHeadlee",
			"ComplexMag",
			"Jonathan_Dunphy",
			"KyodoNewsENG",
			"MungoNGus",
			"NikkiFinke",
			"NoahCRothman",
			"TheStanchion",
			"TheWomensRoomUK",
			"charliespiering",
			"dordotson",
			"heatworld",
			"milanoics",
			"92Y",
			"Chari_S",
			"JJCarafano",
			"ScoutsHonor",
			"SnookerUSA",
			"THR",
			"TheNoLookPass",
			"acognews",
			"graceishuman",
			"hockeyfights",
			"talkhoops",
			"4AllSurfaces",
			"4Catholics",
			"ClementEsebamen",
			"Davis_Harr",
			"ENERGYbits",
			"FXDIRK",
			"Heilemann",
			"OpheliaBenson",
			"Rick_City",
			"Soulseeds1",
			"agreatbigcity",
			"deborahamos",
			"huffpostgay",
			"inrockmusic",
			"jendeaderick",
			"mintel2011",
			"nascarcasm",
			"thedaveywavey",
			"AnjumKiani",
			"B4INBarracuda",
			"IndoSport",
			"MKPS001",
			"TwigsTrillycake",
			"WFANAudio",
			"etribune",
			"lisaorchard1",
			"BBCNewsbeat",
			"BBCSport",
			"CauseofourJoy",
			"HarryFlowersOBE",
			"HelenRazer",
			"HuffPostStyle",
			"JohnCornyn",
			"MarkLazerus",
			"RallyFM",
			"Simplify",
			"TeaStreetBand",
			"Team_MUFC",
			"bigjohns",
			"editorialiste",
			"gelatobaby",
			"hitmanholt",
			"smainfo",
			"sundersays",
			"themarkberman",
			"Benzinga",
			"FuturesTrader71",
			"HackneySociety",
			"LBCI_News_EN",
			"LorraineELLE",
			"WSJcanada",
			"businessinsider",
			"newscomauHQ",
			"showcasingwomen",
			"AlexanderNL",
			"EventRidersAssc",
			"KingofShaves",
			"MainEventTravel",
			"PattieCurran",
			"Pebble",
			"bghayward",
			"jeffbullas",
			"megannyt",
			"rolandsmartin",
			"zerohedge",
			"ABC",
			"AmandaJDavies",
			"BritishVogue",
			"EdMatts",
			"FAC7S",
			"FAPPA",
			"HuffPostUK",
			"LoveMyDressBlog",
			"MarketPlunger",
			"MikeElk",
			"NolteNC",
			"RedAlert",
			"RichardHaass",
			"RogerPielkeJr",
			"Rumplestatskin",
			"SBienkowski",
			"StephanieWei",
			"The405",
			"Variety",
			"alexios13",
			"aliamjadrizvi",
			"bryce_carey",
			"cnnmornings",
			"dbernstein",
			"ireland",
			"ljndawson",
			"new_jersey",
			"ruhlman",
			"sgw94",
			"ForbesWoman",
			"GrubStreetBOS",
			"InnoBystander",
			"JoeYerdonPHT",
			"MattZeitlin",
			"TedStarkey",
			"TheBubbleBubble",
			"TrendsUK",
			"diane_berard",
			"ilpost",
			"jimray",
			"jonubian",
			"markus_uvell",
			"new_york_post",
			"thewillofdc",
			"Bye_Dogma",
			"CartmelSticky",
			"ESPNUK",
			"EverydayHealth",
			"FriendsAllyPark",
			"JohnWilson",
			"KennyHerzog",
			"Linkiesta",
			"PamBelluck",
			"SirSandGoblin",
			"SqueezedEquity",
			"Studentinlife",
			"TuftsVOX",
			"bet365",
			"garethoconnor",
			"DailyMirror",
			"PPOrlando",
			"VAPolitical",
			"carltonkirby",
			"colbycosh",
			"cwharlow",
			"faizanlakhani",
			"jazzagold",
			"juanramajete",
			"CatoInstitute",
			"DustinWelbourne",
			"FatGirlvsWorld",
			"JAMortram",
			"LaMediaInglesa",
			"SpeakComedy",
			"TPBadam",
			"WiredUK",
			"ZimMission",
			"amirightfolks",
			"bbcarabicalerts",
			"dozba",
			"iofiv",
			"jonahkeri",
			"nomfup",
			"paulvieira",
			"proactive_au",
			"pushprlondon",
			"stevekovach",
			"subirch",
			"zhandlen",
			"AJEnglish",
			"FRANCE24",
			"IvanTheK",
			"Liberationtech",
			"MehrTarar",
			"MotherJones",
			"MsBooWho",
			"SWSA_Andrew",
			"Salon_Arts",
			"SkySports",
			"Snoodit",
			"TheOrganicView",
			"arhobley",
			"borislavkiprin",
			"dailydot",
			"gopfirecracker",
			"laurenlaverne",
			"marvinliao",
			"snookerbacker1",
			"stevebuttry",
			"weuropa",
			"wildandreas",
			"1SteveWade",
			"AlecMapa",
			"AndyGlockner",
			"BlackGirlNerds",
			"BloombergTV",
			"Brid1964",
			"CBSThisMorning",
			"CCOOeducacio",
			"ForexLive",
			"LeanGrnBeanBlog",
			"NBCSports",
			"Petrit",
			"PezDOY",
			"ProPublica",
			"SDHoneymonster",
			"TVandDinners",
			"deepextra_cover",
			"mailbox",
			"meekakitty",
			"mrsoaroundworld",
			"raehanbobby",
			"ramzinohra01",
			"rezaaslan",
			"thedailybeast",
			"ChurchillDowns",
			"GlobalGrind",
			"InterviewIQ",
			"IsabelHardman",
			"JuddLegum",
			"Porfirogenita",
			"TibitXimer",
			"TravisHeHateMe",
			"afabbiano",
			"boyand1",
			"ckanal",
			"dkeithwil",
			"eoghanmcdermo",
			"ilawton",
			"lady505a",
			"lesvachesdutour",
			"memeorandum",
			"moronwatch",
			"waiyeehong",
			"AMAnet",
			"Aidan_m_",
			"AntDeRosa",
			"BBCNewsUS",
			"EnigmatikBGDB",
			"HuffPostPol",
			"LouthProlifeNet",
			"RafNicholson",
			"catholicbloggs",
			"eonline",
			"mattstaggs",
			"newcitiesfound",
			"rhrealitycheck",
			"richarddeitsch",
			"thegulfblog",
			"AP",
			"BeardedGenius",
			"JustJared",
			"StephintheUS",
			"dhothersall",
			"michaelwhite147",
			"quatremer",
			"Bundesliga4u",
			"FinancialReview",
			"TheCatchFence",
			"YBDfashion",
			"denverpost",
			"drgrist",
			"futurejourno14",
			"hotdog6969",
			"lrozen",
			"mergerecords",
			"z_herz",
			"AngryBritain",
			"JPie612",
			"LifeExtension",
			"ProSnookerBlog",
			"RT_com",
			"RevRichardColes",
			"SweetSoaps",
			"TLaceyC",
			"aoindependence",
			"draglikepull",
			"eyeseast",
			"intelwire",
			"moorehn",
			"nikkibedi",
			"paddypower",
			"txvoodoo",
			"yourdailycare",
			"ChrisRBarron",
			"DRUDGE_REPORT",
			"ErikFoss8",
			"JoeNBC",
			"Kevin_Healey",
			"LalitKModi",
			"OUPAcademic",
			"PJSvD",
			"RedditCBJ",
			"RussOnPolitics",
			"SPINmagazine",
			"ShutdownLine",
			"SkyBet",
			"allanholloway",
			"bobbymacReports",
			"jamesf40",
			"john_rostron",
			"mirtle",
			"riotta",
			"whitehouse",
			"xpostfactoid1",
			"1Password",
			"Ataraxis00",
			"CNET",
			"Daddymojo",
			"EspuelasVox",
			"FortyDeuceTwits",
			"MaStrozyk",
			"NEAarts",
			"NewOnAudible",
			"SlaughterAM",
			"Squawka",
			"TheCricketGeek",
			"TheF1Blogger",
			"UKConservative",
			"ZekeJMiller",
			"bondskew",
			"bufferapp",
			"emilylhauser",
			"emmacargo",
			"hadiyah",
			"jaredbkeller",
			"jennbookshelves",
			"ktbenner",
			"latinorebels",
			"linushugosson",
			"mvbijlert",
			"nidssserz",
			"noelito",
			"sahildutta",
			"sidlowe",
			"AndrewCouts",
			"AngelaRII1",
			"ArleeBird",
			"BBCWorld",
			"CNBCWorld",
			"DavidClinchNews",
			"Hafsa_Khawaja",
			"HotlineJosh",
			"HuffPostBiz",
			"IndyPolitics",
			"LV_Cricket",
			"Rare",
			"WSJ",
			"WajahatAli",
			"amandapalmer",
			"andreacremer",
			"cfarivar",
			"chrisadamsmkts",
			"domdyer70",
			"haroldpollack",
			"luxury__travel",
			"networkjesus",
			"ntalia_77",
			"smh",
			"tomjensen100",
			"Allison_Good1",
			"Bookgirl96",
			"FarikoIRGRL",
			"FifthGrouper",
			"FreedomWorks",
			"HuffPostLive",
			"ILLuminaTAYY",
			"Lavvelenata",
			"NidaKhanNY",
			"Reuters",
			"ScottKirsner",
			"TfLTrafficNews",
			"TheStalwart",
			"aiww",
			"eatlikeagirl",
			"gralarroude",
			"karenpriestley",
			"karin_sebelin",
			"kate_sheppard",
			"kristoncapps",
			"memomoment",
			"nycjim",
			"nytimes",
			"AlexParkerDC",
			"DavidMDrucker",
			"Ladbrokes",
			"PeoplesBlog1878",
			"ShanghaiUpdate",
			"TeDeumBlog",
			"VampireMob",
			"caracazo2013",
			"cooper_m",
			"geekanoids",
			"mattyglesias",
			"prisonculture",
			"Backbencher",
			"CharleneCac",
			"DomsWildThings",
			"JeremyPond",
			"NOW_eng",
			"Niamh__Flynn",
			"SoccerByIves",
			"jayski_nascar",
			"joshspero",
			"learnpublishing",
			"thecricketcouch",
			"ABCWorldNews",
			"Azizsaadon",
			"BarrioLavapies",
			"HayesBrown",
			"LaurenYoung",
			"NASCARonSPEED",
			"PeterLaBarbera",
			"RaniaKhalek",
			"SabrinaGhayour",
			"Slate",
			"Zonal_Marking",
			"cbSocially",
			"davidhall75",
			"georgegalloway",
			"msnbc",
			"plussone",
			"socialmediavcr",
			"tamarsw",
			"BostonGlobe",
			"BristolOldVic",
			"DrNancyNBCNEWS",
			"Fhamiltontimes",
			"Grange95",
			"KyleTurney",
			"MoodysRatings",
			"ProNetworkBuild",
			"ScotlandTonight",
			"SteveStfler",
			"Turkish_Futbol",
			"UNICEF_uk",
			"ahramonline",
			"autosportlive",
			"clusterstock",
			"metoffice",
			"realbeauties",
			"socprogress",
			"tim_maliyil",
			"wonkawonders",
			"AlsoNamedPhil",
			"CardoGotWings",
			"DailyStarLeb",
			"Ferrari_Boy1017",
			"Forbes",
			"GlobalEcoGuy",
			"GovLab",
			"HuffPostComedy",
			"JohnJGeddes",
			"MichiganDEQ",
			"SimonGleave",
			"Soulseedscoach",
			"TheSportInMind",
			"amhaincreations",
			"eroston",
			"fashawn",
			"matthiasrascher",
			"msnsport",
			"wearerabble",
			"CarloStagnaro",
			"DPJHodges",
			"Dumonjic_Alen",
			"GP3_Official",
			"Greatbigrobot",
			"Jabaldaia",
			"LoHud",
			"MarianHossaSay",
			"NYCSmallBiz",
			"NoMorePage3",
			"RD_Tennistalk",
			"TIME",
			"Toltecjohn",
			"YahooSportsNHL",
			"geekstarter",
			"grahamwitch1",
			"lsarsour",
			"mathewi",
			"thehill",
			"thejournal_ie",
			"Damian_Barr",
			"JoeMyGod",
			"KISSmetrics",
			"MarkMaddenX",
			"OscarSurisWF",
			"PlashingVole",
			"RossoRestaurant",
			"SuicidePass",
			"TemerityJane",
			"aegies",
			"appletweets",
			"aravosis",
			"chrisgeidner",
			"darrenrovell",
			"fangsbites",
			"mashable",
			"moha_doha",
			"racheljoyce",
			"rpetty80",
			"superfooty",
			"Cocoshrugged",
			"JonYoungAuthor",
			"Mamamia",
			"MoAnsar",
			"NeilHarmanTimes",
			"Orbette",
			"QGinfo",
			"SkyNews",
			"StateDept",
			"UberFacts",
			"alex_willis",
			"caropaquin80",
			"emmablackery",
			"finansakrobat",
			"finsbury_pk",
			"justkarl",
			"snookerisland",
			"CSRwire",
			"ChrisSkelton87",
			"EchteDemokratie",
			"KittyBradshaw",
			"MailOnline",
			"MichaelSkolnik",
			"arseblog",
			"calestous",
			"everyword",
			"iPianis",
			"josordoni",
			"quaG27",
			"vkhosla",
			"AutomotiveNews4",
			"BroBible",
			"Choire",
			"DaveMcGlinchey",
			"Flemington_News",
			"JazzShaw",
			"McCaineNL",
			"Mediaite",
			"PeteAbe",
			"UberSoc",
			"YasmineGalenorn",
			"miss_mcinerney",
			"missprofanity",
			"mjrobbins",
			"mosharrafzaidi",
			"mydesire",
			"nadalnews",
			"prtygrlgonebad",
			"50Pips",
			"AnneWheaton",
			"FreeBeacon",
			"HonestFrank",
			"ISAFmedia",
			"IrishTimesSport",
			"MatRicardo",
			"OutFrontCNN",
			"WhatTheBit",
			"billboard",
			"crbncaptrreprt",
			"gaycivilrights",
			"jrishel",
			"ngreenberg",
			"npratc",
			"obrien",
			"ChristopherNFox",
			"Deadline",
			"DundalkStadium",
			"English_AS",
			"FDALawyers",
			"FionaMullenCY",
			"GuardianUS",
			"JulianSimpson1",
			"MentalHealthCop",
			"PPact",
			"ProFootballTalk",
			"RalstonReports",
			"RedCrossEastMA",
			"SpikeLee",
			"TheCut",
			"b3ta_links",
			"pegduncan",
			"seanhackbarth",
			"truth2beingfit",
			"weddady",
			"DanielStrauss4",
			"DazedMagazine",
			"ESPNStatsInfo",
			"Foxgoose",
			"OpenEurope",
			"RugbySpy",
			"Russostrib",
			"SladeHV",
			"SonnyBunch",
			"TPtherapy",
			"Telegraph",
			"WomenInWorld",
			"abcgrandstand",
			"alanwilkins22",
			"baltimoresun",
			"chrissmallcoach",
			"davidcoverdale",
			"jamietarabay",
			"jaweedkaleem",
			"jpodhoretz",
			"nytimesglobal",
			"stevyncolgan",
			"SixxFeva"
		})));
}
