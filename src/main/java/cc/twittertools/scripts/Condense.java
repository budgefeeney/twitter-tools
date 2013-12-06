package cc.twittertools.scripts;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.io.Charsets;
import org.kohsuke.args4j.Option;

/**
 * Given a file with a list of words, one word per line, tab-delimited file of
 * word counts.
 * @author bryanfeeney
 *
 */
public class Condense implements Callable<Integer>
{
	private final static String NULL_WORD = "_bryan_feeney_is_a_programmer_and_not_a_word";
	
	
	@Option(name="-i", aliases="--input", usage="The input file with one word per line", metaVar=" ")
	private String inputPath;
	
	@Option(name="-o", aliases="--output", usage="The output file, tab delimited of words and counts", metaVar=" ")
	private String outputPath;
	
	@Option(name="-z", aliases="--size-hint", usage="Estimate of how many distinct words exist in the input", metaVar=" ")
	private static int sizeHint = 10_000_000;
	
	@Option(name="-s", aliases="--sorted", usage="Has this file been sorted in advance", metaVar=" ")
	private boolean sorted = false;
	
	private Condense()
	{	
	}
	
	/**
	 * parses the command-line arguments
	 */
	public void parseArguments(String[] args)
	{
		
	}
	
	/**
	 * Given an input file of words, one per line, writes out a tab-delimited
	 * file of word-counts
	 */
	public Integer call() throws Exception
	{	return sorted ? callSorted() : callUnsorted();
	}
	
	private Integer callSorted() throws Exception
	{	String lastWord = NULL_WORD;
		String word = null;
		int count = 0;
		int wordCount = 0;
		
		try (BufferedReader rdr = Files.newBufferedReader(Paths.get(inputPath), Charsets.UTF_8);
				 BufferedWriter wtr = Files.newBufferedWriter(Paths.get(outputPath), Charsets.UTF_8);)
		{	
			while ((word = rdr.readLine()) != null)
			{	if (! word.equals(lastWord))
				{	if (word != NULL_WORD)
					{	wtr.write(lastWord + '\t' + count + '\n');
						++wordCount;
					}
					lastWord = word;
					count = 0;
				}
				++count;
			}
		}
		
		return wordCount;
	}
	
	private Integer callUnsorted() throws Exception
	{	String word = null;
		Object2IntMap<String> map = new Object2IntOpenHashMap<>(sizeHint);
		map.defaultReturnValue(0);
		
		try (BufferedReader rdr = Files.newBufferedReader(Paths.get(inputPath), Charsets.UTF_8);)
		{	
			while ((word = rdr.readLine()) != null)
			{	map.put(word, map.get(word) + 1);
			}
		}
		
		try (BufferedReader rdr = Files.newBufferedReader(Paths.get(inputPath), Charsets.UTF_8);)
		{	
			while ((word = rdr.readLine()) != null)
			{	map.put(word, map.get(word) + 1);
			}
		}
		
		return map.size();
	}

	
	public static final void main(String[] args) throws Exception
	{	Condense c = new Condense();
		c.parseArguments(args);
		c.call();
	}
}