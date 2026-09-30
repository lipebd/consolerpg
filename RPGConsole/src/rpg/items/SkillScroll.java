package rpg.items;

import rpg.skills.SkillDefinition;

// Represents an item capable of teaching one scroll-exclusive skill.
public final class SkillScroll extends Item {

	private final SkillScrollDefinition scrollDefinition;
	private final SkillDefinition skillDefinition;

	public SkillScroll(
			ItemDefinition itemDefinition,
			SkillScrollDefinition scrollDefinition,
			SkillDefinition skillDefinition
	) {
		super(itemDefinition);

		if (scrollDefinition == null) {
			throw new IllegalArgumentException("Skill-scroll definition cannot be null.");
		}

		if (skillDefinition == null) {
			throw new IllegalArgumentException("Skill definition cannot be null.");
		}

		if (getCategory() != ItemCategory.SKILL_SCROLL) {
			throw new IllegalArgumentException("Skill scroll must use the SKILL_SCROLL category: " + getId());
		}

		if (!getId().equals(scrollDefinition.getItemId())) {
			throw new IllegalArgumentException("Item and skill-scroll definitions must use the same ID.");
		}

		if (!skillDefinition.getId().equals(scrollDefinition.getSkillId())) {
			throw new IllegalArgumentException("Skill and skill-scroll definitions must use the same skill ID.");
		}

		if (!skillDefinition.getClassId().equals(scrollDefinition.getClassId())) {
			throw new IllegalArgumentException("Skill and skill-scroll definitions must use the same class ID.");
		}

		if (!skillDefinition.isScrollSkill()) {
			throw new IllegalArgumentException(
					"Skill scroll must reference a SCROLL skill: " + skillDefinition.getId()
			);
		}

		this.scrollDefinition = scrollDefinition;
		this.skillDefinition = skillDefinition;
	}

	public SkillDefinition getSkillDefinition() {
		return skillDefinition;
	}

	public String getCompatibleClassId() {
		return scrollDefinition.getClassId();
	}

	public int getLevelRequirement() {
		return skillDefinition.getLevelRequirement();
	}
}
