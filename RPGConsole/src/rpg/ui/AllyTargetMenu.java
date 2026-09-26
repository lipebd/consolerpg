package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

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
				"Não há aliados vivos disponíveis.",
				null,
				null
		);
	}

	public Optional<PlayableCharacter> selectDefeatedTarget(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(
				party.getDefeatedMembers(),
				"Reviver aliado",
				"Escolha um aliado para reviver:",
				"Não há aliados derrotados para reviver.",
				null,
				null
		);
	}

	public Optional<PlayableCharacter> selectRecoveryTarget(
	        Party party,
	        RecoveryItem recoveryItem
	) {
	    Objects.requireNonNull(party, "Party cannot be null.");
	    Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");

	    return selectTarget(
	            party.getMembers(),
	            "Selecionar aliado",
	            "Escolha um aliado:",
	            "Nenhum aliado disponível.",
	            null,
	            recoveryItem
	    );
	}

	private Optional<PlayableCharacter> selectTarget(
	        List<PlayableCharacter> availableMembers,
	        String menuTitle,
	        String prompt,
	        String emptyMessage,
	        BiConsumer<List<PlayableCharacter>, String> battleDisplay,
	        RecoveryItem recoveryItem
	) {
		if (availableMembers.isEmpty()) {
			ConsoleUI.displayWrappedText(emptyMessage);
			return Optional.empty();
		}

		if (availableMembers.size() == 1 && recoveryItem == null) {
		    return Optional.of(availableMembers.get(0));
		}

		String feedbackMessage = null;

		while (true) {
			if (battleDisplay != null) {
				battleDisplay.accept(availableMembers, feedbackMessage);
			} else {
				ConsoleUI.clearScreen();
				ConsoleUI.displayHeading(menuTitle);

				System.out.println();
				System.out.println(prompt);
				System.out.println();

				displayMembers(availableMembers);

				System.out.println();
				System.out.println(feedbackMessage == null ? "" : feedbackMessage);
				System.out.println();
				System.out.println("[0] Voltar");
				System.out.print("> ");
			}

			feedbackMessage = null;
			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= availableMembers.size()) {
					feedbackMessage = "Aliado inválido.";
					continue;
				}

				PlayableCharacter selectedMember = availableMembers.get(selectedIndex);

				if (recoveryItem != null && !recoveryItem.canAffect(selectedMember)) {
				    feedbackMessage = "O item não fará efeito em " + selectedMember.getName() + ".";
				    continue;
				}

				return Optional.of(selectedMember);
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	public Optional<PlayableCharacter>
			selectLivingBattleTarget(Party party, BiConsumer<List<PlayableCharacter>, String> display) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(
				party.getLivingMembers(),
				"Selecionar aliado",
				"Escolha um aliado:",
				"Não há aliados vivos disponíveis.",
				display,
				null
		);
	}

	public Optional<PlayableCharacter>
			selectDefeatedBattleTarget(Party party, BiConsumer<List<PlayableCharacter>, String> display) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(
				party.getDefeatedMembers(),
				"Reviver aliado",
				"Escolha um aliado para reviver:",
				"Não há aliados derrotados para reviver.",
				display,
				null
		);
	}

	public Optional<PlayableCharacter> selectRecoveryBattleTarget(
			Party party,
			RecoveryItem recoveryItem,
			BiConsumer<List<PlayableCharacter>, String> display,
			PlayableCharacter actingCharacter
	) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");
		Objects.requireNonNull(display, "Target display cannot be null.");

		List<PlayableCharacter> availableMembers = party.getMembers().stream()
		        .filter(member -> recoveryItem.canAffect(member)
		                || recoveryItem.canApplyAttributeStage(
		                        member, actingCharacter))
		        .toList();

		return selectTarget(
				availableMembers,
				"Selecionar aliado",
				"Escolha um aliado:",
				"Nenhum aliado pode ser afetado por este item.",
				display,
				null
		);
	}

	private void displayMembers(List<PlayableCharacter> members) {
		for (int index = 0; index < Party.MAXIMUM_SIZE; index++) {
			if (index >= members.size()) {
				System.out.println();
				System.out.println();
				continue;
			}

			PlayableCharacter member = members.get(index);
			String condition = member.isAlive() ? "HP: " + member.getCurrentHp() + "/" + member.getMaxHp()
					: "DERROTADO";

			System.out.printf("[%d] %-16s | %s%n", index + 1, member.getName(), condition);
			System.out.println(
					"    MP: " + member.getCurrentMp() + "/" + member.getMaxMp() + " | SP: " + member.getCurrentSp()
							+ "/" + member.getMaxSp()
			);
		}
	}

	public boolean confirmGroupRecoveryUse(Party party, RecoveryItem recoveryItem) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Confirmar uso");

			System.out.println();
			System.out.println("Usar " + recoveryItem.getName() + " no grupo?");
			System.out.println();

			displayMembers(party.getMembers());

			System.out.println();
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);
			System.out.println();
			System.out.println("[1] Usar    [0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equals("1")) {
				boolean hasAffectedMember = party.getMembers().stream().anyMatch(recoveryItem::canAffect);

				if (!hasAffectedMember) {
					feedbackMessage = "Esse item não fará efeito em nenhum aliado.";
					continue;
				}

				return true;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	public void displayRecoveryResult(Party party, RecoveryItem recoveryItem, List<String> messages) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");
		Objects.requireNonNull(messages, "Item messages cannot be null.");

		List<String> lines = new ArrayList<>();

		for (String message : messages) {
			lines.addAll(ConsoleUI.wrapText(message));
		}

		for (int firstLine = 0; firstLine < lines.size(); firstLine += 3) {
			int lastLine = Math.min(firstLine + 3, lines.size());
			boolean interrupted = false;

			consoleInput.discardPendingInput();
			displayRecoveryResultHeading(party, recoveryItem);

			for (int index = firstLine; index < lastLine; index++) {
				interrupted = ConsoleUI.animateLine(lines.get(index), 25, consoleInput);

				if (interrupted) {
					break;
				}

				System.out.println();

				if (index < lastLine - 1 && consoleInput.pollLine(350).isPresent()) {
					interrupted = true;
					break;
				}
			}

			if (interrupted) {
				// The terminal may have echoed Enter; redraw the complete page.
				displayRecoveryResultHeading(party, recoveryItem);

				for (int index = firstLine; index < lastLine; index++) {
					System.out.println(lines.get(index));
				}
			}

			consoleInput.pollLine(2500);
			consoleInput.discardPendingInput();
		}
	}

	private void displayRecoveryResultHeading(
	        Party party,
	        RecoveryItem recoveryItem
	) {
	    ConsoleUI.clearScreen();
	    ConsoleUI.displayHeading("Item usado");

	    System.out.println();
	    System.out.println(recoveryItem.getName());
	    System.out.println();

	    displayMembers(party.getMembers());

	    System.out.println();
	}
}