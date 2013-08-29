package cc.twittertools.scripts;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
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
public class DateStats implements Callable<Integer>
{
	private static final int MAX_INTER_TWEET_TIME_MINS = 31 * 24 * 60; // basically we expect users to tweet at least once a month
	private static final int NUM_USERS_IN_DATASET = 21000;
	private static final int DATASET_LENGTH_IN_DAYS = 365;
	private static final int EXPECTED_HASH_TAG_COUNT = 3000;
	private static final int EXPECTED_URL_COUNT = 1000000;
	private static final int EXPECTED_WORD_COUNT = 50000;
	
	private static final int MAX_CORRUPTED_TWEETS_PER_FILE = 5;

	private final static Logger LOG = LoggerFactory.getLogger(DateStats.class);
	
	private final Int2IntArrayMap          postsSinceDay;
	private final Map<String,  DateTime>   lastPostByUser;
	private final Map<String,  DateTime>   firstPostByUser;
	private final Map<String,  Integer>    firstPostByUserAsDay;
	private final Int2IntArrayMap          interPostTimeMins;
	private final Map<String,  MutableInt> retweetsByUser;
	private final Map<String,  MutableInt> rtRetweetsByUser;
	private final Map<String,  MutableInt> tweetsPerUser;
	private final Map<Integer, Map<String,  MutableInt>> hashTagCount;
	private final Map<Integer, Map<String,  MutableInt>> smileyCounts;
	private final Set<String>              addressees;
	private final Map<String,  MutableInt> urlCounts;
	private final Set<String>              dictionary;
	
	private final Path datasetDirectory;
	private final Path outputDir;
	
	
	
	public DateStats(Path datasetDirectory, Path outputDir)
	{
		super();
		this.datasetDirectory  = datasetDirectory;
		this.outputDir         = outputDir;
		
		postsSinceDay        = new Int2IntArrayMap(DATASET_LENGTH_IN_DAYS);
		lastPostByUser       = new HashMap<>(NUM_USERS_IN_DATASET);
		firstPostByUser      = new HashMap<>(NUM_USERS_IN_DATASET);
		firstPostByUserAsDay = new HashMap<>(NUM_USERS_IN_DATASET);
		interPostTimeMins    = new Int2IntArrayMap(MAX_INTER_TWEET_TIME_MINS);
		retweetsByUser       = new HashMap<>(NUM_USERS_IN_DATASET);
		rtRetweetsByUser     = new HashMap<>(NUM_USERS_IN_DATASET);
		tweetsPerUser        = new HashMap<>(NUM_USERS_IN_DATASET);
		hashTagCount         = new HashMap<>(13 * 12); // 13 years, month by month
		smileyCounts         = new HashMap<>(13 * 12); // 13 years, month by month
		addressees           = new HashSet<>(NUM_USERS_IN_DATASET * 10);
		urlCounts            = new HashMap<>(EXPECTED_URL_COUNT);
		dictionary           = new HashSet<>(EXPECTED_WORD_COUNT);
		
		postsSinceDay.defaultReturnValue(0);
		interPostTimeMins.defaultReturnValue(0);
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
				  					addressees.add (tokenValue.getValue());
				  					break;
				  				case HASHTAG:
				  					incTagCount (tweetDate, tokenValue.getValue());
				  					break;
				  				case EMOTICON:
				  					incSmileyCount (tweetDate, tokenValue.getValue());
				  					break;
				  				case TOKEN:
				  					dictionary.add (tokenValue.getValue());
				  					if (tokenValue.getValue().length() == 1)
				  						System.out.println ("Tweet " + String.format("%5d", tweetCount) + ": Single character word '" + tokenValue.getValue() + "' dervied from message '" + tweet.getMsg() + "'");
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
				wtr.write(
					user                             + '\t' +
					firstPostByUser.get(user)        + '\t' +
					get (firstPostByUserAsDay, user) + '\t' +
					lastPostByUser.get(user)         + '\t' +
					get (tweetsPerUser, user)        + '\t' +
					get (retweetsByUser, user)       + '\t' +
					get (rtRetweetsByUser, user)     + '\n'
				);
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out user statistics to file " + ioe.getMessage(), ioe);
		}
		
