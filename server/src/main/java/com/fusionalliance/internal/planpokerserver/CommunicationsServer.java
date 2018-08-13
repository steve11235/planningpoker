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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.io.HttpHeader;
import com.fusionalliance.internal.planpokerserver.io.HttpRequest;
import com.fusionalliance.internal.planpokerserver.io.HttpRequestMethods;
import com.fusionalliance.internal.planpokerserver.io.HttpUtility;
import com.fusionalliance.internal.planpokerserver.io.SocketChannelUtility;
import com.fusionalliance.internal.planpokerserver.io.WebSocketFrame;
import com.fusionalliance.internal.planpokerserver.io.WebSocketOpCode;
import com.fusionalliance.internal.planpokerserver.io.WebSocketUtility;
import com.fusionalliance.internal.planpokerserver.utility.CheckCondition;
import com.fusionalliance.internal.planpokerserver.utility.InternalException;
import com.fusionalliance.internal.planpokerserver.vo.ClientRequest;
import com.fusionalliance.internal.planpokerserver.vo.ServerResponse;
import com.fusionalliance.internal.planpokerserver.vo.ServerUpdate;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class CommunicationsServer {
	private static final String KEEP_ALIVE = "Keep alive";
	private static final List<HttpHeader> ERROR_HEADERS = ImmutableList.of( //
			new HttpHeader("Content-Type", "text/plain; charset=utf-8") //
	);
	private static final Gson GSON = new Gson();

	private final ClientRequestListener clientRequestListener;
	private final VoterConnectedListener voterConnectedListener;
	private final Selector selector;
	private final Map<String, SocketChannel> webSocketByVoterName = new HashMap<>();

	/** The next WebSocket connect request must specify this voter name */
	private Set<String> connectPendingVoters = new HashSet<>();

	public CommunicationsServer(final ClientRequestListener clientRequestListenerParm, final VoterConnectedListener voterConnectedListenerParm) {
		check(clientRequestListenerParm != null, "The ClientRequestListner may not be null.");
		check(voterConnectedListenerParm != null, "The VoterConnectedListener may not be null.");

		clientRequestListener = clientRequestListenerParm;
		voterConnectedListener = voterConnectedListenerParm;

		try {
			selector = Selector.open();
		}
		catch (final Exception e) {
			throw new InternalException("Failed to open selector.", e);
		}

		try {
			final ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.bind(new InetSocketAddress(40080));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (final Exception e) {
			throw new InternalException("Failed to create server socket.", e);
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
					}
					else if (key.isReadable()) {
						handleRead(key);
					}
					else {
						System.err.println("Unexpected key operation: " + key.readyOps());
					}

					key = null;
				}

				selectedKeyIterator = null;
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		finally {
			// Add close sockets logic here
		}

	}

	private void handleAccept(ServerSocketChannel serverSocketParm) {
		final SocketChannel socket;

		try {
			socket = serverSocketParm.accept();

			if (socket == null) {
				System.out.println("Null on accept.");

				return;
			}

			socket.configureBlocking(false);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
			socket.register(selector, SelectionKey.OP_READ);
		}
		catch (final Exception e) {
			throw new InternalException("Error accepting socket.", e);
		}
	}

	private void handleRead(final SelectionKey keyParm) {
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
			processPost(request.getPathSteps(), request.getBody(), socket);

			return;
		}

		throw new InternalException("Unknown request type: " + request.getMethod().name());
	}

	private void processGet(final String pathParm, final SocketChannel socketParm) {
		final URL pathUrl = getClass().getResource("/dist" + pathParm);

		// Resource not found
		if (pathUrl == null) {
			HttpUtility.writeToSocket("HTTP/1.1 404 Not Found", ERROR_HEADERS, "404 Not Found: " + pathParm, socketParm);

			return;
		}

		final InputStream inputStream;
		try {
			inputStream = pathUrl.openStream();
		}
		catch (final Exception e) {
			throw new InternalException("Unable to open input stream for resource: " + pathParm, e);
		}

		final int separator = pathParm.lastIndexOf('.');
		final String extension;
		if (separator == -1) {
			extension = "";
		}
		else {
			extension = pathParm.substring(separator + 1);
		}
		final String contentType;
		if (HttpUtility.CONTENT_TYPE_MAP.containsKey(extension)) {
			contentType = HttpUtility.CONTENT_TYPE_MAP.get(extension);
		}
		else {
			contentType = HttpUtility.CONTENT_TYPE_MAP.get("");
		}

		try {
			final List<HttpHeader> headers = ImmutableList.of( //
					new HttpHeader("Content-Type", contentType) //
			);
			HttpUtility.writeToSocket("HTTP/1.1 200 OK", headers, inputStream, socketParm);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (final Exception e) {
				// do nothing
			}
		}
	}

	private void processWebSocket(final HttpRequest requestParm, final SocketChannel socket) {
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
		}
		catch (final Exception e) {
			throw new InternalException("Unable to configure WebSocket for blocking.", e);
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
			throw new InternalException("WebSocket failed initial ping test.");
		}

		webSocketByVoterName.put(voterName, socket);

		voterConnectedListener.voterConnected(voterName);
	}

	/**
	 * Perform a WebSocket ping/pong. Return true if successful.
	 * 
	 * @param socketParm
	 *            required
	 * @return
	 */
	private boolean performWebSocketPing(final SocketChannel socketParm) {
		check(socketParm != null, "The socket may not be null.");

		final String pingMessage = KEEP_ALIVE;
		final WebSocketFrame writeFrame = new WebSocketFrame(true, WebSocketOpCode.PING, false, pingMessage.getBytes(StandardCharsets.UTF_8));

		SocketChannelUtility.writeToSocket(writeFrame.getReadOnlyBuffer(), socketParm);

		// Receive Pong
		final ByteBuffer readBuffer = ByteBuffer.allocate(100);
		try {
			socketParm.read(readBuffer);
		}
		catch (final Exception e) {
			throw new InternalException("Unable to read from WebSocket.", e);
		}

		readBuffer.flip();
		final WebSocketFrame readFrame = new WebSocketFrame(readBuffer);

		final String pingResponse = new String(readFrame.getPayload(), StandardCharsets.UTF_8);

		return KEEP_ALIVE.equals(pingResponse);
	}

	/**
	 * Handle POST. These contain requests from the clients.
	 * 
	 * @param pathStepsParm
	 * @param bodyParm
	 * @param socketParm
	 */
	private void processPost(List<String> pathStepsParm, String bodyParm, SocketChannel socketParm) {
		if (StringUtils.isBlank(bodyParm)) {
			HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: Body content is missing.", socketParm);

			return;
		}

		final ClientRequest clientRequest;

		try {
			clientRequest = GSON.fromJson(bodyParm, ClientRequest.class);
		}
		catch (final Exception e) {
			HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: Body content not recognized: " + bodyParm,
					socketParm);

			return;
		}

		final String errorMessage = clientRequest.validate();
		if (errorMessage != null) {
			HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: " + errorMessage, socketParm);

			return;
		}

		final ServerResponse clientResponse = clientRequestListener.requestReceived(clientRequest);

		final String clientResponseJson = GSON.toJson(clientResponse);

		final List<HttpHeader> headers = ImmutableList.of( //
				new HttpHeader("Content-Type", HttpUtility.CONTENT_TYPE_MAP.get("json")) //
		);
		HttpUtility.writeToSocket("HTTP/1.1 200 OK", headers, clientResponseJson, socketParm);

		return;
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
	 * Broadcast the server update to all connected voters.
	 * 
	 * @param serverUpdateParm
	 *            required
	 */
	public void broadcastServerUpdate(final ServerUpdate serverUpdateParm) {
		check(serverUpdateParm != null, "The server update may not be null.");

		final String serverUpdateJson = GSON.toJson(serverUpdateParm);
		final WebSocketFrame webSocketFrame = new WebSocketFrame(true, WebSocketOpCode.TEXT, false,
				serverUpdateJson.getBytes(StandardCharsets.UTF_8));

		for (SocketChannel socket : webSocketByVoterName.values()) {
			SocketChannelUtility.writeToSocket(webSocketFrame.getReadOnlyBuffer(), socket);
		}
	}

	/**
	 * Remove voter's WebSocket connection.
	 * 
	 * @param voterNameParm
	 */
	public void removeConnectedVoter(final String voterNameParm) {
		final SocketChannel socket = webSocketByVoterName.remove(voterNameParm);

		final WebSocketFrame closeRequestFrame = new WebSocketFrame(true, WebSocketOpCode.CLOSE, false, new byte[0]);
		SocketChannelUtility.writeToSocket(closeRequestFrame.getReadOnlyBuffer(), socket);
		try {
			socket.close();
		}
		catch (final Exception e) {
			// Do nothing
		}
	}

	/**
	 * This interface defines the contract for classes that listen for client request events.
	 * <p>
	 * Implementers must return a non-null ClientResponse.
	 */
	public interface ClientRequestListener extends EventListener {
		ServerResponse requestReceived(final ClientRequest clientRequest);
	}

	/**
	 * This interface defines the contract for classes that listen for voter connected events.
	 */
	public interface VoterConnectedListener extends EventListener {
		void voterConnected(final String voterName);
	}
}
