package cc.twittertools.words.dict;

/**
 * Implements the {@link #capacity()} part of the {@link Dictionary} 
 * interface but nothing else.
 * @author bryanfeeney
 *
 */
public abstract class AbstractDictionary implements Dictionary {

	protected final int capacity;
	
	protected AbstractDictionary(int capacity) {
		this.capacity = capacity;
	}

	protected AbstractDictionary(AbstractDictionary that) {
		this.capacity = that.capacity;
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public abstract AbstractDictionary clone();
}
