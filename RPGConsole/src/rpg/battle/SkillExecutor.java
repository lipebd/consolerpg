package rpg.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import rpg.characters.CharacterAttribute;
import rpg.characters.GameCharacter;
import rpg.effects.AttributeStageRules;
import rpg.effects.SpecialBattleStateType;
import rpg.skills.CombatSkillDefinition;
import rpg.skills.SkillEffectDefinition;
import rpg.skills.SkillEffectType;
import rpg.skills.SkillExecutionPhase;
import rpg.skills.SkillTargetType;

// Executes the ordered components of one skill.
public final class SkillExecutor {

	private final DamageCalculator damageCalculator;
	private final RecoveryCalculator recoveryCalculator;
	private final EffectResistanceCalculator effectResistanceCalculator;
	private final Random random;

	public SkillExecutor(
			DamageCalculator damageCalculator,
			RecoveryCalculator recoveryCalculator,
			EffectResistanceCalculator effectResistanceCalculator,
			Random random
	) {
		this.damageCalculator = Objects.requireNonNull(damageCalculator, "Damage calculator cannot be null.");

		this.recoveryCalculator = Objects.requireNonNull(recoveryCalculator, "Recovery calculator cannot be null.");

		this.effectResistanceCalculator = Objects
				.requireNonNull(effectResistanceCalculator, "Effect resistance calculator cannot be null.");

		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	public SkillUseState beginExecution(CombatSkillDefinition skill, SkillExecutionContext context) {
		SkillUseState execution = new SkillUseState(skill, context);

		executePhase(execution, SkillExecutionPhase.PRE_ACTION);

		executePhase(execution, SkillExecutionPhase.PRIMARY);

		boolean canConsumeTurn = execution.hasAttemptedEffect() || hasApplicablePostActionEffect(execution);

		execution.setCanConsumeTurn(canConsumeTurn);

		return execution;
	}

	public List<SkillEffectResult> executePostAction(SkillUseState execution) {
		Objects.requireNonNull(execution, "Skill execution cannot be null.");

		if (execution.hasPostActionExecuted()) {
			throw new IllegalStateException("Skill POST_ACTION was already executed.");
		}

		if (!execution.canConsumeTurn()) {
			throw new IllegalStateException("An ineffective skill cannot execute POST_ACTION.");
		}

		int previousResultCount = execution.getEffectResultCount();

		executePhase(execution, SkillExecutionPhase.POST_ACTION);

		execution.markPostActionExecuted();

		return execution.getEffectResultsFrom(previousResultCount);
	}

	private void executeHpBalance(SkillUseState execution, SkillEffectDefinition effect) {
		List<GameCharacter> livingAllies = execution.getContext().getTargets(effect.getTargetType()).stream()
				.filter(GameCharacter::isAlive).toList();

		if (livingAllies.isEmpty()) {
			return;
		}

		long totalHp = 0;

		for (GameCharacter ally : livingAllies) {
			totalHp += ally.getCurrentHp();
		}

		int averageHp = (int) Math.round((double) totalHp / livingAllies.size());
		List<GameCharacter> affectedAllies = new ArrayList<>();

		for (GameCharacter ally : livingAllies) {
			if (ally.getCurrentHp() != Math.min(averageHp, ally.getMaxHp())) {
				affectedAllies.add(ally);
			}
		}

		if (affectedAllies.isEmpty()) {
			return;
		}

		if (!passesTriggerCheck(effect)) {
			for (GameCharacter ally : affectedAllies) {
				execution.addEffectResult(new SkillEffectResult(effect, ally, SkillEffectOutcome.NOT_TRIGGERED, 0));
			}

			return;
		}

		for (GameCharacter ally : affectedAllies) {
			int balancedHp = Math.min(averageHp, ally.getMaxHp());
			int difference = balancedHp - ally.getCurrentHp();

			if (difference > 0) {
				ally.recoverHp(difference);
			} else {
				ally.takeNonLethalDamage(-difference);
			}

			execution.addEffectResult(
					new SkillEffectResult(effect, ally, SkillEffectOutcome.APPLIED, ally.getCurrentHp())
			);
		}
	}

	private void executePhase(SkillUseState execution, SkillExecutionPhase phase) {
		boolean pendingPairedDamage = false;
		GameCharacter pendingTarget = null;
		int pendingModifierPercent = 0;

		for (SkillEffectDefinition effect : execution.getSkillDefinition().getEffectsByPhase(phase)) {
			boolean usesPendingTarget = pendingPairedDamage;
			GameCharacter targetForThisHit = pendingTarget;
			int bonusForThisHit = pendingModifierPercent;

			pendingPairedDamage = false;
			pendingTarget = null;
			pendingModifierPercent = 0;

			if (effect.getEffectType() == SkillEffectType.HP_BALANCE) {
				executeHpBalance(execution, effect);
				continue;
			}

			List<GameCharacter> selectedTargets;

			if (usesPendingTarget) {
				selectedTargets = targetForThisHit != null && targetForThisHit.isAlive() ? List.of(targetForThisHit)
						: List.of();
			} else {
				List<GameCharacter> reservedTargets = phase == SkillExecutionPhase.POST_ACTION
						? execution.takeReservedPostActionTargets(effect)
						: null;

				selectedTargets = reservedTargets == null ? selectTargets(execution, effect) : reservedTargets;
			}

			boolean primaryPairedModifier = effect.getPhase() == SkillExecutionPhase.PRIMARY
					&& effect.getEffectType() == SkillEffectType.DAMAGE_POWER_MODIFIER;

			if (primaryPairedModifier) {
				pendingPairedDamage = true;
				pendingTarget = selectedTargets.isEmpty() ? null : selectedTargets.get(0);
			}

			List<GameCharacter> applicableTargets = getApplicableTargets(execution, effect, selectedTargets);

			if (applicableTargets.isEmpty()) {
				continue;
			}

			if (!passesTriggerCheck(effect)) {
				for (GameCharacter target : applicableTargets) {
					execution.addEffectResult(
							new SkillEffectResult(effect, target, SkillEffectOutcome.NOT_TRIGGERED, 0)
					);
				}

				continue;
			}

			for (GameCharacter target : applicableTargets) {
				if (targetResisted(execution, effect, target)) {
					execution.addEffectResult(new SkillEffectResult(effect, target, SkillEffectOutcome.RESISTED, 0));
					continue;
				}

				execution.addEffectResult(
						applyEffect(execution, effect, target, usesPendingTarget ? bonusForThisHit : 0)
				);

				if (primaryPairedModifier) {
					pendingModifierPercent = effect.getPower();
				}
			}
		}
	}

	private List<GameCharacter> selectTargets(SkillUseState execution, SkillEffectDefinition effect) {
		List<GameCharacter> candidates = execution.getContext().getTargets(effect.getTargetType());

		if (effect.getTargetType() != SkillTargetType.RANDOM_ENEMY
				&& effect.getTargetType() != SkillTargetType.RANDOM_ALLY) {
			return candidates;
		}

		boolean targetsDefeated = effect.getEffectType() == SkillEffectType.REVIVE;

		candidates = candidates.stream().filter(target -> targetsDefeated ? !target.isAlive() : target.isAlive())
				.toList();

		if (candidates.isEmpty()) {
			return List.of();
		}

		return List.of(candidates.get(random.nextInt(candidates.size())));
	}

	private List<GameCharacter> getApplicableTargets(
			SkillUseState execution,
			SkillEffectDefinition effect,
			List<GameCharacter> selectedTargets
	) {
		List<GameCharacter> applicableTargets = new ArrayList<>();

		for (GameCharacter target : selectedTargets) {
			if (conditionMatches(execution, effect, target) && canAffect(execution, effect, target)) {
				applicableTargets.add(target);
			}
		}

		return applicableTargets;
	}

	private boolean passesTriggerCheck(SkillEffectDefinition effect) {
		return effect.getTriggerChancePercent() == 100 || random.nextInt(100) < effect.getTriggerChancePercent();
	}

	private boolean targetResisted(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		if (!requiresResistanceCheck(effect)) {
			return false;
		}

		int applicationChancePercent = effectResistanceCalculator
				.calculateApplicationChancePercent(execution.getContext().getActingCharacter(), target);

		return random.nextInt(100) >= applicationChancePercent;
	}

	private boolean requiresResistanceCheck(SkillEffectDefinition effect) {
		if (effect.getEffectType() == SkillEffectType.DISSIPATION) {

			return true;
		}

		return effect.getEffectType() == SkillEffectType.ATTRIBUTE_STAGE && effect.getStages() < 0;
	}

	private SkillEffectResult applyEffect(
			SkillUseState execution,
			SkillEffectDefinition effect,
			GameCharacter target,
			int primaryModifierPercent
	) {

		return switch (effect.getEffectType()) {
		case DAMAGE -> applyDamage(execution, effect, target, primaryModifierPercent);

		case RECOIL_DAMAGE -> applyRecoilDamage(execution, effect, target);

		case HP_SACRIFICE -> applyHpSacrifice(effect, target);

		case HP_RECOVERY -> applyHpRecovery(execution, effect, target);

		case MP_RECOVERY -> createAmountResult(effect, target, target.recoverMp(effect.getPower()));

		case SP_RECOVERY -> createAmountResult(effect, target, target.recoverSp(effect.getPower()));

		case HP_BALANCE -> throw new IllegalStateException("HP balance must execute as a group.");

		case REVIVE -> applyRevival(execution, effect, target);

		case ATTRIBUTE_STAGE -> applyAttributeStages(effect, target);

		case PURIFICATION, DISSIPATION -> createAmountResult(effect, target, removeAttributeStates(effect, target));

		case SPECIAL_STATE -> applySpecialState(effect, target);

		case DAMAGE_POWER_MODIFIER -> applyDamagePowerModifier(execution, effect, target);
		};
	}

	private SkillEffectResult applyHpSacrifice(SkillEffectDefinition effect, GameCharacter target) {
		int calculatedDamage = (int) ((long) target.getCurrentHp() * effect.getPower() / 100);
		int actualDamage = target.takeNonLethalDamage(calculatedDamage);

		return new SkillEffectResult(effect, target, SkillEffectOutcome.APPLIED, actualDamage);
	}

	private int removeAttributeStates(SkillEffectDefinition effect, GameCharacter target) {
		boolean purification = effect.getEffectType() == SkillEffectType.PURIFICATION;
		Integer limit = effect.getPower();

		List<CharacterAttribute> eligible = new ArrayList<>();

		for (CharacterAttribute attribute : CharacterAttribute.values()) {
			int stages = purification ? target.getNegativeAttributeStages(attribute)
					: target.getPositiveAttributeStages(attribute);

			if (stages > 0) {
				eligible.add(attribute);
			}
		}

		if (eligible.isEmpty()) {
			return 0;
		}

		if (limit == null) {
			boolean changed = purification ? target.purifyNegativeAttributeStates()
					: target.dissipatePositiveAttributeStates();

			return changed ? eligible.size() : 0;
		}

		if (limit < eligible.size()) {
			Collections.shuffle(eligible, random);
		}

		EnumSet<CharacterAttribute> selected = EnumSet.noneOf(CharacterAttribute.class);

		for (int index = 0; index < Math.min(limit, eligible.size()); index++) {
			selected.add(eligible.get(index));
		}

		boolean changed = purification ? target.purifyNegativeAttributeStates(selected)
				: target.dissipatePositiveAttributeStates(selected);

		return changed ? selected.size() : 0;
	}

	private SkillEffectResult
			applyDamagePowerModifier(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		if (effect.getPhase() == SkillExecutionPhase.PRE_ACTION) {
			execution.addDamagePowerModifier(target, effect.getPower());
		}

		return createAppliedResult(effect, target);
	}

	private SkillEffectResult applyDamage(
			SkillUseState execution,
			SkillEffectDefinition effect,
			GameCharacter target,
			int primaryModifierPercent
	) {
		double calculatedDamage = damageCalculator.calculateDamage(
				execution.getContext().getActingCharacter(),
				target,
				effect.getDamageType(),
				effect.getAttribute(),
				effect.getPower(),
				execution.getContext().getWeaponBaseDamage(),
				Math.addExact(execution.getDamagePowerModifier(target), primaryModifierPercent)
		);

		int actualDamage = target.takeDamage(calculatedDamage);

		return createAmountResult(effect, target, actualDamage);
	}

	private SkillEffectResult
			applyRecoilDamage(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		int calculatedDamage = resolveRecoilDamage(execution, effect);

		int actualDamage = target.takeNonLethalDamage(calculatedDamage);

		return createAmountResult(effect, target, actualDamage);
	}

	private int resolveRecoilDamage(SkillUseState execution, SkillEffectDefinition recoilEffect) {
		SkillEffectDefinition sourceEffect = execution.getSkillDefinition().getRecoilSourceEffect(recoilEffect);

		int sourceDamage = execution.getSingleTargetDamageAmount(sourceEffect);

		return damageCalculator.calculateRecoilDamage(sourceDamage, recoilEffect.getPower());
	}

	private SkillEffectResult applyAttributeStages(SkillEffectDefinition effect, GameCharacter target) {
		boolean producedEffect;

		if (effect.getAttribute() == CharacterAttribute.VEL) {
			producedEffect = target.applyPendingSpeedStages(effect.getStages());
		} else {
			producedEffect = target
					.applyTimedAttributeStages(effect.getAttribute(), effect.getStages(), effect.getDuration());
		}

		return createBooleanResult(effect, target, producedEffect);
	}

	private SkillEffectResult applySpecialState(SkillEffectDefinition effect, GameCharacter target) {
		if (effect.getSpecialStateType() == SpecialBattleStateType.DISTORTED_ATTRIBUTES) {
			target.toggleDistortedAttributes();

			return createBooleanResult(effect, target, true);
		}

		throw new IllegalArgumentException("Unsupported special battle state: " + effect.getSpecialStateType());
	}

	private SkillEffectResult createAmountResult(SkillEffectDefinition effect, GameCharacter target, int amount) {
		if (amount <= 0) {
			throw new IllegalStateException("An applicable amount effect must produce a positive amount.");
		}

		return new SkillEffectResult(effect, target, SkillEffectOutcome.APPLIED, amount);
	}

	private SkillEffectResult
			createBooleanResult(SkillEffectDefinition effect, GameCharacter target, boolean producedEffect) {
		if (!producedEffect) {
			throw new IllegalStateException("An applicable skill effect failed to alter its target.");
		}

		return createAppliedResult(effect, target);
	}

	private SkillEffectResult createAppliedResult(SkillEffectDefinition effect, GameCharacter target) {
		return new SkillEffectResult(effect, target, SkillEffectOutcome.APPLIED, 0);
	}

	private boolean hasApplicablePostActionEffect(SkillUseState execution) {
		for (SkillEffectDefinition effect : execution.getSkillDefinition()
				.getEffectsByPhase(SkillExecutionPhase.POST_ACTION)) {
			List<GameCharacter> selectedTargets = selectTargets(execution, effect);

			if (getApplicableTargets(execution, effect, selectedTargets).isEmpty()) {
				continue;
			}

			if (effect.getTargetType() == SkillTargetType.RANDOM_ALLY
					|| effect.getTargetType() == SkillTargetType.RANDOM_ENEMY) {
				execution.reservePostActionTargets(effect, selectedTargets);
			}

			return true;
		}

		return false;
	}

	private boolean conditionMatches(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		GameCharacter actingCharacter = execution.getContext().getActingCharacter();

		return switch (effect.getConditionType()) {
		case NONE -> true;
		case SELF_HP_LESS_THAN_PERCENT -> compareHpPercent(actingCharacter, effect.getConditionPercent()) < 0;
		case SELF_HP_GREATER_THAN_PERCENT -> compareHpPercent(actingCharacter, effect.getConditionPercent()) > 0;
		case TARGET_HP_LESS_THAN_PERCENT -> compareHpPercent(target, effect.getConditionPercent()) < 0;
		case TARGET_HP_GREATER_THAN_PERCENT -> compareHpPercent(target, effect.getConditionPercent()) > 0;
		case SELF_GREATER -> compareConditionalAttribute(actingCharacter, target, effect) > 0;
		case SELF_LOWER -> compareConditionalAttribute(actingCharacter, target, effect) < 0;
		};
	}

	private int compareConditionalAttribute(
			GameCharacter actingCharacter,
			GameCharacter target,
			SkillEffectDefinition effect
	) {
		CharacterAttribute attribute = effect.getConditionAttribute();

		return Integer
				.compare(actingCharacter.getEffectiveAttribute(attribute), target.getEffectiveAttribute(attribute));
	}

	private int compareHpPercent(GameCharacter character, int percent) {
		return Long.compare((long) character.getCurrentHp() * 100, (long) character.getMaxHp() * percent);
	}

	private boolean canAffect(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		return switch (effect.getEffectType()) {

		case DAMAGE -> target.isAlive();

		case RECOIL_DAMAGE ->
			target.isAlive() && target.getCurrentHp() > 1 && resolveRecoilDamage(execution, effect) > 0;

		case DAMAGE_POWER_MODIFIER -> target.isAlive();

		case HP_SACRIFICE -> target.isAlive();

		case HP_RECOVERY -> target.isAlive() && target.getCurrentHp() < target.getMaxHp();

		case MP_RECOVERY -> target.isAlive() && target.getCurrentMp() < target.getMaxMp();

		case SP_RECOVERY -> target.isAlive() && target.getCurrentSp() < target.getMaxSp();

		case HP_BALANCE -> throw new IllegalStateException("HP balance must execute as a group.");

		case REVIVE -> !target.isAlive();

		case ATTRIBUTE_STAGE -> canApplyAttributeStages(effect, target);

		case PURIFICATION -> target.isAlive() && target.hasNegativeAttributeStates();

		case DISSIPATION -> target.isAlive() && target.hasPositiveAttributeStates();

		case SPECIAL_STATE -> target.isAlive();
		};

	}

	private boolean canApplyAttributeStages(SkillEffectDefinition effect, GameCharacter target) {
		if (!target.isAlive()) {
			return false;
		}

		CharacterAttribute attribute = effect.getAttribute();

		int stages = effect.getStages();

		if (stages > 0) {
			if (target.getPositiveAttributeStages(attribute) < AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE) {
				return true;
			}

			return attribute != CharacterAttribute.VEL
					&& target.getPositiveAttributeRemainingTurns(attribute) < effect.getDuration();
		}

		if (target.getNegativeAttributeStages(attribute) < AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE) {
			return true;
		}

		return attribute != CharacterAttribute.VEL
				&& target.getNegativeAttributeRemainingTurns(attribute) < effect.getDuration();
	}

	private SkillEffectResult
			applyHpRecovery(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		int calculatedRecovery = recoveryCalculator.calculateRecovery(
				execution.getContext().getActingCharacter(),
				effect.getAttribute(),
				effect.getPower()
		);

		int actualRecovery = target.recoverHp(calculatedRecovery);

		return createAmountResult(effect, target, actualRecovery);
	}

	private SkillEffectResult
			applyRevival(SkillUseState execution, SkillEffectDefinition effect, GameCharacter target) {
		int calculatedRecovery = recoveryCalculator.calculateRecovery(
				execution.getContext().getActingCharacter(),
				effect.getAttribute(),
				effect.getPower()
		);

		int actualRecovery = target.revive(calculatedRecovery);

		return createAmountResult(effect, target, actualRecovery);
	}
}