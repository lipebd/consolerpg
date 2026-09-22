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

		while (true) {
			displayCampaignState(loadResult);

			boolean canContinue = loadResult.isSuccess();

			if (canContinue) {
				System.out.println();
				System.out.println("[1] Continuar");
				System.out.println("[2] Novo Jogo");
			} else {
				System.out.println();
				System.out.println("[1] Novo Jogo");
			}

			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (canContinue && choice.equals("1")) {
				return Selection.CONTINUE;
			}

			boolean selectedNewGame = canContinue ? choice.equals("2") : choice.equals("1");

			if (selectedNewGame) {
				if (loadResult.getStatus() == CampaignLoadStatus.SAVE_NOT_FOUND || confirmNewGame()) {
					return Selection.NEW_GAME;
				}

				ConsoleUI.clearScreen();
				continue;
			}

			System.out.println("Opção inválida.");
		}
	}

	private void displayCampaignState(CampaignLoadResult loadResult) {
		System.out.println("[RPGCONSOLE]");
		System.out.println("[CAMPANHA]");

		switch (loadResult.getStatus()) {
		case SUCCESS -> displayCampaignSummary(loadResult.getRestoredCampaign().orElseThrow());

		case SAVE_NOT_FOUND -> {
			System.out.println();
			System.out.println("Não há campanha ativa.");
		}

		case FILE_READ_FAILED -> {
			System.out.println();
			System.out.println("Não foi possível ler a campanha salva.");
		}

		case INVALID_SAVE -> {
			System.out.println();
			System.out.println("A campanha salva não pôde ser carregada.");
		}
		}
	}

	private void displayCampaignSummary(RestoredCampaign campaign) {
		PlayableCharacter leader = campaign.getParty().getLeader();

		String partySymbols = campaign.getParty().getMembers().stream()
				.map(member -> "[" + member.getPlayableClassDefinition().getSymbol() + "]")
				.collect(Collectors.joining(" "));

		System.out.println();
		System.out.println("Líder: " + leader.getName());
		System.out.println("Nível: " + leader.getLevel());
		System.out.println("Cidade: " + campaign.getCurrentScenario().getName());
		System.out.println("Grupo: " + partySymbols);
	}

	private boolean confirmNewGame() {
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[RPGCONSOLE]");
			System.out.println("[NOVO JOGO]");

			System.out.println();
			System.out.println("A campanha salva atual continuará preservada.");
			System.out.println("Ela só será substituída quando o novo jogo for salvo com sucesso.");

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

			default -> System.out.println("Opção inválida.");
			}
		}
	}
}