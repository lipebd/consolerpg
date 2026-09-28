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

import rpg.battle.DamageType;
import rpg.characters.CharacterAttribute;
import rpg.classes.PlayableClassCatalog;
import rpg.effects.SpecialBattleStateType;

// Loads complete playable and enemy skill definitions from their shared data files.
public final class SkillLoader {

	private static final int PLAYABLE_SKILL_FIELD_COUNT = 8;
	private static final int ENEMY_SKILL_FIELD_COUNT = 4;
	private static final int EFFECT_FIELD_COUNT = 14;

	public LoadedCatalogs load(
			Path skillFilePath,
			Path enemySkillFilePath,
			Path effectFilePath,
			PlayableClassCatalog playableClassCatalog
	) throws IOException {
		Objects.requireNonNull(skillFilePath, "Skill file path cannot be null.");
		Objects.requireNonNull(enemySkillFilePath, "Enemy skill file path cannot be null.");
		Objects.requireNonNull(effectFilePath, "Skill-effect file path cannot be null.");
		Objects.requireNonNull(playableClassCatalog, "Playable-class catalog cannot be null.");

		Map<String, SkillHeader> playableHeadersById = loadPlayableSkillHeaders(skillFilePath, playableClassCatalog);

		Map<String, EnemySkillHeader> enemyHeadersById = loadEnemySkillHeaders(enemySkillFilePath);

		validateUniqueSkillIds(playableHeadersById, enemyHeadersById);

		Map<String, List<SkillEffectDefinition>> effectsBySkillId = loadSkillEffects(
				effectFilePath,
				playableHeadersById,
				enemyHeadersById
		);

		SkillCatalog playableSkillCatalog = createPlayableSkillCatalog(playableHeadersById, effectsBySkillId);

		EnemySkillCatalog enemySkillCatalog = createEnemySkillCatalog(enemyHeadersById, effectsBySkillId);

		return new LoadedCatalogs(playableSkillCatalog, enemySkillCatalog);
	}

	private Map<String, SkillHeader> loadPlayableSkillHeaders(Path filePath, PlayableClassCatalog playableClassCatalog)
			throws IOException {
		Map<String, SkillHeader> headersById = new LinkedHashMap<>();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			line = line.trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			SkillHeader header = parsePlayableSkillHeader(line);

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

	private Map<String, EnemySkillHeader> loadEnemySkillHeaders(Path filePath) throws IOException {
		Map<String, EnemySkillHeader> headersById = new LinkedHashMap<>();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			line = line.trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			EnemySkillHeader header = parseEnemySkillHeader(line);

			if (headersById.containsKey(header.id)) {
				throw new IllegalArgumentException("Enemy skill ID is duplicated in enemy skill data: " + header.id);
			}

			headersById.put(header.id, header);
		}

		return headersById;
	}

