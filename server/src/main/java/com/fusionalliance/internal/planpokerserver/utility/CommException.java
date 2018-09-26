package com.fusionalliance.internal.planpokerserver.utility;

/**
 * This class implements a checked exception. It is used to indicate that a communications error occurred. The cause should already be logged, so
 * there is no need to further log instances of this class.
 */
public class CommException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * <p>
	 * See class documentation {@link CommException}.
	 */
	public CommException() {
		super("already logged");
	}
}
