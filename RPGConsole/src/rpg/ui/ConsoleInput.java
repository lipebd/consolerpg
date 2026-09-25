package rpg.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// Provides centralized UTF-8 console input operations.
public final class ConsoleInput {

	private static final String WINDOWS_CONSOLE_INPUT_DEVICE = "CONIN$";

	private final BlockingQueue<InputResult> pendingInputs;
	private final BufferedReader reader;
	private final boolean recoverableEndOfInput;

	private volatile boolean discardingInput;

	public ConsoleInput() {
		BufferedReader windowsConsoleReader = openWindowsConsoleReader();

		if (windowsConsoleReader != null) {
			reader = windowsConsoleReader;
			recoverableEndOfInput = true;
		} else {
			reader = createReader(System.in);
			recoverableEndOfInput = false;
		}

		pendingInputs = new LinkedBlockingQueue<>();

		startInputReader();
	}

	public ConsoleInput(InputStream inputStream) {
		reader = createReader(Objects.requireNonNull(inputStream, "Input stream cannot be null."));

		recoverableEndOfInput = false;
		pendingInputs = new LinkedBlockingQueue<>();

		startInputReader();
	}

	public String readLine() {
		while (true) {
			InputResult result;

			try {
				result = pendingInputs.take();
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();

				throw new IllegalStateException("Console input waiting was interrupted.", exception);
			}

			if (result.failure() != null) {
				throw result.failure();
			}

			if (result.line() != null) {
				return result.line();
			}

			ConsoleUI.displayIgnoredEndOfInput();
		}
	}

	public String readTrimmedLine() {
		return readLine().trim();
	}

	public Optional<String> pollLine(long timeoutMillis) {
		if (timeoutMillis < 0) {
			throw new IllegalArgumentException("Input timeout cannot be negative.");
		}

		InputResult result;

		try {
			result = pendingInputs.poll(timeoutMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();

			throw new IllegalStateException("Console input waiting was interrupted.", exception);
		}

		if (result == null) {
			return Optional.empty();
		}

		if (result.failure() != null) {
			throw result.failure();
		}

		return Optional.ofNullable(result.line());
	}

	public void discardPendingInput() {
		pendingInputs.removeIf(result -> result.failure() == null);
	}

	void beginDiscardingInput() {
		discardingInput = true;
		discardPendingInput();
	}

	void endDiscardingInput() {
		discardPendingInput();
		discardingInput = false;
	}

	private void startInputReader() {
		Thread inputReader = new Thread(this::readInputContinuously, "console-input-reader");

		inputReader.setDaemon(true);
		inputReader.start();
	}

	private void readInputContinuously() {
		while (true) {
			try {
				String line = reader.readLine();

				if (line == null) {
					if (recoverableEndOfInput) {
						offerInput(new InputResult(null, null));

						continue;
					}

					pendingInputs.offer(new InputResult(null, new EndOfInputException()));

					return;
				}

				offerInput(new InputResult(line, null));
			} catch (IOException exception) {
				pendingInputs.offer(
						new InputResult(null, new UncheckedIOException("Console input could not be read.", exception))
				);

				return;
			}
		}
	}

	private void offerInput(InputResult result) {
		if (!discardingInput) {
			pendingInputs.offer(result);
		}
	}

	private static BufferedReader openWindowsConsoleReader() {
		String operatingSystemName = System.getProperty("os.name", "");

		if (!operatingSystemName.startsWith("Windows")) {
			return null;
		}

		try {
			return createReader(new FileInputStream(WINDOWS_CONSOLE_INPUT_DEVICE));
		} catch (IOException | SecurityException exception) {
			return null;
		}
	}

	private static BufferedReader createReader(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
	}

	private record InputResult(String line, RuntimeException failure) {
	}

	// Signals that no more console input can be read.
	public static final class EndOfInputException extends RuntimeException {

		private EndOfInputException() {
			super("Console input reached its end.");
		}
	}
}