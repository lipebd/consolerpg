package rpg.items.equipment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import rpg.battle.DamageType;
import rpg.items.ItemCategory;
import rpg.items.ItemDefinition;
import rpg.items.ItemDefinitionCatalog;

// Loads equipment definitions from the specialized external files.
public final class EquipmentLoader {

	private static final int WEAPON_FIELD_COUNT = 15;
	private static final int ARMOR_FIELD_COUNT = 12;
	private static final int TRINKET_FIELD_COUNT = 14;

	public EquipmentCatalog
			load(Path weaponsPath, Path armorsPath, Path trinketsPath, ItemDefinitionCatalog itemDefinitionCatalog)
					throws IOException {
		Objects.requireNonNull(weaponsPath, "Weapons path cannot be null.");
		Objects.requireNonNull(armorsPath, "Armors path cannot be null.");
		Objects.requireNonNull(trinketsPath, "Trinkets path cannot be null.");
		Objects.requireNonNull(itemDefinitionCatalog, "Item definition catalog cannot be null.");

		EquipmentCatalog equipmentCatalog = new EquipmentCatalog();

		loadFile(weaponsPath, equipmentCatalog, line -> parseWeapon(line, itemDefinitionCatalog));

		loadFile(armorsPath, equipmentCatalog, line -> parseArmor(line, itemDefinitionCatalog));

		loadFile(trinketsPath, equipmentCatalog, line -> parseTrinket(line, itemDefinitionCatalog));

		return equipmentCatalog;
	}

