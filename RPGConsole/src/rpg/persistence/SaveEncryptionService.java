package rpg.persistence;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// Encrypts and authenticates save payloads through AES-256-GCM.
public final class SaveEncryptionService {

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final String KEY_ALGORITHM = "AES";

	private static final int KEY_LENGTH_BYTES = 32;
	private static final int INITIALIZATION_VECTOR_LENGTH_BYTES = 12;
	private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;
	private static final int AUTHENTICATION_TAG_LENGTH_BYTES = 16;

	private final SecretKey encryptionKey;
	private final SecureRandom secureRandom;

	public SaveEncryptionService(byte[] encryptionKeyBytes) {
		Objects.requireNonNull(encryptionKeyBytes, "Encryption key cannot be null.");

		if (encryptionKeyBytes.length != KEY_LENGTH_BYTES) {
			throw new IllegalArgumentException(
					"AES-256 encryption key must contain exactly " + KEY_LENGTH_BYTES + " bytes."
			);
		}

		byte[] copiedKeyBytes = Arrays.copyOf(encryptionKeyBytes, encryptionKeyBytes.length);

		this.encryptionKey = new SecretKeySpec(copiedKeyBytes, KEY_ALGORITHM);
		this.secureRandom = new SecureRandom();
	}

	public EncryptedSavePayload encrypt(byte[] plaintext) {
		Objects.requireNonNull(plaintext, "Save plaintext cannot be null.");

		if (plaintext.length == 0) {
			throw new IllegalArgumentException("Save plaintext cannot be empty.");
		}

		byte[] initializationVector = new byte[INITIALIZATION_VECTOR_LENGTH_BYTES];

		secureRandom.nextBytes(initializationVector);

		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			GCMParameterSpec parameters = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, initializationVector);

			cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameters);

			byte[] ciphertext = cipher.doFinal(plaintext);

			return new EncryptedSavePayload(initializationVector, ciphertext);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Save payload encryption failed.", exception);
		}
	}

	public byte[] decrypt(EncryptedSavePayload encryptedPayload) {
		Objects.requireNonNull(encryptedPayload, "Encrypted save payload cannot be null.");

		byte[] initializationVector = encryptedPayload.getInitializationVector();

		byte[] ciphertext = encryptedPayload.getCiphertext();

		if (initializationVector.length != INITIALIZATION_VECTOR_LENGTH_BYTES) {
			throw new IllegalArgumentException(
					"Encrypted save initialization vector must contain exactly " + INITIALIZATION_VECTOR_LENGTH_BYTES
							+ " bytes."
			);
		}

		if (ciphertext.length < AUTHENTICATION_TAG_LENGTH_BYTES) {
			throw new IllegalArgumentException("Encrypted save ciphertext is shorter than its authentication tag.");
		}

		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			GCMParameterSpec parameters = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, initializationVector);

			cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameters);

			return cipher.doFinal(ciphertext);
		} catch (AEADBadTagException exception) {
			throw new IllegalArgumentException("Encrypted save authentication failed.", exception);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Save payload decryption failed.", exception);
		}
	}
}