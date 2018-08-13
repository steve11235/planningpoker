export class Voter {
	name: string = "!error!";
	hasVoted: boolean = false;
	vote: number = -1;

	/*
	 * Invalid/missing values are ignored; defaults reflect proper error values
	 */
	constructor(private rawVoter: any) {
		if (!rawVoter) {
			return;
		}
		
		if (typeof rawVoter.name === "string") {
			this.name = rawVoter.name;
		}

		if (typeof rawVoter.hasVoted === "boolean") {
			this.hasVoted = rawVoter.hasVoted;
		}

		if (typeof rawVoter.vote === "number" && rawVoter.vote >= 0 && rawVoter.vote <= 11) {
			this.vote = rawVoter.vote;
		}
	}
}