package local.shared;

import java.util.HashMap;
import java.util.Map;

public enum WebSocketOpCode {
	CONTINUATION(0x0), //
	TEXT(0x1), //
	BINARY(0x2), //
	CLOSE(0x8), //
	PING(0x9), //
	PONG(0xA), //
	;

	private static final Map<Integer, WebSocketOpCode> opCodeByNumericCode = new HashMap<>();

	public static WebSocketOpCode findOpCodeByNumericCode(final int numericCodeParm) {
		if (opCodeByNumericCode.isEmpty()) {
			synchronized (opCodeByNumericCode) {
				if (opCodeByNumericCode.isEmpty()) {
					for (WebSocketOpCode opCode : WebSocketOpCode.values()) {
						opCodeByNumericCode.put(Integer.valueOf(opCode.getNumericCode()), opCode);
					}
				}
			}
		}

		WebSocketOpCode opcode = opCodeByNumericCode.get(Integer.valueOf(numericCodeParm));

		if (opcode == null) {
			throw new IllegalArgumentException("No WebSocket OpCode for: " + numericCodeParm);
		}

		return opcode;
	}

	private final int numericCode;

	private WebSocketOpCode(final int numericCodeParm) {
		numericCode = numericCodeParm;
	}

	public int getNumericCode() {
		return numericCode;
	}
}
