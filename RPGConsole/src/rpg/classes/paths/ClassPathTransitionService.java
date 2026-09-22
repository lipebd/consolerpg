package rpg.classes.paths;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.flags.CampaignFlags;
import rpg.items.Inventory;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.ItemCategory;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.EquipmentService;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillDefinition;

// Prepares and applies the atomic protagonist class-path transition.
public final class ClassPathTransitionService {

    private final ClassPathCatalog classPathCatalog;
    private final PlayableClassCatalog playableClassCatalog;
    private final SkillCatalog skillCatalog;
    private final ItemCatalog itemCatalog;
    private final EquipmentService equipmentService;
    private final CampaignFlags campaignFlags;

    public ClassPathTransitionService(
            ClassPathCatalog classPathCatalog,
            PlayableClassCatalog playableClassCatalog,
            SkillCatalog skillCatalog,
            ItemCatalog itemCatalog,
            EquipmentService equipmentService,
            CampaignFlags campaignFlags) {
        this.classPathCatalog =
                Objects.requireNonNull(classPathCatalog, "Class path catalog cannot be null.");

        this.playableClassCatalog =
                Objects.requireNonNull(
                        playableClassCatalog, "Playable class catalog cannot be null.");

        this.skillCatalog = Objects.requireNonNull(skillCatalog, "Skill catalog cannot be null.");

        this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");

        this.equipmentService =
                Objects.requireNonNull(equipmentService, "Equipment service cannot be null.");

        this.campaignFlags =
                Objects.requireNonNull(campaignFlags, "Campaign flags cannot be null.");
    }

    public ClassPathTransitionPreparation prepare(Party party, String targetClassId) {
        Objects.requireNonNull(party, "Party cannot be null.");

        ClassPathDefinition pathDefinition = classPathCatalog.getByClassId(targetClassId);

        PlayableCharacter protagonist = party.getLeader();

        List<ClassPathTransitionFailure> eligibilityFailures = validateEligibility(protagonist);

        if (!eligibilityFailures.isEmpty()) {
            return ClassPathTransitionPreparation.blocked(eligibilityFailures);
        }

        PlayableClassDefinition targetClassDefinition =
                playableClassCatalog.getById(pathDefinition.getClassId());

        List<SkillDefinition> targetClassSkillDefinitions =
                skillCatalog.getByClassId(targetClassDefinition.getId());

        List<EquipmentDefinition> returnedEquipmentDefinitions =
                protagonist.getEquipment().getAll();

        Map<Item, Integer> introductoryItems = createIntroductoryItems(pathDefinition);

        Map<Item, Integer> inventoryAdditions =
                createInventoryAdditions(returnedEquipmentDefinitions, introductoryItems);

        List<ClassPathTransitionFailure> capacityFailures =
                validateInventoryCapacity(party.getInventory(), inventoryAdditions);

        if (!capacityFailures.isEmpty()) {
            return ClassPathTransitionPreparation.blocked(capacityFailures);
        }

        ClassPathTransitionPlan plan =
                new ClassPathTransitionPlan(
                        party,
                        protagonist,
                        targetClassDefinition,
                        targetClassSkillDefinitions,
                        returnedEquipmentDefinitions,
                        introductoryItems,
                        inventoryAdditions);

        return ClassPathTransitionPreparation.ready(plan);
    }

	public void validate(ClassPathTransitionPlan plan) {
		requireCurrentPlan(plan);
        }

	public void apply(ClassPathTransitionPlan plan) {
		ClassPathTransitionPlan currentPlan =
				requireCurrentPlan(plan);

        unequipAll(currentPlan);

        addIntroductoryItems(currentPlan);

        currentPlan
                .getProtagonist()
                .changeClass(
                        currentPlan.getTargetClassDefinition(),
                        currentPlan.getTargetClassSkillDefinitions());

		if (!campaignFlags.set(
				ClassPathRules.COMPLETION_FLAG_ID)) {
			throw new IllegalStateException(
					"Class path completion flag changed after validation."
					);
        }
    }

	private ClassPathTransitionPlan requireCurrentPlan(
			ClassPathTransitionPlan plan
			) {
		Objects.requireNonNull(
				plan,
				"Class path transition plan cannot be null.");

		ClassPathTransitionPreparation currentPreparation =
				prepare(
						plan.getParty(),
						plan
						.getTargetClassDefinition()
						.getId());

		if (!currentPreparation.isReady()) {
			throw new IllegalStateException(
					"Class path transition plan is no longer applicable."
					);
		}

		ClassPathTransitionPlan currentPlan =
				currentPreparation.getPlan();

		if (currentPlan.getProtagonist()
				!= plan.getProtagonist()
				|| !currentPlan
				.getReturnedEquipmentDefinitions()
				.equals(
						plan.getReturnedEquipmentDefinitions())
				|| !currentPlan
				.getInventoryAdditions()
				.equals(
						plan.getInventoryAdditions())) {
			throw new IllegalStateException(
					"Class path transition state changed after preparation."
					);
		}

		return currentPlan;
	}

