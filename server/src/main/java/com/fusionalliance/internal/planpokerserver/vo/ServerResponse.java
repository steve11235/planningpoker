package com.fusionalliance.internal.planpokerserver.vo;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.utility.CheckCondition;

public class ServerResponse {
	private final boolean error;
	private final String message;

	/**
	 * Success constructor
	 */
	public ServerResponse() {
		error = false;
		message = "OK";
	}

	/**
	 * Error constructor
	 * 
	 * @param messageParm
	 *                    required
	 */
	public ServerResponse(final String messageParm) {
		CheckCondition.check(StringUtils.isNotBlank(messageParm), "The error message is blank.");

		error = true;
		message = messageParm;
	}

	public boolean isError() {
		return error;
	}

	public String getMessage() {
		return message;
	}
}
