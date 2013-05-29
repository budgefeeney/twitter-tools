package cc.twittertools.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.collect.ComparisonChain;

/**
 * All the information we need about a Twitter user, with a
 * constructor to read in the data written out by {@link UserSpider}.
 * <p>
 * Comparable implementation is first by category, then by creationMonthYear
 * and finally by the recent20TweetInterval
 */
final class TwitterUser implements Comparable<TwitterUser>
{ private final static int MONTHS_PER_YEAR   = 12;
  
  private final static int TWITTER_LAUNCH_YEAR = 2006;
  private final static int TWITTER_AGE_YEARS = new DateTime().getYear() - TWITTER_LAUNCH_YEAR + 1;
  private final static int MAX_AGE_IN_MONTHS = TWITTER_AGE_YEARS * MONTHS_PER_YEAR;
  
  private final String category;
  private final String name;
  private final List<String> ancestry;
  private final DateTime creationDate;
  private final int ageInMonths;
  private Duration recent20TweetInterval = new Duration(Long.MAX_VALUE);
  
  public TwitterUser (String line)
  { String[] fields = StringUtils.split(line, '\t');
    
    category     = fields[0];      
    name         = fields[1];
    creationDate = ISODateTimeFormat.basicDateTime().parseDateTime(fields[2]);
    long cursor  = Long.parseLong(fields[3]);
    
    // Here we detect the format: is this a file created by UserSpider,
    // or a file created by toTabDelimLine(). The former will have a cursor
    // identifier, the latter will have a user's age in months.
    final int ancestryColumn;
    if (cursor < 0 || cursor > MAX_AGE_IN_MONTHS) // UserSpider file
    { ancestryColumn = 4; 
      
    }
    else // toTabDelimLine() file
    { ancestryColumn = 5;
      cursor = -1;
      recent20TweetInterval = new Duration(Long.parseLong(fields[4]));
    }
    
    // Read in the ancestry
    List<String> anc = new ArrayList<String>(Math.min(1, ancestryColumn - fields.length));
    for (int i = ancestryColumn; i < fields.length; i++)
      if (! StringUtils.isBlank(fields[i]))
        anc.add(fields[i]);
    if (anc.isEmpty())
      anc.add(name);
    
    ancestry = Collections.unmodifiableList(anc);
    
    Interval age = new Interval(creationDate, new DateTime());
    ageInMonths  = age.toPeriod().getMonths();
  }
  
  /**
   * Return the twitter user as a tab delimited line terminated
   * by a newline.
   */
  public String toTabDelimLine()
  { 
    StringBuilder sb = new StringBuilder (150)
      .append (category)
      .append ('\t')
      .append (name)
      .append ('\t')
      .append (ISODateTimeFormat.basicDateTime().print(creationDate))
      .append ('\t')
      .append (String.valueOf (ageInMonths))
      .append ('\t')
      .append (recent20TweetInterval.getMillis());
    
    for (String ancestor : ancestry)
      sb.append ('\t').append (ancestor);
    sb.append ('\n');
    
    return sb.toString();
  }

  @Override
  public int compareTo(TwitterUser that)
  { int thisIsOlderThanSixMonths = this.ageInMonths >= 6 ? 0 : 1;
    int thatIsOlderThanSixMonths = that.ageInMonths >= 6 ? 0 : 1;
    
    return ComparisonChain.start()
      .compare(this.category, that.category)
      .compare(thisIsOlderThanSixMonths, thatIsOlderThanSixMonths)
      .compare(this.recent20TweetInterval, that.recent20TweetInterval)
      .result();
  }

  public Duration getRecent20TweetInterval() {
    return recent20TweetInterval;
  }

  public void setRecent20TweetInterval(Duration recent20TweetInterval) {
    this.recent20TweetInterval = recent20TweetInterval;
  }

  public String getCategory() {
    return category;
  }

  public String getName() {
    return name;
  }

  public List<String> getAncestry() {
    return ancestry;
  }

  public DateTime getCreationDate() {
    return creationDate;
  }

  public int getAgeInMonths() {
    return ageInMonths;
  }
}