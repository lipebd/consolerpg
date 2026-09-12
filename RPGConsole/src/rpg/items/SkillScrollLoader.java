package rpg.items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import rpg.classes.PlayableClassCatalog;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillDefinition;

// Loads skill-scroll definitions and validates their external references.
public final class SkillScrollLoader {

	private static final int EXPECTED_FIELD_COUNT = 3;

	public SkillScrollDefinitionCatalog load(
			Path filePath,
			ItemDefinitionCatalog itemDefinitionCatalog,
			SkillCatalog skillCatalog,
			PlayableClassCatalog playableClassCatalog
	) throws IOException {
		Objects.requireNonNull(filePath, "Skill-scroll file path cannot be null.");

		Objects.requireNonNull(itemDefinitionCatalog, "Item-definition catalog cannot be null.");

		Objects.requireNonNull(skillCatalog, "Skill catalog cannot be null.");

		Objects.requireNonNull(playableClassCatalog, "Playable-class catalog cannot be null.");

		SkillScrollDefinitionCatalog catalog = new SkillScrollDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				SkillScrollDefinition definition = parseDefinition(
						line,
						itemDefinitionCatalog,
						skillCatalog,
						playableClassCatalog
				);

				catalog.register(definition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid skill-scroll definition at line " + (index + 1) + ": " + line + ". "
								+ exception.getMessage(),
						exception
				);
			}
		}

		validateCompleteSpecialization(catalog, itemDefinitionCatalog, skillCatalog);

		return catalog;
	}

	private SkillScrollDefinition parseDefinition(
			String line,
			ItemDefinitionCatalog itemDefinitionCatalog,
			SkillCatalog skillCatalog,
			PlayableClassCatalog playableClassCatalog
	) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length + "."
			);
		}

		String itemId = fields[0].trim();
		String skillId = fields[1].trim();
		String classId = fields[2].trim();

		ItemDefinition itemDefinition = itemDefinitionCatalog.getById(itemId);

		if (itemDefinition.getCategory() != ItemCategory.SKILL_SCROLL) {
			throw new IllegalArgumentException("Skill-scroll item must use the SKILL_SCROLL category: " + itemId);
		}

		SkillDefinition skillDefinition = skillCatalog.getById(skillId);

		if (!skillDefinition.isScrollSkill()) {
			throw new IllegalArgumentException("Skill-scroll skill must use the SCROLL acquisition type: " + skillId);
		}

		playableClassCatalog.getById(classId);

		if (!skillDefinition.getClassId().equals(classId)) {
			throw new IllegalArgumentException("Skill-scroll class does not match the linked skill class: " + itemId);
		}

		return new SkillScrollDefinition(itemId, skillId, classId);
	}

	private void validateCompleteSpecialization(
			SkillScrollDefinitionCatalog catalog,
			ItemDefinitionCatalog itemDefinitionCatalog,
			SkillCatalog skillCatalog
	) {
		for (ItemDefinition itemDefinition : itemDefinitionCatalog.getAll()) {
			if (itemDefinition.getCategory() == ItemCategory.SKILL_SCROLL
					&& !catalog.containsItemId(itemDefinition.getId())) {
				throw new IllegalArgumentException(
						"Skill-scroll item does not define specialized data: " + itemDefinition.getId()
				);
			}
		}

		for (SkillDefinition skillDefinition : skillCatalog.getAll()) {
			if (skillDefinition.isScrollSkill() && !catalog.containsSkillId(skillDefinition.getId())) {
				throw new IllegalArgumentException(
						"SCROLL skill does not define a skill-scroll item: " + skillDefinition.getId()
				);
			}
		}
	}
}