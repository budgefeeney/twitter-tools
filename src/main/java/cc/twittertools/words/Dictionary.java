package cc.twittertools.words;

/**
 * Interface for class's which maps words to ints and optionally back again.
 * Use {@link #toInt(String)} to map a word to an integer.
 * User {@link #capacity()} to figure out how big your input vector should be
 * 
 * @author bryanfeeney
 *
 */
public interface Dictionary {

	public static final int UNMAPPABLE_WORD = -1;
	
	/** 
	 * Converts a word to an integer. Does not clean the word, we assume
	 * the word is clean already. May return {@link #UNMAPPABLE_WORD} if
	 * the input is not a word, or the dictionary can't be updated to 
	 * accomodated it.
	 * @param a pre-cleaned word to convert to an int
	 * @return the integer Id for that word
	 */
	public int toInt (String word);
	
	/**
	 * The converse of {@link #toWord(String)}. May not be implemented
	 * in all dictionaries.
	 */
	public String toWord (int wordId);
	
	/**
	 * Some dictionaries may alter themselves to accommodate new words.
	 * While this is good for training, it's not the desired behaviour for
	 * testing. Therefore this method will stop any further updates,
	 * making the dictionary an immutable data-structure, and should be
	 * called after creating the training dataset, so the dictionary
	 * can be safely employed thereafter for test.
	 */
	public void seal();
	
	/**
	 * Number of words in this dictionary. May not be implemented in all
	 * dictionaries
	 */
	public int size();
	
	/**
	 * Maximum number of words allowed in this dictionary (recall some
	 * dictionaries will grow to accomodated new words as and when they
	 * arrive.
	 */
	public int capacity();
	
	/** 
	 * A deep copy of this dictionary
	 */
	public Dictionary clone();
}