	private void
			loadFile(Path filePath, EquipmentCatalog equipmentCatalog, Function<String, EquipmentDefinition> parser)
					throws IOException {
		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			int lineNumber = index + 1;

			try {
				EquipmentDefinition definition = parser.apply(line);

				equipmentCatalog.register(definition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid equipment definition in " + filePath + " at line " + lineNumber + ": "
								+ exception.getMessage(),
						exception
				);
			}
		}
	}

	private WeaponDefinition parseWeapon(String line, ItemDefinitionCatalog itemDefinitionCatalog) {
		String[] fields = splitFields(line, WEAPON_FIELD_COUNT, "weapon");

		String itemId = fields[0].trim();

		validateItemCategory(itemId, ItemCategory.WEAPON, itemDefinitionCatalog);

		DamageType damageType = parseEnum(fields[1], DamageType.class, "damage type", itemId);

		WeaponType weaponType = parseEnum(fields[2], WeaponType.class, "weapon type", itemId);

		int levelRequirement = parseRequiredInteger(fields[3], "level requirement", itemId);

		String normalAttackName = fields[4].trim();

		int baseWeaponDamage = parseRequiredInteger(fields[5], "base weapon damage", itemId);

		int hpBonus = parseOptionalInteger(fields[6], "HP bonus", itemId);
		int mpBonus = parseOptionalInteger(fields[7], "MP bonus", itemId);
		int spBonus = parseOptionalInteger(fields[8], "SP bonus", itemId);
		int attackBonus = parseOptionalInteger(fields[9], "ATK bonus", itemId);
		int defenseBonus = parseOptionalInteger(fields[10], "DEF bonus", itemId);
		int magicBonus = parseOptionalInteger(fields[11], "MAG bonus", itemId);
		int resistanceBonus = parseOptionalInteger(fields[12], "RES bonus", itemId);
		int speedBonus = parseOptionalInteger(fields[13], "VEL bonus", itemId);
		int luckBonus = parseOptionalInteger(fields[14], "LCK bonus", itemId);

		return new WeaponDefinition(
				itemId,
				damageType,
				weaponType,
				levelRequirement,
				normalAttackName,
				baseWeaponDamage,
				hpBonus,
				mpBonus,
				spBonus,
				attackBonus,
				defenseBonus,
				magicBonus,
				resistanceBonus,
				speedBonus,
				luckBonus
		);
	}

	private ArmorDefinition parseArmor(String line, ItemDefinitionCatalog itemDefinitionCatalog) {
		String[] fields = splitFields(line, ARMOR_FIELD_COUNT, "armor");

		String itemId = fields[0].trim();

		validateItemCategory(itemId, ItemCategory.ARMOR, itemDefinitionCatalog);

		ArmorWeight armorWeight = parseEnum(fields[1], ArmorWeight.class, "armor weight", itemId);

		int levelRequirement = parseRequiredInteger(fields[2], "level requirement", itemId);

		int hpBonus = parseOptionalInteger(fields[3], "HP bonus", itemId);
		int mpBonus = parseOptionalInteger(fields[4], "MP bonus", itemId);
		int spBonus = parseOptionalInteger(fields[5], "SP bonus", itemId);
		int attackBonus = parseOptionalInteger(fields[6], "ATK bonus", itemId);
		int defenseBonus = parseOptionalInteger(fields[7], "DEF bonus", itemId);
		int magicBonus = parseOptionalInteger(fields[8], "MAG bonus", itemId);
		int resistanceBonus = parseOptionalInteger(fields[9], "RES bonus", itemId);
		int speedBonus = parseOptionalInteger(fields[10], "VEL bonus", itemId);
		int luckBonus = parseOptionalInteger(fields[11], "LCK bonus", itemId);

		return new ArmorDefinition(
				itemId,
				armorWeight,
				levelRequirement,
				hpBonus,
				mpBonus,
				spBonus,
				attackBonus,
				defenseBonus,
				magicBonus,
				resistanceBonus,
				speedBonus,
				luckBonus
		);
	}

	private TrinketDefinition parseTrinket(String line, ItemDefinitionCatalog itemDefinitionCatalog) {
		String[] fields = splitFields(line, TRINKET_FIELD_COUNT, "trinket");

		String itemId = fields[0].trim();

		validateItemCategory(itemId, ItemCategory.TRINKET, itemDefinitionCatalog);

		TrinketType trinketType = parseEnum(fields[1], TrinketType.class, "trinket type", itemId);

		int levelRequirement = parseRequiredInteger(fields[2], "level requirement", itemId);

		int hpBonus = parseOptionalInteger(fields[3], "HP bonus", itemId);
		int mpBonus = parseOptionalInteger(fields[4], "MP bonus", itemId);
		int spBonus = parseOptionalInteger(fields[5], "SP bonus", itemId);
		int attackBonus = parseOptionalInteger(fields[6], "ATK bonus", itemId);
		int defenseBonus = parseOptionalInteger(fields[7], "DEF bonus", itemId);
		int magicBonus = parseOptionalInteger(fields[8], "MAG bonus", itemId);
		int resistanceBonus = parseOptionalInteger(fields[9], "RES bonus", itemId);
		int speedBonus = parseOptionalInteger(fields[10], "VEL bonus", itemId);
		int luckBonus = parseOptionalInteger(fields[11], "LCK bonus", itemId);

		int experienceModifierPercent = parseOptionalInteger(fields[12], "EXP modifier", itemId);

		int goldModifierPercent = parseOptionalInteger(fields[13], "GOLD modifier", itemId);

		return new TrinketDefinition(
				itemId,
				trinketType,
				levelRequirement,
				hpBonus,
				mpBonus,
				spBonus,
				attackBonus,
				defenseBonus,
				magicBonus,
				resistanceBonus,
				speedBonus,
				luckBonus,
				experienceModifierPercent,
				goldModifierPercent
		);
	}

	private String[] splitFields(String line, int expectedFieldCount, String equipmentType) {
		String[] fields = line.split(";", -1);

		if (fields.length != expectedFieldCount) {
			throw new IllegalArgumentException(
					"Invalid " + equipmentType + " field count. Expected " + expectedFieldCount + " fields: " + line
			);
		}

		return fields;
	}

	private int parseRequiredInteger(String value, String fieldName, String itemId) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid equipment " + fieldName + " for " + itemId + ": " + value,
					exception
			);
		}
	}

	private int parseOptionalInteger(String value, String fieldName, String itemId) {
		if (value == null || value.isBlank()) {
			return 0;
		}

		return parseRequiredInteger(value, fieldName, itemId);
	}

	private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String fieldName, String itemId) {
		try {
			return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid equipment " + fieldName + " for " + itemId + ": " + value,
					exception
			);
		}
	}

	private void validateItemCategory(
			String itemId,
			ItemCategory expectedCategory,
			ItemDefinitionCatalog itemDefinitionCatalog
	) {
		ItemDefinition itemDefinition = itemDefinitionCatalog.getById(itemId);

		if (itemDefinition.getCategory() != expectedCategory) {
			throw new IllegalArgumentException(
					"Equipment item " + itemId + " must use category " + expectedCategory + ", but uses "
							+ itemDefinition.getCategory() + "."
			);
		}
	}
}