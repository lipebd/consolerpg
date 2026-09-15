package rpg.persistence;

import java.util.Arrays;
import java.util.Objects;

// Stores one encrypted save payload and its initialization vector.
public final class EncryptedSavePayload {

	private final byte[] initializationVector;
	private final byte[] ciphertext;

	public EncryptedSavePayload(byte[] initializationVector, byte[] ciphertext) {
		Objects.requireNonNull(initializationVector, "Initialization vector cannot be null.");
		Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

		if (initializationVector.length == 0) {
			throw new IllegalArgumentException("Initialization vector cannot be empty.");
		}

		if (ciphertext.length == 0) {
			throw new IllegalArgumentException("Ciphertext cannot be empty.");
		}

		this.initializationVector = Arrays.copyOf(initializationVector, initializationVector.length);

		this.ciphertext = Arrays.copyOf(ciphertext, ciphertext.length);
	}

	public byte[] getInitializationVector() {
		return Arrays.copyOf(initializationVector, initializationVector.length);
	}

	public byte[] getCiphertext() {
		return Arrays.copyOf(ciphertext, ciphertext.length);
	}
}