package cc.twittertools.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
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
  
  
  
  public TwitterUser(String name, String category) {
    super();
    this.category     = category;
    this.name         = name;
    this.ancestry     = Collections.singletonList(name);
    this.creationDate = null;
    this.ageInMonths  = -1;
  }

  public TwitterUser (final String line)
  { 
    try
    {
      String[] fields = StringUtils.split(line, '\t');
      
      category     = fields[0].intern();      
      name         = fields[1];
      creationDate = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(fields[2]);
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
      List<String> anc = new ArrayList<String>(Math.max(1, fields.length - ancestryColumn));
      for (int i = ancestryColumn; i < fields.length; i++)
        if (! StringUtils.isBlank(fields[i]))
          anc.add(fields[i]);
      if (anc.isEmpty())
        anc.add(name);
      
      ancestry = Collections.unmodifiableList(anc);
      
      Period age = new Interval(creationDate, new DateTime()).toPeriod();
      ageInMonths  = age.getYears() * MONTHS_PER_YEAR + age.getMonths();
    }
    catch (Exception e)
    { throw new IllegalArgumentException ("Cannot parse line \"" + line + "\". Error was " + e.getMessage(), e);
    }
  }
  
  
  /**
   * {@inheritDoc}
   * <p>
   * Equality is defined in terms of category and userName only.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Equality is defined in terms of category and userName only.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TwitterUser other = (TwitterUser) obj;
    if (category == null) {
      if (other.category != null)
        return false;
    } else if (!category.equals(other.category))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
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
      .append (ISODateTimeFormat.dateTimeNoMillis().print(creationDate))
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
  
  @Override
  public String toString()
  { return name + " (" + ageInMonths + "mths" + ")";
  }
}