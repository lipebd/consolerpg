package rpg.items;

import rpg.skills.SkillCatalog;
import rpg.skills.SkillDefinition;

// Creates runtime items from their loaded definitions.
public final class ItemFactory {

	public Item create(
			ItemDefinition itemDefinition,
			RecoveryItemDefinitionCatalog recoveryItemDefinitionCatalog,
			SkillScrollDefinitionCatalog skillScrollDefinitionCatalog,
			SkillCatalog skillCatalog
	) {
		if (itemDefinition == null) {
			throw new IllegalArgumentException("Item definition cannot be null.");
		}

		if (recoveryItemDefinitionCatalog == null) {
			throw new IllegalArgumentException("Recovery item definition catalog cannot be null.");
		}

		if (skillScrollDefinitionCatalog == null) {
			throw new IllegalArgumentException("Skill-scroll definition catalog cannot be null.");
		}

		if (skillCatalog == null) {
			throw new IllegalArgumentException("Skill catalog cannot be null.");
		}

		String itemId = itemDefinition.getId();

		boolean isRecoveryItem = recoveryItemDefinitionCatalog.containsItemId(itemId);
		boolean isSkillScroll = skillScrollDefinitionCatalog.containsItemId(itemId);

		if (isRecoveryItem && isSkillScroll) {
			throw new IllegalArgumentException("Item cannot define multiple specialized item types: " + itemId);
		}

		if (isRecoveryItem) {
			RecoveryItemDefinition recoveryDefinition = recoveryItemDefinitionCatalog.getByItemId(itemId);

			return new RecoveryItem(itemDefinition, recoveryDefinition);
		}

		if (isSkillScroll) {
			SkillScrollDefinition scrollDefinition = skillScrollDefinitionCatalog.getByItemId(itemId);

			SkillDefinition skillDefinition = skillCatalog.getById(scrollDefinition.getSkillId());

			return new SkillScroll(itemDefinition, scrollDefinition, skillDefinition);
		}

		return new BasicItem(itemDefinition);
	}
}