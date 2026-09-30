package rpg.items.equipment;

// Defines the available weapon families.
public enum WeaponType {

	DAGGER("Adaga"),
	SCEPTER("Cetro"),
	GAUNTLET("Manopla"),
	LUTE("Alaúde"),
	SCYTHE("Foice"),
	PISTOL("Pistola"),
	RIFLE("Rifle"),
	SWORD("Espada"),
	BOW("Arco"),
	STAFF("Cajado"),
	GRIMOIRE("Grimório");

	private final String displayName;

	WeaponType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}