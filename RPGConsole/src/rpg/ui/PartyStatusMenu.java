package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.GameCurrency;
import rpg.battle.rewards.PartyRewardModifierState;

// Displays the party summary and allows character selection.
public final class PartyStatusMenu {

	private final Party party;
	private final ConsoleInput consoleInput;
	private final CharacterStatusMenu characterStatusMenu;

	public PartyStatusMenu(Party party, ConsoleInput consoleInput, EquipmentMenu equipmentMenu) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.characterStatusMenu = new CharacterStatusMenu(
				this.party,
				consoleInput,
				Objects.requireNonNull(equipmentMenu, "Equipment menu cannot be null.")
		);
	}

	public void open() {
		while (true) {
			List<PlayableCharacter> members = party.getMembers();

			displayPartyStatus(members);

			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

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
		System.out.println("\n[STATUS]\n");
		int nameWidth = party.getMembers().stream().mapToInt(currentMember -> currentMember.getName().length()).max().orElse(1);
		int levelWidth = party.getMembers().stream()
				.mapToInt(currentMember -> String.valueOf(currentMember.getLevel()).length()).max().orElse(1);
		int hpWidth = party.getMembers().stream()
				.mapToInt(currentMember -> (currentMember.getCurrentHp() + "/" + currentMember.getMaxHp()).length())
				.max()
				.orElse(1);
		int mpWidth = party.getMembers().stream()
				.mapToInt(currentMember -> (currentMember.getCurrentMp() + "/" + currentMember.getMaxMp()).length())
				.max()
				.orElse(1);
		for (int index = 0; index < members.size(); index++) {

			PlayableCharacter member = members.get(index);

			displayMemberSummary(member, index + 1, nameWidth, levelWidth, hpWidth, mpWidth);
		}

		System.out.println("\n" + GameCurrency.getName() + ": " + GameCurrency.format(party.getGold()));

		displayRewardModifiers();
	}

private void displayMemberSummary(
                PlayableCharacter member,
                int optionNumber,
                int nameWidth,
                int levelWidth,
                int hpWidth,
                int mpWidth
) {
        String memberLabel = String.format("%-" + nameWidth + "s", member.getName()) + " [" + member.getPlayableClassDefinition().getSymbol() + "]";
        String rowFormat = "[%d] %s%n    %s%n";

        if (!member.isAlive()) {
                System.out.printf(rowFormat, optionNumber, memberLabel, "DERROTADO");

                return;
        }

        String status = String.format(
                        "Nv. %-" + levelWidth + "s | HP: %-" + hpWidth + "s | MP: %-" + mpWidth + "s | SP: %s",
                        member.getLevel(),
                        member.getCurrentHp() + "/" + member.getMaxHp(),
                        member.getCurrentMp() + "/" + member.getMaxMp(),
                        member.getCurrentSp() + "/" + member.getMaxSp()
        );

        System.out.printf(rowFormat, optionNumber, memberLabel, status);
}
	private void displayRewardModifiers() {
		PartyRewardModifierState modifierState = party.getRewardModifierState();

		int totalGoldModifierPercent = Math
				.addExact(modifierState.getGoldModifierPercent(), party.getEquipmentGoldModifierPercent());

		String summary = RewardModifierFormatter.formatSummary(
				modifierState.getExperienceModifierPercent(),
				modifierState.getExperienceVictoriesRemaining(),
				modifierState.hasActiveExperienceModifier(),
				totalGoldModifierPercent,
				modifierState.getGoldVictoriesRemaining(),
				modifierState.hasActiveGoldModifier()
		);

		if (!summary.isEmpty()) {
			System.out.println(summary);
		}
	}
}
