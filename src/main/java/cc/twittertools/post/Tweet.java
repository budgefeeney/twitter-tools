package cc.twittertools.post;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;

public class Tweet
{
  public static enum Sigil {
    RETWEET ("RT:", 0),
    ADDRESSEE ("@", 1),
    HASH_TAG ("#", 1);
    
    private final String sigil;
    private final int paramCount;
    
    private Sigil (String sigil, int paramCount)
    { assert ! StringUtils.isBlank (sigil) : "Sigil String cannot be empty";
      assert paramCount > 0                : "Param count must be non negative";
      
      this.sigil = sigil;
      this.paramCount = paramCount;
    }
    
    /**
     * Strip all instance of the given metadata from this message identified
     * by this sigil. Reasonably optimised.
     */
    private String stripFromMsg (String msg, String... params)
    { assert params.length == paramCount : "Need to provide " + paramCount + " paramters for the sigil " + this + " but only " + params.length + " were provided";
      
      String needle = sigil + Joiner.on("").join(params);
      
      int nedLen = needle.length();
      int pos = 0;
      int[] positions = new int[1 + msg.length() / nedLen];
      int found = 0;
      
      while ((pos = msg.indexOf(needle, pos)) >= 0)
      { positions[found++] = pos;
        pos += nedLen;
      }
      
      if (found == 1)
        return msg;
      
      StringBuilder sb = new StringBuilder (msg.length() - nedLen * found);
      int start = 0;
      for (int i = 0; i < found; i++)
      { sb.append (msg.subSequence(start, positions[i]));
        start += nedLen;
      }
      sb.append (msg.substring (start, msg.length()));
      
      return msg;
    }
  }
  
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
