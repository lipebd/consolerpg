package rpg.skills;

// Stores the common data of a character skill.
public class Skill {

    private final String name;
    private final int requiredLevel;
    private final DamageType damageType;
    private final int baseDamage;
    private final int baseHealing;
    private final int mpCost;
    private final int spCost;
    private final String description;

    public Skill(
            String name,
            int requiredLevel,
            DamageType damageType,
            int baseDamage,
            int baseHealing,
            int mpCost,
            int spCost,
            String description
    ) {
        this.name = name;
        this.requiredLevel = requiredLevel;
        this.damageType = damageType;
        this.baseDamage = baseDamage;
        this.baseHealing = baseHealing;
        this.mpCost = mpCost;
        this.spCost = spCost;
        this.description = description;
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
}