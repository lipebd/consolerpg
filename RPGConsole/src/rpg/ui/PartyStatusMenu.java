package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.GameCurrency;
import rpg.battle.rewards.PartyRewardModifierState;

// Displays the party summary and allows character selection.
public final class PartyStatusMenu {

	private final Party party;
	private final Scanner scanner;
	private final CharacterStatusMenu characterStatusMenu;

	public PartyStatusMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.characterStatusMenu = new CharacterStatusMenu(scanner);
	}

	public void open() {
		while (true) {
			List<PlayableCharacter> members = party.getMembers();

			displayPartyStatus(members);

			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= members.size()) {
					System.out.println("Integrante inválido.");

					continue;
				}

				PlayableCharacter selectedCharacter = members.get(selectedIndex);

				characterStatusMenu.open(selectedCharacter);

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de um integrante.");
			}
		}
	}

	private void displayPartyStatus(List<PlayableCharacter> members) {
		System.out.println("\n[STATUS]");

		for (int index = 0; index < members.size(); index++) {

			PlayableCharacter member = members.get(index);

			displayMemberSummary(member, index + 1);
		}

		System.out.println("\n" + GameCurrency.getName() + ": " + GameCurrency.format(party.getGold()));

		displayRewardModifiers();
	}

	private void displayMemberSummary(PlayableCharacter member, int optionNumber) {
		System.out.print("\n[" + optionNumber + "] " + member.getName());

		if (!member.isAlive()) {
			System.out.println(" | DERROTADO");
			return;
		}

		System.out.println(
				" | Nv. " + member.getLevel() + " | HP: " + member.getCurrentHp() + "/" + member.getMaxHp() + " | MP: "
						+ member.getCurrentMp() + "/" + member.getMaxMp() + " | SP: " + member.getCurrentSp() + "/"
						+ member.getMaxSp()
		);
	}

	private void displayRewardModifiers() {
		PartyRewardModifierState modifierState = party.getRewardModifierState();

		String summary = RewardModifierFormatter.formatTemporarySummary(
				modifierState.getExperienceModifierPercent(),
				modifierState.getExperienceVictoriesRemaining(),
				modifierState.getGoldModifierPercent(),
				modifierState.getGoldVictoriesRemaining()
		);

		if (!summary.isEmpty()) {
			System.out.println(summary);
		}
	}
}