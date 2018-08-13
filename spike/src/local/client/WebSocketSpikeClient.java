package local.client;

import static local.shared.HttpUtility.calculateSecWebSocketAccept;
import static local.shared.HttpUtility.generateSecWebSocketKey;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import local.shared.HttpHeader;
import local.shared.HttpUtility;
import local.shared.WebSocketFrame;
import local.shared.WebSocketOpCode;

public class WebSocketSpikeClient {
	private final String WEBSOCKET_KEY = generateSecWebSocketKey("This is a random phrase.");
	private final String WEBSOCKET_ACCEPT = calculateSecWebSocketAccept(WEBSOCKET_KEY);

	public static void main(final String... args) {
		new WebSocketSpikeClient().run();
	}

	private final SocketChannel socket;

	public WebSocketSpikeClient() {
		try {
			socket = SocketChannel.open(new InetSocketAddress(40080));
			socket.configureBlocking(true);
		}
		catch (final Exception e) {
			e.printStackTrace();

			throw new RuntimeException("Constructor failed.");
		}
	}

	public void run() {
		try {
			final List<HttpHeader> headers = new ArrayList<>();
			headers.add(new HttpHeader("Host", "localhost:40080"));
			headers.add(new HttpHeader("Upgrade", "websocket"));
			headers.add(new HttpHeader("Connection", "upgrade"));
			headers.add(new HttpHeader(HttpUtility.SEC_WEBSOCKET_KEY, WEBSOCKET_KEY));
			headers.add(new HttpHeader("Sec-WebSocket-Version", "13"));

			HttpUtility.writeToSocket("GET /websocket/Steve HTTP/1.1", headers, new byte[0], socket);

			System.out.println("Message sent");

			final ByteBuffer buffer = ByteBuffer.allocate(8000);
			socket.read(buffer);

			buffer.flip();

			final byte[] readBytes = new byte[buffer.limit()];
			buffer.get(readBytes);
			final String response = new String(readBytes, StandardCharsets.UTF_8).trim();
			System.out.println(response);
			System.out.println(response.endsWith(WEBSOCKET_ACCEPT));

			buffer.clear();
			socket.read(buffer);
			buffer.flip();
			final WebSocketFrame readFrame = new WebSocketFrame(buffer);

			System.out.println("Ping: " + readFrame.getOpCode().name());

			final WebSocketFrame writeFrame = new WebSocketFrame(true, WebSocketOpCode.PONG, true, readFrame.getPayload());
			socket.write(writeFrame.getReadOnlyBuffer());
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				socket.close();
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
