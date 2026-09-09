package rpg.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.classes.PlayableClassCatalog;
import rpg.effects.SpecialBattleStateType;

// Loads complete skill definitions from the skill and effect files.
public final class SkillLoader {

	private static final int SKILL_FIELD_COUNT = 7;
	private static final int EFFECT_FIELD_COUNT = 14;

	public SkillCatalog load(Path skillFilePath, Path effectFilePath, PlayableClassCatalog playableClassCatalog)
			throws IOException {
		Objects.requireNonNull(skillFilePath, "Skill file path cannot be null.");

		Objects.requireNonNull(effectFilePath, "Skill-effect file path cannot be null.");

		Objects.requireNonNull(playableClassCatalog, "Playable-class catalog cannot be null.");

		Map<String, SkillHeader> headersById = loadSkillHeaders(skillFilePath, playableClassCatalog);

		Map<String, List<SkillEffectDefinition>> effectsBySkillId = loadSkillEffects(effectFilePath, headersById);

		SkillCatalog catalog = new SkillCatalog();

		for (SkillHeader header : headersById.values()) {
			List<SkillEffectDefinition> effects = effectsBySkillId.get(header.id);

			if (effects == null || effects.isEmpty()) {
				throw new IllegalArgumentException("Skill does not define any effects: " + header.id);
			}

			SkillDefinition skillDefinition = new SkillDefinition(
					header.id,
					header.classId,
					header.acquisitionLevel,
					header.mpCost,
					header.spCost,
					header.name,
					header.description,
					effects
			);

			catalog.register(skillDefinition);
		}

		return catalog;
	}

	private Map<String, SkillHeader> loadSkillHeaders(Path filePath, PlayableClassCatalog playableClassCatalog)
			throws IOException {
		Map<String, SkillHeader> headersById = new LinkedHashMap<>();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			line = line.trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			SkillHeader header = parseSkillHeader(line);

			if (headersById.containsKey(header.id)) {
				throw new IllegalArgumentException("Skill ID is duplicated in skills data: " + header.id);
			}

			playableClassCatalog.getById(header.classId);

			headersById.put(header.id, header);
		}

		if (headersById.isEmpty()) {
			throw new IllegalArgumentException("Skill data must contain at least one definition.");
		}

		return headersById;
	}

	private SkillHeader parseSkillHeader(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != SKILL_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid skill field count. Expected " + SKILL_FIELD_COUNT + " fields: " + line
			);
		}

		String id = fields[0].trim();
		String classId = fields[1].trim();

		int acquisitionLevel = parseRequiredInteger(fields[2], "acquisition level", id);

		int mpCost = parseRequiredInteger(fields[3], "MP cost", id);

		int spCost = parseRequiredInteger(fields[4], "SP cost", id);

		String name = fields[5].trim();

		String description = fields[6].trim().replace("\\n", System.lineSeparator());

		return new SkillHeader(id, classId, acquisitionLevel, mpCost, spCost, name, description);
	}

	private Map<String, List<SkillEffectDefinition>>
			loadSkillEffects(Path filePath, Map<String, SkillHeader> headersById) throws IOException {
		Map<String, List<SkillEffectDefinition>> effectsBySkillId = new LinkedHashMap<>();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			line = line.trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			String[] fields = line.split(";", -1);

			if (fields.length != EFFECT_FIELD_COUNT) {
				throw new IllegalArgumentException(
						"Invalid skill-effect field count. Expected " + EFFECT_FIELD_COUNT + " fields: " + line
				);
			}

			String skillId = fields[0].trim();

			if (!headersById.containsKey(skillId)) {
				throw new IllegalArgumentException("Skill effect references an unknown skill: " + skillId);
			}

			SkillEffectDefinition effect = parseSkillEffect(fields, skillId);

			effectsBySkillId.computeIfAbsent(skillId, ignored -> new ArrayList<>()).add(effect);
		}

		return effectsBySkillId;
	}

	private SkillEffectDefinition parseSkillEffect(String[] fields, String skillId) {
		int sequence = parseRequiredInteger(fields[1], "effect sequence", skillId);

		SkillExecutionPhase phase = parseRequiredEnum(fields[2], SkillExecutionPhase.class, "execution phase", skillId);

		SkillEffectType effectType = parseRequiredEnum(fields[3], SkillEffectType.class, "effect type", skillId);

		SkillTargetType targetType = parseRequiredEnum(fields[4], SkillTargetType.class, "target type", skillId);

		DamageType damageType = parseOptionalEnum(fields[5], DamageType.class, "damage type", skillId);

		Integer power = parseOptionalInteger(fields[6], "power", skillId);

		CharacterAttribute attribute = parseOptionalEnum(fields[7], CharacterAttribute.class, "attribute", skillId);

		Integer stages = parseOptionalInteger(fields[8], "stages", skillId);

		Integer duration = parseOptionalInteger(fields[9], "duration", skillId);

		int chancePercent = parseRequiredInteger(fields[10], "chance percentage", skillId);

		SpecialBattleStateType specialStateType = parseOptionalEnum(
				fields[11],
				SpecialBattleStateType.class,
				"special state type",
				skillId
		);

		SkillConditionType conditionType = parseRequiredEnum(
				fields[12],
				SkillConditionType.class,
				"condition type",
				skillId
		);

		Integer conditionValue = parseOptionalInteger(fields[13], "condition value", skillId);

		return new SkillEffectDefinition(
				sequence,
				phase,
				effectType,
				targetType,
				damageType,
				power,
				attribute,
				stages,
				duration,
				chancePercent,
				specialStateType,
				conditionType,
				conditionValue
		);
	}

	private int parseRequiredInteger(String value, String fieldName, String skillId) {
		String trimmedValue = value.trim();

		if (trimmedValue.isEmpty()) {
			throw new IllegalArgumentException("Missing skill " + fieldName + ": " + skillId);
		}

		try {
			return Integer.parseInt(trimmedValue);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid skill " + fieldName + " for " + skillId + ": " + value,
					exception
			);
		}
	}

	private Integer parseOptionalInteger(String value, String fieldName, String skillId) {
		String trimmedValue = value.trim();

		if (trimmedValue.isEmpty()) {
			return null;
		}

		return parseRequiredInteger(trimmedValue, fieldName, skillId);
	}

	private <T extends Enum<T>> T parseRequiredEnum(String value, Class<T> enumType, String fieldName, String skillId) {
		String trimmedValue = value.trim();

		if (trimmedValue.isEmpty()) {
			throw new IllegalArgumentException("Missing skill " + fieldName + ": " + skillId);
		}

		try {
			return Enum.valueOf(enumType, trimmedValue.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid skill " + fieldName + " for " + skillId + ": " + value,
					exception
			);
		}
	}

	private <T extends Enum<T>> T parseOptionalEnum(String value, Class<T> enumType, String fieldName, String skillId) {
		if (value.trim().isEmpty()) {
			return null;
		}

		return parseRequiredEnum(value, enumType, fieldName, skillId);
	}

	private static final class SkillHeader {

		private final String id;
		private final String classId;
		private final int acquisitionLevel;
		private final int mpCost;
		private final int spCost;
		private final String name;
		private final String description;

		private SkillHeader(
				String id,
				String classId,
				int acquisitionLevel,
				int mpCost,
				int spCost,
				String name,
				String description
		) {
			this.id = id;
			this.classId = classId;
			this.acquisitionLevel = acquisitionLevel;
			this.mpCost = mpCost;
			this.spCost = spCost;
			this.name = name;
			this.description = description;
		}
	}
}