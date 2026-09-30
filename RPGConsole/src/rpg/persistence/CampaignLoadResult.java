package rpg.persistence;

import java.util.Objects;
import java.util.Optional;

// Stores the outcome and optional reconstructed campaign of one load attempt.
public final class CampaignLoadResult {

	private final CampaignLoadStatus status;
	private final RestoredCampaign restoredCampaign;

	private CampaignLoadResult(CampaignLoadStatus status, RestoredCampaign restoredCampaign) {
		this.status = Objects.requireNonNull(status, "Campaign load status cannot be null.");

		boolean success = status == CampaignLoadStatus.SUCCESS;
		boolean hasCampaign = restoredCampaign != null;

		if (success != hasCampaign) {
			throw new IllegalArgumentException(
					"A successful load must contain a restored campaign, and a failed load cannot contain one."
			);
		}

		this.restoredCampaign = restoredCampaign;
	}

	public static CampaignLoadResult success(RestoredCampaign restoredCampaign) {
		return new CampaignLoadResult(
				CampaignLoadStatus.SUCCESS,
				Objects.requireNonNull(restoredCampaign, "Restored campaign cannot be null.")
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

	public Optional<RestoredCampaign> getRestoredCampaign() {
		return Optional.ofNullable(restoredCampaign);
	}

	public boolean isSuccess() {
		return status == CampaignLoadStatus.SUCCESS;
	}
}