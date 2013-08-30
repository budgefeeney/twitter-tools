package cc.twittertools.words.combiners;

import java.util.regex.Pattern;

import com.twitter.common.text.combiner.ExtractorBasedTokenCombiner;
import com.twitter.common.text.detector.PunctuationDetector;
import com.twitter.common.text.extractor.EmoticonExtractor;
import com.twitter.common.text.extractor.RegexExtractor;
import com.twitter.common.text.token.TokenStream;
import com.twitter.common.text.token.attribute.TokenType;

/**
 * Collection of different emoticon combiners, each varying from the
 * other based on the type of eyes used.
 * @author bryanfeeney
 *
 */
public final class EmoticonCombiners
{
	/**
	 * A combiner that captures standard, winking, goggled and glass-wearning
	 * smileys, corresponding to the characters : ; 8 and B used for
	 * :) ;) 8) and B)
	 * @param stream
	 */
	public static final TokenStream newAllEmoticonCombiner(TokenStream stream)
	{	return new Standard(new Winking (new Goggles (new Glasses (new Squinting(stream)))));
	}
	
	/**
	 * Captures emotioncs using the character : for eyes such as :)
	 */
	public final static class Standard extends ExtractorBasedTokenCombiner {
	  public Standard(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor(':'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
	/**
	 * Captures emotioncs using the character ; for eyes such as ;)
	 */
	public final static class Winking extends ExtractorBasedTokenCombiner {
	  public Winking(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor(';'));
	    setType(TokenType.EMOTICON);
	  }
	}

	/**
	 * Captures emotioncs using the character 8 for eyes such as 8)
	 */
	public final static class Goggles extends ExtractorBasedTokenCombiner {
	  public Goggles(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor('8'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
	/**
	 * Captures emotioncs using the character B for eyes such as B)
	 */
	public final static class Glasses extends ExtractorBasedTokenCombiner {
	  public Glasses(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor('B'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
	/**
	 * Captures emotioncs using the character x for eyes such as x-)
	 */
	public final static class Squinting extends ExtractorBasedTokenCombiner {
	  public Squinting(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor('x'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
	/**
	 * A variant of the standard Twitter {@link EmoticonExtractor} which allows
	 * one to customise the character used to provide eyes.
	 * @author bryanfeeney
	 */
	private static class EmoticonWithEyesExtractor extends RegexExtractor {
		private static final String EMOTICON_DELIMITER =
		       PunctuationDetector.SPACE_REGEX + "|" + PunctuationDetector.PUNCTUATION_REGEX;
		
		public static final String SMILEY_REGEX_PATTERN = "%1$s-?[)DdpP]|<3";
		public static final String FROWNY_REGEX_PATTERN = "%1$s-?[(<|]";
		
		public final Pattern EMOTICON_REGEX_PATTERN;
		
		/** Default constructor. **/
		public EmoticonWithEyesExtractor(char eyesCharacter) {
			String eyes = String.valueOf(eyesCharacter);
			EMOTICON_REGEX_PATTERN = 
		       Pattern.compile("(?<=^|" + EMOTICON_DELIMITER + ")"
		      		 + "("
			         		+ String.format(SMILEY_REGEX_PATTERN, eyes)
			         		+ "|"
			         		+ String.format(FROWNY_REGEX_PATTERN, eyes)
			         + ")+(?=$|" + EMOTICON_DELIMITER + ")");
			setRegexPattern(EMOTICON_REGEX_PATTERN, 1, 1);
		}
	}
	

	private EmoticonCombiners()
	{	;
	}
}
