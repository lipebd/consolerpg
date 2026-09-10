package rpg.battle;

import java.util.Objects;

import rpg.characters.GameCharacter;
import rpg.skills.SkillEffectDefinition;

// Records the result produced by one skill component on one target.
public final class SkillEffectResult {

	private final SkillEffectDefinition effect;
	private final GameCharacter target;
	private final SkillEffectOutcome outcome;
	private final int amount;

	public SkillEffectResult(
			SkillEffectDefinition effect,
			GameCharacter target,
			SkillEffectOutcome outcome,
			int amount
	) {
		this.effect = Objects.requireNonNull(effect, "Skill effect cannot be null.");

		this.target = Objects.requireNonNull(target, "Skill effect target cannot be null.");

		this.outcome = Objects.requireNonNull(outcome, "Skill effect outcome cannot be null.");

		if (amount < 0) {
			throw new IllegalArgumentException("Skill effect result amount cannot be negative.");
		}

		if (outcome != SkillEffectOutcome.APPLIED && amount > 0) {
			throw new IllegalArgumentException("An unapplied skill result cannot have an amount.");
		}

		this.amount = amount;
	}

	public SkillEffectDefinition getEffect() {
		return effect;
	}

	public GameCharacter getTarget() {
		return target;
	}

	public SkillEffectOutcome getOutcome() {
		return outcome;
	}

	public int getAmount() {
		return amount;
	}
}