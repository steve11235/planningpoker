package local.shared;

/**
 * This class implements a value object for a HTTP header.
 */
public class HttpHeader {
	private final String key;
	private final String value;

	public HttpHeader(final String keyParm, final String valueParm) {
		super();
		key = keyParm;
		value = valueParm;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return key + ": " + value;
	}
}
