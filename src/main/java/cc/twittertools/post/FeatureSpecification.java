package cc.twittertools.post;

import org.joda.time.Interval;

import cc.twittertools.words.Dictionary;

/**
 * Which features (relating to all aspects of tweets other than the bodies of
 * the tweets themselves) should be included when we're processing tweets by
 * {@link TweetFeatureExtractor}
 * @author bryanfeeney
 *
 */
public class FeatureSpecification
{
  private boolean authorInFeatures        = false;
  private boolean dayOfWeekInFeatures     = false;
  private boolean hourOfDayInFeatures     = false;
  private boolean dayHourOfWeekInFeatures = false;
  private boolean dayOfYearInFeatures     = false;
  private boolean weekOfYearInFeatures    = false;
  private boolean monthOfYearInFeatures   = false;
  private boolean addresseeInFeatures     = false;
  private boolean rtInFeatures            = false;
  private boolean interceptInFeatures     = false;
  
  
  /**
   * The total number of non-zero entries we expect to see in a vector
   * encoding of these features
   */
  public int maxNonZeroFeatures()
  {	int featCount = 0;
  
	  featCount += authorInFeatures        ? 1 : 0;
	  featCount += dayOfWeekInFeatures     ? 1 : 0;
	  featCount += hourOfDayInFeatures     ? 1 : 0;
	  featCount += dayHourOfWeekInFeatures ? 1 : 0;
	  featCount += dayOfYearInFeatures     ? 1 : 0;
	  featCount += weekOfYearInFeatures    ? 1 : 0;
	  featCount += monthOfYearInFeatures   ? 1 : 0;
	  featCount += addresseeInFeatures     ? 1 : 0;
	  featCount += rtInFeatures            ? 1 : 0;
	  featCount += interceptInFeatures     ? 1 : 0;
  	
  	return featCount;
  }
  
	public boolean isAuthorInFeatures()
	{ return authorInFeatures;
	}
	
	public FeatureSpecification setAuthorInFeatures(boolean authorInFeatures)
	{ this.authorInFeatures = authorInFeatures;
		return this;
	}
	
	public boolean isDayOfWeekInFeatures()
	{ return dayOfWeekInFeatures;
	}
	
	public FeatureSpecification setDayOfWeekInFeatures(boolean dayOfWeekInFeatures)
	{ this.dayOfWeekInFeatures = dayOfWeekInFeatures;
		return this;
	}
	
	public boolean isHourOfDayInFeatures()
	{ return hourOfDayInFeatures;
	}
	
	public FeatureSpecification setHourOfDayInFeatures(boolean hourOfDayInFeatures)
	{ this.hourOfDayInFeatures = hourOfDayInFeatures;
		return this;
	}
	
	public boolean isDayHourOfWeekInFeatures()
	{ return dayHourOfWeekInFeatures;
	}
	
	public FeatureSpecification setDayHourOfWeekInFeatures(boolean dayHourOfWeekInFeatures)
	{ this.dayHourOfWeekInFeatures = dayHourOfWeekInFeatures;
		return this;
	}
	
	public boolean isDayOfYearInFeatures()
	{ return dayOfYearInFeatures;
	}
	
	public FeatureSpecification setDayOfYearInFeatures(boolean dayOfYearInFeatures)
	{ this.dayOfYearInFeatures = dayOfYearInFeatures;
		return this;
	}
	
	public boolean isWeekOfYearInFeatures()
	{ return weekOfYearInFeatures;
	}
	
	public FeatureSpecification setWeekOfYearInFeatures(boolean weekOfYearInFeatures)
	{ this.weekOfYearInFeatures = weekOfYearInFeatures;
	return this;
	}
	
	public boolean isMonthOfYearInFeatures()
	{ return monthOfYearInFeatures;
	}
	
	public FeatureSpecification setMonthOfYearInFeatures(boolean monthOfYearInFeatures)
	{ this.monthOfYearInFeatures = monthOfYearInFeatures;
		return this;	
	}
	
	public boolean isAddresseeInFeatures()
	{ return addresseeInFeatures;
	}
	
	public FeatureSpecification setAddresseeInFeatures(boolean addresseeInFeatures)
	{ this.addresseeInFeatures = addresseeInFeatures;
		return this;
	}
	
	public boolean isRtInFeatures()
	{ return rtInFeatures;
	}
	
	public FeatureSpecification setRtInFeatures(boolean rtInFeatures)
	{ this.rtInFeatures = rtInFeatures;
		return this;
	}
	
	public boolean isInterceptInFeatures()
	{ return interceptInFeatures;
	}
	
	public FeatureSpecification setInterceptInFeatures(boolean intercepInFeatures)
	{ this.interceptInFeatures = intercepInFeatures;
		return this;
	}

	/**
	 * Determines the dimensionality of the vector created by this feature
	 * specification
	 * @param userDict the dictionary of users, used to determine the total number of
	 * slots for an author feature (i.e. the capacity of the dictionary)
	 * @param addresseeDict the dictionary of addressees, used to determine the 
	 * total number of of slots for an addressee features (i.e. the capacity of the
	 * dictionary)
	 * @param period
	 * @return
	 */
	public FeatureDimension dimensionality(Dictionary userDict, Interval interval)
	{	return new FeatureDimension(this, userDict, interval);
	}
}
