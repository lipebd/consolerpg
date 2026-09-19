package rpg.classes.paths;

// Stores one selectable final class path and its introductory equipment.
public final class ClassPathDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String classId;
    private final String weaponItemId;
    private final String armorItemId;

    public ClassPathDefinition(String classId, String weaponItemId, String armorItemId) {
        this.classId = validateId(classId, "Class path");
        this.weaponItemId = validateId(weaponItemId, "Class path weapon item");
        this.armorItemId = validateId(armorItemId, "Class path armor item");
    }

    private static String validateId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    fieldName
                            + " ID must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    public String getClassId() {
        return classId;
    }

    public String getWeaponItemId() {
        return weaponItemId;
    }

    public String getArmorItemId() {
        return armorItemId;
    }
}