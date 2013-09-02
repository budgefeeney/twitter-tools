package cc.twittertools.words.dict;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static cc.twittertools.words.dict.CompoundTokenDictionary.ucFirst;

public class CompoundTokenDictionaryTest
{
	@Test
	public void testUcFirst()
	{	assertNull (ucFirst(null));
		assertEquals ("", ucFirst(""));
		assertEquals (" ", ucFirst (" "));
		assertEquals (" capital", ucFirst(" CapitaL"));
		assertEquals ("Capital", ucFirst("CapitaL"));
		assertEquals ("Capital ", ucFirst("cAPITAL "));
	}

}
