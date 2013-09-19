package cc.twittertools.util;

/**
 * Running estimate of the mean and variance. For more see
 * http://www.johndcook.com/skewness_kurtosis.html
 * 
 * This uses the Welfod's algorithm for online variance estimation,
 * which is more numerically stable than the more popular method
 * of keep tally of a sum of samples and a sum of squarded samples.
 * @author bryanfeeney
 *
 */
public final class Stats
{ // sample-size
	private long   n = 0L;
	
	// The first four moments: E[X], E[X^2], E[X^3], E[X^4]
	private double M1         = 0.0;
	private double M2         = 0.0; 
	private double M3         = 0.0;
	private double M4         = 0.0; 
	
	public Stats()
	{	
	}
	
	public final void addSample(double sample)
	{	final long n1 = n;
		++n;
	
		final double 
			diff    = sample - M1,
			diff_n  = diff / n,
			diff_n2 = diff_n * diff_n,
			term1   = diff * diff_n * n1;
		
		M1 += diff_n;
		M4 += term1 * diff_n2 * (n*n - 3*n + 3) + 6 * diff_n2 * M2 - 4 * diff_n * M3;
		M3 += term1 * diff_n * (n - 2) - 3 * diff_n * M2;
		M2 += term1;
	}
	
	public final double mean()
	{	return M1;
	}
	
	public final double var()
	{	return n == 1 ? 0 : M2 / (n - 1);
	}
	
	public final double stdev()
	{	return Math.sqrt(var());
	}
	
	public final double stderr()
	{	return stdev() / Math.sqrt(n);
	}
	
	/** returns the lower confidence interval, the mean, and the upper confidence
	 * interval, at 95% confidence.
	 */
	public final double[] bounds95()
	{	final double interval = stderr() * 1.96;
		final double mean     = mean();
		return new double[] { 
			mean - interval,
			mean,
			mean + interval
		};
	}
	
	// These fail to return the same values as given by R
//	public final double skew()
//	{	return Math.sqrt (n) * M3 / Math.pow (M2, 1.5);
//	}
//	
//	public final double kurtosis()
//	{	return n * M4 / (M2 * M2) - 3.0;
//	}
	
	public long sampleSize()
	{	return n;
	}
}