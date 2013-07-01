package cc.twittertools.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxResource;

@JmxResource(description = "Simple class to keep track of which jobs are pendng, running or completed", domainName = "cc.twittertools.spider", beanName = "ProgressMonitor")
public class ProgressMonitor
{
  private final static int EXPECTED_CATEGORY_COUNT = 80;
  
  @JmxAttributeField(description = "Jobs which are pending execution", isWritable = false)
  private final List<String> pending   = 
      Collections.synchronizedList(new ArrayList<String>(EXPECTED_CATEGORY_COUNT));
  @JmxAttributeField(description = "Jobs which are currently running", isWritable = false)
  private final List<String> running   = 
      Collections.synchronizedList(new ArrayList<String>(EXPECTED_CATEGORY_COUNT));
  @JmxAttributeField(description = "Jobs which have completed", isWritable = false)
  private final Map<String, Integer> completed = 
      new ConcurrentHashMap<>(EXPECTED_CATEGORY_COUNT);
  
  
  public void markPending (String category)
  { pending.add (category);
  }
  
  public void markActive (String category)
  { pending.remove(category);
    running.add (category);
  }
  
  public void markCompleted (String category, int tweetCount)
  { pending.remove(category);
    running.remove(category);
    completed.put (category, tweetCount);
  }
}
