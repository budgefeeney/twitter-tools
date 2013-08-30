package cc.twittertools.words;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import cc.twittertools.scripts.Main;

import com.twitter.common.text.token.attribute.TokenType;

public class TokenizationTest {

	public TokenizationTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testFractionsAndEmoticons()
	{
		String input = "Bad news :( I've realised financial f/cs are wrong 3/4s of the time based on gov'ts advice: agree/disagree? ;-) More at bit.ly/3n32ds2";
  		String[] outputs = new String[] {
  			   "TOKEN", "bad",
  			   "TOKEN", "new",
  			"EMOTICON", ":(",
  			   "TOKEN", "i'v",
  			   "TOKEN", "realis",
  			   "TOKEN", "financi",
  			   "TOKEN", "f/c",
  			   "TOKEN", "wrong",
  			   "TOKEN", "3/4",
  			   "TOKEN", "time",
  			   "TOKEN", "base",
  			   "TOKEN", "gov't",
  			   "TOKEN", "advic",
  			   "TOKEN", "agre",
  			   "TOKEN", "disagre",
  			"EMOTICON", ";-)",
  			   "TOKEN", "more",
  			     "URL", "http://bit.ly/3n32ds2",
  		};
		
		
		Vectorizer vec = new Main().newVectorizer();
  		
  		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
  		int numToks = 0;
  		while (iter.hasNext())
  		{	Pair<TokenType, String> tokenValue = iter.next();
  			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
  			assertEquals(tokenValue.getLeft().toString(), outputs[numToks * 2]);
  			assertEquals(tokenValue.getRight().toString(), outputs[numToks * 2 + 1]);
  			numToks++;
  		}
  		assertEquals (outputs.length / 2, numToks);
	}
	
	@Test
	public void testUrls()
	{
		String input  = "Tech girl blocks tweet plot spoilers << AWESOME story 8D cc: @ShelbyKnox http://www.bbc.co.uk/news/technology-22464364#sa-ns_mchannel=rss&ns_source=PublicRSS20-sa …";
		String[] outputs = new String[] {
	  			   "TOKEN", "bad",
	  			   "TOKEN", "new",
	  			"EMOTICON", ":(",
	  			   "TOKEN", "i'v",
	  			   "TOKEN", "realis",
	  			   "TOKEN", "financi",
	  			   "TOKEN", "f/c",
	  			   "TOKEN", "wrong",
	  			   "TOKEN", "3/4",
	  			   "TOKEN", "time",
	  			   "TOKEN", "base",
	  			   "TOKEN", "gov't",
	  			   "TOKEN", "advic",
	  			   "TOKEN", "agre",
	  			   "TOKEN", "disagre",
	  			"EMOTICON", ";-)",
	  			   "TOKEN", "more",
	  			     "URL", "http://bit.ly/3n32ds2",
	  		};
			
			
			Vectorizer vec = new Main().newVectorizer();
	  		
	  		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
	  		int numToks = 0;
	  		while (iter.hasNext())
	  		{	Pair<TokenType, String> tokenValue = iter.next();
	  			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
//	  			assertEquals(tokenValue.getLeft().toString(), outputs[numToks * 2]);
//	  			assertEquals(tokenValue.getRight().toString(), outputs[numToks * 2 + 1]);
	  			numToks++;
	  		}
//	  		assertEquals (outputs.length / 2, numToks);
	}
}
