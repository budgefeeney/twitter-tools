package cc.twittertools.words;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.util.StringUtils;

import cc.twittertools.post.Sigil;

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
 * <p>
 * @author bryanfeeney
 *
 */
public class CompoundTokenDictionary implements TokenDictionary
{
	private final Map<TokenType, Integer> tokenIds =
		new EnumMap<>(TokenType.class);
		
	private final TokenType[] tokens =
		new TokenType[TokenType.values().length];
	
	private final Dictionary[] dicts =
		new Dictionary[TokenType.values().length];
	
	private final int[] cumulativeCapacity =
		new int[TokenType.values().length];
	
	private final TokenType defaultToken;
	private       int numDicts = 0;
	
	public CompoundTokenDictionary(TokenType defaultToken)
	{	this.defaultToken = defaultToken;
	}
	
	protected CompoundTokenDictionary(CompoundTokenDictionary that)
	{	this.defaultToken = that.defaultToken;
		this.tokenIds.putAll (that.tokenIds);
		
		this.numDicts = that.numDicts;
		System.arraycopy (that.cumulativeCapacity, 0, this.cumulativeCapacity, 0, that.numDicts);
		System.arraycopy (that.tokens,             0, this.tokens,             0, that.numDicts);
		for (int token = 0; token < this.numDicts; token++)
			this.dicts[token] = that.dicts[token].clone();
	}
	
	public void addDictionary (TokenType tokenType, Dictionary dictionary)
	{	Integer tokenIdPtr = tokenIds.get(tokenType);
		final int tokenId;
		if (tokenIdPtr == null)
		{	tokenId = numDicts;
			tokenIds.put (tokenType, tokenId);
			tokens[tokenId] = tokenType;
		}
		else
		{	tokenId = tokenIdPtr.intValue();
		}
		
		dicts[tokenId] = dictionary;
		cumulativeCapacity[tokenId] = tokenId == 0
			? dictionary.capacity()
			: cumulativeCapacity[tokenId - 1] + dictionary.capacity();
		
		++numDicts;
	}

	@Override
	public int toInt(String word)
	{	return toInt (defaultToken, word);
	}
	
	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#toInt(com.twitter.common.text.token.attribute.TokenType, java.lang.String)
	 */
	public int toInt(TokenType tokenType, String word)
	{	assert tokenType != null : "Token type should not be null";
		return dicts[id(tokenType)].toInt(word);
	}
	
	@Override
	public String toWord(int wordId)
	{	return toWordToken(wordId).getValue();
	}
	
	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#toWordToken(int)
	 */
	public Pair<TokenType, String> toWordToken (int wordId)
	{	int token = 0;
		while (wordId >= cumulativeCapacity[token])
			++token;
		
		int baseId = token == 0
			? wordId
			: wordId - cumulativeCapacity[token - 1];
		
		return Pair.of(tokens[token], dicts[token].toWord(baseId));
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
	
	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#size(com.twitter.common.text.token.attribute.TokenType)
	 */
	public int size(TokenType tokenType)
	{	return dicts[id(tokenType)].size();
	}

	@Override
	public int capacity()
	{	return numDicts == 0 ? 0 : cumulativeCapacity[numDicts - 1];
	}
	

	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#capacity(com.twitter.common.text.token.attribute.TokenType)
	 */
	public int capacity(TokenType tokenType)
	{	return dicts[id(tokenType)].capacity();
	}
	
	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#clone()
	 */
	@Override
	public CompoundTokenDictionary clone()
	{	return new CompoundTokenDictionary(this);
	}
	
	@Override
	public void writeAsPythonList (String pyVarName, BufferedWriter writer) throws IOException
	{	String[] dictNames = new String[numDicts];
		for (int tokenId = 0; tokenId < numDicts; tokenId++)
		{	dictNames[tokenId] = pyVarName + StringUtils.capitalizeFirstLetter(tokens[tokenId].toString().toLowerCase());
		}
		
		for (int tokenId = 0; tokenId < numDicts; tokenId++)
		{	// find out if the same dictionary object is being used for more
			// than one token type.
			int dictRef = 0;
			while (dictRef < tokenId)
				if (dicts[dictRef] == dicts[tokenId] 
						|| (dicts[dictRef] instanceof SigilStrippingDictionary 
								&& ((SigilStrippingDictionary) dicts[dictRef]).getDictionary() == dicts[tokenId]))
					break;
				else
					dictRef++;
			
			// If this is just a reference to the previously written dictionary object,
			// just write out in Python a reference to the previously written object, 
			// otherwise write out the new dictionary from scratch
			if (dictRef != tokenId)
				writer.write(dictNames[tokenId] + " = " + dictNames[dictRef]);
			else
				dicts[tokenId].writeAsPythonList(dictNames[tokenId], writer);
		}
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
