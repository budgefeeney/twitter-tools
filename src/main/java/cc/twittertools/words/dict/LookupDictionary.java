package cc.twittertools.words.dict;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.Arrays;



/**
 * A non-threadsafe dictionary maintaining a lookup table of words to word IDs,
 * and a matching table of word IDs to words.
 */
public class LookupDictionary extends AbstractDictionary 
{
	private final static Logger LOG = LoggerFactory.getLogger(LookupDictionary.class);
	
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
	
	/**
	 * Load a dictionary from a file. The last two columns of a file should
	 * be a word and a frequency count respectively. It's okay if the word
	 * occurs several times, in these cases the frequencies are summed. 
	 * Eventually all words whose frequency is greater than or equal to
	 * the limit are taken to create a sealed dictionary
	 */
	public static LookupDictionary fromFile (Path file, int minOccurrenceCount) throws IOException
	{	Map<String, MutableInt> wordFreqs = new HashMap<>(4_000_000);
		String line = null;
		int lineCount = 0;
		
		try (BufferedReader rdr = Files.newBufferedReader(file, Charsets.UTF_8); )
		{	while ((line = rdr.readLine()) != null)
			{	++lineCount;
				if ((line = line.trim()).isEmpty())
					continue;
				if (line.length() < 3)
				{	System.err.println ("Invalid line at line number " + lineCount + " : " + line);
					continue;
				}
				
				int lastTab  = line.lastIndexOf('\t');
				
				if (lastTab < 0)
				{	System.err.println ("Invalid line:" + lineCount + " '" + line + "'");
					continue;
				}
				
				int penulTab = Math.max (-1, line.lastIndexOf('\t', lastTab - 1));
				
				String count = line.substring(lastTab + 1);
				if (penulTab < -1 || lastTab < 0)
					System.out.println ("Whoa");
				String word  = line.substring(penulTab + 1, lastTab);
				
				MutableInt freq = wordFreqs.get (word);
				if (freq == null)
				{	freq = new MutableInt(0);
					wordFreqs.put (word, freq);
				}
				else
				{	//System.out.println ("Repeated word " + word);
				}
				freq.add (Integer.valueOf(count));
			}
		}
		catch (IOException ioe)
		{	throw ioe;
		}
		catch (Exception e)
		{	throw new IOException ("Failed to parse file at line " + lineCount + ". Line was '" + line + "'. Error was " + e.getMessage(), e);
		}
		
		int dictSize = 0;
		for (MutableInt freq : wordFreqs.values())
			if (freq.intValue() >= minOccurrenceCount)
				++dictSize;
		
		LookupDictionary dict = new LookupDictionary(dictSize);
		for (Map.Entry<String, MutableInt> entry : wordFreqs.entrySet())
			if (entry.getValue().intValue() >= minOccurrenceCount)
				dict.toInt(entry.getKey());
		
		dict.seal();
		wordFreqs = null;
		System.gc();
		
		return dict;
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
		
		writer.write (pyVarName + "_len = " + size + '\n');
		writer.write (pyVarName + " = [ \\\n");
		String word = null;
		try
		{	
			for (int wordId = 0; wordId < size; wordId++)
			{	word = toWord(wordId);
				writer.write ("\t\"" + StringEscapeUtils.escapeJava(word) + "\", \\\n");
				word = null; // for debugging in the exception below
			}
		}
		catch (Exception e)
		{	String wordUtf8Bytes = word == null
				? "null"
				: Arrays.toString(Hex.encodeHex(word.getBytes(Charsets.UTF_8)));
			LOG.error("Error whilst writing out the " + pyVarName + " dictionary to a Python file : " + e.getMessage() + ".  The token, rendered as UTF-8 bytes, encoded in hex, is " + wordUtf8Bytes);
		}
		writer.write ("\t]\n\n");
		writer.flush();
	}
	
	@Override
	public LookupDictionary clone()
	{	return new LookupDictionary (this);
	}
	
	@Override
	public void writeDelimited(Path path, Charset charset) throws IOException
	{	try (BufferedWriter wtr = Files.newBufferedWriter(path, charset);)
		{	writeDelimited(wtr, null);
		}
	}
	
	@Override
	public void writeDelimited(BufferedWriter wtr, String prefix) throws IOException
	{	if (prefix == null)
			for (Map.Entry<String, Integer> entry : words.entrySet())
				wtr.write(entry.getKey() + '\t' + entry.getValue() + '\n');
		else
			for (Map.Entry<String, Integer> entry : words.entrySet())
				wtr.write(prefix + '\t' + entry.getKey() + '\t' + entry.getValue() + '\n');
	}

}
