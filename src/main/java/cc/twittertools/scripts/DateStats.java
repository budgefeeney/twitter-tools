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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.twittertools.post.SavedTweetReader;
import cc.twittertools.post.Tweet;
import cc.twittertools.util.FilesInFoldersIterator;

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
	
	private static final int MAX_CORRUPTED_TWEETS_PER_FILE = 5;

	private final static Logger LOG = LoggerFactory.getLogger(DateStats.class);
	
	private final Int2IntArrayMap          postsSinceDay;
	private final Map<String,  DateTime>   firstPostByUser;
	private final Map<String,  Integer>    firstPostByUserAsDay;
	private final Int2IntArrayMap          interPostTimeMins;
	private final Map<String,  MutableInt> retweetsByUser;
	private final Map<String,  MutableInt> rtRetweetsByUser;
	
	private final Path datasetDirectory;
	private final Path postsSinceDayFile;
	private final Path userStatsFile;
	private final Path interPostTimeFile;
	
	
	
	public DateStats(Path datasetDirectory, Path postsSinceDayFile,
			Path userStatsFile, Path interPostTimeFile)
	{
		super();
		this.datasetDirectory  = datasetDirectory;
		this.postsSinceDayFile = postsSinceDayFile;
		this.userStatsFile     = userStatsFile;
		this.interPostTimeFile = interPostTimeFile;
		
		postsSinceDay        = new Int2IntArrayMap(DATASET_LENGTH_IN_DAYS);
		firstPostByUser      = new HashMap<>(NUM_USERS_IN_DATASET);
		firstPostByUserAsDay = new HashMap<>(NUM_USERS_IN_DATASET);
		interPostTimeMins    = new Int2IntArrayMap(MAX_INTER_TWEET_TIME_MINS);
		retweetsByUser       = new HashMap<>(NUM_USERS_IN_DATASET);
		rtRetweetsByUser     = new HashMap<>(NUM_USERS_IN_DATASET);
		
		postsSinceDay.defaultReturnValue(0);
		interPostTimeMins.defaultReturnValue(0);
	}
	
	public Integer call() throws Exception
	{	final DateTime firstDay = new DateTime (2000, 01, 01, 00, 00, 01);
		
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
				  		if (tweet.isRetweetFromId())
				  			inc (retweetsByUser, account);
				  		if (tweet.isRetweetFromMsg())
				  			inc (rtRetweetsByUser, account);
				  		
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
				  			firstPostByUserAsDay.put (account, dayOfTweet);
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
		
		writeStatistics();
		
		return tweetCount;
	}
	
	
	
	
	private void writeStatistics() throws IOException
	{	// User statistics
		try (BufferedWriter wtr = Files.newBufferedWriter(userStatsFile, Charsets.UTF_8))
		{
			List<String> users = new ArrayList<String>(firstPostByUser.keySet());
			Collections.sort(users);
			
			for (String user : users)
				wtr.write(
					user                           + '\t' +
					firstPostByUser.get(user)      + '\t' +
					firstPostByUserAsDay.get(user) + '\t' +
					retweetsByUser.get(user)       + '\t' +
					rtRetweetsByUser.get(user)     + '\n'
				);
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out user statistics to file " + ioe.getMessage(), ioe);
		}
		
		// Inter post time statistics
		try (BufferedWriter wtr = Files.newBufferedWriter(interPostTimeFile, Charsets.UTF_8))
		{	for (Int2IntMap.Entry entry : interPostTimeMins.int2IntEntrySet())
				wtr.write(Integer.toString (entry.getIntKey()) + '\t' + String.valueOf(entry.getIntValue()) + '\n');;
		}
		catch (IOException ioe)
		{	LOG.error("Error writing out inter-post time statistics to file " + ioe.getMessage(), ioe);
		}
		
		// Tweets since date counts
		try (BufferedWriter wtr = Files.newBufferedWriter(postsSinceDayFile, Charsets.UTF_8))
		{	IntSet set    = postsSinceDay.keySet();
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

	private static <K> void inc (Map<K, MutableInt> counts, K key)
	{
		MutableInt count = counts.get(key);
		if (count == null)
		{	count = new MutableInt(0);
			counts.put (key, count);
		}
		count.increment();
	}
	
	private static void inc (Int2IntArrayMap counts, int key)
	{	counts.put (key, counts.get(key) + 1);
	}
	
	public static void main (String[] args) throws Exception
	{
		Path inputDir          = Paths.get("/Users/bryanfeeney/datasets/twitter-20130816/spider");
		Path postsSinceFile    = Paths.get("/Users/bryanfeeney/Desktop/postsSinceDay.txt");
		Path userStatsFile     = Paths.get("/Users/bryanfeeney/Desktop/userStats.txt");
		Path interPostTimeFile = Paths.get("/Users/bryanfeeney/Desktop/interPostTime.txt");
		
		new DateStats (inputDir, postsSinceFile, userStatsFile, interPostTimeFile).call();
	}
}
