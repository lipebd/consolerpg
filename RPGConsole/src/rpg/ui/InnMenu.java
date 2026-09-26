package rpg.ui;

import java.util.Objects;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.world.inns.InnDefinition;
import rpg.persistence.CampaignSaveResult;

// Displays an INN and controls the party-resting flow.
public final class InnMenu {

	private final Party party;
	private final ConsoleInput consoleInput;

	public InnMenu(Party party, ConsoleInput consoleInput) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public Selection open(String cityName, InnDefinition innDefinition) {
		Objects.requireNonNull(innDefinition, "Inn definition cannot be null.");
		if (cityName == null || cityName.isBlank()) {
			throw new IllegalArgumentException("Inn city name cannot be empty.");
		}
		String feedbackMessage = null;

		while (true) {
			displayInn(cityName, innDefinition, feedbackMessage);

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "0" -> {
				return Selection.BACK;
			}

			case "1" -> feedbackMessage = tryRest(cityName, innDefinition);

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

	private void displayInnDetails(String cityName, InnDefinition innDefinition) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(cityName, innDefinition.getName());

		System.out.println();
		ConsoleUI.displayWrappedText(innDefinition.getDescription());

		String stayPriceLabel = "Estadia do grupo:";
		String availableGoldLabel = GameCurrency.getName() + " disponível:";
		int labelWidth = Math.max(stayPriceLabel.length(), availableGoldLabel.length()) + 1;

		System.out.printf("\n%-" + labelWidth + "s%s%n",
				stayPriceLabel, GameCurrency.format(innDefinition.getPrice()));
		System.out.printf("%-" + labelWidth + "s%s%n",
				availableGoldLabel, GameCurrency.format(party.getGold()));
	}

	private void displayInn(
			String cityName,
			InnDefinition innDefinition,
			String feedbackMessage
	) {
		displayInnDetails(cityName, innDefinition);
		System.out.println();
		System.out.println("[1] Descansar");
		System.out.println("[2] Salvar e continuar");
		System.out.println("[3] Salvar e encerrar");
		if (feedbackMessage != null) {
			System.out.println();
			ConsoleUI.displayWrappedText(feedbackMessage);
		}
		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private String tryRest(
			String cityName,
			InnDefinition innDefinition
	) {
		if (isPartyFullyRestored()) {
			return "O grupo ainda está descansado e não precisa de uma estadia.";
		}

		if (party.getGold() < innDefinition.getPrice()) {
			return "As moedas do grupo não são suficientes para pagar a estadia.";
		}

		if (!confirmStay(cityName, innDefinition)) {
			return null;
		}

		if (!party.spendGold(innDefinition.getPrice())) {
			throw new IllegalStateException("Party gold changed before Inn payment.");
		}

		party.fullyRestoreMembers();

		return "Após uma noite de descanso, todos despertam com as forças completamente renovadas.";
	}

	public void displaySaving(String cityName, InnDefinition innDefinition) {
		String message = "O livro mágico da pousada registra sua jornada sozinho...";
		boolean interrupted = false;

		consoleInput.discardPendingInput();
		displayInnDetails(cityName, innDefinition);
		System.out.println();

		for (int offset = 0; offset < message.length();) {
			int codePoint = message.codePointAt(offset);

			System.out.print(new String(Character.toChars(codePoint)));
			System.out.flush();

			if (consoleInput.pollLine(35).isPresent()) {
				interrupted = true;
				break;
			}

			offset += Character.charCount(codePoint);
		}

		if (interrupted) {
			// Remove o Enter exibido pelo terminal e apresenta a frase completa.
			displayInnDetails(cityName, innDefinition);
			System.out.println();
			System.out.println(message);
		} else {
			System.out.println();
			consoleInput.pollLine(1200);
		}

		consoleInput.discardPendingInput();
	}

	public void displaySaveResult(String cityName, InnDefinition innDefinition, CampaignSaveResult saveResult) {
		String resultMessage = switch (saveResult) {
		case SUCCESS -> "Campanha registrada com sucesso.";
		case ENCODING_FAILED -> "Não foi possível preparar os dados da campanha para salvar.";
		case FILE_WRITE_FAILED -> "Não foi possível registrar a campanha.";
		};

		consoleInput.discardPendingInput();
		displayInnDetails(cityName, innDefinition);

		System.out.println();
		System.out.println("O livro mágico da pousada registra sua jornada sozinho...");
		System.out.println();
		ConsoleUI.displayWrappedText(resultMessage);
		System.out.println();
		System.out.print("aperte ENTER para continuar");
		consoleInput.readLine();
	}

	private boolean confirmStay(String cityName, InnDefinition innDefinition) {
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(cityName, innDefinition.getName());

			System.out.println();
			System.out.println("Confirmar estadia por " + GameCurrency.format(innDefinition.getPrice()) + "?");

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

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

			default -> feedbackMessage = "Opção inválida.";
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
