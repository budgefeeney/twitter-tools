package cc.twittertools.post;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Test;

public class TweetFeatureExtractorTest
{
	@Test
	public void testJodaTime() throws Exception
	{
		DateTime start = new DateTime(2013, 02, 13, 15, 37);
		DateTime end   = new DateTime(2013, 04, 27, 03, 04);
		
		Interval intr = new Interval (start, end);
		Period   per  = intr.toPeriod();
		
		System.out.println (per.getDays());
		System.out.println (per.getWeeks());
		System.out.println (per.getMonths());
	}
}
