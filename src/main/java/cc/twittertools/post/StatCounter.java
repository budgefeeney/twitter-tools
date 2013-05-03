package cc.twittertools.post;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.mutable.MutableInt;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Counts the instances of a key, and then writes it to a file.
 */
public class StatCounter
{
  private final Map<String, MutableInt> counts = Maps.newHashMap();
  
  public void inc (String key)
  { assert (key != null) : "Keys can be empty but not null";
    
    MutableInt value = counts.get(key);
    if (value == null)
    { value = new MutableInt(0);
      counts.put (key, value);
    }
    
    value.increment();
  }
  
  public void writeToFile (String path) throws IOException
  { assert (path != null) : "Given path cannot be null";
    
    try (BufferedWriter out = Files.newWriter(new File(path), Charsets.UTF_8))
    { out.write("key\tcount\n");
      for (Map.Entry<String, MutableInt> entry : counts.entrySet())
      { out.write (entry.getKey() + '\t' + entry.getValue() + '\n');
      }
    }
  }
}
