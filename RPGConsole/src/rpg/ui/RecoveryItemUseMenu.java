package rpg.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.function.BiConsumer;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.RecoveryItem;
import rpg.items.RecoveryItemTargetType;
import rpg.items.RecoveryItemUseResult;
import java.util.function.Consumer;

// Controls recovery item targeting, execution and result display.
public final class RecoveryItemUseMenu {

	private final Consumer<String> messageOutput;
	private final Party party;
	private final AllyTargetMenu allyTargetMenu;

	public RecoveryItemUseMenu(Party party, ConsoleInput consoleInput) {
		this(party, consoleInput, ConsoleUI::displayWrappedText);
	}

	public RecoveryItemUseMenu(Party party, ConsoleInput consoleInput, Consumer<String> messageOutput) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.messageOutput = Objects.requireNonNull(messageOutput, "Message output cannot be null.");
		this.allyTargetMenu = new AllyTargetMenu(consoleInput);
	}

	public boolean use(RecoveryItem recoveryItem, PlayableCharacter actingCharacter) {
		return use(recoveryItem, actingCharacter, null);
	}

	public boolean use(
	        RecoveryItem recoveryItem,
	        PlayableCharacter actingCharacter,
	        BiConsumer<List<PlayableCharacter>, String> targetDisplay
	) {
	    return use(recoveryItem, actingCharacter, targetDisplay, null);
	}

	public boolean use(
	        RecoveryItem recoveryItem,
	        PlayableCharacter actingCharacter,
	        BiConsumer<List<PlayableCharacter>, String> targetDisplay,
	        BiConsumer<RecoveryItem, PlayableCharacter> postActionStage
	) {
	    Objects.requireNonNull(recoveryItem, "Recovery item cannot be null.");

	    if (recoveryItem.hasAttributeStage()
	            && targetDisplay == null) {
	        messageOutput.accept("Este item só pode ser usado em batalha.");
	        return false;
	    }

	    if (recoveryItem.getTargetType() == RecoveryItemTargetType.SINGLE_ALLY) {
	        return useSingleTargetItem(
	                recoveryItem, actingCharacter, targetDisplay,
	                postActionStage);
	    }

	    return useGroupItem(recoveryItem, actingCharacter, targetDisplay == null);
	}

	private boolean useSingleTargetItem(
			RecoveryItem recoveryItem,
			PlayableCharacter actingCharacter,
			BiConsumer<List<PlayableCharacter>, String> targetDisplay,
			BiConsumer<RecoveryItem, PlayableCharacter> postActionStage
	) {
		if (targetDisplay != null && party.getMembers().stream().noneMatch(
				member -> recoveryItem.canAffect(member) || recoveryItem.canApplyAttributeStage(member, actingCharacter)
		)) {
			messageOutput.accept("Nenhum aliado pode ser afetado por este item.");
			return false;
		}

		Optional<PlayableCharacter> selectedTarget = targetDisplay == null
				? allyTargetMenu.selectRecoveryTarget(party, recoveryItem)
				: allyTargetMenu.selectRecoveryBattleTarget(party, recoveryItem, targetDisplay, actingCharacter);

		if (selectedTarget.isEmpty()) {
			return false;
		}

		PlayableCharacter target = selectedTarget.get();

		boolean stageWillApply = postActionStage != null
				&& recoveryItem.canApplyAttributeStage(target, actingCharacter);

		RecoveryItemUseResult result = recoveryItem.use(target);

		if (!result.hasAnyEffect() && !stageWillApply) {
			messageOutput.accept("O item não produziu nenhum efeito.");
			return false;
		}

		party.getInventory().removeItem(recoveryItem, 1);

		displayItemUse(actingCharacter, target, recoveryItem);
		displayRecoveryResult(target, result);

		if (stageWillApply) {
			postActionStage.accept(recoveryItem, target);
		}

		return true;
	}

	private boolean useGroupItem(RecoveryItem recoveryItem, PlayableCharacter actingCharacter, boolean outsideBattle) {
		if (outsideBattle && !allyTargetMenu.confirmGroupRecoveryUse(party, recoveryItem)) {
			return false;
		}
		Map<PlayableCharacter, RecoveryItemUseResult> results = new LinkedHashMap<>();

		for (PlayableCharacter member : party.getMembers()) {
			RecoveryItemUseResult result = recoveryItem.use(member);

			if (result.hasAnyEffect()) {
				results.put(member, result);
			}
		}

		if (results.isEmpty()) {
			messageOutput.accept("O item não produziu nenhum efeito.");

			return false;
		}

		party.getInventory().removeItem(recoveryItem, 1);

		displayGroupItemUse(actingCharacter, recoveryItem);

		for (Map.Entry<PlayableCharacter, RecoveryItemUseResult> entry : results.entrySet()) {
			displayRecoveryResult(entry.getKey(), entry.getValue());
		}

		return true;
	}

	private void displayRecoveryResult(PlayableCharacter target, RecoveryItemUseResult result) {
		if (result.getRevivedHp() > 0) {
			messageOutput.accept(target.getName() + " foi revivido com " + result.getRevivedHp() + " de HP.");
		}

		if (result.getRecoveredHp() > 0) {
			messageOutput.accept(
					target.getName() + " recuperou " + result.getRecoveredHp() + " de HP. HP atual: "
							+ target.getCurrentHp() + "/" + target.getMaxHp() + "."
			);
		}

		if (result.getRecoveredMp() > 0) {
			messageOutput.accept(
					target.getName() + " recuperou " + result.getRecoveredMp() + " de MP. MP atual: "
							+ target.getCurrentMp() + "/" + target.getMaxMp() + "."
			);
		}

		if (result.getRecoveredSp() > 0) {
			messageOutput.accept(
					target.getName() + " recuperou " + result.getRecoveredSp() + " de SP. SP atual: "
							+ target.getCurrentSp() + "/" + target.getMaxSp() + "."
			);
		}

		if (result.purifiedStatusEffects()) {
			messageOutput.accept(target.getName() + " teve efeitos negativos de atributos removidos.");
		}
	}

	private void
			displayItemUse(PlayableCharacter actingCharacter, PlayableCharacter target, RecoveryItem recoveryItem) {
		if (actingCharacter == null || actingCharacter == target) {
			messageOutput.accept(target.getName() + " usou " + recoveryItem.getName() + ".");

			return;
		}

		messageOutput.accept(
				actingCharacter.getName() + " usou " + recoveryItem.getName() + " em " + target.getName() + "."
		);
	}

	private void displayGroupItemUse(PlayableCharacter actingCharacter, RecoveryItem recoveryItem) {
		if (actingCharacter == null) {
			messageOutput.accept("O grupo usou " + recoveryItem.getName() + ".");

			return;
		}

		messageOutput.accept(actingCharacter.getName() + " usou " + recoveryItem.getName() + " no grupo.");
	}
}