package rpg.persistence;

import java.util.Optional;

// Stores the equipped item IDs of one character in a campaign snapshot.
public final class EquipmentSnapshot {

	private final String weaponItemId;
	private final String armorItemId;
	private final String trinketItemId;

	public EquipmentSnapshot(String weaponItemId, String armorItemId, String trinketItemId) {
		validateItemId(weaponItemId, "Weapon");
		validateItemId(armorItemId, "Armor");
		validateItemId(trinketItemId, "Trinket");

		this.weaponItemId = weaponItemId;
		this.armorItemId = armorItemId;
		this.trinketItemId = trinketItemId;
	}

	public Optional<String> getWeaponItemId() {
		return Optional.ofNullable(weaponItemId);
	}

	public Optional<String> getArmorItemId() {
		return Optional.ofNullable(armorItemId);
	}

	public Optional<String> getTrinketItemId() {
		return Optional.ofNullable(trinketItemId);
	}

	private void validateItemId(String itemId, String slotName) {
		if (itemId != null && itemId.isBlank()) {
			throw new IllegalArgumentException(slotName + " item ID cannot be blank.");
		}
	}
}