package com.fusionalliance.internal.planpokerserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fusionalliance.internal.planpokerserver.vo.ClientRequest;
import com.fusionalliance.internal.planpokerserver.vo.ServerResponse;
import com.fusionalliance.internal.planpokerserver.vo.ServerUpdate;

public class Application implements //
		Model.ServerUpdateListener, //
		CommunicationsServer.ClientRequestListener, //
		CommunicationsServer.VoterConnectedListener, //
		CommunicationsServer.VoterDroppedListener //
{
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new Application().run();
	}

	private final Model model;
	private final CommunicationsServer communicationsServer;

	public Application() {
		model = new Model(this::handleUpdateGenerated);
		communicationsServer = new CommunicationsServer(this::handleRequestReceived, this::handleVoterConnected, this::handleVoterDropped);
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
	public void handleUpdateGenerated(final ServerUpdate serverUpdateParm) {
		communicationsServer.broadcastServerUpdate(serverUpdateParm);
	}

	@Override
	public ServerResponse handleRequestReceived(final ClientRequest clientRequestParm) {
		final ServerResponse serverResponse;
		final String voterName = clientRequestParm.getVoterName();

		switch (clientRequestParm.getRequestType()) {
		case CANCEL_VOTE:
			serverResponse = model.doCancelVote(voterName);

			break;
		case END_VOTE:
			serverResponse = model.doEndVote(clientRequestParm.getVoterName());

			break;
		case JOIN:
			serverResponse = model.doJoin(voterName);

			// model.doJoin() may detect a duplicate name
			if (!serverResponse.isError()) {
				communicationsServer.addConnectPendingVoter(voterName);
			}

			break;
		case LEAVE:
			serverResponse = model.doLeave(voterName);

			break;
		case REFRESH:
			serverResponse = model.doRefresh(voterName);

			break;
		case START_VOTE:
			serverResponse = model.doStartVote(voterName);

			break;
		case VOTE:
			serverResponse = model.doVote(voterName, clientRequestParm.getVote());

			break;
		case DROP_VOTER:
			serverResponse = model.doDropVoter(voterName, clientRequestParm.getInfo());

			// model.doDropVoter() may find the voter was dropped already
			if (!serverResponse.isError()) {
				communicationsServer.removeVoterWebSocket(voterName);
			}

			break;
		default:
			// This shouldn't happen
			return new ServerResponse("Unknown request type.");
		}

		return serverResponse;
	}

	@Override
	public void handleVoterConnected(final String voterNameParm) {
		model.doConnected(voterNameParm);
	}

	@Override
	public void handleVoterDropped(String voterNameParm) {
		communicationsServer.removeVoterWebSocket(voterNameParm);
		model.removeVoter(voterNameParm);
	}

}
