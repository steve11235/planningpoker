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

	/** ?, .5, 1, 2, 3, 5, 8, 13, 20, 40, 100, zero-based */
	public static final int MAX_VOTE = 10;

	private final Map<String, Voter> voterByName = new HashMap<>();

	final ServerUpdateListener serverUpdateListener;

	/** 0 = no vote 1 = vote in progress 2 = vote complete */
	private int voteStatus = 0;

	/**
	 * Constructor
	 * 
	 * @param serverUpdateListenerParm required
	 */
	public Model(final ServerUpdateListener serverUpdateListenerParm) {
		serverUpdateListener = serverUpdateListenerParm;
	}

	/**
	 * Cancel voting.
	 * 
	 * @param voterNameParm required
	 * @return
	 */
	public ServerResponse doCancelVote(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 0;
		clearVotes();

		final ServerUpdate serverUpdate = generateServerUpdate("Vote canceled by: " + voterNameParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doEndVote(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 2;

		final ServerUpdate serverUpdate = generateServerUpdate("Vote ended by: " + voterNameParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

		return new ServerResponse();
	}

	/**
	 * Add the voter, step 1 of 2. See {@link #doConnected(String)}.
	 * <p>
	 * This is a standard request for the voter to join. If this request is successful, then the client must create a WebSocket connection.
	 * 
	 * @param voterNameParm required, must be unique
	 * @return null if joined; otherwise, an error message
	 */
	public ServerResponse doJoin(String voterNameParm) {
		if (voterByName.containsKey(voterNameParm)) {
			return new ServerResponse("Voter name is already in use: " + voterNameParm);
		}

		voterByName.put(voterNameParm, new Voter(voterNameParm));

		return new ServerResponse();
	}

	/**
	 * Add the voter, step 2 of 2. See {@link #doJoin(String)}.
	 * <p>
	 * After the join request, the client creates a WebSocket connection; this method is invoked after that connection is complete.
	 * 
	 * @param voterNameParm required, must exist
	 * @return null if success; otherwise, an error message
	 */
	public ServerResponse doConnected(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		final ServerUpdate serverUpdate = generateServerUpdate("A new voter joined: " + voterNameParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doLeave(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voterByName.remove(voterNameParm);

		final ServerUpdate serverUpdate = generateServerUpdate("A voter left: " + voterNameParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doRefresh(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 0;
		clearVotes();

		final ServerUpdate serverUpdate = generateServerUpdate("Refresh requested by: " + voterNameParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

		return new ServerResponse();
	}

	public ServerResponse doStartVote(String voterNameParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		voteStatus = 1;
		clearVotes();

		final ServerUpdate serverUpdate = generateServerUpdate("Vote started by: " + voterNameParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

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
		serverUpdateListener.handleUpdateGenerated(serverUpdate);

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

	public ServerResponse doDropVoter(String voterNameParm, String voterNameToDropParm) {
		if (!checkVoterJoined(voterNameParm)) {
			return new ServerResponse(UNKNOWN_VOTER + voterNameParm);
		}

		if (voterByName.remove(voterNameToDropParm) != null) {
			final ServerUpdate serverUpdate = generateServerUpdate("A voter was dropped: " + voterNameToDropParm);
			serverUpdateListener.handleUpdateGenerated(serverUpdate);
		}

		return new ServerResponse();
	}

	/**
	 * Remove a voter.
	 * <p>
	 * Use this method when a comm errors occurs for a specific voter, to remove the voter from the model.
	 * 
	 * @param voterNameToDropParm
	 */
	public void removeVoter(final String voterNameToDropParm) {
		voterByName.remove(voterNameToDropParm);

		final ServerUpdate serverUpdate = generateServerUpdate("A voter dropped because of comm errors: " + voterNameToDropParm);
		serverUpdateListener.handleUpdateGenerated(serverUpdate);
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
	 * @param messageParm required
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
	 * This interface defines the contract for classes that listen for server updates events.
	 */
	public interface ServerUpdateListener extends EventListener {
		/**
		 * Process a server update event.
		 * 
		 * @param serverUpdate required
		 */
		void handleUpdateGenerated(final ServerUpdate serverUpdate);
	}
}
