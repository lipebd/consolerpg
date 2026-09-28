package rpg.battle;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.characters.GameCharacter;
import rpg.skills.CombatSkillDefinition;
import rpg.skills.SkillEffectDefinition;
import rpg.skills.SkillEffectType;
import rpg.skills.SkillTargetType;

// Tracks the mutable execution of one skill use.
public final class SkillUseState {

	private final CombatSkillDefinition skillDefinition;
	private final SkillExecutionContext context;

	private final List<SkillEffectResult> effectResults;
	private final Map<GameCharacter, Integer> damagePowerModifiers;
	private final Map<SkillEffectDefinition, List<GameCharacter>> reservedPostActionTargets;
	private boolean canConsumeTurn;
	private boolean postActionExecuted;

	SkillUseState(CombatSkillDefinition skill, SkillExecutionContext context) {
		this.skillDefinition = Objects.requireNonNull(skill, "Skill definition cannot be null.");

		this.context = Objects.requireNonNull(context, "Skill execution context cannot be null.");

		this.effectResults = new ArrayList<>();

		this.damagePowerModifiers = new IdentityHashMap<>();

		this.canConsumeTurn = false;
		this.postActionExecuted = false;

		this.reservedPostActionTargets = new IdentityHashMap<>();
	}

	void reservePostActionTargets(SkillEffectDefinition effect, List<GameCharacter> targets) {
		reservedPostActionTargets.put(effect, List.copyOf(targets));
	}

	List<GameCharacter> takeReservedPostActionTargets(SkillEffectDefinition effect) {
		return reservedPostActionTargets.remove(effect);
	}

	void addEffectResult(SkillEffectResult result) {
		effectResults.add(Objects.requireNonNull(result, "Skill effect result cannot be null."));
	}

	void addDamagePowerModifier(GameCharacter target, int modifierPercent) {
		damagePowerModifiers.merge(
				target,
				modifierPercent,
				(currentModifier, addedModifier) -> Math.addExact(currentModifier, addedModifier)
		);
	}

	int getDamagePowerModifier(GameCharacter target) {
		return damagePowerModifiers.getOrDefault(target, 0);
	}

	void setCanConsumeTurn(boolean canConsumeTurn) {
		this.canConsumeTurn = canConsumeTurn;
	}

	void markPostActionExecuted() {
		postActionExecuted = true;
	}

	int getEffectResultCount() {
		return effectResults.size();
	}

	int getSingleTargetDamageAmount(SkillEffectDefinition damageEffect) {
		Objects.requireNonNull(damageEffect, "Damage effect cannot be null.");

		if (damageEffect.getEffectType() != SkillEffectType.DAMAGE
				|| damageEffect.getTargetType() != SkillTargetType.SINGLE_ENEMY) {

			throw new IllegalArgumentException("Effect must be single-enemy damage.");
		}

		for (SkillEffectResult result : effectResults) {
			if (result.getEffect() == damageEffect) {
				return result.getAmount();
			}
		}

		return 0;
	}

	List<SkillEffectResult> getEffectResultsFrom(int startingIndex) {
		return List.copyOf(effectResults.subList(startingIndex, effectResults.size()));
	}

	SkillExecutionContext getContext() {
		return context;
	}

	public CombatSkillDefinition getSkillDefinition() {
		return skillDefinition;
	}

	public List<SkillEffectResult> getEffectResults() {
		return List.copyOf(effectResults);
	}

	public boolean hasAttemptedEffect() {
		return !effectResults.isEmpty();
	}

	public boolean canConsumeTurn() {
		return canConsumeTurn;
	}

	public boolean hasPostActionExecuted() {
		return postActionExecuted;
	}
}