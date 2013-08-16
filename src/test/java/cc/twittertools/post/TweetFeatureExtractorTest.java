package cc.twittertools.post;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TweetFeatureExtractorTest
{
	private final static String SAMPLE =
			   "AnaColinaF1	351740397812334593	351740397812334593	2013-07-01T17:33:40+01:00	2013-07-01T09:33:00Z	-07:00	@charlie_whiting He presses that button. Yes, that button. #silverstone #F1 pic.twitter.com/VK7vGlwy33"
			 + "AnaColinaF1	351733844300410881	351733844300410881	2013-07-01T17:07:38+01:00	2013-07-01T09:07:00Z	-07:00	We're Charlie's Angels. RT @nicholegalicia: @AnaColinaF1 @charlie_whiting Nothing compares to a good fake. I'm team fake @charlie_whiting!"
			 + "nicholegalicia	351733415759978496	351733415759978496	2013-07-01T17:05:55+01:00	2013-07-01T09:05:00Z	-07:00	@AnaColinaF1 @charlie_whiting Nothing compares to a good fake. I'm team fake @charlie_whiting!"
			 + "charlie_whiting	351721656311484416	351721656311484416	2013-07-01T16:19:12+01:00	2013-07-01T08:19:00Z	-07:00	@thejudge13 @SomersF1 Well, not to dampen your achievement, but I've forgotten to update my picks for the past 3 races."
			 + "ianparkesf1	351685131121078274	351685131121078274	2013-07-01T13:54:03+01:00	2013-07-01T05:54:00Z	-07:00	Here's the longer version of the story as kindly used by the Daily Mail online - http://www.dailymail.co.uk/sport/formulaone/article-2352473/Pirelli-tyre-latest-Tyre-company-run-unrestricted-tests--says-Bernie-Ecclestone.html …"
			 + "charlie_whiting	351466490257612802	351466490257612802	2013-06-30T23:25:15+01:00	2013-06-30T15:25:00Z	-07:00	@LJ_Pritchard I hope there the fix is quick. If this risk exists at Spa and Monza then we're really in trouble."
			 + "charlie_whiting	351464916168871937	351464916168871937	2013-06-30T23:19:00+01:00	2013-06-30T15:19:00Z	-07:00	@LJ_Pritchard This \"blame Pirelli\" stuff is utter nonsense. They're doing the best they can in a horrid environment."
			 + "charlie_whiting	351463924866097153	351463924866097153	2013-06-30T23:15:04+01:00	2013-06-30T15:15:00Z	-07:00	And around the planet. RT @AussieGrit: Really couldn't believe the reception I received on the podium today. #special #passionatefans"
			 + "charlie_whiting	351463813096292352	351463813096292352	2013-06-30T23:14:37+01:00	2013-06-30T15:14:00Z	-07:00	@LJ_Pritchard I think part of the problem is that the teams don't know what they're getting now either. Both sides need to test together."
			 + "charlie_whiting	351461280646828033	351461280646828033	2013-06-30T23:04:33+01:00	2013-06-30T15:04:00Z	-07:00	@PaulF1B @formula1blog Thank God.";

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
	{	tmpFile = File.createTempFile("tweets-", ".txt");
		tmpFile.deleteOnExit();
		
		tweetsFile = new File (tmpFile.getParent() + File.separator + "charlie_whiting.3");
		FileUtils.write(tweetsFile, SAMPLE, Charsets.UTF_8);
		tweetsFile.deleteOnExit();
	}
	
	@After
	public void tearDown() throws Exception
	{	tmpFile.delete();
		tweetsFile.delete();
	}
	
	@Test
	public void testAccount() throws Exception
	{	SavedTweetReader rdr = new SavedTweetReader(tweetsFile.toPath());
		int i = 0;
		while (rdr.hasNext())
		{	Tweet tweet = rdr.next();
			
			assertEquals ("charlie_whiting", tweet.getAccount());
			assertEquals (NAMES[i], tweet.getAuthor());
		}
	}
}
