package rpg.battle;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Random;

import rpg.characters.CharacterAttribute;
import rpg.characters.GameCharacter;
import rpg.effects.AttributeStageRules;
import rpg.effects.SpecialBattleStateType;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillEffectDefinition;
import rpg.skills.SkillEffectType;
import rpg.skills.SkillExecutionPhase;

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

	public SkillExecution beginExecution(SkillDefinition skill, SkillExecutionContext context) {
		SkillExecution execution = new SkillExecution(skill, context);

		executePhase(execution, SkillExecutionPhase.PRE_ACTION);

		executePhase(execution, SkillExecutionPhase.PRIMARY);

		boolean canConsumeTurn = execution.hasAttemptedEffect() || hasApplicablePostActionEffect(execution);

		execution.setCanConsumeTurn(canConsumeTurn);

		return execution;
	}

	public List<SkillEffectResult> executePostAction(SkillExecution execution) {
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

	private void executePhase(SkillExecution execution, SkillExecutionPhase phase) {
		for (SkillEffectDefinition effect : execution.getSkillDefinition().getEffectsByPhase(phase)) {

			List<GameCharacter> applicableTargets = getApplicableTargets(execution, effect);

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

				execution.addEffectResult(applyEffect(execution, effect, target));
			}
		}
	}

	private List<GameCharacter> getApplicableTargets(SkillExecution execution, SkillEffectDefinition effect) {
		List<GameCharacter> applicableTargets = new ArrayList<>();

		for (GameCharacter target : execution.getContext().getTargets(effect.getTargetType())) {

			if (conditionMatches(execution, effect, target) && canAffect(execution, effect, target)) {

				applicableTargets.add(target);
			}
		}

		return applicableTargets;
	}

	private boolean passesTriggerCheck(SkillEffectDefinition effect) {
		return effect.getTriggerChancePercent() == 100 || random.nextInt(100) < effect.getTriggerChancePercent();
	}

	private boolean targetResisted(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
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

	private SkillEffectResult
			applyEffect(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {

		return switch (effect.getEffectType()) {
		case DAMAGE -> applyDamage(execution, effect, target);

		case RECOIL_DAMAGE -> applyRecoilDamage(execution, effect, target);

		case HP_RECOVERY -> applyHpRecovery(execution, effect, target);

		case MP_RECOVERY -> createAmountResult(effect, target, target.recoverMp(effect.getPower()));

		case SP_RECOVERY -> createAmountResult(effect, target, target.recoverSp(effect.getPower()));

		case REVIVE -> applyRevival(execution, effect, target);

		case ATTRIBUTE_STAGE -> applyAttributeStages(effect, target);

		case PURIFICATION -> createBooleanResult(effect, target, target.purifyNegativeAttributeStates());

		case DISSIPATION -> createBooleanResult(effect, target, target.dissipatePositiveAttributeStates());

		case SPECIAL_STATE -> applySpecialState(effect, target);

		case DAMAGE_POWER_MODIFIER -> applyDamagePowerModifier(execution, effect, target);
		};
	}

	private SkillEffectResult
			applyDamagePowerModifier(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		execution.addDamagePowerModifier(target, effect.getPower());

		return createAppliedResult(effect, target);
	}

	private SkillEffectResult
			applyDamage(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		double calculatedDamage = damageCalculator.calculateDamage(
				execution.getContext().getActingCharacter(),
				target,
				effect.getDamageType(),
				effect.getPower(),
				execution.getContext().getWeaponBaseDamage(),
				execution.getDamagePowerModifier(target)
		);

		int actualDamage = target.takeDamage(calculatedDamage);

		return createAmountResult(effect, target, actualDamage);
	}

	private SkillEffectResult
			applyRecoilDamage(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		int calculatedDamage = resolveRecoilDamage(execution, effect);

		int actualDamage = target.takeNonLethalDamage(calculatedDamage);

		return createAmountResult(effect, target, actualDamage);
	}

	private int resolveRecoilDamage(SkillExecution execution, SkillEffectDefinition recoilEffect) {
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

	private boolean hasApplicablePostActionEffect(SkillExecution execution) {
		for (SkillEffectDefinition effect : execution.getSkillDefinition()
				.getEffectsByPhase(SkillExecutionPhase.POST_ACTION)) {

			for (GameCharacter target : execution.getContext().getTargets(effect.getTargetType())) {
				if (conditionMatches(execution, effect, target) && canAffect(execution, effect, target)) {

					return true;
				}
			}
		}

		return false;
	}

	private boolean conditionMatches(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		return switch (effect.getConditionType()) {
		case NONE -> true;

		case SELF_HP_AT_MOST_PERCENT ->
			isHpAtMostPercent(execution.getContext().getActingCharacter(), effect.getConditionValue());

		case TARGET_HP_AT_MOST_PERCENT -> isHpAtMostPercent(target, effect.getConditionValue());
		};
	}

	private boolean isHpAtMostPercent(GameCharacter character, int maximumPercent) {
		long currentPercentageValue = (long) character.getCurrentHp() * 100;

		long maximumPercentageValue = (long) character.getMaxHp() * maximumPercent;

		return currentPercentageValue <= maximumPercentageValue;
	}

	private boolean canAffect(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		return switch (effect.getEffectType()) {

		case DAMAGE -> target.isAlive();

		case RECOIL_DAMAGE ->
			target.isAlive() && target.getCurrentHp() > 1 && resolveRecoilDamage(execution, effect) > 0;

		case DAMAGE_POWER_MODIFIER -> target.isAlive();

		case HP_RECOVERY -> target.isAlive() && target.getCurrentHp() < target.getMaxHp();

		case MP_RECOVERY -> target.isAlive() && target.getCurrentMp() < target.getMaxMp();

		case SP_RECOVERY -> target.isAlive() && target.getCurrentSp() < target.getMaxSp();

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
			applyHpRecovery(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		int calculatedRecovery = recoveryCalculator.calculateRecovery(
				execution.getContext().getActingCharacter(),
				effect.getAttribute(),
				effect.getPower()
		);

		int actualRecovery = target.recoverHp(calculatedRecovery);

		return createAmountResult(effect, target, actualRecovery);
	}

	private SkillEffectResult
			applyRevival(SkillExecution execution, SkillEffectDefinition effect, GameCharacter target) {
		int calculatedRecovery = recoveryCalculator.calculateRecovery(
				execution.getContext().getActingCharacter(),
				effect.getAttribute(),
				effect.getPower()
		);

		int actualRecovery = target.revive(calculatedRecovery);

		return createAmountResult(effect, target, actualRecovery);
	}
}