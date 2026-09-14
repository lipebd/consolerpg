package rpg.world.crafting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Stores the immutable data of a crafting recipe.
public final class CraftingRecipeDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final String outputItemId;
	private final String name;
	private final String description;
	private final List<CraftingMaterial> materials;

	public CraftingRecipeDefinition(
			String id,
			String outputItemId,
			String name,
			String description,
			List<CraftingMaterial> materials) {
		this.id = validateId(id, "Crafting recipe");
		this.outputItemId = validateId(outputItemId, "Crafting output item");
		this.name = validateText(name, "Crafting recipe name");
		this.description = validateText(description, "Crafting recipe description");

		if (materials == null || materials.isEmpty()) {
			throw new IllegalArgumentException(
					"Crafting recipe must require at least one material."
			);
		}

		Set<String> materialItemIds = new HashSet<>();

		for (CraftingMaterial material : materials) {
			if (material == null) {
				throw new IllegalArgumentException(
						"Crafting recipe materials cannot contain null entries."
				);
			}

			if (!materialItemIds.add(material.getItemId())) {
				throw new IllegalArgumentException(
						"Crafting recipe cannot repeat material item ID: "
								+ material.getItemId()
				);
			}
		}

		this.materials = List.copyOf(materials);
	}

	private String validateId(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
		}

		String trimmedValue = value.trim();

		if (!trimmedValue.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					fieldName
							+ " ID must use lowercase letters, numbers and single underscores: "
							+ trimmedValue
			);
		}

		return trimmedValue;
	}

	private String validateText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}

		return value.trim();
	}

	public String getId() {
		return id;
	}

	public String getOutputItemId() {
		return outputItemId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<CraftingMaterial> getMaterials() {
		return materials;
	}
}