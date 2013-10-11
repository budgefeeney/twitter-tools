package cc.twittertools.words.dict;

/**
 * Never throw by a {@link Dictionary} itself, but may be throw by classed using
 * dictionaries when they receive {@link Dictionary#UNMAPPABLE_WORD} instead of
 * a valid token ID for a given token.
 */
// TODO I really should check my own bloody exceptions...
public class UnmappableTokenException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnmappableTokenException()
	{	super();
	}

	public UnmappableTokenException(String message)
	{	super(message);
	}

	public UnmappableTokenException(Throwable cause)
	{	super(cause);
	}

	public UnmappableTokenException(String message, Throwable cause)
	{	super(message, cause);
	}

	public UnmappableTokenException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace)
	{	super(message, cause, enableSuppression, writableStackTrace);
	}

}
