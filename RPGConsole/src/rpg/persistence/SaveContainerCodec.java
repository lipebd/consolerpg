package rpg.persistence;

import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

// Encodes and decodes the textual encrypted save container.
public final class SaveContainerCodec {

	private SaveContainerCodec() {
	}

	public static byte[] encode(EncryptedSavePayload encryptedPayload) {
		Objects.requireNonNull(encryptedPayload, "Encrypted save payload cannot be null.");

		Base64.Encoder base64Encoder = Base64.getEncoder();

		Properties containerProperties = new Properties();

		containerProperties.setProperty("container", SaveContainerFormat.IDENTIFIER);

		containerProperties.setProperty("containerVersion", Integer.toString(SaveContainerFormat.CURRENT_VERSION));

		containerProperties.setProperty("iv", base64Encoder.encodeToString(encryptedPayload.getInitializationVector()));

		containerProperties.setProperty("ciphertext", base64Encoder.encodeToString(encryptedPayload.getCiphertext()));

		return PropertyPayloadEncoder.encode(containerProperties);
	}

	public static EncryptedSavePayload decode(byte[] encodedContainer) {
		Objects.requireNonNull(encodedContainer, "Encoded save container cannot be null.");

		Properties containerProperties = PropertyPayloadDecoder.decode(encodedContainer);

		StrictPropertyReader reader = new StrictPropertyReader(containerProperties);

		String containerIdentifier = reader.readRequired("container");

		if (!SaveContainerFormat.IDENTIFIER.equals(containerIdentifier)) {
			throw new IllegalArgumentException("Invalid save-container identifier.");
		}

		int containerVersion = reader.readInt("containerVersion");

		if (containerVersion != SaveContainerFormat.CURRENT_VERSION) {
			throw new IllegalArgumentException("Unsupported save-container version: " + containerVersion);
		}

		byte[] initializationVector = decodeBase64(reader.readRequired("iv"), "iv");

		byte[] ciphertext = decodeBase64(reader.readRequired("ciphertext"), "ciphertext");

		reader.rejectUnreadProperties();

		return new EncryptedSavePayload(initializationVector, ciphertext);
	}

	private static byte[] decodeBase64(String encodedValue, String propertyName) {
		byte[] decodedValue;

		try {
			decodedValue = Base64.getDecoder().decode(encodedValue);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid Base64 value: " + propertyName, exception);
		}

		String canonicalEncoding = Base64.getEncoder().encodeToString(decodedValue);

		if (!canonicalEncoding.equals(encodedValue)) {
			throw new IllegalArgumentException("Non-canonical Base64 value: " + propertyName);
		}

		return decodedValue;
	}
}