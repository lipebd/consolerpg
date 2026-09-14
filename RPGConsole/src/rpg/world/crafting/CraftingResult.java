package rpg.world.crafting;

import java.util.List;

// Describes the outcome of one crafting attempt.
public final class CraftingResult {

	public enum Outcome {
		SUCCESS,
		INSUFFICIENT_MATERIALS,
		INVENTORY_CAPACITY_EXCEEDED
	}

	private final Outcome outcome;
	private final List<CraftingMaterial> missingMaterials;

	private CraftingResult(
			Outcome outcome,
			List<CraftingMaterial> missingMaterials) {
		if (outcome == null) {
			throw new IllegalArgumentException(
					"Crafting outcome cannot be null."
			);
		}

		if (missingMaterials == null) {
			throw new IllegalArgumentException(
					"Missing crafting materials cannot be null."
			);
		}

		if (outcome == Outcome.INSUFFICIENT_MATERIALS
				&& missingMaterials.isEmpty()) {
			throw new IllegalArgumentException(
					"Insufficient-material result must identify at least one material."
			);
		}

		if (outcome != Outcome.INSUFFICIENT_MATERIALS
				&& !missingMaterials.isEmpty()) {
			throw new IllegalArgumentException(
					"Only an insufficient-material result may contain missing materials."
			);
		}

		this.outcome = outcome;
		this.missingMaterials = List.copyOf(missingMaterials);
	}

	public static CraftingResult success() {
		return new CraftingResult(Outcome.SUCCESS, List.of());
	}

	public static CraftingResult insufficientMaterials(
			List<CraftingMaterial> missingMaterials) {
		return new CraftingResult(
				Outcome.INSUFFICIENT_MATERIALS,
				missingMaterials
		);
	}

	public static CraftingResult inventoryCapacityExceeded() {
		return new CraftingResult(
				Outcome.INVENTORY_CAPACITY_EXCEEDED,
				List.of()
		);
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public List<CraftingMaterial> getMissingMaterials() {
		return missingMaterials;
	}

	public boolean isSuccessful() {
		return outcome == Outcome.SUCCESS;
	}
}