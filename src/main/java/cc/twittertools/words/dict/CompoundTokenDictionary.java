package cc.twittertools.words.dict;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
	
	// There arrays are one-indexed so we can always do one-minus tokenId
	// on the cumulative capacity
	private final TokenType[] tokens =
		new TokenType[TokenType.values().length + 1];
	
	private final Dictionary[] dicts =
		new Dictionary[TokenType.values().length + 1];
	
	private final int[] cumulativeCapacity =
		new int[TokenType.values().length + 1];
	
	private final TokenType defaultToken;
	private       int numDicts = 0;
	
	public CompoundTokenDictionary(TokenType defaultToken)
	{	this.defaultToken = defaultToken;
	}
	
	protected CompoundTokenDictionary(CompoundTokenDictionary that)
	{	this.defaultToken = that.defaultToken;
		this.tokenIds.putAll (that.tokenIds);
		
		this.numDicts = that.numDicts;
		System.arraycopy (that.cumulativeCapacity, 0, this.cumulativeCapacity, 0, that.numDicts + 1);
		System.arraycopy (that.tokens,             0, this.tokens,             0, that.numDicts + 1);
		for (int token = 1; token <= this.numDicts; token++)
			this.dicts[token] = that.dicts[token].clone();
	}
	
	public void addDictionary (TokenType tokenType, Dictionary dictionary)
	{	Integer tokenIdPtr = tokenIds.get(tokenType);
		final int tokenId;
		if (tokenIdPtr == null)
		{	tokenId = numDicts + 1;
			tokenIds.put (tokenType, tokenId);
			tokens[tokenId] = tokenType;
		}
		else
		{	tokenId = tokenIdPtr.intValue() + 1;
		}
		
		dicts[tokenId] = dictionary;
		cumulativeCapacity[tokenId] = cumulativeCapacity[tokenId - 1] + dictionary.capacity();
		
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
	
		int tokenTypeId = id(tokenType);
		int wordId      = dicts[tokenTypeId].toInt(word);
		
		return cumulativeCapacity[tokenTypeId - 1] + wordId;
	}
	
	@Override
	public String toWord(int wordId)
	{	return toWordToken(wordId).getValue();
	}
	
	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#toWordToken(int)
	 */
	public Pair<TokenType, String> toWordToken (int wordId)
	{	int token = 1;
		while (wordId >= cumulativeCapacity[token])
			++token;
		
		int baseId = wordId - cumulativeCapacity[token - 1];
		
		return Pair.of(tokens[token], dicts[token].toWord(baseId));
	}
	
	@Override
	public void seal()
	{	for (int token = 1; token <= numDicts; token++)
			dicts[token].seal();
	}

	@Override
	public int size()
	{	int size = 0;
		for (int token = 1; token <= numDicts; token++)
			size += dicts[token].size();
		return size;
	}
	
	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#size(com.twitter.common.text.token.attribute.TokenType)
	 */
	@Override
	public int size(TokenType tokenType)
	{	return dicts[id(tokenType)].size();
	}

	@Override
	public int capacity()
	{	return cumulativeCapacity[numDicts];
	}
	

	/* (non-Javadoc)
	 * @see cc.twittertools.words.TokenDictionary#capacity(com.twitter.common.text.token.attribute.TokenType)
	 */
	@Override
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
	{	String[] dictNames = new String[numDicts + 1];
		for (int tokenId = 1; tokenId <= numDicts; tokenId++)
		{	dictNames[tokenId] = pyVarName + ucFirst(tokens[tokenId].toString());
		}
		
		for (int tokenId = 1; tokenId <= numDicts; tokenId++)
		{	// find out if the same dictionary object is being used for more
			// than one token type.
			int dictRef = 1;
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
	 * Given a string, returns the same string, with the first character in 
	 * upper-case, and all subsequent characters in lower-case.
	 * @param string
	 * @return
	 */
	/* pkg */ static final String ucFirst(String string)
	{	if (StringUtils.isEmpty(string))
			return string;
		
		StringBuilder sb = new StringBuilder(string.length());
		sb.append (Character.toUpperCase(string.charAt(0)));
		
		for (int i = 1; i < string.length(); i++)
			sb.append (Character.toLowerCase(string.charAt(i)));
		
		return sb.toString();
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
