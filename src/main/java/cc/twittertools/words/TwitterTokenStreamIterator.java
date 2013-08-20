package cc.twittertools.words;

import java.util.Iterator;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.util.Version;

import com.twitter.common.text.token.TokenStream;
import com.twitter.common.text.token.attribute.CharSequenceTermAttribute;

/**
 * Represents a Twitter implementation of {@link TokenStream} as an iterator
 * over strings, essentially presenting a unified facade to token streams
 * (see {@link TokenStreamIterator} for Lucene).
 * @author bryanfeeney
 *
 */
public class TwitterTokenStreamIterator implements Iterator<String>
{
	private final TokenStream toks;
	private final CharSequenceTermAttribute charTermAttribute;
	
	private boolean hasNext;
	private RuntimeException e;

	public TwitterTokenStreamIterator(TokenStream tokenStream)
	{	super();
		this.toks = tokenStream;
		this.charTermAttribute = tokenStream.getAttribute(CharSequenceTermAttribute.class);
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
		{	hasNext = toks.incrementToken();
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
	public String next()
	{	
		StopAnalyzer stopFilter = new StopAnalyzer(Version.LUCENE_36);
		PorterStemFilter stemmer = new Porter
		
		
		if (e != null)
		{	RuntimeException t = e;
			e = null;
			throw t;
		}
	
		String word = charTermAttribute.getTermString();
		moveToNextToken();
		return word;
	}

	/**
	 * Not supported
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
