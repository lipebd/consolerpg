package rpg.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

// Reads, decodes, validates and reconstructs one encrypted campaign save file.
public final class CampaignLoadService {

	private final Path savePath;
	private final CampaignSaveCodec saveCodec;
	private final CampaignSnapshotRestorer snapshotRestorer;

	public CampaignLoadService(CampaignSnapshotRestorer snapshotRestorer) {
		this(Path.of("save.txt"), snapshotRestorer);
	}

	public CampaignLoadService(Path savePath, CampaignSnapshotRestorer snapshotRestorer) {
		this.savePath = Objects.requireNonNull(savePath, "Save path cannot be null.");

		this.snapshotRestorer = Objects.requireNonNull(snapshotRestorer, "Campaign snapshot restorer cannot be null.");

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

			RestoredCampaign restoredCampaign = snapshotRestorer.restore(campaignSnapshot);

			return CampaignLoadResult.success(restoredCampaign);
		} catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
			exception.printStackTrace();
			return CampaignLoadResult.invalidSave();
		} finally {
			Arrays.fill(encodedSave, (byte) 0);
		}
	}
}