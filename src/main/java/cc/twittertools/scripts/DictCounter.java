package cc.twittertools.scripts;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import cc.twittertools.words.dict.LookupDictionary;

public class DictCounter
{
	public static void main (String[] args) throws IOException
	{	//final int  MIN_OCCURRENCE_COUNT = 10;
		final Path PATH = Paths.get("/Users/bryanfeeney/Desktop/DatasetStats");
		
		for (int minOccurenceCount : new int[] { 1, 5, 10})
		{	for (String dictFile : new String[] { "smileys.txt", "dictionary.txt", "hashtags.txt", "addressees.txt", "urls.txt", "stocks.txt" } )
			{	LookupDictionary dict = LookupDictionary.fromFile(PATH.resolve(dictFile), minOccurenceCount);
			
				System.out.println ("With min-occurrence = " + minOccurenceCount + ", " + dictFile.replaceAll("\\.txt", ".size") + " = " + dict.size());
				
//				if (dict.size() < 7000)
//					for (int i = 0; i < dict.size(); i++)
//						System.out.printf ("%5d %s\n", i, dict.toWord(i));
				
				dict = null;
				System.gc();
			}
			System.out.println();
		}
	}
}
