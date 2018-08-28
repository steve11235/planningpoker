package com.fusionalliance.internal.planpokerserver.io;

import static com.fusionalliance.internal.planpokerserver.utility.CheckCondition.check;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.fusionalliance.internal.planpokerserver.utility.InternalException;

/**
 * This utility class contains methods supporting SocketChannel operations.
 */
public final class SocketChannelUtility {

	/** Millis to wait between reads */
	private static final int READ_DELAY = 20;

	/**
	 * Read from the SocketChannel. Make reasonable efforts to retrieve the full message before returning.
	 * <p>
	 * This is a basic weakness of the HTTP protocol. It doesn't specify the message length up front, and we would have to process the message as a
	 * stream up front, searching for a content-length or the end of headers.
	 * <p>
	 * We sacrifice some performance for simplicity by reading until no more data is found, delaying after each read. Our application has low
	 * throughput, so this won't be an issue.
	 * 
	 * @param socketParm
	 *                   required
	 * @return
	 */
	public static ByteBuffer readUnblocked(final SocketChannel socketParm) {
		check(socketParm != null, "The socket channel is null.");

		final ByteBuffer buffer = ByteBuffer.allocate(8000);
		try {
			// Read with small delays until nothing is read
			while (true) {
				final int bytesRead = socketParm.read(buffer);

				// Socket closed by client
				if (bytesRead == -1) {
					socketParm.close();

					return null;
				}

				if (bytesRead == 0) {
					break;
				}

				try {
					Thread.sleep(READ_DELAY);
				} catch (final Exception e) {
					// Do nothing
				}
			}
		} catch (final Exception e) {
			throw new InternalException("Error reading socket.", e);
		}
		return buffer;
	}

	/**
	 * Write to a SocketChannel.
	 * 
	 * @param byteBufferParm
	 *                       not empty
	 * @param socketParm
	 *                       not null
	 * @throws InternalException
	 */
	public static void writeToSocket(final ByteBuffer byteBufferParm, final SocketChannel socketParm) throws InternalException {
		check(byteBufferParm != null, "The ByteBuffer must not be null.");

		// Non-blocking sockets are not guaranteed to write the full ByteBuffer
		// Loop until the write completes
		// This is an issue only with slow clients or bad connections
		while (true) {
			try {
				socketParm.write(byteBufferParm);
			} catch (final Exception e) {
				throw new InternalException("Error writing to the socket.", e);
			}

			if (!byteBufferParm.hasRemaining()) {
				break;
			}

			try {
				Thread.sleep(1);
			} catch (final Exception e) {
				// do nothing
			}
		}
		byteBufferParm.clear();
	}

	/**
	 * Hidden constructor
	 */
	private SocketChannelUtility() {
		// Do nothing
	}
}
