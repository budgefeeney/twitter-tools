package cc.twittertools.post;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cc.twittertools.scripts.Main;

public class TweetFeatureExtractorTest
{
	private static final int NUM_ADDRESSEES = 100000;
	private static final int NUM_URLS       = 100000;
	private static final int NUM_WORDS      = 50000;
	private static final int NUM_STOCKS     = 50000;
	private static final int NUM_EMOTICONS  = 500;
	private static final int NUM_HASHTAGS   = 50000;

	private final static String SAMPLE =
	   "AnaColinaF1\t351740397812334593\t351740397812334593\t2013-07-01T17:33:40+01:00\t2013-07-01T09:33:00Z\t-07:00\t@charlie_whiting He presses that button. Yes, that button. #silverstone #F1 pic.twitter.com/VK7vGlwy33\n"
	 + "AnaColinaF1\t351733844300410881\t351733844300410881\t2013-07-01T17:07:38+01:00\t2013-07-01T09:07:00Z\t-07:00\tWe're Charlie's Angels ;-) RT @nicholegalicia: @AnaColinaF1 @charlie_whiting Nothing compares to a good fake. I'm team fake @charlie_whiting!\n"
	 + "nicholegalicia\t351733415759978496\t351733415759978496\t2013-07-01T17:05:55+01:00\t2013-07-01T09:05:00Z\t-07:00\t@AnaColinaF1 @charlie_whiting Nothing compares to a good fake. I'm team fake @charlie_whiting!\n"
	 + "charlie_whiting\t351721656311484416\t351721656311484416\t2013-07-01T16:19:12+01:00\t2013-07-01T08:19:00Z\t-07:00\t@thejudge13 @SomersF1 Well, not to dampen your achievement with $AAPL:, but I've forgotten to update my picks for the past 3 races :-) #smug #lazy\n"
	 + "ianparkesf1\t351685131121078274\t351685131121078274\t2013-07-01T13:54:03+01:00\t2013-07-01T05:54:00Z\t-07:00\tHere's the longer version of the story as kindly used by the Daily Mail online - http://www.dailymail.co.uk/sport/formulaone/article-2352473/Pirelli-tyre-latest-Tyre-company-run-unrestricted-tests--says-Bernie-Ecclestone.html …\n"
	 + "charlie_whiting\t351466490257612802\t351466490257612802\t2013-06-30T23:25:15+01:00\t2013-06-30T15:25:00Z\t-07:00\t@LJ_Pritchard I hope there the fix is quick. If this risk exists at Spa and Monza then we're really in trouble :(\n"
	 + "charlie_whiting\t351464916168871937\t351464916168871937\t2013-06-30T23:19:00+01:00\t2013-06-30T15:19:00Z\t-07:00\t@LJ_Pritchard This \"blame Pirelli\" stuff is utter nonsense. They're doing the best they can in a horrid environment.\n"
	 + "charlie_whiting\t351463924866097153\t351463924866097153\t2013-06-30T23:15:04+01:00\t2013-06-30T15:15:00Z\t-07:00\tAnd around the planet. RT @AussieGrit: Really couldn't believe the reception I received on the podium today. #special #passionatefans\n"
	 + "charlie_whiting\t351463813096292352\t351463813096292352\t2013-06-30T23:14:37+01:00\t2013-06-30T15:14:00Z\t-07:00\t@LJ_Pritchard I think part of the problem is that the teams don't know what they're getting now either. Both sides need to test together.\n"
	 + "charlie_whiting\t351461280646828033\t351461280646828033\t2013-06-28T23:04:33+01:00\t2013-06-30T15:04:00Z\t-07:00\t@PaulF1B @formula1blog Thank God.";

	private final static String[] NAMES = new String[] {
		"AnaColinaF1",
		"AnaColinaF1",
		"nicholegalicia",
		"charlie_whiting",
		"ianparkesf1",
		"charlie_whiting",
		"charlie_whiting",
		"charlie_whiting",
		"charlie_whiting",
		"charlie_whiting"
	};
	
	private File tmpFile;
	private File tweetsFile;
	
	@Before
	public void setUp() throws Exception
	{	BasicConfigurator.configure();
		tmpFile = File.createTempFile("tweets-", ".txt");
		tmpFile.deleteOnExit();
		
		tweetsFile = new File (tmpFile.getParent() + File.separator + "charlie_whiting.3");
		if (tweetsFile.exists())
			tweetsFile.delete();
		FileUtils.write(tweetsFile, SAMPLE, Charsets.UTF_8);
		tweetsFile.deleteOnExit();
	}
	
	@After
	public void tearDown() throws Exception
	{	tmpFile.delete();
		tweetsFile.delete();
		BasicConfigurator.resetConfiguration();
	}
	
	@Test
	public void testAccount() throws Exception
	{	try (SavedTweetReader rdr = new SavedTweetReader(tweetsFile.toPath()); )
		{	int i = 0;
			while (rdr.hasNext())
			{	Tweet tweet = rdr.next();
				
				assertEquals ("charlie_whiting", tweet.getAccount());
				assertEquals (NAMES[i++], tweet.getAuthor());
			}
		}
	}
	
	@Test
	public void testFeatures() throws Exception
	{	// Startin configuring the feature extraction
		Main main = new Main();
		
		// Side information features
		main.setAuthorInFeatures(true);
		main.setDayOfYearInFeatures(true);
		main.setMonthOfYearInFeatures(true);
		
		// Word features
		main.setNumAddressees(NUM_ADDRESSEES);
		main.setNumEmoticons(NUM_EMOTICONS);
		main.setNumHashTags(NUM_HASHTAGS);
		main.setNumStocks(NUM_STOCKS);
		main.setNumUrls(NUM_URLS);
		main.setNumWords(NUM_WORDS);
		
		main.setStem(true);
		main.setElimStopWords(true);
		main.setMinWordLen(1);
		main.setMaxWordLen(80);
		main.setMinWordCount(1);
		main.setNumbersAllowed(true);
		
		// Input and output files.
		main.setInPath(tweetsFile.getAbsolutePath());
		main.setOutPath(tweetsFile.getParent());
		
		// Acceptable tweets
		// TODO Need to deal with partial versus full retweets, as illustrated above by tweet 351463924866097153
		main.setStripRetweets(true);
		main.setMinDateIncl("20130630");
		main.setMaxDateExcl("20131230");
		
		// Create the feature extractor and call it.
		TweetFeatureExtractor tfe = main.newTweetFeatExtractor();
		tfe.call();
		
//		Path filename = tfe.getOutputDir().resolve("dicts.py");
//		try (BufferedWriter writer = Files.newBufferedWriter(filename, Charsets.UTF_8))
//		{	writer.write("# -*- coding: utf-8 -*-\n\n");
//			tfe.getVectorizer().getDict().writeAsPythonDict (filename);
//		}
	}

}
