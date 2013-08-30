package cc.twittertools.words;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.twitter.common.text.combiner.ExtractorBasedTokenCombiner;
import com.twitter.common.text.combiner.HashtagTokenCombiner;
import com.twitter.common.text.combiner.StockTokenCombiner;
import com.twitter.common.text.combiner.URLTokenCombiner;
import com.twitter.common.text.combiner.UserNameTokenCombiner;
import com.twitter.common.text.filter.PunctuationFilter;
import com.twitter.common.text.token.attribute.TokenType;
import com.twitter.common.text.tokenizer.LatinTokenizer;

/**
 * Converts text into vectors, it's as simple as that.
 */
public class Vectorizer {
	
	
	private final static class StdEmoticonCombiner extends ExtractorBasedTokenCombiner {
	  public StdEmoticonCombiner(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor(':'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
	private final static class WinkEmoticonCombiner extends ExtractorBasedTokenCombiner {
	  public WinkEmoticonCombiner(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor(';'));
	    setType(TokenType.EMOTICON);
	  }
	}

	private final static class GoogleEmoticonCombiner extends ExtractorBasedTokenCombiner {
	  public GoogleEmoticonCombiner(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor('8'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
	private final static class GlassesEmoticonCombiner extends ExtractorBasedTokenCombiner {
	  public GlassesEmoticonCombiner(com.twitter.common.text.token.TokenStream inputStream) {
	    super(inputStream);
	    setExtractor(new EmoticonWithEyesExtractor('B'));
	    setType(TokenType.EMOTICON);
	  }
	};
	
//	private final static Logger LOG = LoggerFactory.getLogger(Vectorizer.class);
	
	private static final String SPACE_HTTP = " http://";

	/** The different kinds of input text this tokenizer can operate on */
	public static enum InputType { STANDARD_TEXT, TWITTER };
	
	private final Pattern DIGIT_REGEXP = Pattern.compile("[0-9]");
	private final Pattern URL_REGEXP = Pattern.compile ("\\s+(?:\\w{2,}\\.)+\\w{2,}/[A-Z0-9]", Pattern.CASE_INSENSITIVE);
	
	private TokenDictionary dict;
	private boolean         stemEnabled     = true;
	private boolean         stopElimEnabled = true;
	private int             minWordLength   = 2;
	private int             maxWordLength   = 80; // emails, web-addresses etc.
	private boolean         numbersAllowed  = true;
	private int             minWordCount    = 5; // words occuring less often than this will be skipped
	private boolean         sealed          = false;
	private InputType       inputType       = InputType.STANDARD_TEXT;
	
	
	// Due to the way twitter's tokenizer works, we can just keep the
	// one reference to a tokenizer instead of creating one per tweet
	private TwitterTokenStreamIterator twitterTokenIter;
	
	public Vectorizer(TokenDictionary dict) {
		this.dict = dict;
		twitterTokenIter = createTokenizer("");
	}
	
	public Vectorizer(Map<TokenType, Dictionary> dicts) {
		CompoundTokenDictionary compound = new CompoundTokenDictionary(TokenType.TOKEN);
		for (Map.Entry<TokenType, Dictionary> entry : dicts.entrySet())
			compound.addDictionary(entry.getKey(), entry.getValue());
		
		this.dict = compound;
		twitterTokenIter = createTokenizer("");
	}
	
	/**
	 * Seals the configuration of this vectorizer so subsequent
	 * calls to {@link #toInts(Collection)} and its variants will always
	 * return compatible vectors - i.e. you'd seal a vectorizer after you'd
	 * created a training dataset, so subsequent query-datasets would be
	 * compatible with the model.
	 */
	public void seal()
	{	dict.seal();
		sealed = true;
	}
	
	/**
	 * Throws an exception is seal is true. Used in setters to check a
	 * very common case
	 */
	private final void checkSeal()
	{	if (sealed)
			throw new IllegalStateException("Can't modify a vectorizer once it's been sealed");
	}
	
	/**
	 * Converts a piece of text to a series of words. The words will be in lower
	 * case. Depending on the configuration the following may also occur:
	 * <ul>
	 * <li>Words shorter than {@link #getMinWordLength()} will be excluded
	 * <li>Words longer than {@link #getMaxWordLength()} will be excluded
	 * <li>Words may be stemmed, see {@link #isStemEnabled()}
	 * <li>Stop-words may be eliminated see {@link #isStopElimEnabled()}
	 * </li>
	 * This is lazily evaluated.
	 * @param text
	 * @return an iterator over words read (lazily) from the text
	 */
	public Iterator<Pair<TokenType, String>> toWords (String text)
	{	TokenStream tok;
		switch (inputType)
		{	case STANDARD_TEXT:
			{ 	tok = new StandardTokenizer(Version.LUCENE_36, new StringReader (text));
			
				tok = new LowerCaseFilter(Version.LUCENE_36, tok);
				tok = new EnglishPossessiveFilter(Version.LUCENE_36, tok);
				tok = new LengthFilter(true, tok, minWordLength, maxWordLength);
				
				if (stopElimEnabled)
					tok = new StopFilter(Version.LUCENE_36, tok, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
				if (stemEnabled)
					tok = new PorterStemFilter(tok);
				
				CharTermAttribute charTermAttribute = 
						tok.addAttribute(CharTermAttribute.class);
				
				return new TokenStreamIterator(tok, charTermAttribute);
			}
			case TWITTER:
			{ text = withUrlsMadeExplicit(text);
				twitterTokenIter.reset(text);
				return twitterTokenIter;
			}
		default:
			throw new IllegalStateException ("Don't know how to tokenizer and filter inputs of type " + inputType);
		}

	}
	
	/**
	 * Twitter's URL extractor is pretty hopeless, but I want to avoid cluttering
	 * my token dictionary with nonsense IDs
	 */
	private final String withUrlsMadeExplicit (String text)
	{	int start = 0;
		Matcher m = URL_REGEXP.matcher(text);
		while (m.find(start))
		{	String left  = text.substring(0, m.start());
			String right = text.substring(m.start());
			String trimRight = right.trim();
			
			text = left + SPACE_HTTP + trimRight;
			start = m.end() - (right.length() - trimRight.length()) + SPACE_HTTP.length() - 1;
			m = URL_REGEXP.matcher(text);
		}
		return text;
	}

	/**
	 * Creates the single instance of the tokenizer used for all tweets.
	 */
	private TwitterTokenStreamIterator createTokenizer(String text)
	{
		com.twitter.common.text.token.TokenStream twitterTok = 
		    // Remove punctuation not already used in entities below
			new PunctuationFilter(
			  // combine stock symbol
		      new StockTokenCombiner(
		        // combine emoticon like ;) :-D 8D B-| 
		        new StdEmoticonCombiner(new WinkEmoticonCombiner (new GoogleEmoticonCombiner (new GlassesEmoticonCombiner(
		          // combine possessive form (e.g., apple's)
		          new MyPossessiveContractionTokenCombiner(
		            // Combine fractions (3/4s) and abbrvs ("financial f/cs")
		            new SlashTokenCombiner(
		              // combine URL
		              new URLTokenCombiner(
		                // combine # + hashtag
		                new HashtagTokenCombiner(
		                  // combine @ + user name
		                  new UserNameTokenCombiner(new LatinTokenizer.Builder().setKeepPunctuation(true).build())))))))))));
		
		twitterTok.reset(text);
		return new TwitterTokenStreamIterator (
			twitterTok,
			stemEnabled,
			stopElimEnabled,
			/* lower-case = */ true,
			minWordLength,
			maxWordLength
		);
	}
	
	/**
	 * Takes a corpus, i.e. an enumeration of Strings, and returns a list of
	 * enumerations over words. This is lazily evaluated. See {@link #toWords(String)}
	 */
	public Iterator<Iterator<Pair<TokenType, String>>> toWords (Iterator<String> corpus)
	{	return Iterators.transform(corpus, new Function<String, Iterator<Pair<TokenType, String>>>() {
			public Iterator<Pair<TokenType, String>> apply (String input) {
				return toWords (input);
			}
		});
	}
	
	/**
	 * Splits the given text into words using {@link #toWords(String)} and then
	 * uses a {@link Dictionary} to convert those words into ints. Note that
	 * not all words may be translated by the dictionary, so the length of
	 * this array may be less than the number of words returned by {@link #toWords(String)}.
	 * <p>
	 * This version skips the infrequent words check
	 * @param text
	 * @return an int array corresponding to the words in the text
	 */
	public int[] toInts (String text)
	{	if (minWordCount > 1)
			throw new IllegalStateException ("Can't enabled infrequent word-filtering (minWordCount=" + minWordCount + ") and process files one at a time. For infrequenct word-filtering to work, the corpus needs to be vectorized all at once.");
	
		return toIntsInternal(text, Collections.<TokenType, Set<String>>emptyMap());
	}
	
	/**
	 * Splits the given text into words using {@link #toWords(String)} and then
	 * uses a {@link Dictionary} to convert those words into ints. Note that
	 * not all words may be translated by the dictionary, so the length of
	 * this array may be less than the number of words returned by {@link #toWords(String)}.
	 * <p>
	 * This version skips the infrequent words check
	 * @param text
	 * @param infrequentWords a collection of words that are to be skipped as they occur
	 * too rarely
	 * @return an int array corresponding to the words in the text
	 */
	private int[] toIntsInternal (String text, Map<TokenType, Set<String>> infrequentWords)
	{	int[] result = new int[text.length() / 5];
		int numWords = 0;
		Iterator<Pair<TokenType, String>> words = toWords(text);
		while (words.hasNext())
		{	Pair<TokenType, String> wordToken = words.next();
			TokenType tokenType = wordToken.getKey();
			String    word      = wordToken.getValue();
		
			if (contains(infrequentWords, wordToken))
				continue;
			if (tokenType == TokenType.TOKEN && ! numbersAllowed && DIGIT_REGEXP.matcher(word).find())
				continue;
			
			int wordId = dict.toInt(tokenType, word);
			if (wordId == Dictionary.UNMAPPABLE_WORD)
				continue;
			
			result = ArrayUtils.add(result, numWords, wordId);
			++numWords;
		}
		
		return result.length == numWords
			? result
			: ArrayUtils.subarray(result, 0, numWords);
	}

	private boolean contains(Map<TokenType, Set<String>> infrequentWords, Pair<TokenType, String> wordToken) {
		Set<String> infrequentWordSet = infrequentWords.get(wordToken.getKey());
		return infrequentWordSet != null && infrequentWordSet.contains (wordToken.getValue());
	}
	
	
	/**
	 * Given a corpus of sample texts, breaks it into words, and converts those
	 * words into integers. See {@link #toInts(String)}
	 */
	public int[][] toInts (Collection<String> corpus)
	{	return toInts (corpus.iterator(), corpus.size());
	}
	
	/**
	 * Given a corpus of sample texts, breaks it into words, and converts those
	 * words into integers. See {@link #toInts(String)}
	 */
	public int[][] toInts (Iterator<String> corpus)
	{	return toInts (corpus, 1000);
	}
	
	/**
	 * Given a corpus of sample texts, breaks it into words, and converts those
	 * words into integers. See {@link #toInts(String)}. The sizeHint gives the
	 * number of elements in the iterator, to save on wasted memory allocation
	 * with the arrays.
	 */
	public int[][] toInts (Iterator<String> corpus, int sizeHint)
	{	if (minWordCount <= 1)
			return toInts (corpus, Collections.<TokenType, Set<String>>emptyMap(), sizeHint);
	
		// need to buffer the corpus and backup the dictionary as we're taking two
		// passes through it
		List<String> corpusCopy = Lists.newArrayList(corpus);
		TokenDictionary backup = dict.clone();
		
		TokenDictionary swap = dict; // make sure we preserve the original dict
		dict   = backup;  // reference passed in, in case it gets referenced
		backup = swap;    // outside of this method.
		
		// Do one run through the data, converting it to ints, to get counts of
		// how often each token occurs.
		int[][] firstRun = toInts (corpusCopy.iterator(), Collections.<TokenType, Set<String>>emptyMap(), sizeHint);
		int[] wordCounts = new int[dict.capacity()];
		for (int[] document : firstRun)
			for (int wordId : document)
				++wordCounts[wordId];
		
		Map<TokenType, Set<String>> infrequentWords = new HashMap<>();
		for (int wordId = 0; wordId < dict.size(); wordId++)
		{	if (wordCounts[wordId] < minWordCount)
			{	Pair<TokenType, String> wordToken = dict.toWordToken (wordId);
				infrequentWords.get(wordToken.getKey()).add(wordToken.getValue());
			}
		}
		
		// Restore the backup, and use the derived list of infrequent words to
		// convert it to a list of integers
		dict = backup;
		return toInts (corpusCopy.iterator(), infrequentWords, sizeHint);
	}
	
	/**
	 * Given a corpus of sample texts, breaks it into words, and converts those
	 * words into integers. See {@link #toInts(String)}. The sizeHint gives the
	 * number of elements in the iterator, to save on wasted memory allocation
	 * with the arrays.
	 */
	private int[][] toInts (Iterator<String> corpus, Map<TokenType, Set<String>> infrequentWords, int sizeHint)
	{	
		int[][] corpusInts = new int[sizeHint][];
		int numDocs = 0;
		while (corpus.hasNext())
		{	int[] docInts = toIntsInternal (corpus.next(), infrequentWords);
			corpusInts = ArrayUtils.add(corpusInts, numDocs, docInts);
			++numDocs;
		}

		return corpusInts.length == numDocs
			? corpusInts
			: ArrayUtils.subarray(corpusInts, 0, numDocs);
	}
	

	public TokenDictionary getDict() {
		return dict;
	}

	public boolean isStemEnabled() {
		return stemEnabled;
	}

	public void setStemEnabled(boolean stemEnabled) {
		checkSeal();
		this.stemEnabled = stemEnabled;
	}

	public boolean isStopElimEnabled() {
		return stopElimEnabled;
	}

	public void setStopElimEnabled(boolean stopElimEnabled) {
		checkSeal();
		this.stopElimEnabled = stopElimEnabled;
	}

	public int getMinWordLength() {
		return minWordLength;
	}

	public void setMinWordLength(int minWordLength) {
		checkSeal();
		this.minWordLength = minWordLength;
	}

	public int getMaxWordLength() {
		return maxWordLength;
	}

	public void setMaxWordLength(int maxWordLength) {
		checkSeal();
		this.maxWordLength = maxWordLength;
	}
	
	public boolean isNumbersAllowed() {
		return numbersAllowed;
	}
	
	public void setNumbersAllowed(boolean numbersAllowed) {
		checkSeal();
		this.numbersAllowed = numbersAllowed;
	}

	public int getMinWordCount() {
		return minWordCount;
	}

	public void setMinWordCount(int minWordCount) {
		checkSeal();
		this.minWordCount = minWordCount;
	}

	public InputType getInputType()
	{	return inputType;
	}

	public void setInputType(InputType inputType)
	{	this.inputType = inputType;
	}
	
	
}
