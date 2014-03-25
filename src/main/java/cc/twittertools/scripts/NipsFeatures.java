package cc.twittertools.scripts;

import it.unimi.dsi.fastutil.ints.Int2ShortArrayMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import ucl.feeney.bryan.numpy.CsrShortMatrixBuilder;
import cc.twittertools.words.dict.LookupDictionary;

/**
 * An ad-hoc script to create dictionaries from the the list of tags (e.g. authors,
 * categories or references) associated with each NIPS document.
 * @author bryanfeeney
 *
 */
public class NipsFeatures {
	private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
	private final static short ONE = 1;
	private static final int MAX_TAGS_APPROX = 30;
	private final static String IN_PATH  = "/Users/bryanfeeney/Workspace/TextModelling/LdaJava/trunk/src/main/resources/corpora/nipsish";
	private final static String OUT_PATH = "/Users/bryanfeeney/Desktop/NIPS";
	
	
	private NipsFeatures() {
		
	}
	
	public static void main (String[] args) throws Exception
	{	genDictsAndMatrices(IN_PATH, OUT_PATH);
	}
	
	/**
	 * Generates all dictionary files (including a single Python dictionary files) and all
	 * matrix files from all possible tag files
	 * @param inPath the folder where we expect to find the authors, refs and cats tag files
	 * @param outPath the folder where the output will be stored
	 * @throws Exception
	 */
	public static void genDictsAndMatrices(String inPath, String outPath) throws Exception
	{	List<String> pyDictVars = new ArrayList<>(6);
		String pyDict;
		
		Path pyDictsFile = Paths.get(outPath).resolve("feats.py");
		try (BufferedWriter pyDictWtr = Files.newBufferedWriter(pyDictsFile, DEFAULT_CHARSET);)
		{
			for (String tagFile : new String[] { "authors.txt", "cats.txt", "refs.txt" })
			{	for (int minOccurenceCount : new int[] { 1, 4 })
				{	System.out.print ("Processing " + tagFile + " (" + minOccurenceCount + ")... "); System.out.flush();
					
					String dictName = tagFile.replace(".txt", ".dict");
					genDict(inPath, tagFile, outPath, dictName);
					pyDict = genFeatureMatrix(inPath, tagFile, outPath, dictName, pyDictWtr, minOccurenceCount);
					
					pyDictVars.add(pyDict);
					System.out.println ("Done");
				}
			}
			
			pyDictWtr.write('\n');
			pyDictWtr.write("dictNames = ['" + StringUtils.join(pyDictVars, "','") + "']\n");
		}
		
	}
	
	
	/**
	 * Converts a space delimited list of tags for filenames (e.g. author IDs, reference
	 * IDs) to a matrix, using a pre-generated dictionary
	 * @param inputDir the directory in which the input file is found
	 * @param inputFname the name of the input file within that directory
	 * @param outputDir the direcator in which the outputs will be placed
	 * @param dictFname the name of the dictionary being used - the names of the matrix
	 * files are derived from this by replacing the ".dict" suffix
	 * @param pyDictWriter the open writer object to which the python dictionaries are
	 * written. The dictionary variable name is the dictFname (as above) minus the ".dict"
	 * suffix, appended by the value of <code>minOccurrenceCount</code> (assuming it's not 0)
	 * @param minOccurrenceCount when loading a dictionary from a file of terms and global
	 * term-counts, specifies which terms will be excluded from the dictionary. This is
	 * an inclusive lower-bound, a term is included if it's count is geq this value.
	 * @return the name assigned to the dictionary variable in the Python script where
	 * the dictionaries are stored.
	 * @throws Exception if an error occurs when writing the matrix to a file, or the 
	 * related Python dictionary to a file.
	 */
	public static String genFeatureMatrix(String inputDir, String inputFname, String outputDir, String dictFname, BufferedWriter pyDictWriter, int minOccurrenceCount) throws Exception
	{	// Determine the filenames
		String minOccSuffix = minOccurrenceCount > 1 ? String.valueOf(minOccurrenceCount) : "";
		
		Path inFile   = Paths.get(inputDir).resolve(inputFname);
		Path dictFile = Paths.get(outputDir).resolve(dictFname);
		Path matFilePrefix = Paths.get(outputDir).resolve(inputFname.replace(".txt", minOccSuffix));
		
		// Load in the input and the dictionary.
		List<String> tagLines = Files.readAllLines(inFile, DEFAULT_CHARSET);
		Collections.sort(tagLines); // for consistency, we ensure filenames
		                            // are ordered alphabetically.
		LookupDictionary dict = LookupDictionary.fromFile(dictFile, minOccurrenceCount);
		
		// Convert the input to a matrix.
		Int2ShortArrayMap vector = new Int2ShortArrayMap(MAX_TAGS_APPROX);
		CsrShortMatrixBuilder matrix = new CsrShortMatrixBuilder(dict.size());
		for (String tagLine : tagLines)
		{	vector.clear();
			String[] tags = tagLine.split("\\s+");
			for (int t = 1; t < tags.length; t++) // first "tag" is actually the filename
			{	vector.put(dict.toInt(tags[t]), ONE);
			}
			matrix.addRow(vector);
		}
		
		// Write the matrix out and print out a few stats
		matrix.writeToFile(matFilePrefix);
		String pyDictName = dictFname.replace(".dict", minOccSuffix);
		dict.writeAsPythonList(pyDictName, pyDictWriter);
		System.out.println ("Wrote a " + matrix.getRows() + " x " + matrix.getCols() + " matrix to " + matFilePrefix.toString() + ".pkl");
	
		return pyDictName;
	}
	
	/**
	 * Reads in a tags file, and generates a dictionary file for each tag. The dictionary file
	 * is simply a tab-delimited file of tag and corpus with tag-count.
	 * @param inputDir the directory in wihch the tag file is to be found
	 * @param tagsFname the name of that tag file in that input director
	 * @param outputDir the directory into which the dictionary is to be saved
	 * @param dictFname the name of the dictionary file in that output directory
	 * @throws IOException
	 */
	public static void genDict(String inputDir, String tagsFname, String outputDir, String dictFname) throws IOException
	{	Path outPath = Paths.get(outputDir);
		Path inPath  = Paths.get(inputDir).resolve(tagsFname);
		
		SortedMap<String, MutableInt> counts = new TreeMap<>();
		
		try (
			BufferedReader  in = Files.newBufferedReader(inPath, DEFAULT_CHARSET);
		)
		{	String line = null;
			while ((line = in.readLine()) != null)
			{	if ((line = line.trim()).isEmpty())
					continue;
				
				// note the first part is the filename
				String[] parts = line.split("\\s+");
				for (int i = 1; i < parts.length; i++)
				{	inc (counts, parts[i]);
				}
			}
		}
	
		try (
			BufferedWriter out = Files.newBufferedWriter(outPath.resolve(dictFname), DEFAULT_CHARSET);
		)
		{	for (Map.Entry<String, MutableInt> entry : counts.entrySet())
			{	out.write(entry.getKey());
				out.write('\t');
				out.write (String.valueOf(entry.getValue()));
				out.write('\n');
			}
		}
	}
	
	private final static <K> void inc(Map<K, MutableInt> counts, K key)
	{	MutableInt count = counts.get(key);
		if (count == null)
		{	count = new MutableInt(0);
			counts.put(key, count);
		}
		count.increment();
	}

}
