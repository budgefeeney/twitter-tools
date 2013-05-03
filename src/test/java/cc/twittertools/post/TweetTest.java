package cc.twittertools.post;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class TweetTest
{
  private Tweet tweet;
  
  @Before
  public void setUp()
  { tweet = new Tweet (
      /* hashTags = */    Sets.newHashSet("boonykid", "sitdown", "howareyouathug"),
      /* users = */       "dezzy_F_babyy",
      /* msg = */         "RT @LoveKayla_xoxo: #howareyouathug and u aint lived in the hood a day in yo life? you\u0027s a #boonykid #sitdown somewhere lol",
      /* addresseess = */ Sets.newHashSet ("LoveKayla_xoxo"),
      /* id = */          29684302916624384L,
      /* requestedId = */ 29684302916624384L,
      /* isRetweetFromMsg = */ true
    );
  
  }
  
  @Test
  public void testMsgLessSigils()
  {
    String result   = tweet.getMsgLessSigils();
    String expected = " :  and u aint lived in the hood a day in yo life? you\u0027s a   somewhere lol";
    
    assertEquals (expected, result);
  }
}
