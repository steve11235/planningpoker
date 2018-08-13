package com.fusionalliance.internal.planpokerserver;

import com.fusionalliance.internal.planpokerserver.CommunicationsServer.VoterConnectedListener;
import com.fusionalliance.internal.planpokerserver.Model.ServerUpdateListener;
import com.fusionalliance.internal.planpokerserver.Model.VoterJoinedListener;
import com.fusionalliance.internal.planpokerserver.Model.VoterLeftListener;
import com.fusionalliance.internal.planpokerserver.vo.ClientRequest;
import com.fusionalliance.internal.planpokerserver.vo.ServerResponse;
import com.fusionalliance.internal.planpokerserver.vo.ServerUpdate;

public class Application {

	public static void main(String[] args) {
		new Application().run();
	}

	private final Model model;
	private final CommunicationsServer communicationsServer;

	public Application() {
		model = new Model(this::handleVoterJoined, this::handleVoterLeft, this::handleUpdateGenerated);
		communicationsServer = new CommunicationsServer(this::handleRequest, this::handleVoterConnected);
	}

	public void run() {
		try {
			communicationsServer.run();
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle a voter joined event. See {@link VoterJoinedListener#voterJoined(String)}.
	 * 
	 * @param voterNameParm
	 *            required
	 */
	private void handleVoterJoined(final String voterNameParm) {
		communicationsServer.addConnectPendingVoter(voterNameParm);
	}

	/**
	 * Handle a voter connected event. See {@link VoterConnectedListener#voterConnected(String)}.
	 * 
	 * @param voterNameParm
	 *            required
	 */
	private void handleVoterConnected(final String voterNameParm) {
		model.doConnected(voterNameParm);
	}

	/**
	 * Handle a voter left event. See {@link VoterLeftListener#voterLeft(String)}.
	 * 
	 * @param voterNameParm
	 *            required
	 */
	private void handleVoterLeft(final String voterNameParm) {
		communicationsServer.removeConnectedVoter(voterNameParm);
	}

	/**
	 * Handle a server update generated event. See {@link ServerUpdateListener#updateGenerated(ServerUpdate)}.
	 * 
	 * @param serverUpdateParm
	 */
	private void handleUpdateGenerated(final ServerUpdate serverUpdateParm) {
		communicationsServer.broadcastServerUpdate(serverUpdateParm);
	}

	private ServerResponse handleRequest(final ClientRequest clientRequestParm) {
		final ServerResponse serverResponse;

		switch (clientRequestParm.getRequestType()) {
		case CANCEL_VOTE:
			serverResponse = model.doCancelVote(clientRequestParm.getVoterName());

			break;
		case END_VOTE:
			serverResponse = model.doEndVote(clientRequestParm.getVoterName());

			break;
		case JOIN:
			serverResponse = model.doJoin(clientRequestParm.getVoterName());

			break;
		case LEAVE:
			serverResponse = model.doLeave(clientRequestParm.getVoterName());

			break;
		case REFRESH:
			serverResponse = model.doRefresh(clientRequestParm.getVoterName());

			break;
		case START_VOTE:
			serverResponse = model.doStartVote(clientRequestParm.getVoterName());

			break;
		case VOTE:
			serverResponse = model.doVote(clientRequestParm.getVoterName(), clientRequestParm.getVote());

			break;
		default:
			// This can't happen
			return new ServerResponse("Unknown request type.");
		}

		return serverResponse;
	}
}
