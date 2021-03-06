package cc.twittertools.words.dict;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Interface for class's which maps words to ints and optionally back again.
 * Use {@link #toInt(String)} to map a word to an integer.
 * User {@link #capacity()} to figure out how big your input vector should be
 * 
 * @author bryanfeeney
 *
 */
public interface Dictionary {

	/** Dictionary can't map this word to an ID number. */
	public static final int UNMAPPABLE_WORD = -1;
	
	/** 
	 * Dictionary states that this word should be ignored. This is
	 * separate from the normal stop-word mechanism, and is used for
	 * programmatic rather than lookup dictionaries
	 */
	public static final int IGNORABLE_WORD = -2;
	
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
	 * Writes out this dictionary in the format of a Python literal variable
	 * declaration. The literal is an array of dictionary keys, ordered according
	 * to the associated integer. The name of this literal is given by
	 * <tt>pyVarName</tt>
	 */
	public void writeAsPythonList (String pyVarName, BufferedWriter writer) throws IOException;
	
	/** 
	 * A deep copy of this dictionary
	 */
	public Dictionary clone();

	/**
	 * Writes the dictioary out as a tab delimited file, with the
	 * tokens in the first column, and the IDs in the second.
	 * @param path the path to write the dictionary to
	 * @param charset the charset to use when writing out the dictionary
	 */
	public void writeDelimited(Path path, Charset charset) throws IOException;

	/**
	 * Writes the dictioary out as a tab delimited file, with the
	 * tokens in the first column, and the IDs in the second.
	 * @param wtr the write to write to
	 * @param prefix if not null, this is written out ahead of the other
	 * columns.
	 */
	public void writeDelimited(BufferedWriter wtr, String prefix) throws IOException;

}
