package local.server;

import static local.shared.Assert.asserts;

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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import local.shared.HttpHeader;
import local.shared.HttpUtility;
import local.shared.InternalException;
import local.shared.Request;
import local.shared.WebSocketFrame;
import local.shared.WebSocketOpCode;

/**
 * This implements a server that accepts connections and responds to requests on each. It uses non-blocking IO to avoid latency in polling loops and
 * the creation of a thread for every connection.
 */
public class SpikeServer2 {
	private static final List<HttpHeader> ERROR_HEADERS = ImmutableList.of( //
			new HttpHeader("Content-Type", "text/plain; charset=utf-8") //
	);

	public static void main(String[] args) {
		new SpikeServer2().run();
	}

	private final Selector selector;

	private SpikeServer2() {
		try {
			selector = Selector.open();
		}
		catch (final Exception e) {
			e.printStackTrace();

			throw new RuntimeException("Constructor failed.");
		}

		try {
			final ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.bind(new InetSocketAddress(40080));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (final Exception e) {
			e.printStackTrace();

			throw new RuntimeException("Constructor failed.");
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

					try {
						if (key.isAcceptable()) {
							handleAccept((ServerSocketChannel) key.channel());
						}
						else if (key.isReadable()) {
							handleRead(key);
						}
						else {
							System.err.println("Unexpected key operation: " + key.readyOps());
						}
					}
					catch (final InternalException ie) {
						System.err.println(ie.getMessage());
						if (ie.getCause() != null) {
							ie.getCause().printStackTrace();
						}
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

	private void handleAccept(ServerSocketChannel serverSocketParm) throws InternalException {
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

	private void handleRead(final SelectionKey keyParm) throws InternalException {
		final SocketChannel socket = (SocketChannel) keyParm.channel();

		if (!socket.isConnected()) {
			return;
		}

		final ByteBuffer buffer = ByteBuffer.allocate(8000);
		try {
			final int bytesRead = socket.read(buffer);

			// Socket closed by client
			if (bytesRead == -1) {
				socket.close();

				return;
			}
		}
		catch (final Exception e) {
			throw new InternalException("Error reading socket.", e);
		}

		buffer.flip();
		final Request request = new Request(buffer);
		buffer.clear();

		if ("GET".equalsIgnoreCase(request.getMethod())) {
			// Standard request
			if (!request.getHeaders().containsKey("Upgrade")) {
				processGet(request.getPath(), socket);

				return;
			}

			// Handle upgrade to WebSocket request
			processWebSocket(request, socket);

			return;
		}

		if ("POST".equalsIgnoreCase(request.getMethod())) {
			processPost(request.getPath(), request.getBody(), socket);

			return;
		}

		throw new InternalException("Unknown request.");
	}

	private void processGet(final String pathParm, final SocketChannel socketParm) throws InternalException {
		final URL pathUrl = getClass().getResource(pathParm);

		System.out.println("GET " + pathUrl);

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
		catch (final InternalException ie) {
			throw ie;
		}
		catch (final Exception e) {
			throw new InternalException("Unable to write resource: " + pathParm, e);
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

	private void processWebSocket(final Request requestParm, final SocketChannel socket) throws InternalException {
		// Stop handling read requests on the read selector
		// WebSocket requests must be handled separately
		socket.keyFor(selector).cancel();
		try {
			socket.configureBlocking(true);
			// We only read for ping/pong responses; expect them to come back quickly
			socket.socket().setSoTimeout(500);
		}
		catch (final Exception e) {
			throw new InternalException("Unable to configure WebSocket for blocking.", e);
		}

		System.out.println("Socket registered: " + socket.isRegistered() + ", blocking: " + socket.isBlocking());

		final String secWebSocketKey = requestParm.getHeaders().get(HttpUtility.SEC_WEBSOCKET_KEY);
		asserts(StringUtils.isNotBlank(secWebSocketKey), "Sec-WebSocket-Key is missing.");

		List<HttpHeader> headers = new ArrayList<>();
		headers.add(new HttpHeader("Upgrade", "websocket"));
		headers.add(new HttpHeader("Connection", "Upgrade"));
		headers.add(new HttpHeader(HttpUtility.SEC_WEBSOCKET_ACCEPT, HttpUtility.calculateSecWebSocketAccept(secWebSocketKey)));

		// Perform upgrade
		HttpUtility.writeToSocket("HTTP/1.1 101 Switching Protocols", headers, new byte[0], socket);

		// Send Ping
		final String pingMessage = "Keep alive";
		final WebSocketFrame writeFrame = new WebSocketFrame(true, WebSocketOpCode.PING, false, pingMessage.getBytes(StandardCharsets.UTF_8));

		HttpUtility.writeToSocket(writeFrame.getReadOnlyBuffer(), socket);

		// Receive Pong
		final ByteBuffer readBuffer = ByteBuffer.allocate(100);
		try {
			socket.read(readBuffer);
		}
		catch (final Exception e) {
			throw new InternalException("Unable to read from WebSocket.", e);
		}

		readBuffer.flip();
		final WebSocketFrame readFrame = new WebSocketFrame(readBuffer);

		final String pingResponse = new String(readFrame.getPayload(), StandardCharsets.UTF_8);

		System.out.println("WS ping response: " + readFrame.getErrorMessage() + ", " + readFrame.getOpCode().name() + ", " + pingResponse);

		// Send message
		final String message = "Hello, Browser!";
		final WebSocketFrame messageFrame = new WebSocketFrame(true, WebSocketOpCode.TEXT, false, message.getBytes(StandardCharsets.UTF_8));
		HttpUtility.writeToSocket(messageFrame.getReadOnlyBuffer(), socket);

		final String message1 = "Hello, Browser...";
		final WebSocketFrame messageFrame1 = new WebSocketFrame(true, WebSocketOpCode.TEXT, false, message1.getBytes(StandardCharsets.UTF_8));
		HttpUtility.writeToSocket(messageFrame1.getReadOnlyBuffer(), socket);

		final String message2 = "Hello, Browser?";
		final WebSocketFrame messageFrame2 = new WebSocketFrame(true, WebSocketOpCode.TEXT, false, message2.getBytes(StandardCharsets.UTF_8));
		HttpUtility.writeToSocket(messageFrame2.getReadOnlyBuffer(), socket);

		// Close connection
		final WebSocketFrame closeRequestFrame = new WebSocketFrame(true, WebSocketOpCode.CLOSE, false, new byte[0]);
		HttpUtility.writeToSocket(closeRequestFrame.getReadOnlyBuffer(), socket);

		try {
			socket.close();
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void processPost(String pathParm, String bodyParm, SocketChannel socketParm) throws InternalException {
		try {
			if ("/join".equalsIgnoreCase(pathParm) && bodyParm != null && !bodyParm.isEmpty()) {
				final List<HttpHeader> headers = ImmutableList.of( //
						new HttpHeader("Content-Type", "text/plain") //
				);
				final String[] parmKeyValue = bodyParm.split("=");
				HttpUtility.writeToSocket("HTTP/1.1 200 OK", headers, parmKeyValue[1] + " joined.", socketParm);

				return;
			}

			// Unknown path
			HttpUtility.writeToSocket("HTTP/1.1 400 Bad Request", ERROR_HEADERS, "400 Bad Request: Unknown request " + pathParm + ", " + bodyParm,
					socketParm);

			return;
		}
		catch (final InternalException ie) {
			throw ie;
		}
		catch (final Exception e) {
			throw new InternalException("Unable to write resource: " + pathParm, e);
		}
	}
}
