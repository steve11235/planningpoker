import { error } from "util";

export class ClientRequest {
	public static readonly BUMP = "bump";
	public static readonly CANCEL_VOTE = "cancelVote";
	public static readonly END_VOTE = "endVote";
	public static readonly JOIN = "join";
	public static readonly LEAVE = "leave";
	public static readonly REFRESH = "refresh";
	public static readonly START_VOTE = "startVote";
	public static readonly VOTE = "vote";

	private static readonly requestTypes = [ClientRequest.BUMP, ClientRequest.CANCEL_VOTE, ClientRequest.END_VOTE, //
	ClientRequest.JOIN, ClientRequest.LEAVE, ClientRequest.REFRESH, ClientRequest.START_VOTE, ClientRequest.VOTE];

	requestType: string;
	voterName: string;
	vote: number;
	

	constructor(requestTypeParam: string, voterNameParam: string, voteParam: number = -1) {
		this.requestType = requestTypeParam;
		this.voterName = voterNameParam;
		this.vote = voteParam;

		let requestTypeValid = false;
		for (let i = 0; i < ClientRequest.requestTypes.length; i++) {
			if (ClientRequest.requestTypes[i] === this.requestType) {
				requestTypeValid = true;

				break;
			}
		}
		if (!requestTypeValid) {
			throw new error("ClientRequest invalid requestType: " + this.requestType);
		}

		if (!this.voterName) {
			throw new error("ClientRequest voterName is blank.");
		}
	}
}
