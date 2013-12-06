package cc.twittertools.words;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import cc.twittertools.scripts.Main;

import com.twitter.common.text.token.attribute.TokenType;

public class TokenizationTest {

	public TokenizationTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testFractionsAndEmoticons() throws IOException
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
			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight(), outputs[numToks * 2 + 1]);
//			assertEquals(outputs[numToks * 2], tokenValue.getLeft().toString());
//			assertEquals(outputs[numToks * 2 + 1], tokenValue.getRight().toString());
			numToks++;
		}
		assertEquals (outputs.length / 2, numToks);
	}
	
	@Test
	public void testUrls() throws IOException
	{
		String input  = "Tech girl blocks tweet plot spoilers << AWESOME story 8D cc: @ShelbyKnox http://www.bbc.co.uk/news/technology-22464364#sa-ns_mchannel=rss&ns_source=PublicRSS20-sa …";
		String[] outputs = new String[] {
			   "TOKEN", "tech",
			   "TOKEN", "girl",
			   "TOKEN", "block",
			   "TOKEN", "tweet",
			   "TOKEN", "plot",
			   "TOKEN", "spoiler",
			   "TOKEN", "awesom",
			   "TOKEN", "stori",
			"EMOTICON", "8d",
			   "TOKEN", "cc",
			"USERNAME", "@shelbyknox",
			     "URL", "http://www.bbc.co.uk/news/technology-22464364#sa-ns_mchannel=rss&ns_source=PublicRSS20-sa"
	  		};
			
		Vectorizer vec = new Main().newVectorizer();
  		
		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
		int numToks = 0;
		while (iter.hasNext())
		{	Pair<TokenType, String> tokenValue = iter.next();
			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
			assertEquals(outputs[numToks * 2], tokenValue.getLeft().toString());
			assertEquals(outputs[numToks * 2 + 1], tokenValue.getRight().toString());
			numToks++;
		}
		assertEquals (outputs.length / 2, numToks);
	}
	
	@Test
	public void testSmileys() throws IOException
	{
		String input  = "Anyone remember Keith :-P (Mr. what's in the box) x). My mum's not a fan :-|";
		String[] outputs = new String[] {
			   "TOKEN", "anyon",
			   "TOKEN", "rememb",
			   "TOKEN", "keith",
			"EMOTICON", ":-p",
			   "TOKEN", "mr",
			   "TOKEN", "what",
			   "TOKEN", "box",
			"EMOTICON", "x)",
		     "TOKEN", "my",
		     "TOKEN", "mum",
		     // it's a bit disturbing that a sentiment specific word like "not" is in the stop-word list.
		     "TOKEN", "fan",
			"EMOTICON", ":-|",
	  };
			
		Vectorizer vec = new Main().newVectorizer();
  		
		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
		int numToks = 0;
		while (iter.hasNext())
		{	Pair<TokenType, String> tokenValue = iter.next();
			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
			assertEquals(outputs[numToks * 2], tokenValue.getLeft().toString());
			assertEquals(outputs[numToks * 2 + 1], tokenValue.getRight().toString());
			numToks++;
		}
		System.out.flush();
		assertEquals (outputs.length / 2, numToks);
	}
	
	
