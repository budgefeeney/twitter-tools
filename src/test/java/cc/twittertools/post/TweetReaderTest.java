package cc.twittertools.post;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TweetReaderTest 
{
  private final static List<String> INPUT = Collections.unmodifiableList (Lists.newArrayList(
      "{\"id\":29684302916624384,\"screenName\":\"dezzy_F_babyy\",\"text\":\"RT @LoveKayla_xoxo: #howareyouathug and u aint lived in the hood a day in yo life? you\u0027s a #boonykid #sitdown somewhere lol\",\"createdAt\":\"3:37 PM - 24 Jan 11\",\"timestamp\":\"1295912263\",\"requested_id\":29684302916624384}",
      "{\"id\":29684303424131072,\"screenName\":\"maaricorreia_\",\"text\":\"eu não sirvo pra ter unha grande, eu #vivo me arranhando :/\",\"createdAt\":\"3:36 PM - 24 Jan 11\",\"timestamp\":\"1295912176\",\"requested_id\":29684303424131072}",
      "{\"id\":29684303671595009,\"screenName\":\"lunikxio\",\"text\":\"quiero hacer gomitasss \u003eO\u003c\",\"createdAt\":\"3:37 PM - 24 Jan 11\",\"timestamp\":\"1295912264\",\"requested_id\":29684303671595009}",
      "{\"id\":29684303671595011,\"screenName\":\"lunikxio2\",\"text\":\"\",\"createdAt\":\"2:41 AM - 1 Jul 09\",\"timestamp\":\"1295912264\",\"requested_id\":29684303671595009}"
  ));
  
  @Test
  public void testReader() throws IOException
  { cc.twittertools.post.old.Tweet tweet;
    Iterator<cc.twittertools.post.old.Tweet> tweets = new TweetReader (INPUT.iterator());
    
    assertTrue (tweets.hasNext());
    tweet = tweets.next();
    assertEquals (29684302916624384L, tweet.getId());
    assertEquals (29684302916624384L, tweet.getRequestedId());
    assertEquals ("dezzy_F_babyy", tweet.getAuthor());
    assertEquals ("RT @LoveKayla_xoxo: #howareyouathug and u aint lived in the hood a day in yo life? you\u0027s a #boonykid #sitdown somewhere lol", tweet.getMsg());
    assertTrue   (tweet.isRetweetFromMsg());
    assertFalse  (tweet.isRetweetFromId());
    assertEquals (Sets.newHashSet("howareyouathug", "boonykid", "sitdown"), tweet.getHashTags());
    assertEquals (Sets.newHashSet("LoveKayla_xoxo"), tweet.getAddressees());
    assertTrue   (tweet.getLocalTime().toString().startsWith("2011-01-24T15:37:00.000"));
    

    assertTrue (tweets.hasNext());
    tweet = tweets.next();
    assertEquals (29684303424131072L, tweet.getId());
    assertEquals (29684303424131072L, tweet.getRequestedId());
    assertEquals ("maaricorreia_", tweet.getAuthor());
    assertEquals ("eu não sirvo pra ter unha grande, eu #vivo me arranhando :/", tweet.getMsg());
    assertFalse   (tweet.isRetweetFromMsg());
    assertFalse  (tweet.isRetweetFromId());
    assertEquals (Sets.newHashSet("vivo"), tweet.getHashTags());
    assertEquals (Sets.newHashSet(), tweet.getAddressees());
    assertTrue   (tweet.getLocalTime().toString().startsWith("2011-01-24T15:36:00.000"));
    

    assertTrue (tweets.hasNext());
    tweet = tweets.next();
    assertEquals (29684303671595009L, tweet.getId());
    assertEquals (29684303671595009L, tweet.getRequestedId());
    assertEquals ("lunikxio", tweet.getAuthor());
    assertEquals ("quiero hacer gomitasss \u003eO\u003c", tweet.getMsg());
    assertFalse   (tweet.isRetweetFromMsg());
    assertFalse  (tweet.isRetweetFromId());
    assertEquals (Sets.newHashSet(), tweet.getHashTags());
    assertEquals (Sets.newHashSet(), tweet.getAddressees());
    assertTrue   (tweet.getLocalTime().toString().startsWith("2011-01-24T15:37:00.000"));
    

    assertTrue (tweets.hasNext());
    tweet = tweets.next();
    assertEquals (29684303671595011L, tweet.getId());
    assertEquals (29684303671595009L, tweet.getRequestedId());
    assertEquals ("lunikxio2", tweet.getAuthor());
    assertEquals ("", tweet.getMsg());
    assertFalse   (tweet.isRetweetFromMsg());
    assertTrue  (tweet.isRetweetFromId());
    assertEquals (Sets.newHashSet(), tweet.getHashTags());
    assertEquals (Sets.newHashSet(), tweet.getAddressees());
    System.out.println (tweet.getLocalTime());
    assertTrue   (tweet.getLocalTime().toString().startsWith("2009-07-01T02:41:00.000"));
    
    assertFalse (tweets.hasNext());
  }
}
