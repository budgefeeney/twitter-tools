package cc.twittertools.words;

import java.util.regex.Pattern;

import com.twitter.common.text.tokenizer.LatinTokenizer;
import com.twitter.common.text.tokenizer.RegexTokenizer;

/**
 * Patches the {@link LatinTokenizer} shipped by Twitter, which 
 * treated unicode fragments as whitespace. A lot of twitter users
 * are using the extended unicode spec (i.e the bit with lots of 
 * pictures of beer and smileys) and the Twitter version was trimming
 * the last byte from the 3-byte UTF-8 encoding, creating unoutputable
 * text.
 * @author bryanfeeney
 *
 */
public class UnicodeLatinTokenizer extends RegexTokenizer {

	  // delimiter = one or more space, or one or more punctuation followed by space.
	  private static final String DELIMITER = "(?:[\\p{Cc}\\p{Z}&&[^\\n\\r]]+)|([\\p{P}\\p{M}\\p{S}\\n\\r])[\\p{Cc}\\p{Z}&&[^\\n\\r]]*";
	  private static final int PATTERN_FLAGS =
	    Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.DOTALL;
	  private static final Pattern SPLIT_PATTERN = Pattern.compile(DELIMITER, PATTERN_FLAGS);
	  private static final int PUNCTUATION_GROUP = 1;

	  // Please use Builder
	  protected UnicodeLatinTokenizer() {
	    setDelimiterPattern(SPLIT_PATTERN);
	    setPunctuationGroupInDelimiterPattern(PUNCTUATION_GROUP);
	    setKeepPunctuation(true);
	  }

	  public static final class Builder extends AbstractBuilder<UnicodeLatinTokenizer, Builder> {
	    public Builder() {
	      super(new UnicodeLatinTokenizer());
	    }
	  }
}
