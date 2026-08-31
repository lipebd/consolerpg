package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;

// Controls the selection of a living party member.
public final class AllyTargetMenu {

	private final Scanner scanner;

	public AllyTargetMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public Optional<PlayableCharacter> selectLivingTarget(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(party.getLivingMembers(), "Escolha um aliado:", "Não há aliados vivos disponíveis.");
	}

	public Optional<PlayableCharacter> selectDefeatedTarget(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		return selectTarget(party.getDefeatedMembers(), "Escolha um aliado para reviver:",
				"Não há aliados derrotados para reviver.");
	}

	private Optional<PlayableCharacter> selectTarget(List<PlayableCharacter> availableMembers, String prompt,
			String emptyMessage) {
		if (availableMembers.isEmpty()) {
			System.out.println(emptyMessage);
			return Optional.empty();
		}

		if (availableMembers.size() == 1) {
			return Optional.of(availableMembers.get(0));
		}

		while (true) {
			System.out.println("\n" + prompt);

			for (int index = 0; index < availableMembers.size(); index++) {

				PlayableCharacter member = availableMembers.get(index);

				System.out.print("[" + (index + 1) + "] " + member.getName());

				if (member.isAlive()) {
					System.out.println(" | HP: " + member.getCurrentHp() + "/" + member.getMaxHp());
				} else {
					System.out.println(" | DERROTADO");
				}
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

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
	
	public Optional<PlayableCharacter> selectInjuredTarget(
	        Party party
	) {
	    Objects.requireNonNull(
	            party,
	            "Party cannot be null."
	    );

	    List<PlayableCharacter> injuredMembers =
	            party.getLivingMembers()
	                    .stream()
	                    .filter(
	                            member ->
	                                    member.getCurrentHp()
	                                    < member.getMaxHp()
	                    )
	                    .toList();

	    return selectTarget(
	            injuredMembers,
	            "Escolha um aliado para curar:",
	            "Nenhum aliado precisa de cura."
	    );
	}
}