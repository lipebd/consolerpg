package rpg.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.RecoveryItem;
import rpg.items.RecoveryItemTargetType;
import rpg.items.RecoveryItemUseResult;

// Controls recovery item targeting, execution and result display.
public final class RecoveryItemUseMenu {

	private final Party party;
	private final AllyTargetMenu allyTargetMenu;

	public RecoveryItemUseMenu(Party party, ConsoleInput consoleInput) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.allyTargetMenu = new AllyTargetMenu(consoleInput);
	}

	public boolean use(RecoveryItem recoveryItem, PlayableCharacter actingCharacter) {
		Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");

		if (recoveryItem.getTargetType() == RecoveryItemTargetType.SINGLE_ALLY) {
			return useSingleTargetItem(recoveryItem, actingCharacter);
		}

		return useGroupItem(recoveryItem, actingCharacter);
	}

	private boolean useSingleTargetItem(RecoveryItem recoveryItem, PlayableCharacter actingCharacter) {
		Optional<PlayableCharacter> selectedTarget = allyTargetMenu.selectRecoveryTarget(party, recoveryItem);

		if (selectedTarget.isEmpty()) {
			return false;
		}

		PlayableCharacter target = selectedTarget.get();

		RecoveryItemUseResult result = recoveryItem.use(target);

		if (!result.hasAnyEffect()) {
			System.out.println("O item não produziu nenhum efeito.");

			return false;
		}

		party.getInventory().removeItem(recoveryItem, 1);

		displayItemUse(actingCharacter, target, recoveryItem);

		displayRecoveryResult(target, result);

		return true;
	}

	private boolean useGroupItem(RecoveryItem recoveryItem, PlayableCharacter actingCharacter) {
		Map<PlayableCharacter, RecoveryItemUseResult> results = new LinkedHashMap<>();

		for (PlayableCharacter member : party.getMembers()) {
			RecoveryItemUseResult result = recoveryItem.use(member);

			if (result.hasAnyEffect()) {
				results.put(member, result);
			}
		}

		if (results.isEmpty()) {
			System.out.println("O item não produziu nenhum efeito.");

			return false;
		}

		party.getInventory().removeItem(recoveryItem, 1);

		displayGroupItemUse(actingCharacter, recoveryItem);

		for (Map.Entry<PlayableCharacter, RecoveryItemUseResult> entry : results.entrySet()) {
			displayRecoveryResult(entry.getKey(), entry.getValue());
		}

		return true;
	}

	private void displayRecoveryResult(
			PlayableCharacter target,
			RecoveryItemUseResult result
	) {
		if (result.getRevivedHp() > 0) {
			ConsoleUI.displayWrappedText(
					target.getName()
							+ " foi revivido com "
							+ result.getRevivedHp()
							+ " de HP."
			);
		}

		if (result.getRecoveredHp() > 0) {
			ConsoleUI.displayWrappedText(
					target.getName()
							+ " recuperou "
							+ result.getRecoveredHp()
							+ " de HP. HP atual: "
							+ target.getCurrentHp()
							+ "/"
							+ target.getMaxHp()
							+ "."
			);
		}

		if (result.getRecoveredMp() > 0) {
			ConsoleUI.displayWrappedText(
					target.getName()
							+ " recuperou "
							+ result.getRecoveredMp()
							+ " de MP. MP atual: "
							+ target.getCurrentMp()
							+ "/"
							+ target.getMaxMp()
							+ "."
			);
		}

		if (result.getRecoveredSp() > 0) {
			ConsoleUI.displayWrappedText(
					target.getName()
							+ " recuperou "
							+ result.getRecoveredSp()
							+ " de SP. SP atual: "
							+ target.getCurrentSp()
							+ "/"
							+ target.getMaxSp()
							+ "."
			);
		}

		if (result.purifiedStatusEffects()) {
			ConsoleUI.displayWrappedText(
					target.getName()
							+ " teve seus efeitos negativos removidos."
			);
		}
	}

	private void displayItemUse(
			PlayableCharacter actingCharacter,
			PlayableCharacter target,
			RecoveryItem recoveryItem
	) {
		if (actingCharacter == null || actingCharacter == target) {
			ConsoleUI.displayWrappedText(
					target.getName()
							+ " usou "
							+ recoveryItem.getName()
							+ "."
			);

			return;
		}

		ConsoleUI.displayWrappedText(
				actingCharacter.getName()
						+ " usou "
						+ recoveryItem.getName()
						+ " em "
						+ target.getName()
						+ "."
		);
	}

	private void displayGroupItemUse(
			PlayableCharacter actingCharacter,
			RecoveryItem recoveryItem
	) {
		if (actingCharacter == null) {
			ConsoleUI.displayWrappedText(
					"O grupo usou " + recoveryItem.getName() + "."
			);

			return;
		}

		ConsoleUI.displayWrappedText(
				actingCharacter.getName()
						+ " usou "
						+ recoveryItem.getName()
						+ " no grupo."
		);
	}
}