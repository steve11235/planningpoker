import { Voter } from "./Voter";

export class ServerUpdate {
	message: string = "Received an invalid server update!";
	// 0: no vote (waiting to join, waiting for first vote, previous vote canceled, refresh selected)
	// 1: vote in progress (multiple updates as each voter votes)
	// 2: vote complete (previous voting session was closed and results are provided)
	voteStatus: number = 0;
	averageVote: number = -1;
	voters: Voter[] = [];

	/*
	 * Invalid/missing values are ignored; defaults reflect proper error values
	 */
	constructor(private rawServerUpdate: any) {
		if (!rawServerUpdate) {
			return;
		}

		if (typeof rawServerUpdate.message === "string" && rawServerUpdate.message.length > 0) {
			this.message = rawServerUpdate.message;
		}


		if (typeof rawServerUpdate.voteStatus === "number" && rawServerUpdate.voteStatus >= 0 && rawServerUpdate.voteStatus <= 2) {
			this.voteStatus = rawServerUpdate.voteStatus;
		}

		if (rawServerUpdate.averageVote >= 0 && rawServerUpdate.averageVote <= 11) {
			this.averageVote = rawServerUpdate.averageVote;
		}


		// Cannot check typeof array
		if (rawServerUpdate.voters && rawServerUpdate.voters.length) {
			let voter: Voter = null;
			for (let i = 0; i < rawServerUpdate.voters.length; i++) {
				voter = new Voter(rawServerUpdate.voters[i]);
				this.voters.push(voter);
			}
		}
	}
}