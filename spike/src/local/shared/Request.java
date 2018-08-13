package local.shared;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a value object holding the part of an HTTP request. The constructor takes raw request bytes and parses them.
 * <p>
 * Note: This implementation expects
 * <ul>
 * <li>UTF-8 encoding</li>
 * <li>body, if any, contains text only</li>
 * <li>no message-trailers</li>
 * </ul>
 */
public class Request {
	private final String method;
	private final String path;
	private final String version;
	private final Map<String, String> headers = new HashMap<>();
	private final String body;

	/**
	 * Constructor
	 * 
	 * @param bufferParm
	 *            a ByteBuffer containing the request bytes; it <b>must</b> be flipped or processing may produce invalid results
	 */
	public Request(final ByteBuffer bufferParm) throws IllegalArgumentException {
		if (bufferParm == null || bufferParm.limit() == 0) {
			throw new IllegalArgumentException("The buffer passed is null or empty.");
		}
		final int requestLength = bufferParm.limit();

		// Search for the end of the head (two consecutive CRLF)
		final int startBody = findEndOfHead(bufferParm);

		// Drop the blank line and the trailing CRLF
		final byte[] headBytes = new byte[startBody - 4];
		bufferParm.rewind();
		bufferParm.get(headBytes);

		final String[] headLines = new String(headBytes, StandardCharsets.UTF_8).split("\\r\\n");

		// Request start line: Method Path Version
		final String[] startLinePieces = headLines[0].split(" ");
		if (startLinePieces.length != 3) {
			throw new IllegalArgumentException("Invalid request start line: " + headLines[0]);
		}

		method = startLinePieces[0];
		path = startLinePieces[1];
		version = startLinePieces[2];

		String[] keyValue = null;
		for (int i = 1; i < headLines.length; i++) {
			keyValue = headLines[i].split(": ");
			if (keyValue.length != 2) {
				throw new IllegalArgumentException("Invalid header: " + headLines[i]);
			}
			headers.put(keyValue[0], keyValue[1]);
		}

		// We only accept some form of text in the body
		// No body is provided for GET
		if (startBody == requestLength) {
			body = null;
		}
		else {
			final byte[] bodyBytes = new byte[bufferParm.limit() - startBody];
			bufferParm.position(startBody);
			bufferParm.get(bodyBytes);
			body = new String(bodyBytes, StandardCharsets.UTF_8);
		}

		validate();
	}

	private int findEndOfHead(final ByteBuffer bufferParm) {
		byte lastFourBytes1 = 0;
		byte lastFourBytes2 = 0;
		byte lastFourBytes3 = 0;
		byte lastFourBytes4 = 0;
		boolean twoCrLfFound = false;
		while (bufferParm.position() < bufferParm.limit()) {
			lastFourBytes1 = lastFourBytes2;
			lastFourBytes2 = lastFourBytes3;
			lastFourBytes3 = lastFourBytes4;
			lastFourBytes4 = bufferParm.get();
			if (lastFourBytes1 == 13 && lastFourBytes2 == 10 && lastFourBytes3 == 13 && lastFourBytes4 == 10) {
				twoCrLfFound = true;

				break;
			}
		}

		if (!twoCrLfFound) {
			throw new IllegalArgumentException("End of head not found.");
		}

		final int startBody = bufferParm.position();

		return startBody;
	}

	/**
	 * Validate the state of this instance.
	 */
	private void validate() throws IllegalArgumentException {
		// Do nothing
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getVersion() {
		return version;
	}

	public Map<String, String> getHeaders() {
		return new HashMap<>(headers);
	}

	public String getBody() {
		return body;
	}
}
