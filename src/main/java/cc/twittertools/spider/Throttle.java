package cc.twittertools.spider;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxResource;

/**
 * Configurable class that gets called between every
 * call to the HTTP client, in order to limit the total amount of
 * bandwidth used.
 */
@JmxResource(description = "Limits HTTP Requests per unit time", domainName = "cc.twittertools.spider", beanName = "Throttle")
public class Throttle
{
  @JmxAttributeField(description = "Inter-request time in milliseconds outside of business hours", isWritable = true)
  private       long eveningInterRequestWaitMs = TimeUnit.SECONDS.toMillis(5);
  
  @JmxAttributeField(description = "Inter-request time in milliseconds outside of business hours", isWritable = true)
  private       long dayTimeInterRequestWaitMs = TimeUnit.SECONDS.toMillis(10);

  
  /**
   * Pause for some time to stop saturating the network. The 
   * duration depends on whether we're currently active 
   * during working hours or not.
   * @throws InterruptedException
   */
  public void pause() throws InterruptedException
  { DateTime now = new DateTime();
    if (now.getDayOfWeek() < 6 && now.getHourOfDay() >= 8 && now.getHourOfDay() < 19)
      Thread.sleep(dayTimeInterRequestWaitMs);
    else
      Thread.sleep(eveningInterRequestWaitMs);
  }


  public long getEveningInterRequestWaitMs() {
    return eveningInterRequestWaitMs;
  }


  public void setEveningInterRequestWaitMs(long eveningInterRequestWaitMs) {
    this.eveningInterRequestWaitMs = eveningInterRequestWaitMs;
  }


  public long getDayTimeInterRequestWaitMs() {
    return dayTimeInterRequestWaitMs;
  }


  public void setDayTimeInterRequestWaitMs(long dayTimeInterRequestWaitMs) {
    this.dayTimeInterRequestWaitMs = dayTimeInterRequestWaitMs;
  }
  
  
}
