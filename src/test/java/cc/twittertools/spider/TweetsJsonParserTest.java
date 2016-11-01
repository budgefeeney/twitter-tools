package cc.twittertools.spider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import cc.twittertools.post.Tweet;


public class TweetsJsonParserTest
{
  private final static String USER = "charlie_whiting";
  private final TweetsJsonParser parser = new TweetsJsonParser();
  private final String sampleJson;
  
  public TweetsJsonParserTest() throws IOException, URISyntaxException
  { try (
      BufferedReader rdr = Files.newBufferedReader(Paths.get(Resources.getResource("moretweets.json").toURI()), Charsets.UTF_8)
    )
    {
      StringBuilder input = new StringBuilder();
      String line = null;
      while ((line = rdr.readLine()) != null)
        input.append (line);
      
      sampleJson = input.toString();
    }
  }
  
  @Before
  public void setUp() throws Exception
  {
    
  }
  
  @After
  public void tearDown() throws Exception
  {
    
  }
  
  @Test
  public void testParser() throws Exception
  { 
    List<Tweet> tweets = parser.parse("", "  \t \n");
    assertThat(tweets, allOf(notNullValue(), empty()));
    
    tweets = parser.parse(USER, sampleJson);
    System.out.println (StringUtils.join(tweets, "\n\n"));
    
    assertThat(tweets, allOf(notNullValue(), hasSize(20)));
    
    assertEquals(tweets.get(3).getAuthor(), "charlie_whiting");
    assertEquals(tweets.get(3).getMsg(), "@willbuxton Well, Lauda texting Hunt was a bit much, but it did move the story along.");
    assertEquals(tweets.get(3).getMsgLessSigils(), " Well, Lauda texting Hunt was a bit much, but it did move the story along.");
    assertEquals(tweets.get(3).getAddressees(), Sets.newHashSet("willbuxton"));
    assertEquals(tweets.get(3).getHashTags(), Sets.newHashSet());
    assertFalse(tweets.get(3).containsRetweet());
    assertFalse(tweets.get(3).isManualRetweet());
    assertEquals(tweets.get(3).getId(), 335198740673593344L);
    
    assertEquals(tweets.get(16).getAuthor(), "charlie_whiting");
    assertEquals(tweets.get(16).getMsg(), "I'm probably not the only one hoping that the new 2014 engines bring some reliability variance back into the sport. #KABLAMMO!");
    assertEquals(tweets.get(16).getMsgLessSigils(), "I'm probably not the only one hoping that the new 2014 engines bring some reliability variance back into the sport. !");
    assertEquals(tweets.get(16).getAddressees(), Sets.newHashSet());
    assertEquals(tweets.get(16).getHashTags(), Sets.newHashSet("KABLAMMO"));
    assertFalse(tweets.get(16).containsRetweet());
    assertFalse(tweets.get(16).isManualRetweet());
    assertEquals(tweets.get(16).getId(), 335019189523857410L);
    
  }
}
