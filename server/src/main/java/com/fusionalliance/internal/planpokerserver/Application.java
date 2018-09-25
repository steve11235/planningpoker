package com.fusionalliance.internal.planpokerserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fusionalliance.internal.planpokerserver.vo.ClientRequest;
import com.fusionalliance.internal.planpokerserver.vo.ServerResponse;
import com.fusionalliance.internal.planpokerserver.vo.ServerUpdate;

public class Application implements //
		Model.ServerUpdateListener, //
		Model.VoterJoinedListener, //
		Model.VoterLeftListener, //
		CommunicationsServer.ClientRequestListener, //
		CommunicationsServer.VoterConnectedListener //
{
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new Application().run();
	}

	private final Model model;
	private final CommunicationsServer communicationsServer;

	public Application() {
		model = new Model(this::handleVoterJoined, this::handleVoterLeft, this::handleUpdateGenerated);
		communicationsServer = new CommunicationsServer(this::handleRequestReceived, this::handleVoterConnected);
	}

	public void run() {
		LOG.info("The server is starting.");

		try {
			communicationsServer.run();
		} catch (final Exception e) {
			LOG.error("The aapplication failed!", e);
		}

		// Keep the console open until it is externally closed
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	@Override
	public void handleVoterJoined(final String voterNameParm) {
		communicationsServer.addConnectPendingVoter(voterNameParm);
	}

	@Override
	public void handleVoterLeft(final String voterNameParm) {
		communicationsServer.removeConnectedVoter(voterNameParm);
	}

	@Override
	public void handleUpdateGenerated(final ServerUpdate serverUpdateParm) {
		communicationsServer.broadcastServerUpdate(serverUpdateParm);
	}

	@Override
	public ServerResponse handleRequestReceived(final ClientRequest clientRequestParm) {
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

	@Override
	public void handleVoterConnected(final String voterNameParm) {
		model.doConnected(voterNameParm);
	}
}