    private List<ClassPathTransitionFailure> validateEligibility(PlayableCharacter protagonist) {
        List<ClassPathTransitionFailure> failures = new ArrayList<>();

        if (campaignFlags.isSet(ClassPathRules.COMPLETION_FLAG_ID)) {
            failures.add(
                    ClassPathTransitionFailure.simple(
                            ClassPathTransitionFailureType.ALREADY_COMPLETED));
        }

        if (!protagonist
                .getPlayableClassDefinition()
                .getId()
                .equals(ClassPathRules.BEGINNER_CLASS_ID)) {
            failures.add(
                    ClassPathTransitionFailure.simple(
                            ClassPathTransitionFailureType.INELIGIBLE_CURRENT_CLASS));
        }

        if (protagonist.getLevel() < ClassPathRules.CLASS_CHANGE_LEVEL) {
            failures.add(
                    ClassPathTransitionFailure.simple(
                            ClassPathTransitionFailureType.LEVEL_TOO_LOW));
        }

        return List.copyOf(failures);
    }

    private Map<Item, Integer> createIntroductoryItems(ClassPathDefinition pathDefinition) {
        Map<Item, Integer> introductoryItems = new LinkedHashMap<>();

        addItemQuantity(
                introductoryItems, itemCatalog.getById(pathDefinition.getWeaponItemId()), 1);

        addItemQuantity(introductoryItems, itemCatalog.getById(pathDefinition.getArmorItemId()), 1);

        return introductoryItems;
    }

    private Map<Item, Integer> createInventoryAdditions(
            List<EquipmentDefinition> returnedEquipmentDefinitions,
            Map<Item, Integer> introductoryItems) {
        Map<Item, Integer> inventoryAdditions = new LinkedHashMap<>();

        for (EquipmentDefinition equipmentDefinition : returnedEquipmentDefinitions) {
            Item returnedItem = itemCatalog.getById(equipmentDefinition.getItemId());

            addItemQuantity(inventoryAdditions, returnedItem, 1);
        }

        for (Map.Entry<Item, Integer> introductoryItem : introductoryItems.entrySet()) {
            addItemQuantity(
                    inventoryAdditions, introductoryItem.getKey(), introductoryItem.getValue());
        }

        return inventoryAdditions;
    }

    private static void addItemQuantity(Map<Item, Integer> quantities, Item item, int quantity) {
        quantities.merge(item, quantity, Math::addExact);
    }

    private static List<ClassPathTransitionFailure> validateInventoryCapacity(
            Inventory inventory, Map<Item, Integer> inventoryAdditions) {
        List<ClassPathTransitionFailure> failures = new ArrayList<>();

        for (Map.Entry<Item, Integer> addition : inventoryAdditions.entrySet()) {
            int remainingCapacity = inventory.getRemainingCapacity(addition.getKey());

            if (addition.getValue() > remainingCapacity) {
                failures.add(
                        ClassPathTransitionFailure.itemCapacityExceeded(
                                addition.getKey().getId(), addition.getValue(), remainingCapacity));
            }
        }

        return List.copyOf(failures);
    }

    private void unequipAll(ClassPathTransitionPlan plan) {
        for (EquipmentDefinition equipmentDefinition : plan.getReturnedEquipmentDefinitions()) {
            ItemCategory equipmentCategory = getEquipmentCategory(equipmentDefinition);

            if (!equipmentService.unequip(
                    plan.getParty(), plan.getProtagonist(), equipmentCategory)) {
                throw new IllegalStateException(
                        "Validated equipment could not be returned to inventory: "
                                + equipmentDefinition.getItemId());
            }
        }
    }

    private void addIntroductoryItems(ClassPathTransitionPlan plan) {
        Inventory inventory = plan.getParty().getInventory();

        for (Map.Entry<Item, Integer> introductoryItem : plan.getIntroductoryItems().entrySet()) {
            if (!inventory.addItem(introductoryItem.getKey(), introductoryItem.getValue())) {
                throw new IllegalStateException(
                        "Validated introductory item could not be added: "
                                + introductoryItem.getKey().getId());
            }
        }
    }

    private static ItemCategory getEquipmentCategory(EquipmentDefinition equipmentDefinition) {
        if (equipmentDefinition instanceof WeaponDefinition) {
            return ItemCategory.WEAPON;
        }

        if (equipmentDefinition instanceof ArmorDefinition) {
            return ItemCategory.ARMOR;
        }

        if (equipmentDefinition instanceof TrinketDefinition) {
            return ItemCategory.TRINKET;
        }

        throw new IllegalArgumentException(
                "Unsupported equipped definition: " + equipmentDefinition.getItemId());
    }
}
