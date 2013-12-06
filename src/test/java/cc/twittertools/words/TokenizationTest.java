package cc.twittertools.words;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.Charsets;
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
	
	
	@Test
	public void testSupplementaryUnicode() throws IOException
	{
		String input = new String (new byte[] { 0x53, 0x68, 0x69, 0x74, 0x20, 0x49, 0x20, 0x44, 0x6F, 0x6E, 0x27, 0x74, 0x20, 0x4C, 0x69, 0x6B, 0x65, 0x3A, 0x20, 0x4D, 0x6F, 0x6E, 0x64, 0x61, 0x79, 0x73, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x9E, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x9E, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x9E, 0x20, 0x4D, 0x61, 0x74, 0x68, 0x20, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x92, (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x8A, 0x20, 0x53, 0x6C, 0x6F, 0x77, 0x20, 0x74, 0x65, 0x78, 0x74, 0x65, 0x72, 0x73, (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0xB1, 0x20, 0x57, 0x61, 0x6B, 0x69, 0x6E, 0x67, 0x20, 0x75, 0x70, 0x20, 0x65, 0x61, 0x72, 0x6C, 0x79, (byte) 0xF0, (byte) 0x9F, (byte) 0x92, (byte) 0xA4, 0x20, 0x42, 0x65, 0x69, 0x6E, 0x67, 0x20, 0x77, 0x72, 0x6F, 0x6E, 0x67, (byte) 0xE2, (byte) 0x9D, (byte) 0x8C, 0x20, 0x2A, 0x20, 0x49, 0x20, 0x63, 0x61, 0x6E, 0x27, 0x74, 0x20, 0x64, 0x65, 0x61, 0x6C, 0x20, 0x77, 0x69, 0x74, 0x68, 0x20, 0x52, 0x61, 0x79, 0x2D, 0x4A, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x91, 0x0A, (byte) 0xe5, (byte) 0x8f, (byte) 0xaf, (byte) 0xe6, (byte) 0x84, (byte) 0x9b, (byte) 0xe3, (byte) 0x81, (byte) 0x84, (byte) 0xe6, (byte) 0x9c, (byte) 0x8d, (byte) 0xe3, (byte) 0x81, (byte) 0x8c, (byte) 0xe3, (byte) 0x81, (byte) 0x84, (byte) 0xe3, (byte) 0x81, (byte) 0xa3, (byte) 0xe3, (byte) 0x81, (byte) 0xb1, (byte) 0xe3, (byte) 0x81, (byte) 0x84, (byte) 0x28, (byte) 0x2a, (byte) 0x5e, (byte) 0x5e, (byte) 0x2a, (byte) 0x29, (byte) 0xe3, (byte) 0x81, (byte) 0x9f, (byte) 0xe3, (byte) 0x81, (byte) 0x8f, (byte) 0xe3, (byte) 0x81, (byte) 0x95, (byte) 0xe3, (byte) 0x82, (byte) 0x93, (byte) 0xe3, (byte) 0x82, (byte) 0xaa, (byte) 0xe3, (byte) 0x83, (byte) 0xbc, (byte) 0xe3, (byte) 0x83, (byte) 0x80, (byte) 0xe3, (byte) 0x83, (byte) 0xbc, (byte) 0xe3, (byte) 0x81, (byte) 0x97, (byte) 0xe3, (byte) 0x81, (byte) 0xbe, (byte) 0xe3, (byte) 0x81, (byte) 0x97, (byte) 0xe3, (byte) 0x81, (byte) 0x9f, (byte) 0xe2, (byte) 0x99, (byte) 0xa1 }, Charsets.UTF_8);
		
		// This reveals that tokenization of CJKV is pretty poor, but it's a complex
		// problem that's not trivially solved - frankly you're better off with tokenizing
		// such languages into individual ideographs, maybe with some n-gram detection using
		// e.g. Ted Dunning's "The Suprise of Statistics and Confidence" method.
		
		String[] outputs = new String[] 
		{
				 "TOKEN",   /* shit     */ new String(new byte[] { (byte) 0x73, (byte) 0x68, (byte) 0x69, (byte) 0x74 }, Charsets.UTF_8 ),
				 "TOKEN",   /* don't    */ new String(new byte[] { (byte) 0x64, (byte) 0x6f, (byte) 0x6e, (byte) 0x27, (byte) 0x74 }, Charsets.UTF_8 ),
				 "TOKEN",   /* like     */ new String(new byte[] { (byte) 0x6c, (byte) 0x69, (byte) 0x6b, (byte) 0x65 }, Charsets.UTF_8 ),
				 "TOKEN",   /* mondai   */ new String(new byte[] { (byte) 0x6d, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x61, (byte) 0x69 }, Charsets.UTF_8 ),
				"EMOTICON", /* 😞       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x9e }, Charsets.UTF_8 ),
				"EMOTICON", /* 😞       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x9e }, Charsets.UTF_8 ),
				"EMOTICON", /* 😞       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x9e }, Charsets.UTF_8 ),
				 "TOKEN",   /* math     */ new String(new byte[] { (byte) 0x6d, (byte) 0x61, (byte) 0x74, (byte) 0x68 }, Charsets.UTF_8 ),
				"EMOTICON", /* 😒       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x92 }, Charsets.UTF_8 ),
				"EMOTICON", /* 📊       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x93, (byte) 0x8a }, Charsets.UTF_8 ),
				 "TOKEN",   /* slow     */ new String(new byte[] { (byte) 0x73, (byte) 0x6c, (byte) 0x6f, (byte) 0x77 }, Charsets.UTF_8 ),
				 "TOKEN",   /* texter   */ new String(new byte[] { (byte) 0x74, (byte) 0x65, (byte) 0x78, (byte) 0x74, (byte) 0x65, (byte) 0x72 }, Charsets.UTF_8 ),
				"EMOTICON", /* 📱       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x93, (byte) 0xb1 }, Charsets.UTF_8 ),
				 "TOKEN",   /* wake     */ new String(new byte[] { (byte) 0x77, (byte) 0x61, (byte) 0x6b, (byte) 0x65 }, Charsets.UTF_8 ),
				 "TOKEN",   /* up       */ new String(new byte[] { (byte) 0x75, (byte) 0x70 }, Charsets.UTF_8 ),
				 "TOKEN",   /* earli    */ new String(new byte[] { (byte) 0x65, (byte) 0x61, (byte) 0x72, (byte) 0x6c, (byte) 0x69 }, Charsets.UTF_8 ),
				"EMOTICON", /* 💤       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x92, (byte) 0xa4 }, Charsets.UTF_8 ),
				 "TOKEN",   /* be       */ new String(new byte[] { (byte) 0x62, (byte) 0x65 }, Charsets.UTF_8 ),
				 "TOKEN",   /* wrong    */ new String(new byte[] { (byte) 0x77, (byte) 0x72, (byte) 0x6f, (byte) 0x6e, (byte) 0x67 }, Charsets.UTF_8 ),
				"EMOTICON", /* ❌       */ new String(new byte[] { (byte) 0xe2, (byte) 0x9d, (byte) 0x8c }, Charsets.UTF_8 ),
				 "TOKEN",   /* can't    */ new String(new byte[] { (byte) 0x63, (byte) 0x61, (byte) 0x6e, (byte) 0x27, (byte) 0x74 }, Charsets.UTF_8 ),
				 "TOKEN",   /* deal     */ new String(new byte[] { (byte) 0x64, (byte) 0x65, (byte) 0x61, (byte) 0x6c }, Charsets.UTF_8 ),
				 "TOKEN",   /* ray-j    */ new String(new byte[] { (byte) 0x72, (byte) 0x61, (byte) 0x79, (byte) 0x2d, (byte) 0x6a }, Charsets.UTF_8 ),
				"EMOTICON", /* 😑       */ new String(new byte[] { (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x91 }, Charsets.UTF_8 ),
				 "TOKEN",   /* 可愛      */ new String(new byte[] { (byte) 0xe5, (byte) 0x8f, (byte) 0xaf, (byte) 0xe6, (byte) 0x84, (byte) 0x9b }, Charsets.UTF_8 ),
				 "TOKEN",   /* がいっぱい */ new String(new byte[] { (byte) 0xe3, (byte) 0x81, (byte) 0x8c, (byte) 0xe3, (byte) 0x81, (byte) 0x84, (byte) 0xe3, (byte) 0x81, (byte) 0xa3, (byte) 0xe3, (byte) 0x81, (byte) 0xb1, (byte) 0xe3, (byte) 0x81, (byte) 0x84 }, Charsets.UTF_8 ),
				 "TOKEN",   /* たくさん   */ new String(new byte[] { (byte) 0xe3, (byte) 0x81, (byte) 0x9f, (byte) 0xe3, (byte) 0x81, (byte) 0x8f, (byte) 0xe3, (byte) 0x81, (byte) 0x95, (byte) 0xe3, (byte) 0x82, (byte) 0x93 }, Charsets.UTF_8 ),
				 "TOKEN",   /* オーダー   */ new String(new byte[] { (byte) 0xe3, (byte) 0x82, (byte) 0xaa, (byte) 0xe3, (byte) 0x83, (byte) 0xbc, (byte) 0xe3, (byte) 0x83, (byte) 0x80, (byte) 0xe3, (byte) 0x83, (byte) 0xbc }, Charsets.UTF_8 ),
				 "TOKEN",   /* しました   */ new String(new byte[] { (byte) 0xe3, (byte) 0x81, (byte) 0x97, (byte) 0xe3, (byte) 0x81, (byte) 0xbe, (byte) 0xe3, (byte) 0x81, (byte) 0x97, (byte) 0xe3, (byte) 0x81, (byte) 0x9f }, Charsets.UTF_8 ),
				"EMOTICON", /* ♡        */ new String(new byte[] { (byte) 0xe2, (byte) 0x99, (byte) 0xa1 }, Charsets.UTF_8 ),

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
