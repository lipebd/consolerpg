package rpg.characters;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassDefinition;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillLearningResult;

// Represents a character controlled by the player.
public class PlayableCharacter extends GameCharacter {

	private static final int BASE_EXPERIENCE_REQUIREMENT = 50;
	private static final int MAXIMUM_LEARNED_SCROLL_SKILLS = 2;

	private final CharacterEquipment equipment;
	private final PlayableClassDefinition classDefinition;
	private final CharacterGrowthCalculator growthCalculator;
	private final List<SkillDefinition> classSkillDefinitions;
	private final Set<String> learnedSkillIds;

	private int experience;
	private int level;

	public PlayableCharacter(
			String name,
			PlayableClassDefinition classDefinition,
			List<SkillDefinition> classSkillDefinitions,
			CharacterGrowthCalculator growthCalculator
	) {
		super(
				CharacterNameRules.requireValid(name),
				Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.").getBaseStats()
		);

		this.classDefinition = classDefinition;

		this.classSkillDefinitions = List
				.copyOf(Objects.requireNonNull(classSkillDefinitions, "Class skill definitions cannot be null."));
		this.learnedSkillIds = new LinkedHashSet<>();
		this.growthCalculator = Objects.requireNonNull(growthCalculator, "Character growth calculator cannot be null.");
		this.equipment = new CharacterEquipment();
		this.level = 1;
		this.experience = 0;
		learnAutomaticallyAvailableSkills();
	}

	public List<SkillDefinition> getUnlockedSkillDefinitions() {
		List<SkillDefinition> learnedSkillDefinitions = new ArrayList<>();

		for (SkillDefinition skillDefinition : classSkillDefinitions) {
			if (!skillDefinition.isScrollSkill() && learnedSkillIds.contains(skillDefinition.getId())) {
				learnedSkillDefinitions.add(skillDefinition);
			}
		}

		for (String skillId : learnedSkillIds) {
			SkillDefinition skillDefinition = getClassSkillDefinition(skillId);

			if (skillDefinition.isScrollSkill()) {
				learnedSkillDefinitions.add(skillDefinition);
			}
		}

		return List.copyOf(learnedSkillDefinitions);
	}

	public SkillLearningResult learnSkill(SkillDefinition skillDefinition) {
		Objects.requireNonNull(skillDefinition, "Skill definition cannot be null.");

		if (!skillDefinition.getClassId().equals(classDefinition.getId())) {
			return SkillLearningResult.INCOMPATIBLE_CLASS;
		}

		SkillDefinition canonicalSkillDefinition = getClassSkillDefinition(skillDefinition.getId());

		if (learnedSkillIds.contains(canonicalSkillDefinition.getId())) {
			return SkillLearningResult.ALREADY_LEARNED;
		}

		if (level < canonicalSkillDefinition.getLevelRequirement()) {
			return SkillLearningResult.LEVEL_TOO_LOW;
		}

		if (canonicalSkillDefinition.isScrollSkill() && getLearnedScrollSkillCount() >= MAXIMUM_LEARNED_SCROLL_SKILLS) {
			return SkillLearningResult.SCROLL_LIMIT_REACHED;
		}

		learnedSkillIds.add(canonicalSkillDefinition.getId());

		return SkillLearningResult.LEARNED;
	}

	public SkillLearningResult
			replaceLearnedScrollSkill(SkillDefinition replacedSkillDefinition, SkillDefinition newSkillDefinition) {
		Objects.requireNonNull(replacedSkillDefinition, "Replaced skill definition cannot be null.");

		Objects.requireNonNull(newSkillDefinition, "New skill definition cannot be null.");

		if (!newSkillDefinition.getClassId().equals(classDefinition.getId())) {
			return SkillLearningResult.INCOMPATIBLE_CLASS;
		}

		SkillDefinition canonicalReplacedSkill = getClassSkillDefinition(replacedSkillDefinition.getId());

		SkillDefinition canonicalNewSkill = getClassSkillDefinition(newSkillDefinition.getId());

		if (!canonicalReplacedSkill.isScrollSkill() || !canonicalNewSkill.isScrollSkill()) {
			throw new IllegalArgumentException("Only scroll skills can participate in scroll-skill replacement.");
		}

		if (!learnedSkillIds.contains(canonicalReplacedSkill.getId())) {
			throw new IllegalArgumentException(
					"Replaced scroll skill is not currently learned: " + canonicalReplacedSkill.getId()
			);
		}

		if (learnedSkillIds.contains(canonicalNewSkill.getId())) {
			return SkillLearningResult.ALREADY_LEARNED;
		}

		if (level < canonicalNewSkill.getLevelRequirement()) {
			return SkillLearningResult.LEVEL_TOO_LOW;
		}

		learnedSkillIds.remove(canonicalReplacedSkill.getId());

		learnedSkillIds.add(canonicalNewSkill.getId());

		return SkillLearningResult.REPLACED;
	}

	public boolean hasLearnedSkill(String skillId) {
		if (skillId == null || skillId.isBlank()) {
			throw new IllegalArgumentException("Skill ID cannot be empty.");
		}

		return learnedSkillIds.contains(skillId);
	}

	public List<SkillDefinition> getLearnedScrollSkillDefinitions() {
		return learnedSkillIds.stream().map(this::getClassSkillDefinition).filter(SkillDefinition::isScrollSkill)
				.toList();
	}

	public int getLearnedScrollSkillCount() {
		return getLearnedScrollSkillDefinitions().size();
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
			learnAutomaticallyAvailableSkills();
		}

		return levelsGained;
	}

	private void learnAutomaticallyAvailableSkills() {
		for (SkillDefinition skillDefinition : classSkillDefinitions) {
			if (skillDefinition.isScrollSkill() || level < skillDefinition.getLevelRequirement()) {
				continue;
			}

			SkillLearningResult result = learnSkill(skillDefinition);

			if (result != SkillLearningResult.LEARNED && result != SkillLearningResult.ALREADY_LEARNED) {
				throw new IllegalStateException(
						"Automatically available skill could not be learned: " + skillDefinition.getId()
				);
			}
		}
	}

	private SkillDefinition getClassSkillDefinition(String skillId) {
		return classSkillDefinitions.stream().filter(skillDefinition -> skillDefinition.getId().equals(skillId))
				.findFirst().orElseThrow(
						() -> new IllegalArgumentException(
								"Skill does not belong to the character's class definitions: " + skillId
						)
				);
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