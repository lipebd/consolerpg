package rpg.persistence;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

// Decodes a strict UTF-8 property payload and rejects malformed structures.
public final class PropertyPayloadDecoder {

	private PropertyPayloadDecoder() {
	}

	public static Properties decode(byte[] encodedPayload) {
		Objects.requireNonNull(encodedPayload, "Encoded payload cannot be null.");

		String payload = decodeUtf8(encodedPayload);

		if (payload.isEmpty()) {
			throw new IllegalArgumentException("Property payload cannot be empty.");
		}

		if (payload.startsWith("\uFEFF")) {
			throw new IllegalArgumentException("Property payload cannot contain a UTF-8 BOM.");
		}

		if (payload.indexOf('\r') >= 0) {
			throw new IllegalArgumentException("Property payload must use LF line endings.");
		}

		if (!payload.endsWith("\n")) {
			throw new IllegalArgumentException("Property payload must end with a line break.");
		}

		Properties properties = new Properties();

		String[] lines = payload.split("\n", -1);

		for (int lineIndex = 0; lineIndex < lines.length - 1; lineIndex++) {
			parseLine(properties, lines[lineIndex], lineIndex + 1);
		}

		return properties;
	}

	private static String decodeUtf8(byte[] encodedPayload) {
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(encodedPayload)).toString();
		} catch (CharacterCodingException exception) {
			throw new IllegalArgumentException("Property payload is not valid UTF-8.", exception);
		}
	}

	private static void parseLine(Properties properties, String line, int lineNumber) {
		if (line.isEmpty()) {
			throw new IllegalArgumentException("Property payload contains an empty line at line " + lineNumber + ".");
		}

		int separatorIndex = line.indexOf('=');

		if (separatorIndex <= 0) {
			throw new IllegalArgumentException("Invalid property structure at line " + lineNumber + ".");
		}

		String propertyName = line.substring(0, separatorIndex);
		String propertyValue = line.substring(separatorIndex + 1);

		if (propertyName.isBlank()) {
			throw new IllegalArgumentException("Property name cannot be blank at line " + lineNumber + ".");
		}

		if (properties.containsKey(propertyName)) {
			throw new IllegalArgumentException("Duplicated property name at line " + lineNumber + ": " + propertyName);
		}

		properties.setProperty(propertyName, propertyValue);
	}
}