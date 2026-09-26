package rpg.ui;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import rpg.battle.BattleType;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Item;
import rpg.items.RecoveryItem;

// Displays and uses items available during battle.
public final class BattleItemMenu {

	private static final int ITEMS_PER_PAGE = 9;
	private static final int DESCRIPTION_LINES_PER_PAGE = 3;

	private final BiConsumer<RecoveryItem, PlayableCharacter> postActionStage;
	private final BiConsumer<List<PlayableCharacter>, String> targetDisplay;
	private final BattleScreen battleScreen;
	private final String scenarioName;
	private final BattleType battleType;
	private final Party party;
	private final ConsoleInput consoleInput;
	private final Consumer<List<String>> battleMessageDisplay;

	public BattleItemMenu(
			Party party,
			ConsoleInput consoleInput,
			BattleScreen battleScreen,
			String scenarioName,
			BattleType battleType,
			Consumer<List<String>> battleMessageDisplay,
			BiConsumer<List<PlayableCharacter>, String> targetDisplay,
			BiConsumer<RecoveryItem, PlayableCharacter> postActionStage
	) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.battleScreen = Objects.requireNonNull(battleScreen, "Battle screen cannot be null.");
		this.scenarioName = Objects.requireNonNull(scenarioName, "Scenario name cannot be null.");
		this.battleType = Objects.requireNonNull(battleType, "Battle type cannot be null.");
		this.battleMessageDisplay = Objects
				.requireNonNull(battleMessageDisplay, "Battle message display cannot be null.");
		this.targetDisplay = Objects.requireNonNull(targetDisplay, "Target display cannot be null.");
		this.postActionStage = Objects.requireNonNull(
		        postActionStage, "Post-action item stage cannot be null.");
	}

	public boolean open(PlayableCharacter actingCharacter) {
		Objects.requireNonNull(actingCharacter, "Acting character cannot be null.");
		List<Map.Entry<Item, Integer>> availableEntries = party.getInventory().getContents().entrySet().stream()
				.filter(entry -> entry.getKey() instanceof RecoveryItem)
				.sorted(
						Comparator.comparing(
								(Map.Entry<Item, Integer> entry) -> entry.getKey().getName(),
								String.CASE_INSENSITIVE_ORDER
						).thenComparing(entry -> entry.getKey().getId())
				).toList();

		if (availableEntries.isEmpty()) {
			System.out.println("Não há itens disponíveis para usar em batalha.");

			return false;
		}

		return openItemList(availableEntries, actingCharacter);
	}

	private boolean openItemList(List<Map.Entry<Item, Integer>> entries, PlayableCharacter actingCharacter) {
		int currentPage = 0;
		int totalPages = (entries.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
		String feedbackMessage = null;

		while (true) {
			int firstIndex = currentPage * ITEMS_PER_PAGE;
			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, entries.size());

			displayPage(entries, actingCharacter, firstIndex, lastIndex, currentPage, totalPages, feedbackMessage);
			feedbackMessage = null;

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equalsIgnoreCase("P") && currentPage < totalPages - 1) {
				currentPage++;
				continue;
			}

			if (choice.equalsIgnoreCase("A") && currentPage > 0) {
				currentPage--;
				continue;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;
				int pageSize = lastIndex - firstIndex;

				if (selectedIndex < 0 || selectedIndex >= pageSize) {
					feedbackMessage = "Item inválido.";
					continue;
				}

				Map.Entry<Item, Integer> entry = entries.get(firstIndex + selectedIndex);
				RecoveryItem recoveryItem = (RecoveryItem) entry.getKey();

				if (openDetails(recoveryItem, entry.getValue(), actingCharacter)) {
					return true;
				}
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private void displayPage(
			List<Map.Entry<Item, Integer>> entries,
			PlayableCharacter actingCharacter,
			int firstIndex,
			int lastIndex,
			int currentPage,
			int totalPages,
			String feedbackMessage
	) {
		battleScreen.displayItemHeading(scenarioName, battleType);
		System.out.println();
		System.out.println("Página " + (currentPage + 1) + "/" + totalPages);
		System.out.println(ItemMenuFormatter.formatItemTableHeader());
		for (int index = firstIndex; index < lastIndex; index++) {
			Map.Entry<Item, Integer> entry = entries.get(index);
			System.out.println(
					ItemMenuFormatter
							.formatItemTableRow(index - firstIndex + 1, entry.getKey().getName(), entry.getValue())
			);
		}

		for (int index = lastIndex - firstIndex; index < ITEMS_PER_PAGE; index++) {
			System.out.println(ItemMenuFormatter.formatEmptyItemTableRow());
		}
		StringBuilder navigation = new StringBuilder("");
		if (currentPage > 0) {
			navigation.append("    [A] Anterior");
		}
		if (currentPage < totalPages - 1) {
			navigation.append("    [P] Próxima");
		}
		System.out.println(navigation);
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private boolean openDetails(RecoveryItem recoveryItem, int quantity, PlayableCharacter actingCharacter) {
		List<String> descriptionLines = ConsoleUI.wrapText(recoveryItem.getDescription());
		int currentPage = 0;
		String feedbackMessage = null;

		while (true) {
			battleScreen.displayItemHeading(scenarioName, battleType);

			System.out.println();
			System.out.println(recoveryItem.getName());
			System.out.println("Categoria: " + recoveryItem.getCategory().getDisplayName());
			System.out.println("Alvo: " + recoveryItem.getTargetType().getDisplayName());
			System.out.println("Efeitos:");
			ConsoleUI.displayWrappedText(ItemMenuFormatter.getRecoveryEffectSummary(recoveryItem));

			System.out.println();

			int firstLine = currentPage * DESCRIPTION_LINES_PER_PAGE;
			int lastLine = Math.min(firstLine + DESCRIPTION_LINES_PER_PAGE, descriptionLines.size());

			for (int index = firstLine; index < lastLine; index++) {
				System.out.println(descriptionLines.get(index));
			}

			for (int index = lastLine - firstLine; index < DESCRIPTION_LINES_PER_PAGE; index++) {
				System.out.println();
			}

			System.out.println();

			StringBuilder options = new StringBuilder("[1] Usar    [0] Voltar");
			if (currentPage > 0) {
				options.append("    [A] Anterior");
			}
			if (lastLine < descriptionLines.size()) {
				options.append("    [P] Próxima");
			}
			System.out.println();
			System.out.println("Quantidade: " + quantity);
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);
			System.out.println(options);
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();
			feedbackMessage = null;

			switch (choice.toUpperCase(Locale.ROOT)) {
			case "1" -> {
				List<String> messages = new ArrayList<>();
				RecoveryItemUseMenu useMenu = new RecoveryItemUseMenu(party, consoleInput, messages::add);

				boolean itemUsed = useMenu.use(
				        recoveryItem, actingCharacter, targetDisplay, postActionStage);

				if (itemUsed) {
					battleMessageDisplay.accept(messages);
					return true;
				}

				feedbackMessage = messages.isEmpty() ? null : messages.get(0);
			}

			case "0" -> {
				return false;
			}

			case "A" -> {
				if (currentPage > 0) {
					currentPage--;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}

			case "P" -> {
				if (lastLine < descriptionLines.size()) {
					currentPage++;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	public boolean hasAvailableItems() {
		return party.getInventory().getContents().keySet().stream().anyMatch(RecoveryItem.class::isInstance);
	}
}