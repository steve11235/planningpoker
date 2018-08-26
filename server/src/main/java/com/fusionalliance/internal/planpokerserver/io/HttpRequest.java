package com.fusionalliance.internal.planpokerserver.io;

import static com.fusionalliance.internal.planpokerserver.utility.CheckCondition.check;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.utility.CheckCondition;
import com.fusionalliance.internal.planpokerserver.utility.InternalException;

/**
 * This class implements an HTTP request. The constructor takes raw request bytes and parses them.
 * <p>
 * Note: This implementation expects
 * <ul>
 * <li>the ByteBuffer contains the full content</li>
 * <li>UTF-8 encoding</li>
 * <li>body, if any, contains text only</li>
 * <li>no message-trailers</li>
 * </ul>
 */
public class HttpRequest {
	private final HttpRequestMethods method;
	private final String path;
	private final List<String> pathSteps;
	private final String version;
	private final Map<String, String> headers = new HashMap<>();
	private final String body;

	/**
	 * Constructor
	 * 
	 * @param bufferParm
	 *                   a ByteBuffer containing the request bytes; it <b>must</b> be flipped
	 */
	public HttpRequest(final ByteBuffer bufferParm) {
		check(bufferParm != null && bufferParm.limit() > 0, "The buffer passed is null or empty.");
		check(bufferParm.position() == 0, "The buffer is not flipped.");

		final int startBodyIndex = findStartBodyIndex(bufferParm);

		// Account for the index being zero-based
		// Drop the blank line and the trailing CRLF
		final byte[] headBytes = new byte[startBodyIndex - 4];
		bufferParm.get(headBytes);

		final String[] headLines = new String(headBytes, StandardCharsets.UTF_8).split("\\r\\n");

		// Request start line: Method Path Version
		final String[] startLinePieces = headLines[0].split(" ");
		check(startLinePieces.length == 3, "Invalid request start line: " + headLines[0]);

		try {
			method = HttpRequestMethods.valueOf(startLinePieces[0]);
		} catch (final Exception e) {
			throw new InternalException("Unknown request method: " + startLinePieces[0]);
		}

		// Path must have each step URL decoded, then reassembled
		pathSteps = parsePath(startLinePieces[1]);
		path = pathSteps.stream().collect(Collectors.joining("/", "/", ""));

		version = startLinePieces[2];

		String[] keyValue = null;
		for (int i = 1; i < headLines.length; i++) {
			keyValue = headLines[i].split(": ");
			check(keyValue.length == 2, "Invalid header: " + headLines[i]);

			headers.put(keyValue[0], keyValue[1]);
		}

		// We only accept some form of text in the body
		// No body is provided for GET
		bufferParm.position(startBodyIndex);
		if (bufferParm.remaining() == 0) {
			body = null;
		} else {
			final byte[] bodyBytes = new byte[bufferParm.remaining()];
			bufferParm.get(bodyBytes);
			body = new String(bodyBytes, StandardCharsets.UTF_8);
		}
	}

	/**
	 * Return a list decoded of path steps.
	 * 
	 * @param pathParm
	 *                 required
	 * @return
	 */
	private List<String> parsePath(String pathParm) {
		CheckCondition.check(StringUtils.isNotBlank(pathParm), "Path may not be blank.");

		final List<String> pathSteps = new ArrayList<>();
		final String[] pathSplits = pathParm.split("/");

		// The first element is empty (before the leading "/")
		for (int i = 1; i < pathSplits.length; i++) {
			try {
				pathSteps.add(URLDecoder.decode(pathSplits[i], StandardCharsets.UTF_8.name()));
			} catch (final Exception e) {
				throw new InternalException("Error decoding path step: " + pathSplits[i], e);
			}
		}

		return pathSteps;
	}

	/**
	 * Find the position of the start of the body.
	 * 
	 * @param bufferParm
	 * @return
	 */
	private int findStartBodyIndex(final ByteBuffer bufferParm) {
		// The head is delimited by two CRLF.
		byte lastFourBytes1 = 0;
		byte lastFourBytes2 = 0;
		byte lastFourBytes3 = 0;
		byte lastFourBytes4 = 0;

		int index = 0;
		for (index = 0; index < bufferParm.limit(); index++) {
			lastFourBytes1 = lastFourBytes2;
			lastFourBytes2 = lastFourBytes3;
			lastFourBytes3 = lastFourBytes4;
			lastFourBytes4 = bufferParm.get(index);

			if (lastFourBytes1 == 13 && lastFourBytes2 == 10 && lastFourBytes3 == 13 && lastFourBytes4 == 10) {
				break;
			}
		}

		check(index < bufferParm.limit(), "Could not find the end of the head.");

		final int startBody = index + 1;

		return startBody;
	}

	public HttpRequestMethods getMethod() {
		return method;
	}

	/**
	 * Return the decoded path.
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the decoded path as a list of steps.
	 * 
	 * @return
	 */
	public List<String> getPathSteps() {
		return pathSteps;
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
