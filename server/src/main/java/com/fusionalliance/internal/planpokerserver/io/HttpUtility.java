package com.fusionalliance.internal.planpokerserver.io;

import static com.fusionalliance.internal.planpokerserver.utility.CheckCondition.check;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fusionalliance.internal.planpokerserver.utility.InternalException;
import com.google.common.collect.ImmutableMap;

public final class HttpUtility {
	public static final String CRLF = "\r\n";
	private static final byte[] CRLF_BYTES = CRLF.getBytes(UTF_8);
	private static final int CHUNK_LENGTH = 2048;
	/**
	 * This is a very limited map of file extensions to MIME types.
	 * <p>
	 * Our .ico file is actually a PNG. <br>
	 * Only extensions used by <i>our</i> Angular app are used. Note that .map files are Angular files containing JSON.
	 * <p>
	 * All text-type files should use the UTF-8 charset.
	 */
	public static final Map<String, String> CONTENT_TYPE_MAP = new ImmutableMap.Builder<String, String>() //
			.put("html", "text/html; charset=utf-8") //
			.put("js", "application/javascript") //
			.put("css", "text/css; charset=utf-8") //
			.put("png", "image/png") //
			.put("ico", "image/png") //
			.put("json", "application/json") //
			.put("map", "application/json") //
			.put("txt", "text/plain; charset=utf-8") //
			.put("", "application/octet-stream") //
			.build();

	/**
	 * Write to a SocketChannel.
	 * 
	 * @param responseStatusParm
	 *                           required
	 * @param headersParm
	 *                           required; do <i>not</i> include Content-Length or Transfer-Encoding: chunked
	 * @param bodyParm
	 *                           null or empty if no body
	 * @param socketParm
	 *                           required
	 * @throws InternalException
	 */
	public static void writeToSocket(final String responseStatusParm, final List<HttpHeader> headersParm, final String bodyParm,
			final SocketChannel socketParm) throws InternalException {
		final byte[] bodyBytes;

		if (StringUtils.isEmpty(bodyParm)) {
			bodyBytes = new byte[0];
		} else {
			bodyBytes = bodyParm.getBytes(UTF_8);
		}

		writeToSocket(responseStatusParm, headersParm, bodyBytes, socketParm);
	}

	/**
	 * Write to a SocketChannel.
	 * 
	 * @param responseStatusParm
	 *                           required
	 * @param headersParm
	 *                           required; do <i>not</i> include Content-Length or Transfer-Encoding: chunked
	 * @param bodyBytesParm
	 *                           required, may be empty
	 * @param socketParm
	 *                           required
	 * @throws InternalException
	 */
	public static void writeToSocket(final String responseStatusParm, final List<HttpHeader> headersParm, final byte[] bodyBytesParm,
			final SocketChannel socketParm) throws InternalException {
		// If no body, just send headers with no Content-Length
		if (bodyBytesParm.length == 0) {
			final byte[] headerBytes = assembleHead(responseStatusParm, headersParm);
			final ByteBuffer buffer = ByteBuffer.wrap(headerBytes);
			buffer.position(headerBytes.length);
			SocketChannelUtility.writeToSocket(buffer, socketParm);

			return;
		}

		// If the body is reasonably small, send the response all at once with a Content-Length
		if (bodyBytesParm.length <= CHUNK_LENGTH) {
			final List<HttpHeader> allHeaders = new ArrayList<>(headersParm);
			allHeaders.add(new HttpHeader("Content-Length", Integer.toString(bodyBytesParm.length)));
			final byte[] headerBytes = assembleHead(responseStatusParm, allHeaders);

			final ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + bodyBytesParm.length);
			byteBuffer.put(headerBytes);
			byteBuffer.put(bodyBytesParm);
			SocketChannelUtility.writeToSocket(byteBuffer, socketParm);

			return;
		}
	}

	/**
	 * Write to a SocketChannel.
	 * 
	 * @param responseStatusParm
	 *                           required
	 * @param headersParm
	 *                           required, may be empty
	 * @param bodyStreamParm
	 *                           required; always closed before method return
	 * @param socketParm
	 *                           required
	 * @throws InternalException
	 */
	public static void writeToSocket(final String responseStatusParm, final List<HttpHeader> headersParm, final InputStream bodyStreamParm,
			final SocketChannel socketParm) throws InternalException {
		check(bodyStreamParm != null, "Body input stream may not be null.");

		final BufferedInputStream bufferedBodyStream;
		if (bodyStreamParm instanceof BufferedInputStream) {
			bufferedBodyStream = (BufferedInputStream) bodyStreamParm;
		} else {
			bufferedBodyStream = new BufferedInputStream(bodyStreamParm);
		}

		try {
			final List<HttpHeader> allHeaders = new ArrayList<>(headersParm);
			allHeaders.add(new HttpHeader("Transfer-Encoding", "chunked"));

			final byte[] headBytes = assembleHead(responseStatusParm, allHeaders);

			// Avoid copying the array; requires setting the position
			final ByteBuffer headBuffer = ByteBuffer.wrap(headBytes);
			headBuffer.position(headBytes.length);

			SocketChannelUtility.writeToSocket(headBuffer, socketParm);

			// The buffer needs to be large enough to hold the chunk plus the up to 3 hex-digit length and two CRLF
			// 800RNbodyRN
			final byte[] bodyBytes = new byte[CHUNK_LENGTH];
			final ByteBuffer bodyBuffer = ByteBuffer.allocate(CHUNK_LENGTH + 7);
			int bytesRead = 0;

			while (true) {
				bytesRead = bufferedBodyStream.read(bodyBytes);
				if (bytesRead == -1) {
					break;
				}

				bodyBuffer.clear();
				bodyBuffer.put(Integer.toHexString(bytesRead).getBytes(UTF_8));
				bodyBuffer.put(CRLF_BYTES);
				bodyBuffer.put(bodyBytes, 0, bytesRead);
				bodyBuffer.put(CRLF_BYTES);

				SocketChannelUtility.writeToSocket(bodyBuffer, socketParm);
			}

			bodyBuffer.clear();
			bodyBuffer.put("0".getBytes(UTF_8));
			bodyBuffer.put(CRLF_BYTES);
			bodyBuffer.put(CRLF_BYTES);

			SocketChannelUtility.writeToSocket(bodyBuffer, socketParm);
		} catch (final Exception e) {
			throw new InternalException("Failure during chunked write.", e);
		} finally {
			try {
				bufferedBodyStream.close();
			} catch (final Exception e) {
				// Do nothing
			}
		}
	}

	/**
	 * Return a byte[] containing the response status and headers.
	 * 
	 * @param responseStatusParm
	 * @param headersParm
	 * @return
	 */
	private static byte[] assembleHead(String responseStatusParm, List<HttpHeader> headersParm) {
		final StringBuilder responseBuilder = new StringBuilder(500);

		responseBuilder.append(responseStatusParm).append(CRLF);

		for (HttpHeader header : headersParm) {
			responseBuilder.append(header.toString()).append(CRLF);
		}

		responseBuilder.append(CRLF);

		return responseBuilder.toString().getBytes(UTF_8);
	}

	private HttpUtility() {
		// Do nothing
	}
}
