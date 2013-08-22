package cc.twittertools.words;

import org.apache.commons.lang3.tuple.Pair;

import com.twitter.common.text.token.attribute.TokenType;

/**
 * Like the {@link Dictionary} but in this case we also define custom 
 * methods for pairs of {@link TokenType} and words. The behaviour for
 * when one calls one of the inherited methods, which don't specify
 * a token type, is to use a pre-specified default token.
 * @author bryanfeeney
 *
 */
public interface TokenDictionary extends Dictionary
{
	/** Converts a tokenType and word pair to an integer */
	public abstract int toInt(TokenType tokenType, String word);

	/** 
	 * Maps an integer from {@link #toInt(TokenType, String)} back to its
	 * corresponding {@link TokenType}, String pair
	 */
	public abstract Pair<TokenType, String> toWordToken(int wordId);

	/**
	 * The number of words we've observed paired with the given token
	 */
	public abstract int size(TokenType tokenType);

	/**
	 * The number of possible words that can be associated with the
	 * given token. This is essentially the maximum returnable value
	 * from {@link #toInt(TokenType, String)} for a given {@link TokenType}
	 */
	public abstract int capacity(TokenType tokenType);

	/**
	 * Creates a deep copy of this {@link TokenDictionary}
	 */
	public abstract TokenDictionary clone();

}