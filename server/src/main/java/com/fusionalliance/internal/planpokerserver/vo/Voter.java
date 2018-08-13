package com.fusionalliance.internal.planpokerserver.vo;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.utility.CheckCondition;

/**
 * This class implements a voter.
 * <p>
 * This class is used as a DTO and should not be modified.
 */
public class Voter implements Comparable<Voter> {
	private final String name;
	private boolean hasVoted = false;
	private int vote = -1;

	public Voter(final String nameParm) {
		CheckCondition.check(StringUtils.isNotBlank(nameParm), "Voter name cannot be blank.");

		name = nameParm;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof Voter)) {
			return false;
		}

		return name.equals(((Voter) other).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(final Voter otherVoter) {
		if (otherVoter == null) {
			return 1;
		}

		return name.compareTo(otherVoter.getName());
	}

	public String getName() {
		return name;
	}

	public boolean isHasVoted() {
		return hasVoted;
	}

	public int getVote() {
		return vote;
	}

	public void setVote(int voteParm) {
		vote = voteParm;

		hasVoted = (vote != -1);
	}
}
