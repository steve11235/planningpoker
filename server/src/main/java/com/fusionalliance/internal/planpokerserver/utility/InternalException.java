package com.fusionalliance.internal.planpokerserver.utility;

/**
 * This class implements a generic unchecked exception. It is used to indicate that an error occurred and to pass a message that should be logged. If
 * a cause is provided, then the cause stack trace should be logged as well.
 */
public class InternalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InternalException(String messageParm, Throwable causeParm) {
		super(messageParm, causeParm);
	}

	public InternalException(String messageParm) {
		super(messageParm);
	}
}
