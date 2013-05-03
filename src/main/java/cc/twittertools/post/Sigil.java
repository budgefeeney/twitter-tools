package cc.twittertools.post;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;

/**
 * A Twitter sigil, which appears in a message, and denotes an element
 * of metadata. Contains methods to extract such sigils.
 * @author bfeeney
 *
 */
public enum Sigil {
  RETWEET (0, "RT:", "RT"),
  ADDRESSEE (1, "@"),
  HASH_TAG (1, "#");
  
  private final String[] sigils;
  private final int paramCount;
  
  private Sigil (int paramCount, String... sigils)
  { assert paramCount > 0    : "Param count must be non negative";
    assert sigils.length > 0 : "Need to supply at least one sigil";
    for (String sigil : sigils)
      assert ! StringUtils.isBlank(sigil) : "Sigil cannot be null, empty or simply whitespace";
  
    this.sigils = sigils;
    this.paramCount = paramCount;
  }
  
  /**
   * Strip all instance of the given metadata from this message identified
   * by this sigil. Reasonably optimised.
   */
  String stripFromMsg (String msg, String... params)
  { assert params.length == paramCount : "Need to provide " + paramCount + " paramters for the sigil " + this + " but only " + params.length + " were provided";
    
    for (String sigil : sigils)
    { String needle = sigil + Joiner.on("").join(params);
      
      int nedLen = needle.length();
      int pos = 0;
      int[] positions = new int[1 + msg.length() / nedLen];
      int found = 0;
      
      while ((pos = msg.indexOf(needle, pos)) >= 0)
      { positions[found++] = pos;
        pos += nedLen;
      }
      
      if (found == 0)
        continue;
      
      StringBuilder sb = new StringBuilder (msg.length() - nedLen * found);
      int start = 0;
      for (int i = 0; i < found; i++)
      { sb.append (msg.substring(start, positions[i]));
        start = positions[i] + nedLen;
      }
      sb.append (msg.substring (start, msg.length()));
      
      msg = sb.toString();
    }
    
    return msg;
  }
  
  /**
   * Identifies the positions of all instances of a given sigil]
   * in the message.
   * @return an array of sigil positions
   */
  private extractSigilPositions (String msg, String... params)
  { assert params.length == paramCount : "Need to provide " + paramCount + " paramters for the sigil " + this + " but only " + params.length + " were provided";
    
    for (String sigil : sigils)
    { String needle = sigil + Joiner.on("").join(params);
      
      int nedLen = needle.length();
      int pos = 0;
      int[] positions = new int[1 + msg.length() / nedLen];
      int found = 0;
      
      while ((pos = msg.indexOf(needle, pos)) >= 0)
      { positions[found++] = pos;
        pos += nedLen;
      }
      
      if (found == 0)
        continue;
      
      StringBuilder sb = new StringBuilder (msg.length() - nedLen * found);
      int start = 0;
      for (int i = 0; i < found; i++)
      { sb.append (msg.substring(start, positions[i]));
        start = positions[i] + nedLen;
      }
      sb.append (msg.substring (start, msg.length()));
      
      msg = sb.toString();
    }
    
    return msg;
  }
}