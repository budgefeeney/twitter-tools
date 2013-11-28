package cc.twittertools.words.dict;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * For twitter use, strips a leading sigil from a word before passing it down
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
	public void writeAsPythonList (String pyVarName, BufferedWriter writer) throws IOException
	{	dict.writeAsPythonList(pyVarName, writer);
	}

	@Override
	public SigilStrippingDictionary clone()
	{	return new SigilStrippingDictionary(sigil, dict);
	}

	public Dictionary getDictionary()
	{	return dict;
	}
	
	@Override
	public void writeDelimited(Path path, Charset charset) throws IOException
	{	dict.writeDelimited(path, charset);
	}
	
	@Override
	public void writeDelimited(BufferedWriter wtr, String prefix) throws IOException
	{	dict.writeDelimited(wtr, prefix);
	}

}
