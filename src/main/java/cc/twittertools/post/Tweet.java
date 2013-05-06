package cc.twittertools.post;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

public class Tweet
{
  private final DateTime localTime;
  private final Set<String> hashTags;
  private final String user;
  private final String msg;
  private final Set<String> addressees;
  private final long id;
  private final long requestedId;
  private final boolean isRetweetFromId;
  private final boolean isRetweetFromMsg;
  
  
  public Tweet(Set<String> hashTags, String user, String msg, Set<String> addressees,
      long id, long requestedId, boolean isRetweetFromMsg, DateTime localTime) {
    super();
    assert hashTags != null            : "Hash tags set can be empty but not null";
    assert ! StringUtils.isBlank(user) : "Username can be neither blank nor null";
    assert msg != null                 : "Message cannot be null";
    assert addressees != null          : "Addressees cannot be null";
    assert id > 0                      : "ID must be strictly positive";
    assert requestedId > 0             : "Requested ID must be strictly positive";
    
    this.hashTags = hashTags;
    this.user = user;
    this.msg = msg;
    this.addressees = addressees;
    this.id = id;
    this.requestedId = requestedId;
    this.isRetweetFromId = id != requestedId;
    this.isRetweetFromMsg = isRetweetFromMsg;
    this.localTime = localTime;
  }
  

  public Set<String> getHashTags() {
    return hashTags;
  }

  public String getUser() {
    return user;
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
  
  public DateTime getTime() {
    return localTime;
  }
  
  @Override
  public String toString()
  { return localTime + " - @" + user + " : \t " + msg;
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
