package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import rpg.characters.CharacterNameRules;
import rpg.characters.Party;

// Stores the complete party state in a campaign snapshot.
public final class PartySnapshot {

	private final int gold;
	private final List<PlayableCharacterSnapshot> members;
	private final List<InventoryEntrySnapshot> inventoryEntries;
	private final RewardModifierSnapshot experienceRewardModifier;
	private final RewardModifierSnapshot goldRewardModifier;

	public PartySnapshot(
			int gold,
			List<PlayableCharacterSnapshot> members,
			List<InventoryEntrySnapshot> inventoryEntries,
			RewardModifierSnapshot experienceRewardModifier,
			RewardModifierSnapshot goldRewardModifier
	) {
		if (gold < 0 || gold > Party.MAXIMUM_GOLD) {
			throw new IllegalArgumentException("Party GOLD must be between 0 and " + Party.MAXIMUM_GOLD + ".");
		}

		Objects.requireNonNull(members, "Party members cannot be null.");

		if (members.size() < Party.MINIMUM_SIZE || members.size() > Party.MAXIMUM_SIZE) {
			throw new IllegalArgumentException(
					"Party size must be between " + Party.MINIMUM_SIZE + " and " + Party.MAXIMUM_SIZE + "."
			);
		}

		validateMembers(members);

		Objects.requireNonNull(inventoryEntries, "Inventory entries cannot be null.");

		validateInventoryEntries(inventoryEntries);

		this.gold = gold;
		this.members = List.copyOf(members);
		this.inventoryEntries = List.copyOf(inventoryEntries);
		this.experienceRewardModifier = Objects
				.requireNonNull(experienceRewardModifier, "Experience reward modifier cannot be null.");
		this.goldRewardModifier = Objects.requireNonNull(goldRewardModifier, "GOLD reward modifier cannot be null.");
	}

	public int getGold() {
		return gold;
	}

	public List<PlayableCharacterSnapshot> getMembers() {
		return members;
	}

	public List<InventoryEntrySnapshot> getInventoryEntries() {
		return inventoryEntries;
	}

	public RewardModifierSnapshot getExperienceRewardModifier() {
		return experienceRewardModifier;
	}

	public RewardModifierSnapshot getGoldRewardModifier() {
		return goldRewardModifier;
	}

	private void validateMembers(List<PlayableCharacterSnapshot> members) {
		for (int index = 0; index < members.size(); index++) {
			PlayableCharacterSnapshot member = Objects
					.requireNonNull(members.get(index), "Party member cannot be null.");

			if (!CharacterNameRules.isValid(member.getName())) {
				throw new IllegalArgumentException("Invalid character name: " + member.getName());
			}

			for (int comparedIndex = 0; comparedIndex < index; comparedIndex++) {
				String previousName = members.get(comparedIndex).getName();

				if (CharacterNameRules.areEquivalent(previousName, member.getName())) {
					throw new IllegalArgumentException("Character name is duplicated: " + member.getName());
				}
			}
		}
	}

	private void validateInventoryEntries(List<InventoryEntrySnapshot> inventoryEntries) {
		Set<String> uniqueItemIds = new HashSet<>();

		for (InventoryEntrySnapshot inventoryEntry : inventoryEntries) {
			Objects.requireNonNull(inventoryEntry, "Inventory entry cannot be null.");

			if (!uniqueItemIds.add(inventoryEntry.getItemId())) {
				throw new IllegalArgumentException("Inventory item ID is duplicated: " + inventoryEntry.getItemId());
			}
		}
	}
}