package cc.twittertools.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Given a file of tokens and counts, ranks it according to token-count in
 * decreasing order.
 * @author bryanfeeney
 *
 */
public class Rank implements Callable<Integer>
{
	private final static String NULL_WORD = "_bryan_feeney_is_a_programmer_and_not_a_word";
	
	private final static class TokenCount implements Comparable<TokenCount>
	{	private final String token;
		private final int count;
		
		public TokenCount(String token, int count)
		{	this.token = token;
			this.count = count;
		}
		
		public TokenCount(String tabDelimLine)
		{	String[] parts = StringUtils.split(tabDelimLine);
			this.token = parts[0];
			this.count = Integer.parseInt(parts[1]);
		}

		public String getToken()
		{	return token;
		}

		public int getCount()
		{	return count;
		}
		
		/** The token, a tab, the count, and a newline */
		public String toTabDelim()
		{	return token + '\t' + String.valueOf(count) + '\n';
		}

		@Override
		public int compareTo(TokenCount that)
		{	return that.count - this.count;
		}
		
		 
	}
	
	@Option(name="-i", aliases="--input", usage="The input file with one word per line", metaVar=" ")
	private String inputPath;
	
	@Option(name="-o", aliases="--output", usage="The output file, tab delimited of words and counts", metaVar=" ")
	private String outputPath;
	
	@Option(name="-h", aliases="--help", usage="Show this help message", metaVar=" ")
	private boolean showHelp = false;
	
	private Rank()
	{	
	}


	/** Parses the arguments */
	private void parseArguments(String[] args)
	{	CmdLineParser parser = null;
		try
		{	parser = new CmdLineParser(this);
			parser.parseArgument(args);
		
			if (showHelp)
			{	System.out.println("Help for this command:");
				showHelp (System.out, parser);
				System.exit(0);
			}
		}
		catch (CmdLineException e)
		{	System.err.println (e.getMessage());
			showHelp (System.err, parser);
		}

	}

	/** Shows the help message to the given stream. Needs the parser object to say what the options are. */
	private void showHelp(PrintStream out, CmdLineParser parser)
	{	out.println ("Usage: java -jar JARNAME.jar <options>");
		if (parser != null)
			parser.printUsage(out);
	}

	/**
	 * Given an input file of words, one per line, writes out a tab-delimited
	 * file of word-counts
	 */
	public Integer call() throws Exception
	{	List<TokenCount> tokenCounts = new ArrayList<TokenCount>(3_000_000);
	
		try (BufferedReader rdr = Files.newBufferedReader(Paths.get(inputPath), Charsets.UTF_8);)
		{	String line = null;
			while ((line = rdr.readLine()) != null)
				tokenCounts.add(new TokenCount (line));
		}
		
		Collections.sort(tokenCounts);
		
		try (BufferedWriter wtr = Files.newBufferedWriter(Paths.get(outputPath), Charsets.UTF_8);)
		{	for (TokenCount tc : tokenCounts)
				wtr.write(tc.toTabDelim());
		}
		
		return tokenCounts.size();
	}
	
	
	public static final void main(String[] args) throws Exception
	{	Rank c = new Rank();
		c.parseArguments(args);
		c.call();
	}
}