package com.fusionalliance.internal.planpokerserver.utility;

/**
 * This utility class checks a condition and throws an exception if the condition is not true. This provides the same functionality as the
 * <code>assert</code> keyword without the need to enable it.
 */
public final class CheckCondition {

	/**
	 * Check the condition and throw an exception if it is false.
	 * 
	 * @param condition
	 *            The condition to evaluate, it is expected to be true
	 * @param message
	 *            The exception message, if an exception is thrown
	 * @throws ConditionNotMetException
	 */
	public static void check(final boolean condition, final String message) {
		if (condition) {
			return;
		}

		throw new ConditionNotMetException(message);
	}

	/**
	 * Hidden constructor
	 */
	private CheckCondition() {
		// Do nothing
	}

	/**
	 * This class implements a RuntimeException indicating that an condition was not met.
	 */
	public static final class ConditionNotMetException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ConditionNotMetException(final String message) {
			super(message);
		}
	}
}
