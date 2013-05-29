package cc.twittertools.spider;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Details of a user we fetched and how we fetched them.
 * TODO Merge this with {@link TwitterUser}
 */
final class FetchedUser
{ private final String category;
  private final String screenName;
  private final List<String> antecedents;
  private final Date signupDate;
  private final long cursor;
  
  public FetchedUser(String category, String screenName, Date signupDate)
  { this (category, screenName, signupDate, Collections.<String>emptyList(), -1L);
  }
  
  public FetchedUser(String category, String screenName, Date signupDate, List<String> antecedents, long cursor)
  { super();
    this.category = category;
    this.screenName = screenName;
    this.antecedents = antecedents;
    this.cursor = cursor;
    this.signupDate = signupDate;
  }

  public String getCategory()
  { return category;
  }

  public String getScreenName()
  { return screenName;
  }

  public List<String> getAntecedents()
  { return antecedents;
  }

  public long getCursor() {
    return cursor;
  }

  public Date getSignupDate() {
    return signupDate;
  }
}