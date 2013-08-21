package cc.twittertools.words;

import java.util.EnumMap;
import java.util.Map;

import com.twitter.common.text.token.attribute.TokenType;

/**
 * A non-threadsafe dictionary mapping pairs of token and word to IDs. This 
 * essentially is a map {@link TokenType} to {@link LookupDictionary} with
 * a useful {@link Dictionary} style API applied on top.
 * <p>
 * If no token is specified, we use the default {@link TokenType} specified
 * in the constructor. You can set this to null if you want, it just means
 * that we'll throw an exception should you call the vanilla {@link #toInt(String)}
 * method.
 * @author bryanfeeney
 *
 */
public class TokenDictionary implements Dictionary
{
	private final Map<TokenType, Integer> tokenIds =
		new EnumMap<>(TokenType.class);
	
	private final Dictionary[] dicts =
		new Dictionary[TokenType.values().length];
	
	private final int[] cumulativeCapacity =
		new int[TokenType.values().length];
	
	private final TokenType defaultToken;
	private       int numDicts = 0;
	
	public TokenDictionary(TokenType defaultToken)
	{	this.defaultToken = defaultToken;
	}
	
	protected TokenDictionary(TokenDictionary that)
	{	this.defaultToken = that.defaultToken;
		this.tokenIds.putAll (that.tokenIds);
		
		this.numDicts = that.numDicts;
		System.arraycopy (that.cumulativeCapacity, 0, this.cumulativeCapacity, 0, that.numDicts);
		for (int token = 0; token < this.numDicts; token++)
			this.dicts[token] = that.dicts[token].clone();
	}
	
	public void addDictionary (TokenType tokenType, Dictionary dictionary)
	{	Integer tokenIdPtr = tokenIds.get(tokenType);
		final int tokenId;
		if (tokenIdPtr == null)
		{	tokenId = numDicts;
			tokenIds.put (tokenType, tokenId);
		}
		else
		{	tokenId = tokenIdPtr.intValue();
		}
		
		dicts[tokenId] = dictionary;
		cumulativeCapacity[tokenId] = tokenId == 0
			? dictionary.capacity()
			: cumulativeCapacity[tokenId - 1] + dictionary.capacity();
	}

	@Override
	public int toInt(String word)
	{	return toInt (defaultToken, word);
	}
	
	public int toInt(TokenType tokenType, String word)
	{	assert tokenType != null : "Token type should not be null";
		return dicts[id(tokenType)].toInt(word);
	}
	
	@Override
	public String toWord(int wordId)
	{	int token = 0;
		while (wordId >= cumulativeCapacity[token])
			++token;
		
		int baseId = token == 0
			? wordId
			: wordId - cumulativeCapacity[token - 1];
		
		return dicts[token].toWord(baseId);
	}
	
	@Override
	public void seal()
	{	for (int token = 0; token < numDicts; token++)
			dicts[token].seal();
	}

	@Override
	public int size()
	{	int size = 0;
		for (int token = 0; token < numDicts; token++)
			size += dicts[token].size();
		return size;
	}
	
	public int size(TokenType tokenType)
	{	return dicts[id(tokenType)].size();
	}

	@Override
	public int capacity()
	{	return cumulativeCapacity[numDicts - 1];
	}
	

	public int capacity(TokenType tokenType)
	{	return dicts[id(tokenType)].capacity();
	}
	
	@Override
	public TokenDictionary clone()
	{	return new TokenDictionary(this);
	}

	/**
	 * Dictionaries and their capacity are stored in arrays. This maps a
	 * {@link TokenType} to an index into those arrays. It throws a
	 * sensible exception if the given token type can't be found in the
	 * array.
	 */
	private final int id (TokenType tokenType)
	{	Integer tokenId = tokenIds.get(tokenType);
		if (tokenId == null)
			throw new IllegalArgumentException ("No dictionary defined for token " + tokenType);
		
		return tokenId.intValue();
	}
}
