package cc.twittertools.words.combiners;

import java.util.regex.Pattern;

import com.twitter.common.text.combiner.ExtractorBasedTokenCombiner;
import com.twitter.common.text.extractor.RegexExtractor;
import com.twitter.common.text.token.TokenStream;

/**
 * Merges tokens which are split by a slash. There are two use cases:
 * <ul>
 *   <li>Fractions, e.g."The gov'ts policy ignores 2/3s of people"
 *   <li>Abbreviations, e.g. "Financial f/cs aren't always reliable"
 * </ul>
 * @author bryanfeeney
 *
 */
public class SlashTokenCombiner extends ExtractorBasedTokenCombiner {
	//private static final Pattern APOSTROPHE_S = Pattern.compile("([a-zA-Z]+'(?i:t|ts|s|m|re|ve|ll|d))([^a-zA-Z]|$)");
	private static final Pattern SLASH_S = Pattern.compile("((?:\\w\\w?|\\d)/(?:\\w\\w?|\\d))([^a-zA-Z0-9]|$)");

	public SlashTokenCombiner(TokenStream inputStream) {
	  super(inputStream);
	  setExtractor(
			new RegexExtractor
			.Builder()
			.setRegexPattern(SLASH_S, 1, 1)
	    .setTriggeringChar('/')
	    .build()
	  );
	}
}
