package rpg.characters;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassDefinition;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.SkillDefinition;

// Represents a character controlled by the player.
public class PlayableCharacter extends GameCharacter {

	private static final int BASE_EXPERIENCE_REQUIREMENT = 50;

	private final CharacterEquipment equipment;
	private final PlayableClassDefinition classDefinition;
	private final CharacterGrowthCalculator growthCalculator;
	private final List<SkillDefinition> classSkillDefinitions;

	private int experience;
	private int level;

	public PlayableCharacter(
			String name,
			PlayableClassDefinition classDefinition,
			List<SkillDefinition> classSkillDefinitions,
			CharacterGrowthCalculator growthCalculator
	) {
		super(
				name,
				Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.").getBaseStats()
		);

		this.classDefinition = classDefinition;

		this.classSkillDefinitions = List
				.copyOf(Objects.requireNonNull(classSkillDefinitions, "Class skill definitions cannot be null."));

		this.growthCalculator = Objects.requireNonNull(growthCalculator, "Character growth calculator cannot be null.");
		this.equipment = new CharacterEquipment();
		this.level = 1;
		this.experience = 0;
	}

	public List<SkillDefinition> getUnlockedSkillDefinitions() {
		return classSkillDefinitions.stream().filter(skillDefinition -> level >= skillDefinition.getAcquisitionLevel())
				.toList();
	}

	public int gainExperience(int amount) {
		if (amount <= 0) {
			return 0;
		}

		experience = Math.addExact(experience, amount);

		int levelsGained = 0;

		while (experience >= getExperienceRequiredForNextLevel()) {
			experience -= getExperienceRequiredForNextLevel();

			level = Math.incrementExact(level);
			levelsGained++;
		}

		if (levelsGained > 0) {
			CharacterStats naturalStats = growthCalculator.calculateNaturalStats(classDefinition, level);

			replaceNaturalStats(naturalStats);
		}

		return levelsGained;
	}

	public boolean canEquip(EquipmentDefinition definition) {
		if (definition == null || level < definition.getLevelRequirement()) {
			return false;
		}

		if (definition instanceof ArmorDefinition armorDefinition) {
			return armorDefinition.getArmorWeight() == classDefinition.getArmorWeight();
		}

		return definition instanceof WeaponDefinition || definition instanceof TrinketDefinition;
	}

	public Optional<WeaponDefinition> equipWeapon(WeaponDefinition weaponDefinition) {
		Objects.requireNonNull(weaponDefinition, "Weapon definition cannot be null.");

		validateLevelRequirement(weaponDefinition);

		WeaponDefinition previousWeapon = equipment.equipWeapon(weaponDefinition);

		clampCurrentResourcesToMaximums();

		return Optional.ofNullable(previousWeapon);
	}

	public Optional<ArmorDefinition> equipArmor(ArmorDefinition armorDefinition) {
		Objects.requireNonNull(armorDefinition, "Armor definition cannot be null.");

		validateLevelRequirement(armorDefinition);

		if (armorDefinition.getArmorWeight() != classDefinition.getArmorWeight()) {
			throw new IllegalArgumentException(
					"Armor weight is incompatible with " + getName() + ": " + armorDefinition.getItemId()
			);
		}

		ArmorDefinition previousArmor = equipment.equipArmor(armorDefinition);

		clampCurrentResourcesToMaximums();

		return Optional.ofNullable(previousArmor);
	}

	public Optional<TrinketDefinition> equipTrinket(TrinketDefinition trinketDefinition) {
		Objects.requireNonNull(trinketDefinition, "Trinket definition cannot be null.");

		validateLevelRequirement(trinketDefinition);

		TrinketDefinition previousTrinket = equipment.equipTrinket(trinketDefinition);

		clampCurrentResourcesToMaximums();

		return Optional.ofNullable(previousTrinket);
	}

	public Optional<WeaponDefinition> unequipWeapon() {
		WeaponDefinition previousWeapon = equipment.unequipWeapon();

		clampCurrentResourcesToMaximums();

		return Optional.ofNullable(previousWeapon);
	}

	public Optional<ArmorDefinition> unequipArmor() {
		ArmorDefinition previousArmor = equipment.unequipArmor();

		clampCurrentResourcesToMaximums();

		return Optional.ofNullable(previousArmor);
	}

	public Optional<TrinketDefinition> unequipTrinket() {
		TrinketDefinition previousTrinket = equipment.unequipTrinket();

		clampCurrentResourcesToMaximums();

		return Optional.ofNullable(previousTrinket);
	}

	private void validateLevelRequirement(EquipmentDefinition definition) {
		if (level < definition.getLevelRequirement()) {
			throw new IllegalArgumentException(
					getName() + " must be at least level " + definition.getLevelRequirement() + " to equip "
							+ definition.getItemId() + "."
			);
		}
	}

	@Override
	protected int getStructuralMaxHpBonus() {
		return equipment.getHpBonus();
	}

	@Override
	protected int getStructuralMaxMpBonus() {
		return equipment.getMpBonus();
	}

	@Override
	protected int getStructuralMaxSpBonus() {
		return equipment.getSpBonus();
	}

	@Override
	protected int getStructuralAttributeBonus(CharacterAttribute attribute) {
		return switch (attribute) {
		case ATK -> equipment.getAttackBonus();
		case DEF -> equipment.getDefenseBonus();
		case MAG -> equipment.getMagicBonus();
		case RES -> equipment.getResistanceBonus();
		case VEL -> equipment.getSpeedBonus();
		case LCK -> equipment.getLuckBonus();
		};
	}

	public CharacterEquipment getEquipment() {
		return equipment;
	}

	public int getExperienceRequiredForNextLevel() {
		return Math.multiplyExact(BASE_EXPERIENCE_REQUIREMENT, level);
	}

	public PlayableClassDefinition getPlayableClassDefinition() {
		return classDefinition;
	}

	public int getExperience() {
		return experience;
	}

	public int getLevel() {
		return level;
	}
}