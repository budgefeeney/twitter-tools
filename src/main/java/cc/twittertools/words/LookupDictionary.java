package cc.twittertools.words;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;



/**
 * A non-threadsafe dictionary maintaining a lookup table of words to word IDs,
 * and a matching table of word IDs to words.
 * @author bryanfeeney
 *
 */
public class LookupDictionary extends AbstractDictionary 
{

	private static final int MAX_INITIAL_CAPACITY = 10000;
	private final Map<String, Integer> words;
	private final Map<Integer, String> wordIds;
	private boolean sealed = false;
	
	/**
	 * Create a new lookup dictionary
	 * @param capacity the maximum size of this dictionary
	 */
	public LookupDictionary(int capacity) 
	{
		super(capacity);
		int initialSize = Math.min (MAX_INITIAL_CAPACITY, capacity);
		words   = new HashMap<String, Integer>(initialSize);
		wordIds = new HashMap<Integer, String>(initialSize);
	}
	
	/**
	 * Create a copy of the given dictionary
	 */
	protected LookupDictionary (LookupDictionary that)
	{	super (that);
		int initialSize = Math.min (that.wordIds.size(), that.capacity);
		this.words = new HashMap<String, Integer>(initialSize);
		this.words.putAll (that.words);
		
		this.wordIds = new HashMap<Integer, String>(initialSize);
		this.wordIds.putAll (that.wordIds);
	}

	@Override
	public int toInt(String word) 
	{
		if (StringUtils.isBlank(word))
			return UNMAPPABLE_WORD;
		Integer i = words.get(word);
		if (i != null)
		{	return i.intValue();
		}
		else // if (i == null) 
		{	if (! canAddNewWords()) 
			{	return UNMAPPABLE_WORD;
			}
			else 
			{	final int wordIndex = size();
				words.put(word, wordIndex);
				wordIds.put (wordIndex, word);
				return wordIndex;
			}
		}	
	}
	
	@Override
	public void seal()
	{	this.sealed = true;
	}

	/**
	 * Returns true if we can add more words to the dictionary, false otherwise.
	 * Cases where we can't add more words are if the dictionary has been
	 * sealed ({@link #seal()} or has hit it's maximum word limit.
	 */
	private boolean canAddNewWords() {
		return !sealed && size() < capacity;
	}

	@Override
	public String toWord(int wordId) {
		if (wordId < 0 || wordId >= size()) throw new IllegalArgumentException ("Word ID must be in the range [0.." + (size() - 1) + "]");
			return wordIds.get(wordId);
	}

	@Override
	public int size() {
		return words.size();
	}
	
	@Override
	public void writeAsPythonList (String pyVarName, BufferedWriter writer) throws IOException
	{	int size = size();
		if (size == 0)
		{	writer.write (pyVarName + " = [ ]\n");
			return;
		}
		
		writer.write (pyVarName + " = [ \\\n");
		for (int wordId = 0; wordId < size; wordId++)
		{	writer.write ("\t\"" + StringEscapeUtils.escapeJava(toWord(wordId)) + "\" \\\n");
		}
		writer.write ("\t]\n\n");
	}
	
	@Override
	public LookupDictionary clone()
	{	return new LookupDictionary (this);
	}

}
