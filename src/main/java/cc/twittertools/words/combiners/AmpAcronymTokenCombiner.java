package cc.twittertools.words.combiners;

import java.util.regex.Pattern;

import com.twitter.common.text.combiner.ExtractorBasedTokenCombiner;
import com.twitter.common.text.extractor.RegexExtractor;
import com.twitter.common.text.token.TokenStream;

/**
* Handles the case where an acronym contains an ampersand. Examples are:
* <ul>
* <li>AT&T
* <li>T&C's
* <li>A&E
* </ul>
*/
public class AmpAcronymTokenCombiner extends ExtractorBasedTokenCombiner {
	// an acronym involving an ampersand consists of one or two upper-case
	// letters, an ampersand, a further one or two upper-case letters, and
	// maybe an S (any case), optionally prefixed by an apostrophe indicating
	// a plural
	private static final Pattern AMP_ACRONYM = Pattern.compile("([A-Z][A-Z]?&[A-Z][A-Z]?(?:'?[Ss])?)([^a-zA-Z]|$)");

	public AmpAcronymTokenCombiner(TokenStream inputStream) {
	  super(inputStream);
	  setExtractor(
			new RegexExtractor
			.Builder()
			.setRegexPattern(AMP_ACRONYM, 1, 1)
	    .setTriggeringChar('\'')
	    .build()
	  );
	}
}
