package rpg.world.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.characters.Party;
import rpg.items.Inventory;
import rpg.items.Item;
import rpg.items.ItemCatalog;

// Applies validated crafting transactions to the party inventory.
public final class CraftingService {

	private final Party party;
	private final ItemCatalog itemCatalog;

	public CraftingService(Party party, ItemCatalog itemCatalog) {
		this.party = Objects.requireNonNull(
				party,
				"Party cannot be null."
		);
		this.itemCatalog = Objects.requireNonNull(
				itemCatalog,
				"Item catalog cannot be null."
		);
	}

	public CraftingResult craft(
			CraftingRecipeDefinition recipeDefinition) {
		Objects.requireNonNull(
				recipeDefinition,
				"Crafting recipe definition cannot be null."
		);

		Inventory inventory = party.getInventory();
		List<CraftingMaterial> missingMaterials =
				findMissingMaterials(recipeDefinition, inventory);

		if (!missingMaterials.isEmpty()) {
			return CraftingResult.insufficientMaterials(missingMaterials);
		}

		Item outputItem =
				itemCatalog.getById(recipeDefinition.getOutputItemId());

		if (!canAddOutput(recipeDefinition, outputItem, inventory)) {
			return CraftingResult.inventoryCapacityExceeded();
		}

		applyCraft(recipeDefinition, outputItem, inventory);

		return CraftingResult.success();
	}

	private List<CraftingMaterial> findMissingMaterials(
			CraftingRecipeDefinition recipeDefinition,
			Inventory inventory) {
		List<CraftingMaterial> missingMaterials = new ArrayList<>();

		for (CraftingMaterial material : recipeDefinition.getMaterials()) {
			Item materialItem = itemCatalog.getById(material.getItemId());
			int ownedQuantity = inventory.getQuantity(materialItem);

			if (ownedQuantity < material.getRequiredQuantity()) {
				missingMaterials.add(material);
			}
		}

		return List.copyOf(missingMaterials);
	}

	private boolean canAddOutput(
			CraftingRecipeDefinition recipeDefinition,
			Item outputItem,
			Inventory inventory) {
		int resultingOutputQuantity = inventory.getQuantity(outputItem);

		for (CraftingMaterial material : recipeDefinition.getMaterials()) {
			if (material.getItemId().equals(outputItem.getId())) {
				resultingOutputQuantity -= material.getRequiredQuantity();
			}
		}

		try {
			Math.addExact(resultingOutputQuantity, 1);
			return true;
		} catch (ArithmeticException exception) {
			return false;
		}
	}

	private void applyCraft(
			CraftingRecipeDefinition recipeDefinition,
			Item outputItem,
			Inventory inventory) {
		List<CraftingMaterial> removedMaterials = new ArrayList<>();

		for (CraftingMaterial material : recipeDefinition.getMaterials()) {
			Item materialItem = itemCatalog.getById(material.getItemId());

			if (!inventory.removeItem(
					materialItem,
					material.getRequiredQuantity())) {
				restoreMaterials(removedMaterials, inventory);

				throw new IllegalStateException(
						"Validated crafting material could not be removed: "
								+ material.getItemId()
				);
			}

			removedMaterials.add(material);
		}

		try {
			if (!inventory.addItem(outputItem, 1)) {
				throw new IllegalStateException(
						"Crafting output could not be added: "
								+ outputItem.getId()
				);
			}
		} catch (RuntimeException exception) {
			restoreMaterials(removedMaterials, inventory);

			throw new IllegalStateException(
					"Crafting transaction could not add its output.",
					exception
			);
		}
	}

	private void restoreMaterials(
			List<CraftingMaterial> removedMaterials,
			Inventory inventory) {
		for (CraftingMaterial material : removedMaterials) {
			Item materialItem = itemCatalog.getById(material.getItemId());

			try {
				if (!inventory.addItem(
						materialItem,
						material.getRequiredQuantity())) {
					throw new IllegalStateException(
							"Crafting material could not be restored: "
									+ material.getItemId()
					);
				}
			} catch (RuntimeException exception) {
				throw new IllegalStateException(
						"Crafting transaction rollback failed.",
						exception
				);
			}
		}
	}
}