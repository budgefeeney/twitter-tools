package cc.twittertools.words;

import java.text.BreakIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.twitter.common.text.token.TokenStream;
import com.twitter.common.text.token.attribute.CharSequenceTermAttribute;
import com.twitter.common.text.token.attribute.TokenType;
import com.twitter.common.text.token.attribute.TokenTypeAttribute;

/**
 * Tokenizes text, in a manner compatible with the Twitter
 * {@link TokenStream} interface, using the Java {@link BreakIterator}
 * class, which does a substantially better job than the Twitter
 * approach.
 */
public class BreakTokenizer extends TokenStream {

	  private BreakIterator iter;
	  private int tokenStart;
	  private int tokenEnd;

	  private CharSequenceTermAttribute termAttr;
	  private TokenTypeAttribute typeAttr;

	  // please use Builder instead.
	  protected BreakTokenizer() {
	    termAttr = addAttribute(CharSequenceTermAttribute.class);
	    typeAttr = addAttribute(TokenTypeAttribute.class);
	  }


	  @Override
	  public boolean incrementToken() {
		String DEBUG = null;
		do
		{	if (tokenEnd == BreakIterator.DONE)
			  return false;

	    	termAttr.setOffset(tokenStart);
	    	termAttr.setLength(tokenEnd - tokenStart);
	    	
	    	DEBUG = termAttr.getTermString(); 
	    	typeAttr.setType(isPunc(DEBUG) ? TokenType.PUNCTUATION : TokenType.TOKEN);
	    	
		    tokenStart = tokenEnd;
		    tokenEnd   = iter.next();
		} while (isWhitespace(DEBUG));
	    

	    return true;
	  }
	  
	  private final static boolean isWhitespace(String str)
	  {	for (int i = 0; i < str.length(); i++)
		  if (! Character.isWhitespace(str.charAt(i)))
			  return false;
		return true;
	  }
	  
	  private final static Pattern PUNC = Pattern.compile("\\p{P}+");
	  private final static boolean isPunc(String str)
	  {	return PUNC.matcher(str).matches();
	  }

	  @Override
	  public void reset(CharSequence input) {
	    termAttr.setCharSequence(input);
	    
	    String inputStr = input instanceof String
	    	? (String) input
	    	: new StringBuilder(input.length()).append(input).toString();
	    iter = BreakIterator.getWordInstance();
	    iter.setText(inputStr);
	    tokenStart = iter.first();
	    tokenEnd   = iter.next();
	  }

	  /**
	   * Builder for BreakTokenizer.
	   */
	  public static final class Builder extends AbstractBuilder<BreakTokenizer, Builder> {
	    public Builder() {
	      super(new BreakTokenizer());
	    }
	  }

	  public abstract static class
	      AbstractBuilder<N extends TokenStream, T extends AbstractBuilder<N, T>> {
	    private final N tokenizer;

	    protected AbstractBuilder(N tokenizer) {
	      this.tokenizer = Preconditions.checkNotNull(tokenizer);
	    }

	    @SuppressWarnings("unchecked")
	    protected T self() {
	      return (T) this;
	    }


	    /**
	     * Specifies whether to keep punctuations (which is specified
	     * by delimiterPattern and punctuationGroupInDelimiterPattern)
	     * in the output token stream.
	     *
	     * @param keepPunctuation true to keep delimiters. false otherwise.
	     * @return this Builder object.
	     */
	    public T setKeepPunctuation(boolean keepPunctuation) {
	      //tokenizer.setKeepPunctuation(keepPunctuation);
	      return self();
	    }

	    public N build() {
	      return tokenizer;
	    }
	  }

}
