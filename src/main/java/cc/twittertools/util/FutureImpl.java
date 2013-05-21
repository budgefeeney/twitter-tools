package cc.twittertools.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FutureImpl<T> implements Future<T>
{
  public boolean cancelled = false;
  public boolean finished = false;
  public T value;
  public Exception error;

  @Override
  public synchronized boolean cancel(boolean mayInterruptIfRunning)
  { boolean succeeded = ! cancelled && ! finished;
  
    cancelled = true;
    notifyAll();
    
    return succeeded;
  }

  @Override
  public synchronized T get() throws InterruptedException, ExecutionException
  {
    while (! cancelled && ! finished)
      wait();
    
    if (cancelled)
      throw new InterruptedException ("The task was cancelled");
    else if (error != null)
      throw new ExecutionException(error);
    else
      return value;
  }

  @Override
  public synchronized T get(long timeout, TimeUnit units) throws InterruptedException, ExecutionException,
      TimeoutException
  { 
    if (! cancelled && ! finished)
      wait(units.toMillis(timeout));
    
    if (cancelled)
      throw new InterruptedException ("The task was cancelled");
    else if (! finished)
      throw new TimeoutException ("Task timed out before a value was supplied");
    else if (error != null)
      throw new ExecutionException(error);
    else
      return value;
  }
  
  public synchronized void put(T newVal) throws InterruptedException
  { if (cancelled)
      throw new IllegalStateException ("Can't put a value in a future that's been cancelled");
    if (finished)
      throw new IllegalStateException ("A value has already been assigned to this future, you cannot assign another value");
    
    value = newVal;
    finished = true;
    notifyAll();
  }
  
  public synchronized void putError(Exception e) throws InterruptedException
  {
    if (cancelled)
      throw new IllegalStateException ("Can't put an error in a future that's been cancelled");
    if (finished)
      throw new IllegalStateException ("A value has already been assigned to this future, you cannot assign an error");
    
    error = e;
    finished = true;
    notifyAll();
  }

  @Override
  public synchronized boolean isCancelled()
  { return cancelled;
  }

  @Override
  public synchronized boolean isDone()
  { return finished;
  }

}