//	 public static void main(String args[]) {
//	      String stringToExamine = "AT&T isn't " + /* good for me: The*/ " T&C's & AT&T's crappy 4G network is so bad I'm now in A&E :| Time to trash&burn that contract of mine. LOVE&HATE";
//	  		
//          //print each word in order
//          BreakIterator boundary = BreakIterator.getWordInstance();
//          boundary.setText(stringToExamine);
//          printEachForward(boundary, stringToExamine);
//          //print each sentence in reverse order
//          boundary = BreakIterator.getSentenceInstance();
//          boundary.setText(stringToExamine);
//          printEachForward(boundary, stringToExamine);
//	      
//	 }
//	 
//	 public static void printEachForward(BreakIterator boundary, String source) {
//	     int start = boundary.first();
//	     for (int end = boundary.next();
//	          end != BreakIterator.DONE;
//	          start = end, end = boundary.next()) {
//	          System.out.println(source.substring(start,end));
//	     }
//	 }
	
	@Test
	public void testAcronymsWithAmpersands() throws IOException
	{
		String input = "AT&T isn't  good for me: The T&Cs & AT&T's crappy 4G network is so bad I'm now in A&E :| Time to trash&burn that contract of mine. LOVE&HATE";
		String[] outputs = new String[] 
		{
	   "TOKEN", "at&t",
	   "TOKEN", "isn't",
	   "TOKEN", "good",
	   "TOKEN", "me",
	   "TOKEN", "t&c",
	   "TOKEN", "at&t",
	   "TOKEN", "crappi",
	   "TOKEN", "4g",
	   "TOKEN", "network",
	   "TOKEN", "so",
	   "TOKEN", "bad",
	   "TOKEN", "i'm",
	   "TOKEN", "now",
	   "TOKEN", "a&e",
	   "EMOTICON", ":|",
	   "TOKEN", "time",
	   "TOKEN", "trash",
	   "TOKEN", "burn",
	   "TOKEN", "contract",
	   "TOKEN", "mine",
	   "TOKEN", "love",
	   "TOKEN", "hate",
		};
		
		Vectorizer vec = new Main().newVectorizer();
  		
		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
		int numToks = 0;
		while (iter.hasNext())
		{	Pair<TokenType, String> tokenValue = iter.next();
			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
			assertEquals(outputs[numToks * 2], tokenValue.getLeft().toString());
			assertEquals(outputs[numToks * 2 + 1], tokenValue.getRight().toString());
			numToks++;
		}
		System.out.flush();
		assertEquals (outputs.length / 2, numToks);
	}
	
	@Test
	public void testSlashes() throws IOException
	{	
		String input = "Cont. claims more interesting than initial claims. Down again. Implies risk of even lower U/E rate.";
		String[] outputs = new String[] {
	   "TOKEN", "cont",
	   "TOKEN", "claim",
	   "TOKEN", "more",
	   "TOKEN", "interest",
	   "TOKEN", "than",
	   "TOKEN", "initi",
	   "TOKEN", "claim",
	   "TOKEN", "down",
	   "TOKEN", "again",
	   "TOKEN", "impli",
	   "TOKEN", "risk",
	   "TOKEN", "even",
	   "TOKEN", "lower",
	   "TOKEN", "u/e",
	   "TOKEN", "rate"
		};
		
		Vectorizer vec = new Main().newVectorizer();
		
		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
		int numToks = 0;
		while (iter.hasNext())
		{	Pair<TokenType, String> tokenValue = iter.next();
			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
			assertEquals(outputs[numToks * 2], tokenValue.getLeft().toString());
			assertEquals(outputs[numToks * 2 + 1], tokenValue.getRight().toString());
			numToks++;
		}
		System.out.flush();
		assertEquals (outputs.length / 2, numToks);
	}
	
	@Test
	public void testUrlOnly() throws Exception
	{	String input = "pic.twitter.com/0CYzIF3MaI";
		String[] outputs = new String[] { "URL", "http://pic.twitter.com/0CYzIF3MaI" };
		
		Vectorizer vec = new Main().newVectorizer();
		
		Iterator<Pair<TokenType, String>> iter = vec.toWords(input);
		int numToks = 0;
		while (iter.hasNext())
		{	Pair<TokenType, String> tokenValue = iter.next();
			System.out.printf ("%8s --> %s\n", tokenValue.getLeft(), tokenValue.getRight());
			assertEquals(outputs[numToks * 2], tokenValue.getLeft().toString());
			assertEquals(outputs[numToks * 2 + 1], tokenValue.getRight().toString());
			numToks++;
		}
		System.out.flush();
		assertEquals (outputs.length / 2, numToks);
	}
}
