package cc.twittertools.post;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

public class Tweet
{
  private final Set<String> hashTags;
  private final String user;
  private final String msg;
  private final Set<String> addressees;
  private final long id;
  private final long requestedId;
  private final boolean isRetweetFromId;
  private final boolean isRetweetFromMsg;
  
  
  public Tweet(Set<String> hashTags, String user, String msg, Set<String> addressees,
      long id, long requestedId, boolean isRetweetFromMsg) {
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
  }
  
  
  public Tweet(String user, String msg, long id, long requestedId) {
    super();
    assert ! StringUtils.isBlank(user) : "Username can be neither blank nor null";
    assert ! StringUtils.isBlank(user) : "Message cannot be null or blank";
    assert id > 0                      : "ID must be strictly positive";
    assert requestedId > 0             : "Requested ID must be strictly positive";
    
    this.hashTags   = Sigil.HASH_TAG.extract (msg);
    this.addressees = Sigil.ADDRESSEE.extract (msg);
    this.user = user;
    this.msg  = msg;
    this.id   = id;
    this.requestedId      = requestedId;
    this.isRetweetFromId  = id != requestedId;
    this.isRetweetFromMsg = Sigil.RETWEET.containsSigil();
    
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
