package rpg.persistence;

import java.util.Objects;
import java.util.Optional;

// Stores the outcome and optional snapshot of one campaign load attempt.
public final class CampaignLoadResult {

	private final CampaignLoadStatus status;
	private final CampaignSnapshot campaignSnapshot;

	private CampaignLoadResult(CampaignLoadStatus status, CampaignSnapshot campaignSnapshot) {
		this.status = Objects.requireNonNull(status, "Campaign load status cannot be null.");

		boolean success = status == CampaignLoadStatus.SUCCESS;
		boolean hasSnapshot = campaignSnapshot != null;

		if (success != hasSnapshot) {
			throw new IllegalArgumentException(
					"A successful load must contain a campaign snapshot, " + "and a failed load cannot contain one."
			);
		}

		this.campaignSnapshot = campaignSnapshot;
	}

	public static CampaignLoadResult success(CampaignSnapshot campaignSnapshot) {
		return new CampaignLoadResult(
				CampaignLoadStatus.SUCCESS,
				Objects.requireNonNull(campaignSnapshot, "Campaign snapshot cannot be null.")
		);
	}

	public static CampaignLoadResult saveNotFound() {
		return new CampaignLoadResult(CampaignLoadStatus.SAVE_NOT_FOUND, null);
	}

	public static CampaignLoadResult fileReadFailed() {
		return new CampaignLoadResult(CampaignLoadStatus.FILE_READ_FAILED, null);
	}

	public static CampaignLoadResult invalidSave() {
		return new CampaignLoadResult(CampaignLoadStatus.INVALID_SAVE, null);
	}

	public CampaignLoadStatus getStatus() {
		return status;
	}

	public Optional<CampaignSnapshot> getCampaignSnapshot() {
		return Optional.ofNullable(campaignSnapshot);
	}

	public boolean isSuccess() {
		return status == CampaignLoadStatus.SUCCESS;
	}
}