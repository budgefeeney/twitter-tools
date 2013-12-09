package cc.twittertools.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import static org.junit.Assert.*;

public class CondenseTest
{
	private final static String[] WORDS = new String[] {
		"bad",
		"new",
		":(",
		"i'v",
		"realis",
		"financi",
		"f/c",
		"wrong",
		"3/4",
		"http://bit.ly/3n32ds2",
		"tech",
		"girl",
	};
	
	private final static int[] COUNTS = new int[] {
		2,
		16,
		3,
		6,
		18,
		5,
		37,
		3,
		14,
		16,
		43,
		7,
	};
	
	private Path inFileUnsorted;
	private Path inFileSorted;
	private Path outFile;
	
	@Before
	public void setUp() throws Exception
	{	inFileUnsorted = preDeletedTmpFile();
		inFileSorted   = preDeletedTmpFile(); 
		outFile        = preDeletedTmpFile();
		
		try (BufferedWriter wtr = Files.newBufferedWriter(inFileSorted,   Charsets.UTF_8);)
		{	for (int w = 0; w < WORDS.length; w++)
				for (int c = 0; c < COUNTS.length; c++)
					wtr.write(WORDS[w] + '\n');
		}
		
		try (BufferedWriter wtr = Files.newBufferedWriter(inFileUnsorted, Charsets.UTF_8);)
		{	int[] counts = Arrays.copyOf(COUNTS, COUNTS.length);
			for (int i = 0; i < max(counts); i++)
				for (int w = 0; w < WORDS.length; w++)
					if (counts[w] > 0)
					{	--counts[w];
						wtr.write(WORDS[w] + '\n');
					}
		}
	}

	private final static int max(int[] values)
	{	int result = Integer.MIN_VALUE;
		for (int value : values)
			result = Math.max(result, value);
		return result;
	}

	private final static Path preDeletedTmpFile() throws IOException
	{	File file = File.createTempFile("wordList-", ".txt");
		file.deleteOnExit();
		return file.toPath();
	}
	
	@After
	public void tearDown() throws Exception
	{	try
		{	Files.deleteIfExists(inFileSorted);
		}
		finally
		{	try
			{	Files.deleteIfExists(inFileUnsorted);
			}
			finally
			{	Files.deleteIfExists(outFile);
			}
		}
	}
	
	
	@Test
	public void testSorted() throws Exception
	{	Condense.main(new String[] {
			"-i", inFileSorted.toString(),
			"-o", outFile.toString(),
			"-z", "15",
			"-s"
		});
	
		verifyDict (outFile);
	}
	
	@Test
	public void testUnorted() throws Exception
	{	Condense.main(new String[] {
			"-i", inFileUnsorted.toString(),
			"-o", outFile.toString(),
			"-z", "15"
		});
	
		verifyDict (outFile);
	}

	private void verifyDict(Path file) throws IOException
	{	String line = null;
		
		Set<String> words = new HashSet<>();
		for (String word : WORDS)
			words.add (word);
		
		try (BufferedReader rdr = Files.newBufferedReader(file, Charsets.UTF_8);)
		{	while ((line = rdr.readLine()) != null)
			{	String[] parts = StringUtils.split(line, '\t');
				String word  = parts[0];
				int    count = Integer.parseInt(parts[1]);
				
				assertTrue (words.contains(word));
				int i = ArrayUtils.indexOf(WORDS, word);
				assertEquals (COUNTS[i], count);
				
				words.remove(word);
			}
		}
		
		assertTrue(words.isEmpty());
	}
	
}
