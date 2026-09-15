package rpg.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

// Reads and decodes one encrypted campaign save file.
public final class CampaignLoadService {

	private final Path savePath;
	private final CampaignSaveCodec saveCodec;

	public CampaignLoadService() {
		this(Path.of("save.txt"));
	}

	public CampaignLoadService(Path savePath) {
		this.savePath = Objects.requireNonNull(savePath, "Save path cannot be null.");

		this.saveCodec = new CampaignSaveCodec();
	}

	public CampaignLoadResult load() {
		byte[] encodedSave;

		try {
			if (Files.notExists(savePath)) {
				return CampaignLoadResult.saveNotFound();
			}

			encodedSave = Files.readAllBytes(savePath);
		} catch (IOException | SecurityException exception) {
			return CampaignLoadResult.fileReadFailed();
		}

		try {
			CampaignSnapshot campaignSnapshot = saveCodec.decode(encodedSave);

			return CampaignLoadResult.success(campaignSnapshot);
		} catch (IllegalArgumentException | IllegalStateException exception) {
			return CampaignLoadResult.invalidSave();
		} finally {
			Arrays.fill(encodedSave, (byte) 0);
		}
	}
}