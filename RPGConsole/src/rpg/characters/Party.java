package rpg.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.items.Inventory;
import rpg.battle.rewards.PartyRewardModifierState;

// Stores the playable characters and shared party resources.
public final class Party {

	public static final int MINIMUM_SIZE = 1;
	public static final int MAXIMUM_SIZE = 4;
	public static final int MAXIMUM_GOLD = 999999999;

	private final List<PlayableCharacter> members;
	private final Inventory inventory;
	private final PartyRewardModifierState rewardModifierState;
	private int gold;

	public Party(PlayableCharacter initialMember) {
		Objects.requireNonNull(initialMember, "Initial party member cannot be null.");

		this.members = new ArrayList<>();
		this.inventory = new Inventory();
		this.rewardModifierState = new PartyRewardModifierState();
		this.gold = 0;

		members.add(initialMember);
	}

	public boolean addMember(PlayableCharacter newMember) {
		Objects.requireNonNull(newMember, "Party member cannot be null.");

		boolean nameAlreadyUsed = members.stream()
				.anyMatch(member -> CharacterNameRules.areEquivalent(member.getName(), newMember.getName()));

		if (members.size() >= MAXIMUM_SIZE || members.contains(newMember) || nameAlreadyUsed) {
			return false;
		}

		members.add(newMember);
		return true;
	}

	public List<PlayableCharacter> getMembers() {
		return List.copyOf(members);
	}

	public List<PlayableCharacter> getLivingMembers() {
		return members.stream().filter(PlayableCharacter::isAlive).toList();
	}

	public List<PlayableCharacter> getDefeatedMembers() {
		return members.stream().filter(member -> !member.isAlive()).toList();
	}

	public boolean isDefeated() {
		return members.stream().noneMatch(PlayableCharacter::isAlive);
	}

	public void fullyRestoreMembers() {
		for (PlayableCharacter member : members) {
			if (!member.isAlive()) {
				member.revive(member.getMaxHp());
			}

			member.restoreAllResources();
		}
	}

	public boolean hasAvailableSlot() {
		return members.size() < MAXIMUM_SIZE;
	}

	public int size() {
		return members.size();
	}

	public Inventory getInventory() {
		return inventory;
	}

	public PartyRewardModifierState getRewardModifierState() {
		return rewardModifierState;
	}

	public boolean addGold(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Gold amount cannot be negative.");
		}

		long resultingGold = (long) gold + amount;

		if (resultingGold > MAXIMUM_GOLD) {
			return false;
		}

		gold = (int) resultingGold;

		return true;
	}

	public boolean spendGold(int amount) {
		if (amount <= 0 || amount > gold) {
			return false;
		}

		gold -= amount;
		return true;
	}

	public int getEquipmentGoldModifierPercent() {
		int totalModifierPercent = 0;

		for (PlayableCharacter member : members) {
			totalModifierPercent = Math.addExact(totalModifierPercent, member.getEquipment().getGoldModifierPercent());
		}

		return totalModifierPercent;
	}

	public int getGold() {
		return gold;
	}

	public int getRemainingGoldCapacity() {
		return MAXIMUM_GOLD - gold;
	}

	public PlayableCharacter getLeader() {
		return members.get(0);
	}

}
