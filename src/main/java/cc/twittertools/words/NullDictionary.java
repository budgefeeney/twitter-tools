package cc.twittertools.words;

import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.twitter.common.text.token.attribute.TokenType;

/**
 * A dictionary that will always return {@link Dictionary#UNMAPPABLE_WORD} for
 * any given word, and will always have a {@link #size()} and {@link #capacity()}
 * of zero.
 */
public class NullDictionary implements TokenDictionary
{
	public final static NullDictionary INSTANCE = new NullDictionary();

	private NullDictionary()
	{	;
	}
	
	public int toInt(String word)
	{	return Dictionary.UNMAPPABLE_WORD;
	}

	public String toWord(int wordId)
	{	throw new IllegalArgumentException("The given word ID " + wordId + " was never mapped to an integer by this dictionary");
	}

	public void seal()
	{	;
	}

	public int size()
	{	return 0;
	}

	public int capacity()
	{	return 0;
	}

	public int toInt(TokenType tokenType, String word)
	{	return Dictionary.UNMAPPABLE_WORD;
	}

	public Pair<TokenType, String> toWordToken(int wordId)
	{	throw new IllegalArgumentException("The given word ID " + wordId + " was never mapped to an integer by this dictionary");
	}

	public int size(TokenType tokenType)
	{	return 0;
	}

	public int capacity(TokenType tokenType)
	{	return 0;
	}

	@Override
	public void writeAsPythonList (String pyVarName, BufferedWriter writer) throws IOException
	{	writer.write (pyVarName + " = []\n\n");
	}
	
	@Override
	public NullDictionary clone()
	{	return this;
	}
}
