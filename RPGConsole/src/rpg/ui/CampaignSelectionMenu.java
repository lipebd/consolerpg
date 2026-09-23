package rpg.ui;

import java.util.Objects;
import java.util.stream.Collectors;

import rpg.characters.PlayableCharacter;
import rpg.persistence.CampaignLoadResult;
import rpg.persistence.CampaignLoadStatus;
import rpg.persistence.RestoredCampaign;

// Selects whether the player continues a saved campaign or starts a new game.
public final class CampaignSelectionMenu {

	public enum Selection {
		CONTINUE, NEW_GAME
	}

	private final ConsoleInput consoleInput;

	public CampaignSelectionMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public Selection select(CampaignLoadResult loadResult) {
		Objects.requireNonNull(loadResult, "Campaign load result cannot be null.");

		boolean canContinue = loadResult.isSuccess();
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			displayCampaignState(loadResult);

			System.out.println();

			if (canContinue) {
				System.out.println("[1] Continuar");
				System.out.println("[2] Nova Campanha");
			} else {
				System.out.println("[1] Nova Campanha");
			}

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (canContinue && choice.equals("1")) {
				return Selection.CONTINUE;
			}

			boolean selectedNewCampaign = canContinue ? choice.equals("2") : choice.equals("1");

			if (selectedNewCampaign) {
				if (loadResult.getStatus() == CampaignLoadStatus.SAVE_NOT_FOUND || confirmNewGame()) {
					return Selection.NEW_GAME;
				}

				feedbackMessage = null;
				continue;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private void displayCampaignState(CampaignLoadResult loadResult) {
		if (loadResult.isSuccess()) {
			ConsoleUI.displayHeading("Continuar");
		} else {
			ConsoleUI.displayHeading("Nova Campanha");
		}

		switch (loadResult.getStatus()) {
		case SUCCESS -> displayCampaignSummary(loadResult.getRestoredCampaign().orElseThrow());

		case SAVE_NOT_FOUND -> {
			System.out.println();
			ConsoleUI.displayWrappedText("Nenhuma campanha ativa.");
		}

		case FILE_READ_FAILED -> {
			System.out.println();
			ConsoleUI.displayWrappedText("Não foi possível ler a campanha salva.");
		}

		case INVALID_SAVE -> {
			System.out.println();
			ConsoleUI.displayWrappedText("A campanha salva não pôde ser carregada.");
		}
		}
	}

	private void displayCampaignSummary(RestoredCampaign campaign) {
		PlayableCharacter leader = campaign.getParty().getLeader();

		String partySymbols = campaign.getParty().getMembers().stream()
				.map(member -> "[" + member.getPlayableClassDefinition().getSymbol() + "]")
				.collect(Collectors.joining(" "));

		System.out.println();
		System.out.printf("%-7s%s%n", "Líder:", leader.getName());
		System.out.printf("%-7s%d%n", "Nível:", leader.getLevel());
		System.out.printf("%-7s%s%n", "Local:", campaign.getCurrentScenario().getName());
		System.out.printf("%-7s%s%n", "Grupo:", partySymbols);
	}

	private boolean confirmNewGame() {
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Nova Campanha");

			System.out.println();
			ConsoleUI.displayWrappedText("A campanha salva atual continuará preservada.");
			ConsoleUI.displayWrappedText("Ela só será substituída quando a nova campanha for salva com sucesso.");

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Voltar");
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
}