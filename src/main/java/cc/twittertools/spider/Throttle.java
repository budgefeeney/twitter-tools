package cc.twittertools.spider;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;

import com.j256.simplejmx.common.JmxAttributeMethod;
import com.j256.simplejmx.common.JmxResource;

/**
 * Configurable class that gets called between every
 * call to the HTTP client, in order to limit the total amount of
 * bandwidth used.
 */
@JmxResource(description = "Limits HTTP Requests per unit time", domainName = "cc.twittertools.spider", beanName = "Throttle")
public class Throttle
{
  private final AtomicLong eveningInterRequestWaitMs = new AtomicLong (TimeUnit.SECONDS.toMillis(3));
  
  private final AtomicLong dayTimeInterRequestWaitMs = new AtomicLong (TimeUnit.SECONDS.toMillis(6));

  
  /**
   * Pause for some time to stop saturating the network. The 
   * duration depends on whether we're currently active 
   * during working hours or not.
   * @throws InterruptedException
   */
  public void pause() throws InterruptedException
  { DateTime now = new DateTime();
    if (now.getDayOfWeek() < 6 && now.getHourOfDay() >= 8 && now.getHourOfDay() < 19)
      Thread.sleep(dayTimeInterRequestWaitMs.get());
    else
      Thread.sleep(eveningInterRequestWaitMs.get());
  }


  @JmxAttributeMethod(description = "Inter-request time in milliseconds outside of business hours")
  public long getEveningInterRequestWaitMs() {
    return eveningInterRequestWaitMs.get();
  }


  @JmxAttributeMethod(description = "Inter-request time in milliseconds outside of business hours")
  public void setEveningInterRequestWaitMs(long eveningInterRequestWaitMs) {
    this.eveningInterRequestWaitMs.set (eveningInterRequestWaitMs);
  }


  @JmxAttributeMethod(description = "Inter-request time in milliseconds outside of business hours")
  public long getDayTimeInterRequestWaitMs() {
    return dayTimeInterRequestWaitMs.get();
  }


  @JmxAttributeMethod(description = "Inter-request time in milliseconds outside of business hours")
  public void setDayTimeInterRequestWaitMs(long dayTimeInterRequestWaitMs) {
    this.dayTimeInterRequestWaitMs.set (dayTimeInterRequestWaitMs);
  }
  
  
}
