package rpg.classes.paths;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.PlayableClassDefinition;
import rpg.items.Item;
import rpg.items.equipment.EquipmentDefinition;
import rpg.skills.SkillDefinition;

// Stores one fully validated protagonist class-path transition plan.
public final class ClassPathTransitionPlan {

    private final Party party;
    private final PlayableCharacter protagonist;
    private final PlayableClassDefinition targetClassDefinition;
    private final List<SkillDefinition> targetClassSkillDefinitions;
    private final List<EquipmentDefinition> returnedEquipmentDefinitions;
    private final Map<Item, Integer> introductoryItems;
    private final Map<Item, Integer> inventoryAdditions;

    ClassPathTransitionPlan(
            Party party,
            PlayableCharacter protagonist,
            PlayableClassDefinition targetClassDefinition,
            List<SkillDefinition> targetClassSkillDefinitions,
            List<EquipmentDefinition> returnedEquipmentDefinitions,
            Map<Item, Integer> introductoryItems,
            Map<Item, Integer> inventoryAdditions) {
        this.party = Objects.requireNonNull(party, "Party cannot be null.");

        this.protagonist = Objects.requireNonNull(protagonist, "Protagonist cannot be null.");

        this.targetClassDefinition =
                Objects.requireNonNull(
                        targetClassDefinition, "Target playable class definition cannot be null.");

        this.targetClassSkillDefinitions =
                List.copyOf(
                        Objects.requireNonNull(
                                targetClassSkillDefinitions,
                                "Target class skill definitions cannot be null."));

        this.returnedEquipmentDefinitions =
                List.copyOf(
                        Objects.requireNonNull(
                                returnedEquipmentDefinitions,
                                "Returned equipment definitions cannot be null."));

        this.introductoryItems =
                new LinkedHashMap<>(
                        Objects.requireNonNull(
                                introductoryItems, "Introductory items cannot be null."));

        this.inventoryAdditions =
                new LinkedHashMap<>(
                        Objects.requireNonNull(
                                inventoryAdditions, "Inventory additions cannot be null."));
    }

    Party getParty() {
        return party;
    }

    PlayableCharacter getProtagonist() {
        return protagonist;
    }

    List<SkillDefinition> getTargetClassSkillDefinitions() {
        return targetClassSkillDefinitions;
    }

    List<EquipmentDefinition> getReturnedEquipmentDefinitions() {
        return returnedEquipmentDefinitions;
    }

    Map<Item, Integer> getIntroductoryItems() {
        return new LinkedHashMap<>(introductoryItems);
    }

    public PlayableClassDefinition getTargetClassDefinition() {
        return targetClassDefinition;
    }

    public Map<Item, Integer> getInventoryAdditions() {
        return new LinkedHashMap<>(inventoryAdditions);
    }
}
