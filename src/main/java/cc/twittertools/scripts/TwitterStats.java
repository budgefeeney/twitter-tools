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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.twittertools.post.SavedTweetReader;
import cc.twittertools.post.Tweet;
import cc.twittertools.util.FilesInFoldersIterator;
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
//	private static final int EXPECTED_WORD_COUNT = 50_000;
	
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
		
		postsSinceDay.defaultReturnValue(0);
		firstPostByUserAsDay.defaultReturnValue(0);
		interPostTimeMins.defaultReturnValue(0);
		retweetsByUser.defaultReturnValue(0);
		rtRetweetsByUser.defaultReturnValue(0);
		tweetsPerUser.defaultReturnValue(0);
		addresseeCounts.defaultReturnValue(0);
		urlCounts.defaultReturnValue(0);
		wordCounts.defaultReturnValue(0);
		hashTagCounts.defaultReturnValue(0);
		smileyCounts.defaultReturnValue(0);
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
				  		Iterator<Pair<TokenType, String>> iter = vec.toWords(tweet.getMsg());
				  		while (iter.hasNext())
				  		{	Pair<TokenType, String> tokenValue = iter.next();
				  			switch (tokenValue.getKey())
				  			{	case URL:
				  					inc (urlCounts, tokenValue.getValue());
				  					break;
				  				case USERNAME:
				  					inc (addresseeCounts, tokenValue.getValue());
				  					break;
				  				case HASHTAG:
				  					incTagCount (tweetDate, tokenValue.getValue());
				  					break;
				  				case EMOTICON:
				  					incSmileyCount (tweetDate, tokenValue.getValue());
				  					break;
				  				case TOKEN:
				  					inc (wordCounts, tokenValue.getValue());
				  					if (tokenValue.getValue().length() == 1)
				  						LOG.warn ("Tweet " + String.format("%5d", tweetCount) + ": Single character word '" + tokenValue.getValue() + "' dervied from message '" + tweet.getMsg() + "'");
				  				default:
				  					break;
				  			}
				  		}
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
		writeMapToFile (outputDir.resolve("dictionary.txt"), Charsets.UTF_8, wordCounts, "word-counts");
		
		// The list of addressees
		writeMapToFile (outputDir.resolve("addressees.txt"), Charsets.UTF_8, addresseeCounts, "addressee-counts");
		
		// The list of hashtags
		writeMapToFile (outputDir.resolve("hashtags.txt"), Charsets.UTF_8, hashTagCounts, "hashtag-counts");	
		
		// The list of smileys
		writeMapToFile (outputDir.resolve("smileys.txt"), Charsets.UTF_8, hashTagCounts, "smiley-counts");	
		
		// Dictionary of URLs
		writeMapToFile(outputDir.resolve("urls.txt"), Charsets.UTF_8, urlCounts, "url-counts");
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
		{	LOG.error("Can't write out the following line to the " + text + " due to a charset issue " + mie.getMessage() + "\n\t" + text, mie);
		}
	}
	

	/**
	 * Writes a map out to a file. Keys are delimited from values by tabs, and key-value
	 * pairs are delimited from one another by newlines.
	 */
	private final static void writeMapToFile(Path file, Charset charset, Object2IntMap<String> urlCounts, String... mapName)
	{	String fileName = mapName.length == 0 ? "" : " " + mapName[0];
		try (BufferedWriter wtr = Files.newBufferedWriter(file, Charsets.UTF_8);)
		{	for (Object2IntMap.Entry<String> entry : urlCounts.object2IntEntrySet())
			{	writeSafely(wtr, fileName, entry.getKey().toString() + '\t' + String.valueOf (entry.getIntValue()) + '\n');
			}
		}
		catch (Exception ioe)
		{	LOG.error("Error writing out the" + (mapName.length > 0 ? mapName[0] : "") + " map to file " + ioe.getMessage(), ioe);
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

	/** Tidies string keys in hashmaps - trimmed and to lower-case */
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
		Path inputDir  = Paths.get("/Users/bryanfeeney/datasets/twitter-20130828");
		Path outputDir = Paths.get("/Users/bryanfeeney/Desktop/");
		
		new TwitterStats (inputDir, outputDir).call();
	}
}
