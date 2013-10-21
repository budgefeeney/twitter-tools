package cc.twittertools.words.dict;

/**
 * Never throw by a {@link Dictionary} itself, but may be throw by classed using
 * dictionaries when they receive {@link Dictionary#UNMAPPABLE_WORD} instead of
 * a valid token ID for a given token.
 */
// TODO I really should check my own bloody exceptions...
public class ExcessUnmappableTokens extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private final double amt;

	public ExcessUnmappableTokens(double amt)
	{	super();
		this.amt = amt;
	}

	public ExcessUnmappableTokens(double amt, String message)
	{	super(message);
		this.amt = amt;
	}

	public ExcessUnmappableTokens(double amt, Throwable cause)
	{	super(cause);
		this.amt = amt;
	}

	public ExcessUnmappableTokens(double amt, String message, Throwable cause)
	{	super(message, cause);
		this.amt = amt;
	}

	public ExcessUnmappableTokens(double amt, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace)
	{	super(message, cause, enableSuppression, writableStackTrace);
		this.amt = amt;
	}

	/**
	 * The token that could not be mapped.
	 * @return
	 */
	public double getProportionTokenized()
	{	return amt;
	}

}
