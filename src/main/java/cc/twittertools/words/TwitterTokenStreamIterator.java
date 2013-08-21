package cc.twittertools.words;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;
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
				if (stem)
				{	stemmer.setCurrent(term);
					if (stemmer.stem())
						term = stemmer.getCurrent();
				}
				break;
			}
		}
		catch (Exception ioe)
		{	e = new RuntimeException (ioe.getMessage(), ioe);
			hasNext = true; // so they call next and see the exception
		}
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
		moveToNextToken();
		
		System.out.println ("---> " + result);
		return Pair.of (tokenAttr.getType(), result);
	}

	/**
	 * Not supported
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
