package rpg.ui;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.RecoveryItem;

//Controls party-member selection according to the current target context.
public final class AllyTargetMenu {

	private final ConsoleInput consoleInput;

	public AllyTargetMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public Optional<PlayableCharacter> selectLivingTarget(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(
				party.getLivingMembers(),
				"Selecionar aliado",
				"Escolha um aliado:",
				"Não há aliados vivos disponíveis."
		);
	}

	public Optional<PlayableCharacter> selectDefeatedTarget(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(
				party.getDefeatedMembers(),
				"Reviver aliado",
				"Escolha um aliado para reviver:",
				"Não há aliados derrotados para reviver."
		);
	}

	public Optional<PlayableCharacter> selectRecoveryTarget(Party party, RecoveryItem recoveryItem) {
		Objects.requireNonNull(party, "Party cannot be null.");

		Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");

		List<PlayableCharacter> availableMembers = party.getMembers().stream().filter(recoveryItem::canAffect).toList();

		return selectTarget(
				availableMembers,
				"Selecionar aliado",
				"Escolha um aliado:",
				"Nenhum aliado pode ser afetado por este item."
		);
	}

	private Optional<PlayableCharacter> selectTarget(
			List<PlayableCharacter> availableMembers,
			String menuTitle,
			String prompt,
			String emptyMessage
	) {
		if (availableMembers.isEmpty()) {
			System.out.println(emptyMessage);
			return Optional.empty();
		}

		if (availableMembers.size() == 1) {
			return Optional.of(availableMembers.get(0));
		}

		int nameColumnWidth = availableMembers.stream().mapToInt(member -> member.getName().length()).max().orElse(0);

		String rowFormat = "[%d] %-" + nameColumnWidth + "s | %s%n";

		while (true) {
			System.out.println("\n[" + menuTitle.toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println(prompt);
			System.out.println();

			for (int index = 0; index < availableMembers.size(); index++) {
				PlayableCharacter member = availableMembers.get(index);

				String condition;

				if (member.isAlive()) {
					condition = "HP: " + member.getCurrentHp() + "/" + member.getMaxHp();
				} else {
					condition = "DERROTADO";
				}

				System.out.printf(rowFormat, index + 1, member.getName(), condition);
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= availableMembers.size()) {
					System.out.println("Aliado inválido.");
					continue;
				}

				return Optional.of(availableMembers.get(selectedIndex));
			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}
}