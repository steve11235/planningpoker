/*
* Copyright (C) 2018, Liberty Mutual Group
*
* Created on Jun 5, 2018
*/
package com.fusionalliance.internal.planpokerserver.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * This class provides support for logging exceptions with limited stack traces. This ensures that needed information is available to understand the
 * exception and the context in which it occurred without excessive stack frame logging.
 */
public final class LoggerUtility {
	private static final int BASE_STACK_FRAMES = 5;
	private static volatile String NEWLINE;

	static {
		setMultilineStackTrace(true);
	}

	/**
	 * Set multi-line stack trace on or off. When on, the stack trace appears with each entry on a separate line. When off, each entry is separated by
	 * a " <newline> ", keeping them on a single line. This feature is useful for Splunk Web, where lines can be jumbled.
	 * <p>
	 * <b>Warning:</b> In an application server environment, change this setting only on server start.
	 * 
	 * @param multilineParm
	 */
	public static void setMultilineStackTrace(final boolean multilineParm) {
		if (multilineParm) {
			NEWLINE = "\n\t";

			return;
		}

		NEWLINE = " <newline> ";
	}

	/**
	 * Log an issue with stack frame information. Optionally, log an exception as well. Note that the stack frame information will appear on one line
	 * with " <newline> " used as a separator. This is to offset issues with Splunk/Cloud, which scrambles stack frame entries.
	 * <p>
	 * Logs an issue as an ERROR, or WARN, entry to the Logger, specifying the message, up to {@link #BASE_STACK_FRAMES} stack frames of the caller,
	 * and, optionally, the throwable message and up to {@link #BASE_STACK_FRAMES} of its stack frames.
	 * <p>
	 * The static method creates an instance to do the processing. This minimizes passing of shared instances as parameters and separates logging
	 * concerns from determining the message to be logged, which facilitates testing.
	 *
	 * @param loggerParm      required
	 * @param messageParm     required, not blank
	 * @param treatAsWarnParm if true, create a WARN instead of an ERROR
	 * @param exceptionParm   optional, no exception stack trace if null
	 */
	public static void logIssueWithStackTrace(final Logger loggerParm, final String messageParm, final boolean treatAsWarnParm,
			final Throwable exceptionParm) {

		Preconditions.checkNotNull(loggerParm, "Logger is null");
		Preconditions.checkArgument(StringUtils.isNotBlank(messageParm), "Message is blank");

		final LoggerUtility instance = new LoggerUtility();

		instance.process(messageParm, exceptionParm);

		if (treatAsWarnParm) {
			loggerParm.warn(instance.getMessage());
		} else {
			loggerParm.error(instance.getMessage());
		}
	}

	/**
	 * Return a stack trace as a String, showing a limited number of stack frames. Note that the stack frame information will appear on one line with
	 * " <newline> " used as a separator. This is to offset issues with Splunk/Cloud, which scrambles stack frame entries.
	 *
	 * @param exceptionParm   optional, returns empty if null
	 * @param stackFramesParm
	 * @return
	 */
	public static String generateStackTrace(final Throwable exceptionParm, final int stackFramesParm) {
		final LoggerUtility instance = new LoggerUtility();

		instance.processCause(exceptionParm, stackFramesParm, true);

		return instance.messageBuilder.toString();
	}

	/**
	 * Return the message without logging. This is used for test purposes.
	 *
	 * @param messageParm
	 * @param exceptionParm
	 * @return
	 */
	@VisibleForTesting
	static String testProcess(final String messageParm, final Throwable exceptionParm) {
		final LoggerUtility instance = new LoggerUtility();

		instance.process(messageParm, exceptionParm);

		return instance.getMessage();
	}

	private final StringBuilder messageBuilder = new StringBuilder(1000);

	/**
	 * Hidden constructor
	 */
	private LoggerUtility() {
		// Do nothing
	}

	/**
	 * Process the request.
	 *
	 * @param messageParm
	 * @param exceptionParm
	 */
	private void process(final String messageParm, final Throwable exceptionParm) {
		messageBuilder.append(messageParm);

		final Exception exception = new Exception();

		final List<StackTraceElement> stackTraceElementList = new ArrayList<>(Arrays.asList(exception.getStackTrace()));
		// Remove the entry for this method and the static calling method
		// This avoids adding this utility to the stack trace
		stackTraceElementList.remove(0);
		stackTraceElementList.remove(0);
		processStackFrames(stackTraceElementList, BASE_STACK_FRAMES);

		processCause(exceptionParm, BASE_STACK_FRAMES, true);
	}

	/**
	 * Process the exception and its stack frames, add them to the message builder. Recursively processes the cause's causes, if any.
	 *
	 * @param exceptionParm       optional, no action if null
	 * @param stackFrames         number of stack frames to include
	 * @param isRootExceptionParm if true root exception; otherwise, cause
	 */
	private void processCause(final Throwable exceptionParm, final int maxStackFramesParm, final boolean isRootExceptionParm) {
		if (exceptionParm == null) {

			return;
		}

		messageBuilder.append(NEWLINE);

		if (isRootExceptionParm) {
			messageBuilder.append("Exception in thread \"");
			messageBuilder.append(Thread.currentThread().getName());
			messageBuilder.append("\": ");
		} else {
			messageBuilder.append("Caused by: ");
		}

		messageBuilder.append(exceptionParm.getClass().getSimpleName());
		messageBuilder.append(" -- ");
		messageBuilder.append(exceptionParm.getMessage());

		final List<StackTraceElement> stackTraceElementList = new ArrayList<>(Arrays.asList(exceptionParm.getStackTrace()));
		processStackFrames(stackTraceElementList, maxStackFramesParm);

		final Throwable cause = exceptionParm.getCause();
		if (cause == null || cause == exceptionParm) {
			return;
		}

		processCause(cause, maxStackFramesParm, false);
	}

	/**
	 * Build a five frame stack trace for the caller of the utility, providing its location in the call stack.
	 */
	private void processStackFrames(final List<StackTraceElement> stackTraceElementListParm, final int maxStackFramesParm) {
		int entryCount = 0;

		for (StackTraceElement stackTraceElement : stackTraceElementListParm) {
			entryCount++;

			if (entryCount > maxStackFramesParm) {
				break;
			}

			messageBuilder.append(NEWLINE);

			if (entryCount == 1) {
				messageBuilder.append("At ");
			} else {
				messageBuilder.append("Caller ");
			}

			messageBuilder.append(stackTraceElement.getClassName());
			messageBuilder.append(", ");
			messageBuilder.append(stackTraceElement.getMethodName());
			messageBuilder.append("(), ");
			messageBuilder.append(stackTraceElement.getLineNumber());
		}
	}

	private String getMessage() {
		return messageBuilder.toString();
	}
}