import { ServerUpdate } from './ServerUpdate';
import { Voter } from './Voter';

export class ServerUpdateHandlerFake {
	voters: string[] = [
		"Curly",
		"Ethel",
		"Fred",
		"Larry",
		"Lucy",
		"Moe",
		"Ricky"
	];
	myVote: number = -1;


	addVoter(voterName: string) {
		if (!voterName) {
			return;
		}

		this.voters.push(voterName);
	}

	removeAddedVoter() {
		if (this.voters.length == 0) {
			return;
		}

		this.voters.pop();
	}

	setMyVote(vote: number) {
		this.myVote = vote;
	}

	retrieveEmptyUpdate(): ServerUpdate {
		const rawServerUpdate: any = new Object();
		rawServerUpdate.message = "Waiting to join.";
		rawServerUpdate.voteStatus = 0;
		rawServerUpdate.averageVote = -1;
		rawServerUpdate.voters = [];

		const json = JSON.stringify(rawServerUpdate);
		console.log(json);
		const rawServerUpdate1 = JSON.parse(json);

		const serverUpdate: ServerUpdate = new ServerUpdate(rawServerUpdate1);

		return serverUpdate;
	}

	retrieveNoVoteUpdate(): ServerUpdate {
		const rawServerUpdate: any = new Object();
		rawServerUpdate.message = "Ready to start voting.";
		rawServerUpdate.voteStatus = 0;
		rawServerUpdate.averageVote = -1;

		rawServerUpdate.voters = [];
		for (let i = 0; i < this.voters.length; i++) {
			rawServerUpdate.voters.push({ "name": this.voters[i], "hasVoted": false, "vote": -1 });
		}

		const json = JSON.stringify(rawServerUpdate);
		console.log(json);
		const rawServerUpdate1 = JSON.parse(json);

		const serverUpdate: ServerUpdate = new ServerUpdate(rawServerUpdate1);

		return serverUpdate;
	}

	retrieveVoteInProgressUpdate(): ServerUpdate {
		const rawServerUpdate: any = new Object();
		rawServerUpdate.message = "Voting in progress.";
		rawServerUpdate.voteStatus = 1;
		rawServerUpdate.averageVote = -1;

		rawServerUpdate.voters = [];
		for (let i = 0; i < this.voters.length; i++) {
			rawServerUpdate.voters.push({ "name": this.voters[i], "hasVoted": (Math.random() >= .5), "vote": -1 });
		}

		const json = JSON.stringify(rawServerUpdate);
		console.log(json);
		const rawServerUpdate1 = JSON.parse(json);

		const serverUpdate: ServerUpdate = new ServerUpdate(rawServerUpdate1);

		return serverUpdate;
	}

	retrieveVoteClosedUpdate(): ServerUpdate {
		const rawServerUpdate: any = new Object();
		rawServerUpdate.message = "Voting complete.";
		rawServerUpdate.voteStatus = 2;

		rawServerUpdate.voters = [];
		let vote: number = 0;
		let votes: number = 0;
		let voteTotal: number = 0;
		for (let i = 0; i < this.voters.length - 1; i++) {
			vote = Math.floor(Math.random() * 11);
			rawServerUpdate.voters.push({ "name": this.voters[i], "hasVoted": true, "vote": vote });

			// Ignore "?" for average purposes
			if (vote > 0) {
				votes++;
				voteTotal += vote;
			}
		}

		rawServerUpdate.voters.push({ "name": this.voters[this.voters.length - 1], "hasVoted": (this.myVote != -1), "vote": this.myVote });

		// Ignore "?" for average purposes
		if (this.myVote > 0) {
			votes++;
			voteTotal += this.myVote;
		}

		if (votes == 0) {
			rawServerUpdate.averageVote = 0;
		} else {
			rawServerUpdate.averageVote = Math.round(voteTotal / votes);
		}

		const json = JSON.stringify(rawServerUpdate);
		console.log(json);
		const rawServerUpdate1 = JSON.parse(json);

		const serverUpdate: ServerUpdate = new ServerUpdate(rawServerUpdate1);

		return serverUpdate;
	}
}