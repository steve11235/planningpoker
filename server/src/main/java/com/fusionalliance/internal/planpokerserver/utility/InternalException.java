package com.fusionalliance.internal.planpokerserver.utility;

/**
 * This class implements a generic unchecked exception. It is used to indicate that an error occurred. The cause should already have been logged,
 * and there is no need for further logging of instances of this class.
 */
public class InternalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InternalException() {
		super("already logged");
	}
}
