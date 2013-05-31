package cc.twittertools.post;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Sets;

public class Tweet
{
  /**
   *  Sample is 3:37 PM - 24 Jan 11 
   *  
   *  <p>We use the UTC time zone to avoid impossible times (e.g. 00:30 the day
   *  the clocks move forward).
   */
  public final static DateTimeFormatter TWITTER_FMT =
      DateTimeFormat.forPattern("h:m a - d MMM yy").withZone(DateTimeZone.UTC);
  
  private final DateTime localTime;
  private final DateTime utcTime;
  private final Set<String> hashTags;
  private final String author;
  private final String msg;
  private final Set<String> addressees;
  private final long id;
  private final long requestedId;
  private final boolean isRetweetFromId;
  private final boolean isRetweetFromMsg;
  

  public Tweet (long id, long reqId, String date, String author, String msg) {
    this(id, reqId, null, TWITTER_FMT.parseDateTime(date), author, msg);
  }
  
  
  public Tweet (long id, long reqId, DateTime utcTime, DateTime localTime, String author, String msg) {
    this(
      /* hashTags = */     Sets.newHashSet(Sigil.HASH_TAG.extractSigils(msg).getRight()),
      /* author = */       author,
      /* msg = */          msg,
      /* addressees = */   Sets.newHashSet(Sigil.ADDRESSEE.extractSigils(msg).getRight()),
      /* id = */           id,
      /* requestedId = */  reqId,
      /* isRetweetFromMsg = */ ! Sigil.RETWEET.extractSigils(msg).getRight().isEmpty(),
      /* utcTime = */      utcTime,
      /* localTime = */    localTime
    );
  }
  
  public Tweet(Set<String> hashTags, String author, String msg, Set<String> addressees,
      long id, long requestedId, boolean isRetweetFromMsg, DateTime utcTime, DateTime localTime) {
    super();
    assert hashTags != null              : "Hash tags set can be empty but not null";
    assert ! StringUtils.isBlank(author) : "Username can be neither blank nor null";
    assert msg != null                   : "Message cannot be null";
    assert addressees != null            : "Addressees cannot be null";
    assert id > 0                        : "ID must be strictly positive";
    assert requestedId > 0               : "Requested ID must be strictly positive";
    
    this.hashTags = hashTags;
    this.author = author;
    this.msg = msg;
    this.addressees = addressees;
    this.id = id;
    this.requestedId = requestedId;
    this.isRetweetFromId = id != requestedId;
    this.isRetweetFromMsg = isRetweetFromMsg;
    this.utcTime   = utcTime;
    this.localTime = localTime;
  }
  

  public Set<String> getHashTags() {
    return hashTags;
  }

  public String getAuthor() {
    return author;
  }

  public String getMsg() {
    return msg;
  }

  public Set<String> getAddressees() {
    return addressees;
  }

  public long getId() {
    return id;
  }

  public long getRequestedId() {
    return requestedId;
  }

  public boolean isRetweetFromId() {
    return isRetweetFromId;
  }

  public boolean isRetweetFromMsg() {
    return isRetweetFromMsg;
  }
  
  public DateTime getLocalTime() {
    return localTime;
  }
  
  public DateTime getUtcTime() {
    return utcTime;
  }
  
  @Override
  public String toString()
  { return localTime + " - @" + author + " : \t " + msg;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Tweet other = (Tweet) obj;
    if (id != other.id)
      return false;
    return true;
  }


  public String getMsgLessSigils() {
    String msg = this.msg;
    for (String addressee : addressees)
      msg = Sigil.ADDRESSEE.stripFromMsg(msg, addressee);
    for (String hashTag : hashTags)
      msg = Sigil.HASH_TAG.stripFromMsg(msg, hashTag);
    if (isRetweetFromMsg)
      msg = Sigil.RETWEET.stripFromMsg(msg);
    
    return msg;
  }
  
  
}
