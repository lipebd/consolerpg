package rpg.persistence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

// Replaces a save file only after its complete replacement is safely written.
public final class AtomicSaveFileWriter {

	private AtomicSaveFileWriter() {
	}

	public static void write(Path targetPath, byte[] contents) throws IOException {
		Objects.requireNonNull(targetPath, "Target path cannot be null.");
		Objects.requireNonNull(contents, "Save contents cannot be null.");

		if (contents.length == 0) {
			throw new IllegalArgumentException("Save contents cannot be empty.");
		}

		Path normalizedTarget = targetPath.toAbsolutePath().normalize();
		Path parentDirectory = normalizedTarget.getParent();

		if (parentDirectory == null || !Files.isDirectory(parentDirectory)) {
			throw new IOException("Save parent directory does not exist.");
		}

		byte[] copiedContents = Arrays.copyOf(contents, contents.length);

		Path temporaryFile = Files.createTempFile(parentDirectory, normalizedTarget.getFileName() + ".", ".tmp");

		boolean temporaryFileWasMoved = false;

		try {
			writeFully(temporaryFile, copiedContents);

			try {
				Files.move(
						temporaryFile,
						normalizedTarget,
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING
				);

				temporaryFileWasMoved = true;
			} catch (AtomicMoveNotSupportedException exception) {
				replaceWithFallback(temporaryFile, normalizedTarget);

				temporaryFileWasMoved = true;
			}
		} finally {
			if (!temporaryFileWasMoved) {
				deleteBestEffort(temporaryFile);
			}

			Arrays.fill(copiedContents, (byte) 0);
		}
	}

	private static void writeFully(Path filePath, byte[] contents) throws IOException {
		try (FileChannel channel = FileChannel
				.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			ByteBuffer buffer = ByteBuffer.wrap(contents);

			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}

			channel.force(true);
		}
	}

	private static void replaceWithFallback(Path temporaryFile, Path targetFile) throws IOException {
		Path backupFile = null;
		boolean backupContainsPreviousSave = false;
		boolean replacementSucceeded = false;
		boolean restorationSucceeded = false;

		try {
			if (Files.exists(targetFile)) {
				backupFile = Files
						.createTempFile(targetFile.getParent(), targetFile.getFileName() + ".backup.", ".tmp");

				Files.copy(targetFile, backupFile, StandardCopyOption.REPLACE_EXISTING);

				forceFile(backupFile);

				backupContainsPreviousSave = true;
			}

			try {
				Files.move(temporaryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

				replacementSucceeded = true;
			} catch (IOException replacementException) {
				if (backupContainsPreviousSave) {
					try {
						Files.copy(backupFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

						forceFile(targetFile);

						restorationSucceeded = true;
					} catch (IOException restorationException) {
						replacementException.addSuppressed(restorationException);
					}
				}

				throw replacementException;
			}
		} finally {
			if (backupFile != null && (!backupContainsPreviousSave || replacementSucceeded || restorationSucceeded)) {
				deleteBestEffort(backupFile);
			}
		}
	}

	private static void forceFile(Path filePath) throws IOException {
		try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.WRITE)) {
			channel.force(true);
		}
	}

	private static void deleteBestEffort(Path filePath) {
		try {
			Files.deleteIfExists(filePath);
		} catch (IOException exception) {
			// A leftover temporary file must not invalidate a successful save replacement.
		}
	}
}