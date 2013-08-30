package cc.twittertools.words;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.StopAnalyzer;
import org.tartarus.snowball.ext.PorterStemmer;

import com.twitter.common.text.token.TokenStream;
import com.twitter.common.text.token.TokenStream2LuceneTokenizerWrapper;
import com.twitter.common.text.token.attribute.CharSequenceTermAttribute;
import com.twitter.common.text.token.attribute.TokenType;
import com.twitter.common.text.token.attribute.TokenTypeAttribute;

/**
 * Represents a Twitter implementation of {@link TokenStream} as an iterator
 * over strings, essentially presenting a unified facade to token streams
 * (see {@link TokenStreamIterator} for Lucene).
 * <p>
 * Due to the failure of {@link TokenStream2LuceneTokenizerWrapper} I've
 * implemented most of the filtering logic in this class.
 * @author bryanfeeney
 *
 */
public class TwitterTokenStreamIterator implements Iterator<Pair<TokenType, String>>
{
	private final TokenStream toks;
	private final CharSequenceTermAttribute charTermAttribute;
	private final TokenTypeAttribute tokenAttr;

	private final boolean stem ;
	private final boolean stop ;
	private final boolean lowerCase;
	private final int     minLengthIncl;
	private final int     maxLengthExcl;
	
	@SuppressWarnings("unchecked")
	private final Set<String> stops = (Set<String>) StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	private final PorterStemmer stemmer = new PorterStemmer();
	
	private boolean hasNext;
	private String term;
	private RuntimeException e;

	
	
	public TwitterTokenStreamIterator(TokenStream toks, boolean stem,
			boolean stop, boolean lowerCase, int minLengthIncl,
			int maxLengthExcl) {
		super();
		this.toks = toks;
		this.stem = stem;
		this.stop = stop;
		this.lowerCase = lowerCase;
		this.minLengthIncl = minLengthIncl;
		this.maxLengthExcl = maxLengthExcl;
		this.charTermAttribute = toks.getAttribute(CharSequenceTermAttribute.class);
		this.tokenAttr = toks.getAttribute(TokenTypeAttribute.class);
		moveToNextToken();
	}
	
	/**
	 * Resets this iterator with the given text
	 */
	public void reset(String text)
	{	toks.reset(text);
		moveToNextToken();
	}
	

	/**
	 * Moves to the next token. Should that throw an Exception, stores
	 * it, and ensures that hasNext returns true so the exception can
	 * be thrown by next(). Otherwise sets hasNext to return the standard
	 * return value (did we move ahead or not)
	 */
	private void moveToNextToken()
	{
		try
		{	while ((hasNext = toks.incrementToken()))
			{	term = charTermAttribute.getTermString();
				boolean isAllCapLetters = isAllCapLetters (term);
				if (lowerCase)
				{	term = term.toLowerCase();
				}
				if (tokenAttr.getType() != TokenType.TOKEN)
				{	break; // it's a hashtag, or an addressee, or a stock
				}          // or something else that doesn't need stemming etc.
				if (stop && stops.contains (term))
				{	continue;
				}
				if (term.length() < minLengthIncl)
				{	continue;
				}
				if (term.length() >= maxLengthExcl)
				{	continue;
				}
				if (stem) // Have to be careful with stemming twitter text due to abbreviations
				{	if (term.length() > 2 && ! isProbablyAnAcronym(term, isAllCapLetters)) 
					{	stemmer.setCurrent(term);                       // the lucene stemmer tends to
						if (stemmer.stem())                             // completely dismantle acronyms
						{	term = stemmer.getCurrent();                  // (e.g. "IEDs" --> "I") so we 
					    // "it's" goes to "it'" instead of "it"       // avoid using it.
							while (term.length() > 0 && ! Character.isLetterOrDigit(term.charAt(term.length() - 1)))
								term = term.substring(0, term.length() - 1);
							
							if (term.isEmpty())
								continue;
						}
					}                                                 
					else
					{	// Poor man's acronym stemmer - strip off terminating "s" characters
						// on the presumption that they're _always_ there for pluralisation
						if (term.charAt (term.length() - 1) == 's')
						{	String stemmed = term.substring (0, term.length() - 1);
							if (isProbablyAnAcronym (stemmed, true))
								term = stemmed;
						}
					}
				}
				break;
			}
		}
		catch (Exception ioe)
		{	e = new RuntimeException (ioe.getMessage(), ioe);
			hasNext = true; // so they call next and see the exception
		}
	}
	
	/**
	 * Soft test to see if the given term is an acronym
	 * It uses a lookup list of common acroyms, and if no match is found 
	 * against that set, it then returns true iff the term is all uppercase,
	 * all letters, and its length is four characters or less
	 */
	private boolean isProbablyAnAcronym(String term, boolean isAllCapLetters)
	{	return AcronymSet.INSTANCE.contains (term)
			|| (term.length() < 5 && term.length() > 1 && isAllCapLetters);
	}

	/**
	 * Is the given term entirely in capital letters. 
	 * There are two exceptions to this rule:
	 * <ul>
	 * <li>It may contain one ampersand in the middle of the sequence
	 * <li>If all characters but the last are uppercase letters, and the last
	 * letter is the lowercase character "s", this also returns true. This
	 * is due to the fact that this method is used to detect acronyms and
	 * sometimes users will pluralise an upper-case acronym by appending a
	 * lower-case s.
	 * </ul>
	 */
	public static boolean isAllCapLetters (String term)
	{	if (term.isEmpty())
			return false;
		
		boolean foundAmp = false;
		for (int i = 0; i < term.length() - 1; i++)
		{	
			char c = term.charAt(i);
			if (c == '&' && ! foundAmp && i > 0 && i < term.length() - 1)
				foundAmp = true;
			else 
				if (! Character.isLetter(c) || Character.isLowerCase(c))
				return false;
		}
	
		char lastChar = term.charAt(term.length() - 1);
		return (lastChar == 's' && term.length() > 1)
				|| (Character.isLetter(lastChar) && Character.isUpperCase(lastChar));
	}

	@Override
	public boolean hasNext() 
	{	return hasNext;
	}

	@Override
	public Pair<TokenType, String> next()
	{	
		if (e != null)
		{	RuntimeException t = e;
			e = null;
			throw t;
		}
	
		String result = term;
		TokenType token = tokenAttr.getType();
		moveToNextToken();
		
//		System.out.println (token + " ---> " + result);
		return Pair.of (token, result);
	}

	/**
	 * Not supported
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
