package com.fusionalliance.internal.planpokerserver;

import static com.fusionalliance.internal.planpokerserver.utility.CheckCondition.check;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fusionalliance.internal.planpokerserver.io.HttpHeader;
import com.fusionalliance.internal.planpokerserver.io.HttpRequest;
import com.fusionalliance.internal.planpokerserver.io.HttpRequestMethods;
import com.fusionalliance.internal.planpokerserver.io.HttpUtility;
import com.fusionalliance.internal.planpokerserver.io.SocketChannelUtility;
import com.fusionalliance.internal.planpokerserver.io.WebSocketFrame;
import com.fusionalliance.internal.planpokerserver.io.WebSocketOpCode;
import com.fusionalliance.internal.planpokerserver.io.WebSocketUtility;
import com.fusionalliance.internal.planpokerserver.utility.CheckCondition;
import com.fusionalliance.internal.planpokerserver.utility.CommException;
import com.fusionalliance.internal.planpokerserver.utility.InternalException;
import com.fusionalliance.internal.planpokerserver.utility.LoggerUtility;
import com.fusionalliance.internal.planpokerserver.vo.ClientRequest;
import com.fusionalliance.internal.planpokerserver.vo.ServerResponse;
import com.fusionalliance.internal.planpokerserver.vo.ServerUpdate;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class CommunicationsServer {
	private static final Logger LOG = LoggerFactory.getLogger(CommunicationsServer.class);

	private static final String KEEP_ALIVE = "Keep alive";
	private static final List<HttpHeader> ERROR_HEADERS = ImmutableList.of( //
			new HttpHeader("Content-Type", "text/plain; charset=utf-8") //
	);
	private static final Gson GSON = new Gson();

	private final ClientRequestListener clientRequestListener;
	private final VoterConnectedListener voterConnectedListener;
	private final VoterDroppedListener voterDroppedListener;
	private final Selector selector;
	private final Map<String, SocketChannel> webSocketByVoterName = new HashMap<>();

	/** The next WebSocket connect request must specify this voter name */
	private Set<String> connectPendingVoters = new HashSet<>();

	public CommunicationsServer(
			final ClientRequestListener clientRequestListenerParm, //
			final VoterConnectedListener voterConnectedListenerParm, //
			final VoterDroppedListener voterDroppedListenerParm) {
		check(clientRequestListenerParm != null, "The ClientRequestListner may not be null.");
		check(voterConnectedListenerParm != null, "The VoterConnectedListener may not be null.");
		check(voterDroppedListenerParm != null, "The VoterDroppedListener may not be null.");

		clientRequestListener = clientRequestListenerParm;
		voterConnectedListener = voterConnectedListenerParm;
		voterDroppedListener = voterDroppedListenerParm;

		try {
			selector = Selector.open();
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Failed to open selector.", false, e);

			throw new InternalException();
		}

		try {
			final ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.bind(new InetSocketAddress(40080));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Failed to create server socket.", false, e);

			throw new InternalException();
		}
	}

	public void run() {
		try {
			int keysUpdated = 0;
			Iterator<SelectionKey> selectedKeyIterator = null;
			SelectionKey key = null;

			while (true) {
				keysUpdated = selector.select();

				if (keysUpdated == 0) {
					continue;
				}

				selectedKeyIterator = selector.selectedKeys().iterator();

				while (selectedKeyIterator.hasNext()) {
					key = selectedKeyIterator.next();
					selectedKeyIterator.remove();

					if (key.isAcceptable()) {
						handleAccept((ServerSocketChannel) key.channel());
					} else if (key.isReadable()) {
						handleRead(key);
					} else {
						LOG.warn("Unexpected key operation: " + key.readyOps());
					}

					key = null;
				}

				selectedKeyIterator = null;
			}
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Fatal error in selector loop!", false, e);
		} finally {
			// Add close sockets logic here
		}
	}

	/**
	 * Broadcast the server update to all connected voters.
	 * 
	 * @param serverUpdateParm required
	 */
	public void broadcastServerUpdate(final ServerUpdate serverUpdateParm) {
		check(serverUpdateParm != null, "The server update may not be null.");

		final String serverUpdateJson = GSON.toJson(serverUpdateParm);
		final WebSocketFrame webSocketFrame = new WebSocketFrame(true, WebSocketOpCode.TEXT, false,
				serverUpdateJson.getBytes(StandardCharsets.UTF_8));

		// Keep track of voters whose Web sockets failed for later removal
		final Set<String> failedVoterNames = new HashSet<>();

		for (Entry<String, SocketChannel> webSocketByVoterNameEntry : webSocketByVoterName.entrySet()) {
			try {
				SocketChannelUtility.writeToSocket(webSocketFrame.getReadOnlyBuffer(), webSocketByVoterNameEntry.getValue());
			} catch (final CommException ce) {
				failedVoterNames.add(webSocketByVoterNameEntry.getKey());
			}
		}

		for (String failedVoterName : failedVoterNames) {
			voterDroppedListener.handleVoterDropped(failedVoterName);
		}
	}

	/**
	 * Add a voter that is expecting a WebSocket connection.
	 * 
	 * @param voterNameParm
	 */
	public void addConnectPendingVoter(final String voterNameParm) {
		connectPendingVoters.add(voterNameParm);
	}

	/**
	 * Remove voter's WebSocket connection.
	 * 
	 * @param voterNameParm
	 */
	public void removeVoterWebSocket(final String voterNameParm) {
		final SocketChannel socket = webSocketByVoterName.remove(voterNameParm);

		final WebSocketFrame closeRequestFrame = new WebSocketFrame(true, WebSocketOpCode.CLOSE, false, new byte[0]);
		try {
			SocketChannelUtility.writeToSocket(closeRequestFrame.getReadOnlyBuffer(), socket);
		} catch (final CommException ce) {
			// Already logged, no further action
			// We expect the write to fail if the voter is being removed cause of a bad socket
		}
		try {
			socket.close();
		} catch (final Exception e) {
			// Do nothing
		}
	}

	private void handleAccept(ServerSocketChannel serverSocketParm) {
		try {
			final SocketChannel socket = serverSocketParm.accept();

			if (socket == null) {
				System.out.println("Null on accept.");

				return;
			}

			socket.configureBlocking(false);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
			socket.register(selector, SelectionKey.OP_READ);
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Error accepting socket.", false, e);
		}
	}

	private void handleRead(final SelectionKey keyParm) throws CommException {
		try {
			final SocketChannel socket = (SocketChannel) keyParm.channel();

			if (!socket.isConnected()) {
				return;
			}

			final ByteBuffer buffer = SocketChannelUtility.readUnblocked(socket);

			// Socket closed during read
			if (buffer == null) {
				return;
			}

			buffer.flip();
			final HttpRequest request = new HttpRequest(buffer);
			buffer.clear();

			if (request.getMethod() == HttpRequestMethods.GET) {
				// Standard GET request
				if (!request.getHeaders().containsKey("Upgrade")) {
					processGet(request.getPath(), socket);

					return;
				}

				// Handle upgrade to WebSocket request
				processWebSocket(request, socket);

				return;
			}

			if (request.getMethod() == HttpRequestMethods.POST) {
				processPost(request.getBody(), socket);

				return;
			}

			LOG.warn("Unknown request method: " + request.getMethod().name());
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Error while reading socket.", false, e);
		}
	}

	private void processGet(final String pathParm, final SocketChannel socketParm) {
		final URL pathUrl = getClass().getResource("/dist" + pathParm);

		// Resource not found
		if (pathUrl == null) {
			LOG.warn("Unknown resource requested: " + pathParm);
			try {
				HttpUtility.writeToSocket("HTTP/1.1 404 Not Found", ERROR_HEADERS, "404 Not Found: " + pathParm, socketParm);
			} catch (final Exception e) {
				LoggerUtility.logIssueWithStackTrace(LOG, "Failed to send 404 response to client.", false, e);
			}

			return;
		}

		final InputStream inputStream;
		try {
			inputStream = pathUrl.openStream();
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Unable to open input stream for resource: " + pathParm, true, e);

			try {
				HttpUtility.writeToSocket("HTTP/1.1 500 Internal Error", ERROR_HEADERS, "500 Internal error processing: " + pathParm, socketParm);
			} catch (final Exception e1) {
				LoggerUtility.logIssueWithStackTrace(LOG, "Failed to send 500 response to client.", false, e1);
			}

			return;
		}

		final int separator = pathParm.lastIndexOf('.');
		final String extension;
		if (separator == -1) {
			extension = "";
		} else {
			extension = pathParm.substring(separator + 1);
		}
		final String contentType;
		if (HttpUtility.CONTENT_TYPE_MAP.containsKey(extension)) {
			contentType = HttpUtility.CONTENT_TYPE_MAP.get(extension);
		} else {
			contentType = HttpUtility.CONTENT_TYPE_MAP.get("");
		}

		try {
			final List<HttpHeader> headers = ImmutableList.of(new HttpHeader("Content-Type", contentType));
			HttpUtility.writeToSocket("HTTP/1.1 200 OK", headers, inputStream, socketParm);
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Failed to send resource: " + pathParm, true, e);
		} finally {
			try {
				inputStream.close();
			} catch (final Exception e) {
				// do nothing
			}
		}
	}

	private void processWebSocket(final HttpRequest requestParm, final SocketChannel socket) throws CommException {
		// Stop handling read requests on the read selector
		// WebSocket requests must be handled separately
		socket.keyFor(selector).cancel();

		// Verify that the path is properly formed and that this upgrade was preceded by a join request
		check(requestParm.getPathSteps().size() == 2, "Invalid WebSocket request path: " + requestParm.getPath());
		check("webSocket".equals(requestParm.getPathSteps().get(0)),
				"WebSocket request path does not start with 'webSocket': " + requestParm.getPath());
		final String voterName = requestParm.getPathSteps().get(1);
		check(connectPendingVoters.contains(voterName), "WebSocket voter name has no prior join request: " + voterName);

		connectPendingVoters.remove(voterName);

		try {
			socket.configureBlocking(true);
			// We only read for ping/pong responses; expect them to come back quickly
			socket.socket().setSoTimeout(500);
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Unable to configure WebSocket for blocking.", false, e);

			throw new CommException();
		}

		final String secWebSocketKey = requestParm.getHeaders().get(WebSocketUtility.SEC_WEBSOCKET_KEY);
		CheckCondition.check(StringUtils.isNotBlank(secWebSocketKey), "Sec-WebSocket-Key is missing.");

		List<HttpHeader> headers = new ArrayList<>();
		headers.add(new HttpHeader("Upgrade", "websocket"));
		headers.add(new HttpHeader("Connection", "Upgrade"));
		headers.add(new HttpHeader(WebSocketUtility.SEC_WEBSOCKET_ACCEPT, WebSocketUtility.calculateSecWebSocketAccept(secWebSocketKey)));

		// Perform upgrade
		HttpUtility.writeToSocket("HTTP/1.1 101 Switching Protocols", headers, new byte[0], socket);

		// Perform ping test
		if (!performWebSocketPing(socket)) {
			LoggerUtility.logIssueWithStackTrace(LOG, "WebSocket failed initial ping test.", false, null);

			throw new CommException();
		}

		webSocketByVoterName.put(voterName, socket);

		voterConnectedListener.handleVoterConnected(voterName);
	}

	/**
	 * Perform a WebSocket ping/pong. Return true if successful.
	 * 
	 * @param socketParm required
	 * @return
	 * @throws CommException
	 */
	private boolean performWebSocketPing(final SocketChannel socketParm) {
		check(socketParm != null, "The socket may not be null.");

		final String pingMessage = KEEP_ALIVE;
		final WebSocketFrame writeFrame = new WebSocketFrame(true, WebSocketOpCode.PING, false, pingMessage.getBytes(StandardCharsets.UTF_8));

		try {
			SocketChannelUtility.writeToSocket(writeFrame.getReadOnlyBuffer(), socketParm);
		} catch (final Exception e) {
			e.printStackTrace();

			return false;
		}

		// Receive Pong
		final ByteBuffer readBuffer = ByteBuffer.allocate(100);
		try {
			socketParm.read(readBuffer);
		} catch (final Exception e) {
			e.printStackTrace();

			return false;
		}

		readBuffer.flip();
		final WebSocketFrame readFrame = new WebSocketFrame(readBuffer);

		final String pingResponse = new String(readFrame.getPayload(), StandardCharsets.UTF_8);

		return KEEP_ALIVE.equals(pingResponse);
	}

	/**
	 * Handle POST. These contain requests from the clients.
	 * <p>
	 * If a comm error occurs and the voter name is available, then remove the voter. However, this is only possible if the request is valid and the
	 * error occurs when sending a response.
	 * 
	 * @param bodyParm
	 * @param socketParm
	 */
	private void processPost(String bodyParm, SocketChannel socketParm) {
		if (StringUtils.isBlank(bodyParm)) {
			try {
				HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: Body content is missing.", socketParm);
			} catch (final Exception e) {
				LoggerUtility.logIssueWithStackTrace(LOG, "Unable to write body content missing response.", false, e);
				;
			}

			return;
		}

		final ClientRequest clientRequest;

		try {
			clientRequest = GSON.fromJson(bodyParm, ClientRequest.class);
		} catch (final Exception e) {
			e.printStackTrace();

			try {
				HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: Body content not recognized: " + bodyParm,
						socketParm);
			} catch (final Exception e1) {
				LoggerUtility.logIssueWithStackTrace(LOG, "Unable to write body content not recognized response.", false, e);
				;
			}

			return;
		}

		final String errorMessage = clientRequest.validate();
		if (errorMessage != null) {
			try {
				HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: " + errorMessage, socketParm);
			} catch (final Exception e) {
				LoggerUtility.logIssueWithStackTrace(LOG, "Unable to write body content invalid response.", false, e);
				;
			}

			return;
		}

		final ServerResponse clientResponse = clientRequestListener.handleRequestReceived(clientRequest);

		final String clientResponseJson = GSON.toJson(clientResponse);

		final List<HttpHeader> headers = ImmutableList.of( //
				new HttpHeader("Content-Type", HttpUtility.CONTENT_TYPE_MAP.get("json")) //
		);
		try {
			HttpUtility.writeToSocket("HTTP/1.1 200 OK", headers, clientResponseJson, socketParm);
		} catch (final Exception e) {
			LoggerUtility.logIssueWithStackTrace(LOG, "Unable to write OK response.", false, e);
		}

		return;
	}

	/**
	 * This interface defines the contract for classes that listen for client request events.
	 */
	public interface ClientRequestListener extends EventListener {
		/**
		 * Process a client request, returning a server response.
		 * 
		 * @param clientRequest required
		 * @return not null
		 */
		ServerResponse handleRequestReceived(final ClientRequest clientRequest);
	}

	/**
	 * This interface defines the contract for classes that listen for voter connected events.
	 * <p>
	 * The model is not informed of a new voter until the WebSocket is established.
	 */
	public interface VoterConnectedListener extends EventListener {
		/**
		 * Process a voter connected event.
		 * <p>
		 * Voters connect by establishing WebSocket connection.
		 * 
		 * @param voterName required
		 */
		void handleVoterConnected(final String voterName);
	}

	/**
	 * This interface defines the contract for classes that listen for voter dropped events. Dropped events are caused by comm failures.
	 * <p>
	 * Voter dropped involves closing the WebSocket and informing the model.
	 */
	public interface VoterDroppedListener extends EventListener {
		/**
		 * Process a voter dropped event.
		 * 
		 * @param voterName required
		 */
		void handleVoterDropped(final String voterName);
	}
}
