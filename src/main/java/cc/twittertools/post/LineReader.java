package cc.twittertools.post;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

/**
 * Given a directory of (optionally GZipped) JSON files, reads in lines
 * one by one (as they occur in the file) file by file (files sorted alphanumerically).
 * <p>
 * GZipping is detected simply by the presence of a ".gz" suffix.
 */
public class LineReader implements Iterator<String>
{
  private final Iterator<Path> paths;
  private       String         nextLine;
  private       Exception      nextError;
  private       BufferedReader rdr;
  
  /**
   * Creates a new JsonTweetReader
   * @param path a path to a particular file, or a path to a directory 
   * full of files containing the ".json" or the ".json.gz" prefix 
   * (not case-sensitive)
   * @throws IOException 
   */
  public LineReader (Path path) throws IOException
  { if (Files.isDirectory(path))
    { List<Path> pathsList = Lists.newLinkedList();
      DirectoryStream<Path> dirContents = Files.newDirectoryStream(path);
      
      for (Path dirPath : dirContents)
      { if (isJsonFile (dirPath))
          pathsList.add (dirPath);
      }
      Collections.sort(pathsList);
      paths = pathsList.iterator();
    }
    else
    { paths = Collections.singleton(path).iterator();
    }
  }
  
  /**
   * Is a JSON file, compressed or not.
   * Basically does the file name end with ".json" or ".json.gz"
   * regardless of case
   */
  private final static boolean isJsonFile (Path path)
  { String name = path.getFileName().toString().toUpperCase();
    return name.endsWith (".JSON") || name.endsWith(".JSON.GZ");
  }
  
  /**
   * Creates a new JsonTweetReader
   * @param path a path to a particular file, or a path to a directory 
   * full of files containing the ".json" or the ".json.gz" prefix 
   * (not case-sensitive)
   * @throws IOException 
   */
  public LineReader (String path) throws IOException
  { this (Paths.get(path));
  }

  @Override
  public boolean hasNext()
  { try
    {
      while (nextLine == null)
      { 
        if (rdr == null)
        { 
          if (! paths.hasNext())
            return false;
          rdr = Files.newBufferedReader (paths.next(), Charsets.UTF_8);
        }
        
        nextLine = rdr.readLine();
        if (nextLine == null)
        { rdr = closeAndNull (rdr);
        }
      }
      return true;
    }
    catch (Exception e)
    { nextError = e;
      IOUtils.closeStream(rdr); // TODO Import Apache Commons IO
      return true;
    }
  }
  
  public static <C extends Closeable> C closeAndNull (C stream) throws IOException
  { stream.close();
    return stream;
  }

  @Override
  public String next()
  { if (nextError != null)
    { Exception errorResult = nextError;
      nextError = null;
      throw new RuntimeException (errorResult.getMessage(), errorResult);
    }
    
    return nextLine;
  }

  /**
   * Unsupported
   * @throws UnsupportedOperationException on every call, as it's not supported
   */
  @Override
  public void remove()
  { throw new UnsupportedOperationException();
  }


}
