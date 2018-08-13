package local.shared;

public final class Assert {

	public static void asserts(final boolean condition, final String message) {
		if (condition) {
			return;
		}

		throw new IllegalArgumentException(message);
	}

	private Assert() {
		// Do nothing
	}
}
