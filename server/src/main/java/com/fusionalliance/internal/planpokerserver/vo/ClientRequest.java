package com.fusionalliance.internal.planpokerserver.vo;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.Model;
import com.google.common.annotations.VisibleForTesting;

/**
 * This class implements a value object for a client request.
 * <p>
 * Typically, instances will be populated from JSON.
 */
public class ClientRequest {
	private ClientRequestType requestType;
	private String voterName;
	private int vote;
	/** This can hold any information, such as the voter name to drop */
	private String info = "";

	/**
	 * Return an error message containg validation issues. The message will be null if no errors.
	 * 
	 * @return
	 */
	public String validate() {
		final StringBuilder errorBuilder = new StringBuilder();

		if (requestType == null) {
			errorBuilder.append("The request type is invalid.\n");
		}

		if (StringUtils.isBlank(voterName)) {
			errorBuilder.append("The voter is blank.\n");
		}

		if (vote < -1 || vote > Model.MAX_VOTE) {
			errorBuilder.append("The vote value is not between -1 and " + Model.MAX_VOTE + ": " + vote);
		}

		if (info == null) {
			info = "";
		}

		return errorBuilder.length() == 0 ? null : errorBuilder.toString();
	}

	public ClientRequestType getRequestType() {
		return requestType;
	}

	public String getVoterName() {
		return voterName;
	}

	public int getVote() {
		return vote;
	}

	public String getInfo() {
		return info;
	}

	@VisibleForTesting
	void setRequestType(ClientRequestType requestParm) {
		requestType = requestParm;
	}

	@VisibleForTesting
	void setVoterName(String voterNameParm) {
		voterName = voterNameParm;
	}

	@VisibleForTesting
	void setVote(int voteParm) {
		vote = voteParm;
	}

	@VisibleForTesting
	void setInfo(final String infoParm) {
		if (infoParm == null) {
			info = "";

			return;
		}

		info = infoParm;
	}
}
