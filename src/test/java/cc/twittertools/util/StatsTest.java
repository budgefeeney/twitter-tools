package cc.twittertools.util;

import java.util.Arrays;

import org.junit.Test;

import static java.lang.System.out;

public class StatsTest
{
	@Test
	public void singleSampleTest()
	{	out.println ("\n\nSingle Sample - 4");
		Stats stats = new Stats();
		stats.addSample(4);
		
		out.println(stats.mean());
		out.println(stats.stdev());
		out.println(stats.stderr());
		out.println(Arrays.toString (stats.bounds95()));
//		out.println(stats.skew());
//		out.println(stats.kurtosis());
	}
	

	@Test
	public void singleSampleZeroTest()
	{	out.println ("\n\nSingle Sample - 0");
		Stats stats = new Stats();
		stats.addSample(0);
		
		out.println(stats.mean());
		out.println(stats.stdev());
		out.println(stats.stderr());
		out.println(Arrays.toString (stats.bounds95()));
//		out.println(stats.skew());
//		out.println(stats.kurtosis());
	}
	
	@Test
	public void manySaampleTest()
	{	out.println ("\n\nMany samples");
		final double[] samples = new double[] { 0, -1, +4, 1.2, 0.00008 };
		
		Stats stats = new Stats();
		for (double sample : samples)
			stats.addSample(sample);
		
		out.println(stats.mean());
		out.println(stats.stdev());
		out.println(stats.stderr());
		out.println(Arrays.toString (stats.bounds95()));
//		out.println(stats.skew());
//		out.println(stats.kurtosis());
	}
}
