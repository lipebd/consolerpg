package rpg.characters;

import java.util.ArrayList;
import java.util.List;

import rpg.skills.Skill;

// Represents a character controlled by the player.
public class PlayableCharacter extends GameCharacter {
	
	private static final int BASE_EXPERIENCE_REQUIREMENT = 50;

	private int experience;
	private int level;
	private List<Skill> skills;

	public PlayableCharacter(String name, CharacterStats stats) {
	    super(name, stats);
		
		this.level = 1;
		this.skills = new ArrayList<>();
		this.experience = 0;
	}

	public boolean learnSkill(Skill skill) {
		if (level < skill.getRequiredLevel()) {
			return false;
		}

		for (Skill knownSkill : skills) {
			if (knownSkill.getName().equals(skill.getName())) {
				return false;
			}
		}

		skills.add(skill);
		return true;
	}

	public int getLevel() {
		return level;
	}

	public List<Skill> getSkills() {
		return List.copyOf(skills);
	}
	
	public int usePhysicalSkill(Skill skill, GameCharacter target) {
	    int damage = getAttack()
	            + skill.getBaseDamage()
	            - target.getDefense();

	    if (damage < 1) {
	        damage = 1;
	    }

	    return target.takeDamage(damage);
	}
	
	public int gainExperience(int amount) {
	    if (amount <= 0) {
	        return 0;
	    }

	    experience = experience + amount;

	    int levelsGained = 0;

	    while (experience >= getExperienceRequiredForNextLevel()) {
	        experience =
	                experience - getExperienceRequiredForNextLevel();

	        level++;
	        levelsGained++;
	    }

	    return levelsGained;
	}

	public int getExperienceRequiredForNextLevel() {
	    return BASE_EXPERIENCE_REQUIREMENT * level;
	}

	public int getExperience() {
	    return experience;
	}

}