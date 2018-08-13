package com.fusionalliance.internal.planpokerserver.io;

import static com.fusionalliance.internal.planpokerserver.utility.CheckCondition.check;

import java.nio.ByteBuffer;

/**
 * This class implements a WebSocket frame.
 * <ul>
 * <li>byte 001 Fin, RSV1, RSV2, RSV3, opcode(4)</li>
 * <li>byte 002 MASK, byte payload len(7)</li>
 * <li>byte 003 (optional) short payload len OR long payload len</li>
 * <li>byte 004 (optional) short payload len OR long payload len</li>
 * <li>byte 005 (optional) long payload len</li>
 * <li>byte 006 (optional) long payload len</li>
 * <li>byte 007 (optional) long payload len</li>
 * <li>byte 008 (optional) long payload len</li>
 * <li>byte 009 (optional) long payload len</li>
 * <li>byte 010 (optional) long payload len</li>
 * <li>byte 003 OR 005 OR 011 mask 1</li>
 * <li>byte 004 OR 006 OR 012 mask 2</li>
 * <li>byte 005 OR 007 OR 013 mask 3</li>
 * <li>byte 006 OR 008 OR 014 mask 4</li>
 * <li>byte 007 OR 009 OR 015 payload data</li>
 * </ul>
 */
public class WebSocketFrame {
	private static final int MAX_PAYLOAD_LENGTH = 0x2800;

	private final boolean finished;
	private final WebSocketOpCode opCode;
	private final boolean masked;
	/** Contrary to the RFC, we do not accept payloads over 10K in length */
	private final int payloadLength;
	private final byte[] mask;
	private final byte[] payload;
	private final ByteBuffer readOnlyBuffer;

	/**
	 * Constructor using fields.
	 * 
	 * @param finishedParm
	 * @param opCodeParm
	 *            required
	 * @param maskedParm
	 * @param payloadParm
	 *            required, length < =10k
	 */
	public WebSocketFrame(final boolean finishedParm, final WebSocketOpCode opCodeParm, final boolean maskedParm, final byte[] payloadParm) {
		check(opCodeParm != null, "OP code is null.");
		check(payloadParm != null, "Payload is null.");
		check(payloadParm.length <= MAX_PAYLOAD_LENGTH, "Payload length is too long: " + payloadParm.length);

		finished = finishedParm;
		opCode = opCodeParm;
		masked = maskedParm;
		payloadLength = payloadParm.length;
		payload = payloadParm;

		final ByteBuffer tempBuffer = ByteBuffer.allocate(payloadLength + 14);

		byte currentByte = 0;

		// FIN, OpCode
		currentByte = (byte) opCode.getNumericCode();
		if (finished) {
			currentByte |= 0x80;
		}
		tempBuffer.put(currentByte);

		// MASK, byte payload length
		if (payloadLength < 126) {
			currentByte = (byte) payloadLength;
		}
		else {
			currentByte = (byte) 126;
		}
		if (masked) {
			currentByte |= 0x80;
		}
		tempBuffer.put(currentByte);

		if (payloadLength >= 126) {
			tempBuffer.putShort((short) payloadLength);
		}

		if (!masked) {
			mask = new byte[] { 0, 0, 0, 0 };
		}
		else {
			mask = generateMask();

			tempBuffer.put(mask[0]);
			tempBuffer.put(mask[1]);
			tempBuffer.put(mask[2]);
			tempBuffer.put(mask[3]);

			applyMask();
		}

		tempBuffer.put(payload);

		tempBuffer.flip();

		readOnlyBuffer = tempBuffer.asReadOnlyBuffer();
	}

	/**
	 * Constructor from an incoming message.
	 *
	 * @param requestBufferParm
	 *            required, must be flipped, no side-effects on passed buffer
	 */
	public WebSocketFrame(final ByteBuffer requestBufferParm) {
		check(requestBufferParm != null, "Request ByteBuffer is null.");
		check(requestBufferParm.position() == 0, "Request ByteBuffer not flipped (positon not 0): " + requestBufferParm.position());

		// Create a deep copy of the input data
		readOnlyBuffer = ByteBuffer.allocate(requestBufferParm.limit()).put(requestBufferParm);
		readOnlyBuffer.flip();

		byte currentByte = 0;

		// Fin, RSV1-3, op code
		currentByte = readOnlyBuffer.get();

		finished = (currentByte & 0x80) != 0;
		opCode = WebSocketOpCode.findOpCodeByNumericCode(currentByte & 0x0F);

		// Mask set, byte payload length
		currentByte = readOnlyBuffer.get();
		masked = (currentByte & 0x80) != 0;

		final int bytePayloadLength = currentByte & 0x7F;

		if (bytePayloadLength < 126) {
			payloadLength = bytePayloadLength;
		}
		else if (bytePayloadLength == 126) {
			payloadLength = readOnlyBuffer.getShort();
		}
		else {
			// 127: We support this only in case a caller always uses this format, even for reasonable values
			final long longPayloadLength = readOnlyBuffer.getLong();
			payloadLength = (longPayloadLength <= Integer.MAX_VALUE ? (int) longPayloadLength : Integer.MAX_VALUE);
		}

		check(payloadLength <= MAX_PAYLOAD_LENGTH, "Incoming payload length is too large: " + payloadLength);

		// Client messages must always be masked; server messages are not masked
		mask = new byte[] { 0, 0, 0, 0 };
		if (masked) {
			mask[0] = readOnlyBuffer.get();
			mask[1] = readOnlyBuffer.get();
			mask[2] = readOnlyBuffer.get();
			mask[3] = readOnlyBuffer.get();
		}

		payload = new byte[payloadLength];
		readOnlyBuffer.get(payload);

		readOnlyBuffer.rewind();

		applyMask();
	}

	/**
	 * Generate a random mask for this frame.
	 * <p>
	 * For the purposes of the RFC, the resulting mask is sufficiently random.
	 * 
	 * @return
	 */
	private byte[] generateMask() {
		final byte[] mask = new byte[4];
		long longValue = System.currentTimeMillis() + System.nanoTime();

		for (int i = 0; i < 4; i++) {
			mask[i] = (byte) (longValue % 0xFF);
			longValue /= 0xFF;
		}

		return mask;
	}

	/**
	 * Apply the mask to the payload, if masked.
	 * <p>
	 * This operation is reversible.
	 */
	private void applyMask() {
		if (!masked) {
			return;
		}

		int maskIndex = 0;
		for (int i = 0; i < payloadLength; i++) {
			payload[i] ^= mask[maskIndex];

			maskIndex++;
			if (maskIndex >= mask.length) {
				maskIndex = 0;
			}
		}
	}

	public boolean isFinished() {
		return finished;
	}

	public WebSocketOpCode getOpCode() {
		return opCode;
	}

	public boolean isMasked() {
		return masked;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public byte[] getMask() {
		return mask;
	}

	public byte[] getPayload() {
		return payload;
	}

	/**
	 * Return a read-only ByteBuffer that is ready for read (position = 0, limit = content length.)
	 * 
	 * @return
	 */
	public ByteBuffer getReadOnlyBuffer() {
		readOnlyBuffer.rewind();

		return readOnlyBuffer;
	}
}
