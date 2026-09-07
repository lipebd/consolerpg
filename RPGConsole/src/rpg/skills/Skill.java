package rpg.skills;

import java.util.Objects;

// Stores the common data of a character skill.
public class Skill {

	private final RecoveryType recoveryType;
	private final SkillTargetType recoveryTargetType;
	private final String name;
	private final int requiredLevel;
	private final DamageType damageType;
	private final int baseDamage;
	private final int baseHealing;
	private final int mpCost;
	private final int spCost;
	private final String description;

	public Skill(String name, int requiredLevel, DamageType damageType, int baseDamage, int baseHealing,
			RecoveryType recoveryType, SkillTargetType recoveryTargetType, int mpCost, int spCost, String description) {
		if (baseHealing < 0) {
		    throw new IllegalArgumentException(
		            "Base healing cannot be negative."
		    );
		}

		if (recoveryType == RecoveryType.NONE) {
		    if (
		            baseHealing != 0
		            || recoveryTargetType != SkillTargetType.NONE
		    ) {
		        throw new IllegalArgumentException(
		                "A skill without recovery cannot have healing power or a recovery target."
		        );
		    }
		} else {
		    if (
		            baseHealing <= 0
		            || recoveryTargetType == SkillTargetType.NONE
		    ) {
		        throw new IllegalArgumentException(
		                "A recovery skill must have positive healing and a valid recovery target."
		        );
		    }

		    if (
		            recoveryTargetType == SkillTargetType.SINGLE_ENEMY
		            || recoveryTargetType == SkillTargetType.ALL_ENEMIES
		    ) {
		        throw new IllegalArgumentException(
		                "A recovery effect cannot target enemies."
		        );
		    }
		}
		this.name = name;
		this.requiredLevel = requiredLevel;
		this.damageType = damageType;
		this.baseDamage = baseDamage;
		this.baseHealing = baseHealing;
		this.mpCost = mpCost;
		this.spCost = spCost;
		this.description = description;
		this.recoveryType = Objects.requireNonNull(recoveryType, "Recovery type cannot be null.");
		this.recoveryTargetType = Objects.requireNonNull(recoveryTargetType, "Recovery target type cannot be null.");
	}

	public String getName() {
		return name;
	}

	public int getRequiredLevel() {
		return requiredLevel;
	}

	public DamageType getDamageType() {
		return damageType;
	}

	public int getBaseDamage() {
		return baseDamage;
	}

	public int getBaseHealing() {
		return baseHealing;
	}

	public int getMpCost() {
		return mpCost;
	}

	public int getSpCost() {
		return spCost;
	}

	public String getDescription() {
		return description;
	}
	
	public RecoveryType getRecoveryType() {
	    return recoveryType;
	}

	public SkillTargetType getRecoveryTargetType() {
	    return recoveryTargetType;
	}
}