package rpg.ui;

import java.util.Locale;
import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.world.inns.InnDefinition;
import rpg.GameCurrency;

// Displays an INN and controls the party-resting flow.
public final class InnMenu {

	private final Party party;
	private final ConsoleInput consoleInput;

	public InnMenu(Party party, ConsoleInput consoleInput) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public Selection open(InnDefinition innDefinition) {
		Objects.requireNonNull(innDefinition, "Inn definition cannot be null.");

		String feedbackMessage = null;

		while (true) {
			displayInn(innDefinition, feedbackMessage);

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "0" -> {
				return Selection.BACK;
			}

			case "1" -> feedbackMessage = tryRest(innDefinition);

			case "2" -> {
				return Selection.SAVE_AND_CONTINUE;
			}

			case "3" -> {
				return Selection.SAVE_AND_EXIT;
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	public enum Selection {
		BACK, SAVE_AND_CONTINUE, SAVE_AND_EXIT
	}

	private void displayInn(InnDefinition innDefinition, String feedbackMessage) {
		ConsoleUI.clearScreen();
		System.out.println("\n[" + innDefinition.getName().toUpperCase(Locale.ROOT) + "]");

		System.out.println(innDefinition.getDescription());

		String stayPriceLabel = "Estadia do grupo:";
		String availableGoldLabel = GameCurrency.getName() + " disponível:";
		int labelWidth = Math.max(stayPriceLabel.length(), availableGoldLabel.length()) + 1;

		System.out.printf("\n%-" + labelWidth + "s%s%n", stayPriceLabel, GameCurrency.format(innDefinition.getPrice()));
		System.out.printf("%-" + labelWidth + "s%s%n", availableGoldLabel, GameCurrency.format(party.getGold()));

		if (feedbackMessage != null) {
			System.out.println();
			System.out.println(feedbackMessage);
		}

		System.out.println();
		System.out.println("[1] Descansar");
		System.out.println("[2] Salvar e continuar");
		System.out.println("[3] Salvar e encerrar");
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private String tryRest(InnDefinition innDefinition) {
		if (isPartyFullyRestored()) {
			return "O grupo ainda está descansado e não precisa de uma estadia.";
		}

		if (party.getGold() < innDefinition.getPrice()) {
			return "As moedas do grupo não são suficientes para pagar a estadia.";
		}

		if (!confirmStay(innDefinition.getPrice())) {
			return null;
		}

		if (!party.spendGold(innDefinition.getPrice())) {
			throw new IllegalStateException("Party gold changed before Inn payment.");
		}

		party.fullyRestoreMembers();

		return "Após uma noite de descanso, todos despertam com as forças completamente renovadas.";
	}

	private boolean confirmStay(int price) {
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("\nConfirmar estadia por " + GameCurrency.format(price) + "?");

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private boolean isPartyFullyRestored() {
		for (PlayableCharacter member : party.getMembers()) {
			if (!member.isAlive() || member.getCurrentHp() < member.getMaxHp()
					|| member.getCurrentMp() < member.getMaxMp() || member.getCurrentSp() < member.getMaxSp()) {
				return false;
			}
		}

		return true;
	}
}
