package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores the permanent flags set in one campaign snapshot.
public final class CampaignFlagsSnapshot {

	private final List<String> setFlagIds;

	public CampaignFlagsSnapshot(List<String> setFlagIds) {
		Objects.requireNonNull(
				setFlagIds,
				"Set campaign flag IDs cannot be null."
		);

		validateFlagIds(setFlagIds);

		this.setFlagIds = List.copyOf(setFlagIds);
	}

	private static void validateFlagIds(List<String> setFlagIds) {
		Set<String> uniqueFlagIds = new HashSet<>();

		for (String flagId : setFlagIds) {
			if (flagId == null || flagId.isBlank()) {
				throw new IllegalArgumentException(
						"Campaign flag ID cannot be null or blank."
				);
			}

			if (!uniqueFlagIds.add(flagId)) {
				throw new IllegalArgumentException(
						"Campaign flag ID is duplicated: " + flagId
				);
			}
		}
	}

	public List<String> getSetFlagIds() {
		return setFlagIds;
	}
}