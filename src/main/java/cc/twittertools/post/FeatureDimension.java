package cc.twittertools.post;

import java.util.concurrent.TimeUnit;

import org.joda.time.Interval;
import org.joda.time.Period;

import cc.twittertools.words.dict.Dictionary;

/**
 * For each of the given features, gives the total possible number of values
 * it may take, given a specific dictionary of users and a time-period.
 * @author bryanfeeney
 *
 */
public class FeatureDimension
{
  private final int authorDim;
  private final int dayOfWeekDim;
  private final int hourOfDayDim;
  private final int dayHourOfWeekDim;
  private final int dayOfYearDim;
  private final int weekOfYearDim;
  private final int monthOfYearDim;
  private final int addresseeDim;
  private final int rtDim;
  private final int interceptDim;
  
  private final int total;
  
  public FeatureDimension (FeatureSpecification featSpec, Dictionary userDict, Interval interval)
  {	Period p = interval.toPeriod();
  	int days   = (int) TimeUnit.MILLISECONDS.toDays(interval.toDurationMillis()) + 1;
  	int weeks  = days / 7 + 1;
  	int months = p.getMonths() + 1;
  	
  	authorDim        = featSpec.isAuthorInFeatures()        ? userDict.capacity() : 0;
  	dayOfWeekDim     = featSpec.isDayOfWeekInFeatures()     ? 7      : 0;
  	hourOfDayDim     = featSpec.isHourOfDayInFeatures()     ? 24     : 0; // of course one day a year there are 25hours in a day...
  	dayHourOfWeekDim = featSpec.isDayHourOfWeekInFeatures() ? 7 * 24 : 0;
  	dayOfYearDim     = featSpec.isDayOfYearInFeatures()     ? days   : 0;
  	weekOfYearDim    = featSpec.isWeekOfYearInFeatures()    ? weeks  : 0;
  	monthOfYearDim   = featSpec.isMonthOfYearInFeatures()   ? months : 0;
  	addresseeDim     = featSpec.isAddresseeInFeatures()     ? userDict.capacity() : 0;
  	rtDim            = featSpec.isRtInFeatures()            ? 1      : 0;
  	interceptDim     = featSpec.isInterceptInFeatures()     ? 1      : 0;
  		
  	
  	total =
     + authorDim
     + dayOfWeekDim
     + hourOfDayDim
     + dayHourOfWeekDim
     + dayOfYearDim
     + weekOfYearDim
     + monthOfYearDim
     + addresseeDim
     + rtDim
     + interceptDim;
  }

	public int getAuthorDim()
	{ return authorDim;
	}

	public int getDayOfWeekDim()
	{ return dayOfWeekDim;
	}

	public int getHourOfDayDim()
	{ return hourOfDayDim;
	}

	public int getDayHourOfWeekDim()
	{ return dayHourOfWeekDim;
	}

	public int getDayOfYearDim()
	{ return dayOfYearDim;
	}

	public int getWeekOfYearDim()
	{ return weekOfYearDim;
	}

	public int getMonthOfYearDim()
	{ return monthOfYearDim;
	}

	public int getAddresseeDim()
	{ return addresseeDim;
	}

	public int getRtDim()
	{ return rtDim;
	}

	public int getInterceptDim()
	{ return interceptDim;
	}

	public int getTotal()
	{ return total;
	}
  
  
}
