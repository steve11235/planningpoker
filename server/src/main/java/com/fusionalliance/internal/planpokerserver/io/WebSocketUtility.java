package com.fusionalliance.internal.planpokerserver.io;

import static com.fusionalliance.internal.planpokerserver.utility.CheckCondition.check;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public final class WebSocketUtility {
	public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
	public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

	/**
	 * Generate a Sec_WebSocket_Key from a phrase.
	 * <p>
	 * Note: This is <i>not</i> a security feature and the phrase need not be protected.
	 * 
	 * @param phraseParm
	 *                   not empty
	 * @return
	 */
	public static String generateSecWebSocketKey(final String phraseParm) {
		if (Strings.isNullOrEmpty(phraseParm)) {
			throw new IllegalArgumentException("The phrase must not be empty.");
		}

		final String secWebSocketKey = hashToBase64(phraseParm);

		return secWebSocketKey;
	}

	/**
	 * Calculate the Sec_WebSocket_Accept value from a Sec_WebSocket_Key value.
	 * <p>
	 * Note: This is <i>not</i> a security feature.
	 * 
	 * @param secWebSocketKeyParm
	 *                            not empty
	 * @return
	 */
	public static String calculateSecWebSocketAccept(final String secWebSocketKeyParm) {
		check(StringUtils.isNotEmpty(secWebSocketKeyParm), "The Sec-WebSocket-Key must not be empty.");

		// From RFC 6455, concatenate the "magic value" to the key
		final String secWebSocketAccept = hashToBase64(secWebSocketKeyParm + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11");

		return secWebSocketAccept;
	}

	/**
	 * Per RFC 6455, hash the input to a byte[] and encode the byte[] using base64 encoding.
	 * <p>
	 * SHA1 is deprecated because it is not secure, not because it will be removed. The RFC mandates its use; note that this is not a security
	 * feature.
	 * 
	 * @param inputParm
	 *                  required, not empty
	 * @return
	 */
	private static final String hashToBase64(final String inputParm) {
		@SuppressWarnings("deprecation")
		final byte[] bytes = Hashing.sha1().hashBytes(inputParm.getBytes(UTF_8)).asBytes();
		final String base64String = BaseEncoding.base64().encode(bytes);

		return base64String;
	}

	/**
	 * Hidden constructor
	 */
	private WebSocketUtility() {
		// Do nothing
	}
}
