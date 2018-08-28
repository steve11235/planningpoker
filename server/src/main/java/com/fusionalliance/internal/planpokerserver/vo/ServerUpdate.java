package com.fusionalliance.internal.planpokerserver.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.google.common.annotations.VisibleForTesting;

/**
 * This class represents a server update that is broadcast to clients. Typically, it is transformed to JSON after it is populated.
 * <p>
 * This class is used as a DTO and should not be modified.
 */
public class ServerUpdate {
	private final String message;
	private final int voteStatus;
	private final int averageVote;
	private final List<Voter> voters = new ArrayList<>();

	public ServerUpdate(final String messageParm, final int voteStatusParm, final SortedSet<Voter> votersParm) {
		message = messageParm;
		voteStatus = voteStatusParm;
		voters.addAll(votersParm);
		averageVote = calculateAverageVote();
	}

	private int calculateAverageVote() {
		// Don't calculate the average vote if the vote is not complete
		if (voteStatus != 2) {

			return -1;
		}

		int validVoters = 0;
		int sumVotes = 0;
		for (Voter voter : voters) {
			if (!voter.isHasVoted()) {
				continue;
			}
			// Don't average in "?" votes
			if (voter.getVote() == 0) {
				continue;
			}

			validVoters++;
			sumVotes += voter.getVote();
		}

		// Boundary case: vote ended with no one voting or every voting "?"
		if (validVoters == 0) {
			return -1;
		}

		final BigDecimal average = new BigDecimal(sumVotes).divide(new BigDecimal(validVoters), 0, BigDecimal.ROUND_HALF_UP);

		return average.intValue();
	}

	@VisibleForTesting
	String getMessage() {
		return message;
	}

	@VisibleForTesting
	int getVoteStatus() {
		return voteStatus;
	}

	@VisibleForTesting
	int getAverageVote() {
		return averageVote;
	}

	@VisibleForTesting
	List<Voter> getVoters() {
		return voters;
	}
}
