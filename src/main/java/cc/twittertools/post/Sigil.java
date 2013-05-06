package cc.twittertools.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
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
    assert paramCount < 2    : "More than one parameter per sigil not yet supported";
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
   * Extracts all instance of a sigil from the given message, and returns the
   * list of sigil instances, and the message less those sigils
   * @param msg
   * @return
   */
  public Pair<String, List<String>> extractSigils (String msg)
  { int[] pos = extractSigilPositions(msg);
    int sigilLen = 0;
    for (int i = 0; i < pos.length; i += 2)
      sigilLen = pos[i + 1] - pos[i + 0];
    
    StringBuilder newMsg = new StringBuilder (msg.length() - sigilLen);
    List<String> sigils = new ArrayList<String>(pos.length / 2);
    
    int start = 0;
    for (int i = 0; i < pos.length; i += 2)
    { newMsg.append(msg.substring(start, pos[i + 0]));
      sigils.add(msg.substring(pos[i + 0] + 1, pos[i + 1]));
      start = pos[i + 1] + 1;
    }
    if (start < msg.length())
      newMsg.append (msg.substring (start, msg.length()));
    
    return Pair.of (newMsg.toString(), sigils);
  }
  
  /**
   * Identifies the positions of all instances of a given sigil]
   * in the message.
   * @return an array of sigil positions
   */
  private int[] extractSigilPositions (String msg)
  { int[] result = new int[12];
    int found = 0;
    int start = 0;
    
    for (String sigil : sigils)
    { int beginning = 0;
      while (start < msg.length() && beginning >= 0)
      { beginning = msg.indexOf(sigil, start);
        if (beginning < 0)
          continue;
        int end = beginning + sigil.length();
        
        if (paramCount > 0)
          while (Character.isJavaIdentifierPart(msg.charAt(end)))
            ++end;
        
        int len = end - beginning;
        start = end + 1;
        if ((paramCount == 0 && len == sigil.length()) || (paramCount == 1 && len > sigil.length()))
        { result[found * 2 + 0] = beginning;
          result[found * 2 + 1] = end;
          ++found;
          
          if (found * 2 > result.length)
            result = Arrays.copyOf(result, result.length * 2);
        }
      }
    }

    return ArrayUtils.subarray(result, 0, found * 2);
  }
}