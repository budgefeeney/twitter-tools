package cc.twittertools.spider;

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


public class TweetsHtmlParserTest
{
  private final static String USER = "charlie_whiting";
  private final TweetsHtmlParser parser = new TweetsHtmlParser();
  private final String samplePageHtml;
  private final String sampleUser;
  
  public TweetsHtmlParserTest() throws IOException, URISyntaxException
  { sampleUser = "rtraister";
    String variant = "-3";
    try (
      BufferedReader rdr = Files.newBufferedReader(Paths.get(Resources.getResource(sampleUser + "2016" + variant + ".html").toURI()), Charsets.UTF_8)
    )
    {
      StringBuilder input = new StringBuilder();
      String line = null;
      while ((line = rdr.readLine()) != null)
        input.append (line);
      
      samplePageHtml = input.toString();
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
    List<Tweet> tweets = parser.parse("rtraister", "  \t \n");
    //assertThat(tweets, allOf(notNullValue(), empty()));
    
    tweets = parser.parse(sampleUser, samplePageHtml);
    //assertThat(tweets, allOf(notNullValue(), hasSize(20)));
    assertEquals(tweets.get(3).getAuthor(), "charlie_whiting");
    assertEquals(tweets.get(3).getMsg(), "Cool as well as lump-in-throat inducing. @RoyalAirForceUK is tweeting the signals received from the #Dambusters70 on their 70th anniv.");
    assertEquals(tweets.get(3).getMsgLessSigils(), "Cool as well as lump-in-throat inducing.  is tweeting the signals received from the  on their 70th anniv.");
    assertEquals(tweets.get(3).getAddressees(), Sets.newHashSet("RoyalAirForceUK"));
    assertEquals(tweets.get(3).getHashTags(), Sets.newHashSet("Dambusters70"));
    assertFalse(tweets.get(3).containsRetweet());
    assertFalse(tweets.get(3).isManualRetweet());
    assertEquals(tweets.get(3).getId(), 335212126207619072L);
    
    System.out.println (StringUtils.join(tweets, "\n\n"));
  }
}
