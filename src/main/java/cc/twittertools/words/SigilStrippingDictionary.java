package cc.twittertools.words;

/**
 * For twitter use, strips a learing sigil from a word before passing it down
 * to the wrapped dictionary.
 */
public class SigilStrippingDictionary implements Dictionary
{
	private final char sigil;
	private final Dictionary dict;
	
	public SigilStrippingDictionary(char sigil, Dictionary dict)
	{	super();
		this.sigil = sigil;
		this.dict  = dict;
	}

	@Override
	public int toInt(String word)
	{	return word.length() > 0 && word.charAt(0) == sigil
			? dict.toInt(word.substring(1))
			: dict.toInt(word);
	}

	@Override
	public String toWord(int wordId)
	{	return dict.toWord (wordId);
	}

	@Override
	public void seal()
	{	dict.seal();
	}

	@Override
	public int size()
	{	return dict.size();
	}

	@Override
	public int capacity()
	{	return dict.capacity();
	}

	@Override
	public SigilStrippingDictionary clone()
	{	return new SigilStrippingDictionary(sigil, dict);
	}
}
