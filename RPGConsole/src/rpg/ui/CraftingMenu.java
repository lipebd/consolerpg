package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import rpg.quests.QuestProgressEvent;
import rpg.quests.QuestProgressEventType;
import rpg.characters.Party;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.world.crafting.CrafterDefinition;
import rpg.world.crafting.CraftingMaterial;
import rpg.world.crafting.CraftingRecipeCatalog;
import rpg.world.crafting.CraftingRecipeDefinition;
import rpg.world.crafting.CraftingResult;
import rpg.world.crafting.CraftingService;

// Displays a crafter and coordinates player crafting choices.
public final class CraftingMenu {

	private final QuestProgressCoordinator questProgressCoordinator;
	private final Party party;
	private final ConsoleInput consoleInput;
	private final ItemCatalog itemCatalog;
	private final CraftingRecipeCatalog craftingRecipeCatalog;
	private final CraftingService craftingService;

	public CraftingMenu(
	        Party party,
	        ConsoleInput consoleInput,
	        ItemCatalog itemCatalog,
	        CraftingRecipeCatalog craftingRecipeCatalog,
	        QuestProgressCoordinator questProgressCoordinator) {
		this.party = Objects.requireNonNull(
				party,
				"Party cannot be null."
		);
		this.consoleInput = Objects.requireNonNull(
				consoleInput,
				"Console input cannot be null."
		);
		this.itemCatalog = Objects.requireNonNull(
				itemCatalog,
				"Item catalog cannot be null."
		);
		this.craftingRecipeCatalog = Objects.requireNonNull(
				craftingRecipeCatalog,
				"Crafting recipe catalog cannot be null."
		);
		this.craftingService =
				new CraftingService(this.party, this.itemCatalog);
		this.questProgressCoordinator =
		        Objects.requireNonNull(
		                questProgressCoordinator,
		                "Quest progress coordinator cannot be null.");
	}

	public void open(String cityName, CrafterDefinition crafterDefinition) {
		Objects.requireNonNull(crafterDefinition, "Crafter definition cannot be null.");
		if (cityName == null || cityName.isBlank()) {
			throw new IllegalArgumentException("Crafter city name cannot be empty.");
		}

		List<CraftingRecipeDefinition> recipes =
				resolveRecipes(crafterDefinition);

		String feedbackMessage = null;

		while (true) {
			displayCrafter(
					cityName,
					crafterDefinition,
					recipes,
					feedbackMessage
			);

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= recipes.size()) {
					feedbackMessage = "Receita inválida.";
					continue;
				}

				feedbackMessage = openRecipe(cityName, crafterDefinition, recipes.get(selectedIndex));

			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de uma receita.";
			}
		}
	}

	private List<CraftingRecipeDefinition> resolveRecipes(
			CrafterDefinition crafterDefinition) {
		List<CraftingRecipeDefinition> recipes = new ArrayList<>();

		for (String recipeId : crafterDefinition.getRecipeIds()) {
			recipes.add(craftingRecipeCatalog.getById(recipeId));
		}

		return List.copyOf(recipes);
	}

	private void displayCrafter(
			String cityName,
			CrafterDefinition crafterDefinition,
			List<CraftingRecipeDefinition> recipes,
			String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(
				cityName,
				crafterDefinition.getName()
		);

		System.out.println();
		ConsoleUI.displayWrappedText(
				crafterDefinition.getDescription()
		);
		System.out.println();
		System.out.println("Receitas disponíveis:");

		for (int index = 0; index < recipes.size(); index++) {
			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}
			System.out.println(
					"["
							+ (index + 1)
							+ "] "
							+ recipes.get(index).getName()
			);
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private String openRecipe(
			String cityName,
			CrafterDefinition crafterDefinition,
			CraftingRecipeDefinition recipeDefinition
	) {

		String feedbackMessage = null;
		while (true) {
			displayRecipe(
					cityName,
					crafterDefinition,
					recipeDefinition,
					feedbackMessage
			);

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
                case "1" -> {
                    CraftingResult result = craftingService.craft(recipeDefinition);

                    if (result.isSuccessful()) {
                        QuestProgressEvent itemEvent =
                                new QuestProgressEvent(
                                        QuestProgressEventType.ITEM_OBTAINED,
                                        Optional.of(recipeDefinition.getOutputItemId()),
                                        1);

                        questProgressCoordinator.processAndDisplay(List.of(itemEvent));
                    }

                    return getCraftingFeedback(result, recipeDefinition);
                }

			case "0" -> {
				return null;
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displayRecipe(
			String cityName,
			CrafterDefinition crafterDefinition,
			CraftingRecipeDefinition recipeDefinition,
			String feedbackMessage
	) {
		Item outputItem =
				itemCatalog.getById(
						recipeDefinition.getOutputItemId()
				);

		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(
				cityName,
				crafterDefinition.getName()
		);

		System.out.println();
		System.out.println(recipeDefinition.getName());

		System.out.println();
		ConsoleUI.displayWrappedText(
				recipeDefinition.getDescription()
		);
		System.out.println();
		System.out.println("Produz:");
		System.out.println("- " + outputItem.getName() + " x1");

		System.out.println();
		System.out.println("Materiais necessários:");

		for (CraftingMaterial material :
				recipeDefinition.getMaterials()) {
			Item materialItem =
					itemCatalog.getById(material.getItemId());

			int ownedQuantity =
					party.getInventory().getQuantity(materialItem);

			System.out.println(
					"- "
							+ materialItem.getName()
							+ " x"
							+ material.getRequiredQuantity()
							+ " (possui: "
							+ ownedQuantity
							+ ")"
			);
		}

		System.out.println();
		System.out.println("[1] Fabricar");
		if (feedbackMessage != null) {
		    System.out.println();
		    ConsoleUI.displayWrappedText(feedbackMessage);
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private String getCraftingFeedback(
			CraftingResult result,
			CraftingRecipeDefinition recipeDefinition) {
		return switch (result.getOutcome()) {
		case SUCCESS ->
			"Você fabricou "
					+ itemCatalog
							.getById(recipeDefinition.getOutputItemId())
							.getName()
					+ " com sucesso.";

		case INSUFFICIENT_MATERIALS ->
			formatMissingMaterials(result.getMissingMaterials());

		case INVENTORY_CAPACITY_EXCEEDED ->
			"O inventário não comporta o item produzido.";
		};
	}

	private String formatMissingMaterials(
			List<CraftingMaterial> missingMaterials) {
		StringBuilder message =
				new StringBuilder("Materiais insuficientes:");

		for (CraftingMaterial material : missingMaterials) {
			Item materialItem =
					itemCatalog.getById(material.getItemId());

			int ownedQuantity =
					party.getInventory().getQuantity(materialItem);

			message
					.append(System.lineSeparator())
					.append("- ")
					.append(materialItem.getName())
					.append(": ")
					.append(ownedQuantity)
					.append("/")
					.append(material.getRequiredQuantity());
		}

		return message.toString();
	}
}