	private SkillHeader parsePlayableSkillHeader(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != PLAYABLE_SKILL_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid skill field count. Expected " + PLAYABLE_SKILL_FIELD_COUNT + " fields: " + line
			);
		}

		String id = fields[0].trim();
		String classId = fields[1].trim();

		SkillAcquisitionType acquisitionType = parseRequiredEnum(
				fields[2],
				SkillAcquisitionType.class,
				"acquisition type",
				id
		);

		int levelRequirement = parseRequiredInteger(fields[3], "level requirement", id);
		int mpCost = parseRequiredInteger(fields[4], "MP cost", id);
		int spCost = parseRequiredInteger(fields[5], "SP cost", id);
		String name = fields[6].trim();
		String description = fields[7].trim().replace("\\n", System.lineSeparator());

		return new SkillHeader(id, classId, acquisitionType, levelRequirement, mpCost, spCost, name, description);
	}

	private EnemySkillHeader parseEnemySkillHeader(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != ENEMY_SKILL_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid enemy skill field count. Expected " + ENEMY_SKILL_FIELD_COUNT + " fields: " + line
			);
		}

		String id = fields[0].trim();
		int mpCost = parseRequiredInteger(fields[1], "MP cost", id);
		int spCost = parseRequiredInteger(fields[2], "SP cost", id);
		String name = fields[3].trim();

		return new EnemySkillHeader(id, mpCost, spCost, name);
	}

	private void validateUniqueSkillIds(
			Map<String, SkillHeader> playableHeadersById,
			Map<String, EnemySkillHeader> enemyHeadersById
	) {
		for (String enemySkillId : enemyHeadersById.keySet()) {
			if (playableHeadersById.containsKey(enemySkillId)) {
				throw new IllegalArgumentException(
						"Skill ID is duplicated across playable and enemy skill data: " + enemySkillId
				);
			}
		}
	}

	private Map<String, List<SkillEffectDefinition>> loadSkillEffects(
			Path filePath,
			Map<String, SkillHeader> playableHeadersById,
			Map<String, EnemySkillHeader> enemyHeadersById
	) throws IOException {
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

			if (!playableHeadersById.containsKey(skillId) && !enemyHeadersById.containsKey(skillId)) {
				throw new IllegalArgumentException("Skill effect references an unknown skill: " + skillId);
			}

			SkillEffectDefinition effect = parseSkillEffect(fields, skillId);

			effectsBySkillId.computeIfAbsent(skillId, ignored -> new ArrayList<>()).add(effect);
		}

		return effectsBySkillId;
	}

	private SkillCatalog createPlayableSkillCatalog(
			Map<String, SkillHeader> headersById,
			Map<String, List<SkillEffectDefinition>> effectsBySkillId
	) {
		SkillCatalog catalog = new SkillCatalog();

		for (SkillHeader header : headersById.values()) {
			List<SkillEffectDefinition> effects = requireSkillEffects(header.id, effectsBySkillId);

			SkillDefinition skillDefinition = new SkillDefinition(
					header.id,
					header.classId,
					header.acquisitionType,
					header.levelRequirement,
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

	private EnemySkillCatalog createEnemySkillCatalog(
			Map<String, EnemySkillHeader> headersById,
			Map<String, List<SkillEffectDefinition>> effectsBySkillId
	) {
		EnemySkillCatalog catalog = new EnemySkillCatalog();

		for (EnemySkillHeader header : headersById.values()) {
			List<SkillEffectDefinition> effects = requireSkillEffects(header.id, effectsBySkillId);

			EnemySkillDefinition skillDefinition = new EnemySkillDefinition(
					header.id,
					header.mpCost,
					header.spCost,
					header.name,
					effects
			);

			catalog.register(skillDefinition);
		}

		return catalog;
	}

	private List<SkillEffectDefinition>
			requireSkillEffects(String skillId, Map<String, List<SkillEffectDefinition>> effectsBySkillId) {
		List<SkillEffectDefinition> effects = effectsBySkillId.get(skillId);

		if (effects == null || effects.isEmpty()) {
			throw new IllegalArgumentException("Skill does not define any effects: " + skillId);
		}

		return effects;
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

		Integer configuredTriggerChancePercent = parseOptionalInteger(fields[10], "trigger chance percentage", skillId);

		int triggerChancePercent = configuredTriggerChancePercent == null ? 100 : configuredTriggerChancePercent;

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

		String conditionValue = fields[13].trim();

		if (conditionValue.isEmpty()) {
			conditionValue = null;
		}

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
				triggerChancePercent,
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

	public record LoadedCatalogs(SkillCatalog playableSkills, EnemySkillCatalog enemySkills) {
		public LoadedCatalogs {
			Objects.requireNonNull(playableSkills, "Playable skill catalog cannot be null.");
			Objects.requireNonNull(enemySkills, "Enemy skill catalog cannot be null.");
		}
	}

	private static final class SkillHeader {

		private final String id;
		private final String classId;
		private final SkillAcquisitionType acquisitionType;
		private final int levelRequirement;
		private final int mpCost;
		private final int spCost;
		private final String name;
		private final String description;

		private SkillHeader(
				String id,
				String classId,
				SkillAcquisitionType acquisitionType,
				int levelRequirement,
				int mpCost,
				int spCost,
				String name,
				String description
		) {
			this.id = id;
			this.classId = classId;
			this.acquisitionType = acquisitionType;
			this.levelRequirement = levelRequirement;
			this.mpCost = mpCost;
			this.spCost = spCost;
			this.name = name;
			this.description = description;
		}
	}

	private static final class EnemySkillHeader {

		private final String id;
		private final int mpCost;
		private final int spCost;
		private final String name;

		private EnemySkillHeader(String id, int mpCost, int spCost, String name) {
			this.id = id;
			this.mpCost = mpCost;
			this.spCost = spCost;
			this.name = name;
		}
	}
}