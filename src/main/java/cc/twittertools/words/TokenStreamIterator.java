package cc.twittertools.words;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * Adaptor to provide an Iterator interface to a TokenStream
 * @author bryanfeeney
 *
 */
public class TokenStreamIterator implements Iterator<String>
{	private final TokenStream toks;
	private final CharTermAttribute charTermAttribute;
	private boolean hasNext;
	private RuntimeException e;

	/* package */ TokenStreamIterator (TokenStream tokenStream, CharTermAttribute charTermAttribute)
	{	this.toks = tokenStream;
		this.charTermAttribute = charTermAttribute;
		moveToNextToken();
	}
	
	/**
	 * Creates a simple instance splitting an input into words. No fancy
	 * processing of text occurs.
	 */
	public static TokenStreamIterator simpleWordIterator (String input)
	{
		TokenStream tok = new StandardTokenizer(Version.LUCENE_36, new StringReader (input));
		CharTermAttribute charTermAttribute = 
			tok.addAttribute(CharTermAttribute.class);
		
		return new TokenStreamIterator(tok, charTermAttribute);
	}

	/**
	 * Moves to the next token. Should that throw an Exception, stores
	 * it, and ensures that hasNext returns true so the exception can
	 * be thrown by next(). Otherwise sets hasNext to return the standard
	 * return value (did we move ahead or not)
	 */
	private void moveToNextToken() {
		try
		{	hasNext = toks.incrementToken();
			if (! hasNext)
				toks.close();
		}
		catch (IOException ioe)
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
	{	if (e != null)
		{	RuntimeException t = e;
			e = null;
			throw t;
		}
	
		String word = charTermAttribute.toString();
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