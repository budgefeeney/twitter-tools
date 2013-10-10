package cc.twittertools.scripts;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import cc.twittertools.words.dict.LookupDictionary;

public class DictCounter
{
	public static void main (String[] args) throws IOException
	{	final int  MIN_OCCURRENCE_COUNT = 1;
		final Path PATH = Paths.get("/Users/bryanfeeney/Desktop/DatasetStats/dictionary.txt");
		
		LookupDictionary dict = LookupDictionary.fromFile(PATH, MIN_OCCURRENCE_COUNT);
		
		System.out.println ("With min-occurrence = " + MIN_OCCURRENCE_COUNT + ". Size = " + dict.size());
	}
}