		// Inter post time statistics
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("interPostStats.txt"), Charsets.UTF_8))
		{	for (Int2IntMap.Entry entry : interPostTimeMins.int2IntEntrySet())
				wtr.write(Integer.toString (entry.getIntKey()) + '\t' + String.valueOf(entry.getIntValue()) + '\n');;
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out inter-post time statistics to file " + ioe.getMessage(), ioe);
		}
		
		// Tweets since date counts
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("postsSinceDay.txt"), Charsets.UTF_8))
		{	IntSet set   = postsSinceDay.keySet();
			int    start = min (set);
			int    end   = max (set);
			
			int cumulative = 0;
			for (int day = end; day >= start; day--)
			{	cumulative += postsSinceDay.get(day);
				wtr.write(Integer.toString (day) + '\t' + Integer.toString (cumulative) + '\n');
			}
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out counts of tweets since dates to file " + ioe.getMessage(), ioe);
		}
		
		// The dictionary of words
		try
		{	FileUtils.writeLines(outputDir.resolve("dictionary.txt").toFile(), Charsets.UTF_8.name(), dictionary);
		}
		catch (IOException ioe)
		{	LOG.error ("Error writing out dictionary of words to file " + ioe.getMessage(), ioe);
		}
		
		// The list of addressees
		try
		{	FileUtils.writeLines(outputDir.resolve("addressees.txt").toFile(), Charsets.UTF_8.name(), addressees);
		}
		catch (IOException ioe)
		{	LOG.error ("Error writing out list of addressees to file " + ioe.getMessage(), ioe);
		}
		
		// The list of hashtags
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("hashtags.txt"), Charsets.UTF_8); )
		{	List<Integer> dates = new ArrayList<Integer>(hashTagCount.keySet());
			Collections.sort(dates);
			for (int key : dates)
			{	int month = key % 100;
				int year  = key / 100;
				
				for (Map.Entry<String, MutableInt> entry : hashTagCount.get(key).entrySet())
				{	wtr.write (
						"" + year        + '\t' +
						month            + '\t' +
						entry.getKey()   + '\t' +
						entry.getValue() + '\n'
					);
				}
			}
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out counts of hashtags to file " + ioe.getMessage(), ioe);
		}
		
		
		// The list of smileys
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("smileys.txt"), Charsets.UTF_8); )
		{	List<Integer> dates = new ArrayList<Integer>(smileyCounts.keySet());
			Collections.sort(dates);
			for (int key : dates)
			{	int month = key % 100;
				int year  = key / 100;
				
				for (Map.Entry<String, MutableInt> entry : smileyCounts.get(key).entrySet())
				{	wtr.write (
						"" + year        + '\t' +
						month            + '\t' +
						entry.getKey()   + '\t' +
						entry.getValue() + '\n'
					);
				}
			}
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out counts of smileys to file " + ioe.getMessage(), ioe);
		}
		
		// Dictionary of URLs
		try (BufferedWriter wtr = Files.newBufferedWriter(outputDir.resolve("urls.txt"), Charsets.UTF_8);)
		{	for (Map.Entry<String, MutableInt> entry : urlCounts.entrySet())
			{	wtr.write(entry.getKey() + '\t' + entry.getValue() + '\n');
			}
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out counts of URLs to file " + ioe.getMessage(), ioe);
		}
	}
	
	private int min(IntSet set)
	{	int min = Integer.MAX_VALUE;
		IntIterator iter = set.iterator();
				
		while (iter.hasNext())
		{	min = Math.min (min, iter.nextInt());
		}
		
		return min;
	}
	
	private int max(IntSet set)
	{	int max = Integer.MIN_VALUE;
		IntIterator iter = set.iterator();
				
		while (iter.hasNext())
		{	max = Math.max (max, iter.nextInt());
		}
		
		return max;
	}

	private static void inc (Map<String, MutableInt> counts, String key)
	{	key = tidyStringKey(key);
		MutableInt count = counts.get(key);
		if (count == null)
		{	count = new MutableInt(0);
			counts.put (key, count);
		}
		count.increment();
	}

	/** Tidies string keys in hashmaps - trimmed and to lower-case */
	private static String tidyStringKey(String key)
	{	return StringUtils.trimToEmpty(key).toLowerCase();
	}
	
	private static void inc (Int2IntArrayMap counts, int key)
	{	counts.put (key, counts.get(key) + 1);
	}
	
	private static <V extends Number> int get (Map<String, V> counts, String key)
	{	key = tidyStringKey(key);
		V count = counts.get(key);
		return count == null ? 0 : count.intValue();
	}
	
	private final void incTagCount (DateTime tweetDate, String hashTag)
	{	inc (hashTagCount, tweetDate, hashTag);
	}
	
	private final void incSmileyCount (DateTime tweetDate, String smiley)
	{	inc (smileyCounts, tweetDate, smiley);
	}
	
	private final void inc (Map<Integer, Map<String, MutableInt>> counts, DateTime tweetDate, String hashTag)
	{
		int key = tweetDate.getYear() * 100 + tweetDate.getMonthOfYear();
		Map<String, MutableInt> tagCounts = counts.get(key);
		if (tagCounts == null)
		{	tagCounts = new HashMap<>(EXPECTED_HASH_TAG_COUNT);
			counts.put (key, tagCounts);
		}
		inc (tagCounts, hashTag.toLowerCase());
	}
	
	public static void main (String[] args) throws Exception
	{
		Path inputDir  = Paths.get("/Users/bryanfeeney/datasets/twitter-20130828");
		Path outputDir = Paths.get("/Users/bryanfeeney/Desktop/");
		
		new DateStats (inputDir, outputDir).call();
	}
}
