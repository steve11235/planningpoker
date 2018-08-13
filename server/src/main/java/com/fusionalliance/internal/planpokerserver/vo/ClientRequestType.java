package com.fusionalliance.internal.planpokerserver.vo;

import com.google.gson.annotations.SerializedName;

public enum ClientRequestType {
	@SerializedName("join")
	JOIN, //
	@SerializedName("startVote")
	START_VOTE, //
	@SerializedName("vote")
	VOTE, //
	@SerializedName("endVote")
	END_VOTE, //
	@SerializedName("cancelVote")
	CANCEL_VOTE, //
	@SerializedName("refresh")
	REFRESH, //
	@SerializedName("leave")
	LEAVE, //
	@SerializedName("bump")
	BUMP, //
	;
}
