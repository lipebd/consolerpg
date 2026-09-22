package rpg.recruitment;

import java.util.List;
import java.util.Objects;

import rpg.characters.PlayableCharacter;
import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.paths.ClassPathCatalog;
import rpg.classes.paths.ClassPathDefinition;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillDefinition;

// Builds a complete recruited ally without modifying campaign state.
public final class RecruitmentAllyFactory {

	private final ClassPathCatalog classPathCatalog;
	private final PlayableClassCatalog playableClassCatalog;
	private final SkillCatalog skillCatalog;
	private final EquipmentCatalog equipmentCatalog;
	private final CharacterGrowthCalculator growthCalculator;

	public RecruitmentAllyFactory(
			ClassPathCatalog classPathCatalog,
			PlayableClassCatalog playableClassCatalog,
			SkillCatalog skillCatalog,
			EquipmentCatalog equipmentCatalog,
			CharacterGrowthCalculator growthCalculator
			) {
		this.classPathCatalog =
				Objects.requireNonNull(
						classPathCatalog,
						"Class path catalog cannot be null.");

		this.playableClassCatalog =
				Objects.requireNonNull(
						playableClassCatalog,
						"Playable class catalog cannot be null.");

		this.skillCatalog =
				Objects.requireNonNull(
						skillCatalog,
						"Skill catalog cannot be null.");

		this.equipmentCatalog =
				Objects.requireNonNull(
						equipmentCatalog,
						"Equipment catalog cannot be null.");

		this.growthCalculator =
				Objects.requireNonNull(
						growthCalculator,
						"Character growth calculator cannot be null.");
	}

	public PlayableCharacter create(
			RecruitmentCandidateState candidateState,
			String allyClassId,
			int level
			) {
		Objects.requireNonNull(
				candidateState,
				"Recruitment candidate state cannot be null.");

		if (candidateState.isRecruited()) {
			throw new IllegalStateException(
					"Recruitment candidate is already recruited: "
							+ candidateState.getCandidateId()
					);
		}

		PlayableClassDefinition classDefinition =
				playableClassCatalog.getById(allyClassId);

		ClassPathDefinition pathDefinition =
				classPathCatalog.getByClassId(
						classDefinition.getId());

		List<SkillDefinition> classSkills =
				skillCatalog.getByClassId(
						classDefinition.getId());

		WeaponDefinition weaponDefinition =
				equipmentCatalog.getWeaponByItemId(
						pathDefinition.getWeaponItemId());

		ArmorDefinition armorDefinition =
				equipmentCatalog.getArmorByItemId(
						pathDefinition.getArmorItemId());

		PlayableCharacter ally =
				new PlayableCharacter(
						candidateState.getName(),
						classDefinition,
						classSkills,
						growthCalculator,
						level);

		if (!ally.canEquip(weaponDefinition)) {
			throw new IllegalStateException(
					"Ally cannot equip introductory weapon: "
							+ weaponDefinition.getItemId()
					);
		}

		if (!ally.canEquip(armorDefinition)) {
			throw new IllegalStateException(
					"Ally cannot equip introductory armor: "
							+ armorDefinition.getItemId()
					);
		}

		if (ally.equipWeapon(weaponDefinition).isPresent()) {
			throw new IllegalStateException(
					"New ally unexpectedly replaced an equipped weapon."
					);
		}

		if (ally.equipArmor(armorDefinition).isPresent()) {
			throw new IllegalStateException(
					"New ally unexpectedly replaced an equipped armor."
					);
		}

		ally.restoreAllResources();

		return ally;
	}

}
