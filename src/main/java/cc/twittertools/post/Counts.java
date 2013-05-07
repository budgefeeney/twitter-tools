package cc.twittertools.post;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang.mutable.MutableInt;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

/**
 * Keeps a count of the instances of some key. Writes the counts
 * out to a tab-delimited file.
 * @param <T>
 */
public class Counts<T>
{
  private static final char ROW_DELIMITER = '\n';
  private static final char COL_DELIMITER = '\t';
  
  private final Map<T, MutableInt> counts = Maps.newHashMap();
  
  public void inc (T key)
  { MutableInt value = counts.get(key);
    if (value == null)
    { value = new MutableInt(0);
      counts.put (key, value);
    }
    value.increment();
  }
  
  public int get(T key)
  { MutableInt value = counts.get(key);
    return value == null ? 0 : value.intValue();
  }
  
  public void write (Path path) throws IOException
  {
    try (
        BufferedWriter writer = Files.newBufferedWriter(path, Charsets.UTF_8);
    )
    { for (Map.Entry<T, MutableInt> entry : counts.entrySet())
      { writer.write(entry.getKey().toString());
        writer.write(COL_DELIMITER);
        writer.write(entry.getValue().toString());
        writer.write(ROW_DELIMITER);
      }
      
    }
  }
}
