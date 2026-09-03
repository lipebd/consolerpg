package rpg.ui;

import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.world.inns.InnDefinition;

// Displays an INN and controls the party-resting flow.
public final class InnMenu {

	private final Party party;
	private final Scanner scanner;

	public InnMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public void open(InnDefinition innDefinition) {
		Objects.requireNonNull(innDefinition, "Inn definition cannot be null.");

		String feedbackMessage = null;

		while (true) {
			displayInn(innDefinition, feedbackMessage);

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> feedbackMessage = tryRest(innDefinition);

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displayInn(
			InnDefinition innDefinition,
			String feedbackMessage
	) {
		System.out.println(
				"\n[" + innDefinition.getName().toUpperCase(Locale.ROOT) + "]"
		);

		System.out.println(innDefinition.getDescription());

		System.out.println(
				"\nEstadia do grupo: " + innDefinition.getPrice() + " ouro"
		);
		System.out.println("Ouro disponível: " + party.getGold());

		if (feedbackMessage != null) {
			System.out.println();
			System.out.println(feedbackMessage);
		}

		System.out.println();
		System.out.println("[1] Descansar");
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

		restoreParty();

		return "Após uma noite de descanso, todos despertam "
				+ "com as forças completamente renovadas.";
	}

	private boolean confirmStay(int price) {
		while (true) {
			System.out.println("\nConfirmar estadia por " + price + " ouro?");

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

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
			if (!member.isAlive()
					|| member.getCurrentHp() < member.getMaxHp()
					|| member.getCurrentMp() < member.getMaxMp()
					|| member.getCurrentSp() < member.getMaxSp()) {
				return false;
			}
		}

		return true;
	}

	private void restoreParty() {
		for (PlayableCharacter member : party.getMembers()) {
			if (!member.isAlive()) {
				member.revive(member.getMaxHp());
			}

			member.restoreAllResources();
		}
	}
}