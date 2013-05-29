package cc.twittertools.spider;

import java.util.concurrent.TimeUnit;

/**
 * Keeps track of the number of requests, ensuring that we don't
 * go over our limit, which by default (and according to the Twitter
 * API) is 15 requests per 15mins
 */
final class BlockingRequestCounter
{ private final static int BASIC_REQUESTS_PER_QTR_HR = 15;
  private final static long WINDOW_MS = TimeUnit.MINUTES.toMillis(15);

  private final int  requestsPerQtrHr;
  private       long minInterReqTimeMs;
  private       long startTimeMs;
  private       int  requestsMade;

  public BlockingRequestCounter()
  { this (BASIC_REQUESTS_PER_QTR_HR);
  }
  
  public BlockingRequestCounter(int requestsPerQtrHr)
  { this.requestsPerQtrHr = requestsPerQtrHr;
  
    // We set the minimum inter-request time to be the time needed for 10-times the intensity.
    // There seems to be some requirement to limit this.
    this.minInterReqTimeMs = 0;
    reset();
  }
  
  /**
   * Increment the requests made counter, and wait until a suitable amount of
   * time has passed for use to execute that request.
   */
  public void incAndWait() throws InterruptedException
  { long windowEndMs = startTimeMs + WINDOW_MS;
    long nowMs       = System.currentTimeMillis();
    
    if (windowEndMs < nowMs)
    { reset();
      windowEndMs = startTimeMs + WINDOW_MS;
    }
    else if (requestsMade >= requestsPerQtrHr)
    { sleepTillWindowReopens();
      reset();
    }
    else
    { Thread.sleep (minInterReqTimeMs);        
    }
    ++requestsMade;
    
    // Now update the inter-request time
    int reqsRemaining  = requestsPerQtrHr - requestsMade;
    long timeRemaining = windowEndMs - System.currentTimeMillis();
    minInterReqTimeMs  = Math.max(0, timeRemaining) / (reqsRemaining + 1);
  }

  private void sleepTillWindowReopens() throws InterruptedException
  { // We wait for the allotted window time to expire, then wait another minute just to be sure.
    Thread.sleep((WINDOW_MS - (System.currentTimeMillis() - startTimeMs)) + TimeUnit.MINUTES.toMillis(1));
  }

  /** Reset this counter */
  private void reset() {
    this.startTimeMs       = System.currentTimeMillis();
    this.requestsMade      = 0;
    this.minInterReqTimeMs = 0;
  }
}