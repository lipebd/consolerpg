package rpg.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

// Encodes and atomically writes one complete campaign snapshot.
public final class CampaignSaveService {

	private final Path savePath;
	private final CampaignSaveCodec saveCodec;

	public CampaignSaveService() {
		this(Path.of("save.txt"));
	}

	public CampaignSaveService(Path savePath) {
		this.savePath = Objects.requireNonNull(savePath, "Save path cannot be null.");

		this.saveCodec = new CampaignSaveCodec();
	}

	public CampaignSaveResult save(CampaignSnapshot campaignSnapshot) {
		Objects.requireNonNull(campaignSnapshot, "Campaign snapshot cannot be null.");

		byte[] encodedSave;

		try {
			encodedSave = saveCodec.encode(campaignSnapshot);
		} catch (IllegalArgumentException | IllegalStateException exception) {
			return CampaignSaveResult.ENCODING_FAILED;
		}

		try {
			AtomicSaveFileWriter.write(savePath, encodedSave);

			return CampaignSaveResult.SUCCESS;
		} catch (IOException exception) {
			return CampaignSaveResult.FILE_WRITE_FAILED;
		} finally {
			Arrays.fill(encodedSave, (byte) 0);
		}
	}
}