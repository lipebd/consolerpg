package rpg.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.items.Inventory;

// Stores the playable characters and shared party resources.
public final class Party {

	public static final int MINIMUM_SIZE = 1;
	public static final int MAXIMUM_SIZE = 4;

	private final List<PlayableCharacter> members;
	private final Inventory inventory;
	private int gold;

	public Party(PlayableCharacter initialMember) {
		Objects.requireNonNull(initialMember, "Initial party member cannot be null.");

		this.members = new ArrayList<>();
		this.inventory = new Inventory();
		this.gold = 0;

		members.add(initialMember);
	}

	public boolean addMember(PlayableCharacter newMember) {
		Objects.requireNonNull(newMember, "Party member cannot be null.");

		if (members.size() >= MAXIMUM_SIZE || members.contains(newMember)) {
			return false;
		}

		members.add(newMember);
		return true;
	}

	public boolean removeMember(PlayableCharacter member) {
		if (member == null || members.size() <= MINIMUM_SIZE) {
			return false;
		}

		return members.remove(member);
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

	public boolean hasAvailableSlot() {
		return members.size() < MAXIMUM_SIZE;
	}

	public int size() {
		return members.size();
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void addGold(int amount) {
		if (amount > 0) {
			gold += amount;
		}
	}

	public boolean spendGold(int amount) {
		if (amount <= 0 || amount > gold) {
			return false;
		}

		gold -= amount;
		return true;
	}

	public int getGold() {
		return gold;
	}
	
	public PlayableCharacter getLeader() {
	    return members.get(0);
	}
	
}