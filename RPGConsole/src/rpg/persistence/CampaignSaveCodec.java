package rpg.persistence;

import java.util.Arrays;
import java.util.Properties;

// Converts campaign snapshots to and from encrypted save-container bytes.
public final class CampaignSaveCodec {

	private final SaveEncryptionService encryptionService;

	public CampaignSaveCodec() {
		this.encryptionService = createApplicationEncryptionService();
	}

	public byte[] encode(CampaignSnapshot campaignSnapshot) {
		Properties campaignProperties = CampaignPropertiesSerializer.serialize(campaignSnapshot);

		byte[] plaintext = PropertyPayloadEncoder.encode(campaignProperties);

		EncryptedSavePayload encryptedPayload = encryptionService.encrypt(plaintext);

		return SaveContainerCodec.encode(encryptedPayload);
	}

	public CampaignSnapshot decode(byte[] encodedContainer) {
		EncryptedSavePayload encryptedPayload = SaveContainerCodec.decode(encodedContainer);

		byte[] plaintext = encryptionService.decrypt(encryptedPayload);

		Properties campaignProperties = PropertyPayloadDecoder.decode(plaintext);

		return CampaignPropertiesDeserializer.deserialize(campaignProperties);
	}

	private static SaveEncryptionService createApplicationEncryptionService() {
		byte[] encryptionKey = ApplicationSaveEncryptionKey.getBytes();

		try {
			return new SaveEncryptionService(encryptionKey);
		} finally {
			Arrays.fill(encryptionKey, (byte) 0);
		}
	}
}