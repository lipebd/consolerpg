package rpg.persistence;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

// Encodes campaign properties into a stable UTF-8 plaintext payload.
public final class PropertyPayloadEncoder {

	private PropertyPayloadEncoder() {
	}

	public static byte[] encode(Properties properties) {
		Objects.requireNonNull(properties, "Properties cannot be null.");

		validateEntryTypes(properties);

		List<String> propertyNames = new ArrayList<>(properties.stringPropertyNames());

		propertyNames.sort(String::compareTo);

		StringBuilder payload = new StringBuilder();

		for (String propertyName : propertyNames) {
			String propertyValue = properties.getProperty(propertyName);

			validatePropertyName(propertyName);
			validatePropertyValue(propertyValue, propertyName);

			payload.append(propertyName).append('=').append(propertyValue).append('\n');
		}

		return payload.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static void validateEntryTypes(Properties properties) {
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof String)) {
				throw new IllegalArgumentException("Property names and values must be strings.");
			}
		}
	}

	private static void validatePropertyName(String propertyName) {
		if (propertyName.isBlank()) {
			throw new IllegalArgumentException("Property name cannot be blank.");
		}

		if (propertyName.indexOf('=') >= 0 || propertyName.indexOf('\n') >= 0 || propertyName.indexOf('\r') >= 0) {
			throw new IllegalArgumentException("Property name contains an unsupported character: " + propertyName);
		}
	}

	private static void validatePropertyValue(String propertyValue, String propertyName) {
		if (propertyValue == null) {
			throw new IllegalArgumentException("Property value cannot be null: " + propertyName);
		}

		if (propertyValue.indexOf('\n') >= 0 || propertyValue.indexOf('\r') >= 0) {
			throw new IllegalArgumentException("Property value cannot contain a line break: " + propertyName);
		}
	}
}