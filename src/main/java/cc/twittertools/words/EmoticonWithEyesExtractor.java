package cc.twittertools.words;

//=================================================================================================
//Copyright 2011 Twitter, Inc.
//-------------------------------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this work except in compliance with the License.
//You may obtain a copy of the License in the LICENSE file, or at:
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//=================================================================================================


//=================================================================================================
// Amended to handle cases of different kinds of smileys
//
//- Bryan Feeney
//=================================================================================================



import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.twitter.common.text.detector.PunctuationDetector;
import com.twitter.common.text.extractor.RegexExtractor;

/**
* Extracts emoticons (e.g., :), :-( ) from a text.
*/
public class EmoticonWithEyesExtractor extends RegexExtractor {
	private static final String EMOTICON_DELIMITER =
	       PunctuationDetector.SPACE_REGEX + "|" + PunctuationDetector.PUNCTUATION_REGEX;
	
	public static final String SMILEY_REGEX_PATTERN = "%1$s[)DdpP]|%1$s[ -]\\)|<3";
	public static final String FROWNY_REGEX_PATTERN = "%1$s[(<]|%1$s[ -]\\(";
	
	public final Pattern EMOTICON_REGEX_PATTERN;
	
	/** The term of art for referring to {positive, negative} sentiment is polarity. */
	public enum Polarity {
	 HAPPY,
	 SAD
	}
	
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
	
	/**
	* Returns the polarity (happy, sad...) of a given emoticon.
	*
	* @param emoticon emoticon text
	* @return polarity of the emoticon
	*/
	public static final Polarity getPolarityOf(CharSequence emoticon) {
		Preconditions.checkNotNull(emoticon);
		Preconditions.checkArgument(emoticon.length() > 0);
		
		char lastChar = emoticon.charAt(emoticon.length() - 1);
		if (lastChar == '(' || lastChar == '<') {
			return Polarity.SAD;
		}

		return Polarity.HAPPY;
	}
}