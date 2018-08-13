package com.fusionalliance.internal.planpokerserver;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.utility.CheckCondition;
import com.fusionalliance.internal.planpokerserver.vo.ServerResponse;
import com.fusionalliance.internal.planpokerserver.vo.ServerUpdate;
import com.fusionalliance.internal.planpokerserver.vo.Voter;

public class Model {

	private static final String UNKNOWN_VOTER = "Unknown voter: ";

	private static final String OK = "OK";

	/** ?, .5, 1, 2, 3, 5, 8, 13, 20, 40, 100, zero-based */
	public static final int MAX_VOTE = 10;

	private final Map<String, Voter> voterByName = new HashMap<>();

	final VoterJoinedListener voterJoinedListener;
	final VoterLeftListener voterLeftListener;
	final ServerUpdateListener serverUpdateListener;

	/** 0 = no vote 1 = vote in progress 2 = vote complete */
	private int voteStatus = 0;

	/**
	 * Constructor
	 * 
	 * @param voterJoinedListenerParm
	 *            required
	 * @param voterLeftListenerParm
	 *            required
	 * @param serverUpdateListenerParm
	 *            required
	 */
	public Model(final VoterJoinedListener voterJoinedListenerParm, final VoterLeftListener voterLeftListenerParm,
			final ServerUpdateListener serverUpdateListenerParm) {
		voterJoinedListener = voterJoinedListenerParm;
		voterLeftListener = voterLeftListenerParm;
		serverUpdateListener = serverUpdateListenerParm;
	}

	/**
	 * Cancel voting.
	 * 
	 * @param voterNameParm
	 *            required
	 * @return
	 */
	public ServerResponse doCancelVote(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 0;
		clearVotes();

		final ServerUpdate serverUpdate = generateServerUpdate("Vote canceled by: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doEndVote(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 2;

		final ServerUpdate serverUpdate = generateServerUpdate("Vote ended by: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		return new ServerResponse();
	}

	/**
	 * Add the voter, step 1 of 2. See {@link #doConnected(String)}.
	 * <p>
	 * This is a standard request for the voter to join. If this request is successful, then the client must create a WebSocket connection.
	 * 
	 * @param voterNameParm
	 *            required, must be unique
	 * @return null if joined; otherwise, an error message
	 */
	public ServerResponse doJoin(String voterNameParm) {
		if (voterByName.containsKey(voterNameParm)) {
			return new ServerResponse("Voter name is already in use: " + voterNameParm);
		}

		voterByName.put(voterNameParm, new Voter(voterNameParm));

		voterJoinedListener.voterJoined(voterNameParm);

		return new ServerResponse();
	}

	/**
	 * Add the voter, step 2 of 2. See {@link #doJoin(String)}.
	 * <p>
	 * After the join request, the client creates a WebSocket connection; this method is invoked after that connection is complete.
	 * 
	 * @param voterNameParm
	 *            required, must exist
	 * @return null if success; otherwise, an error message
	 */
	public ServerResponse doConnected(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		final ServerUpdate serverUpdate = generateServerUpdate("A new voter joined: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doLeave(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voterByName.remove(voterNameParm);

		voterLeftListener.voterLeft(voterNameParm);

		final ServerUpdate serverUpdate = generateServerUpdate("A voter left: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doRefresh(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 0;
		clearVotes();

		final ServerUpdate serverUpdate = generateServerUpdate("Refresh requested by: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doStartVote(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 1;
		clearVotes();

		final ServerUpdate serverUpdate = generateServerUpdate("Vote started by: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doVote(String voterNameParm, int voteParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		if (voteParm < 0 || voteParm > MAX_VOTE) {
			return new ServerResponse("Invalid vote: " + voteParm);
		}

		final Voter voter = voterByName.get(voterNameParm);
		voter.setVote(voteParm);

		final ServerUpdate serverUpdate = generateServerUpdate("Vote by: " + voterNameParm);
		serverUpdateListener.updateGenerated(serverUpdate);

		boolean voteComplete = true;
		for (Voter loopVoter : voterByName.values()) {
			if (!loopVoter.isHasVoted()) {
				voteComplete = false;

				break;
			}
		}

		if (voteComplete) {
			doEndVote(voterNameParm);
		}

		return new ServerResponse();
	}

	/**
	 * Check that the voter is joined.
	 * 
	 * @param voterNameParm
	 * @return false if blank or not joined
	 */
	private boolean checkVoterJoined(String voterNameParm) {
		if (StringUtils.isBlank(voterNameParm)) {
			return false;
		}

		if (!voterByName.containsKey(voterNameParm)) {
			return false;
		}

		return true;
	}

	/**
	 * Generate a server update from the current model state.
	 * 
	 * @param messageParm
	 *            required
	 * @return
	 */
	private ServerUpdate generateServerUpdate(final String messageParm) {
		CheckCondition.check(StringUtils.isNotBlank(messageParm), "The message is blank.");

		final ServerUpdate serverUpdate = new ServerUpdate(messageParm, voteStatus, new TreeSet<Voter>(voterByName.values()));

		return serverUpdate;
	}

	/**
	 * Clear all voters' votes.
	 */
	private void clearVotes() {
		for (Voter voter : voterByName.values()) {
			voter.setVote(-1);
		}
	}

	/**
	 * This interface defines the contract for classes that listen for voter joined events.
	 */
	public interface VoterJoinedListener extends EventListener {
		void voterJoined(final String voterName);
	}

	/**
	 * This interface defines the contract for classes that listen for voter left events.
	 */
	public interface VoterLeftListener extends EventListener {
		void voterLeft(final String voterName);
	}

	/**
	 * This interface defines the contract for classes that listen for server updates events.
	 */
	public interface ServerUpdateListener extends EventListener {
		void updateGenerated(final ServerUpdate serverUpdate);
	}
}