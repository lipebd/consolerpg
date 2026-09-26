package rpg.classes.paths;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.WeaponDefinition;

// Loads and validates selectable final class paths.
public final class ClassPathLoader {

    private static final int FIELD_COUNT = 3;

    public ClassPathCatalog load(
            Path filePath,
            PlayableClassCatalog playableClassCatalog,
            EquipmentCatalog equipmentCatalog) {
        Objects.requireNonNull(filePath, "Class path file path cannot be null.");
        Objects.requireNonNull(playableClassCatalog, "Playable class catalog cannot be null.");
        Objects.requireNonNull(equipmentCatalog, "Equipment catalog cannot be null.");

        ClassPathCatalog classPathCatalog = new ClassPathCatalog();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                String normalizedLine = line.trim();

                if (normalizedLine.isEmpty() || normalizedLine.startsWith("#")) {
                    continue;
                }

                try {
                    ClassPathDefinition pathDefinition =
                            parseDefinition(normalizedLine, playableClassCatalog, equipmentCatalog);

                    classPathCatalog.register(pathDefinition);
                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException(
                            "Invalid class path definition in "
                                    + filePath
                                    + " at line "
                                    + lineNumber
                                    + ": "
                                    + normalizedLine
                                    + ". "
                                    + exception.getMessage(),
                            exception);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read class path definitions: " + filePath, exception);
        }

        validateCompleteCoverage(classPathCatalog, playableClassCatalog);

        return classPathCatalog;
    }

    private static ClassPathDefinition parseDefinition(
            String line,
            PlayableClassCatalog playableClassCatalog,
            EquipmentCatalog equipmentCatalog) {
        String[] fields = line.split(";", -1);

        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Class path field count must be exactly "
                            + FIELD_COUNT
                            + " but was "
                            + fields.length
                            + ".");
        }

        ClassPathDefinition pathDefinition =
                new ClassPathDefinition(fields[0], fields[1], fields[2]);

        validateReferences(pathDefinition, playableClassCatalog, equipmentCatalog);

        return pathDefinition;
    }

    private static void validateReferences(
            ClassPathDefinition pathDefinition,
            PlayableClassCatalog playableClassCatalog,
            EquipmentCatalog equipmentCatalog) {
        String classId = pathDefinition.getClassId();

        if (classId.equals(ClassPathRules.BEGINNER_CLASS_ID)) {
            throw new IllegalArgumentException(
                    "Beginner class cannot be registered as a final class path: " + classId);
        }

        PlayableClassDefinition classDefinition = playableClassCatalog.getById(classId);

        EquipmentDefinition weaponDefinition =
                equipmentCatalog.getByItemId(pathDefinition.getWeaponItemId());

        if (!(weaponDefinition instanceof WeaponDefinition)) {
            throw new IllegalArgumentException(
                    "Class path weapon item is not a weapon: " + pathDefinition.getWeaponItemId());
        }

        EquipmentDefinition armorDefinition =
                equipmentCatalog.getByItemId(pathDefinition.getArmorItemId());

        if (!(armorDefinition instanceof ArmorDefinition armor)) {
            throw new IllegalArgumentException(
                    "Class path armor item is not armor: " + pathDefinition.getArmorItemId());
        }

        if (weaponDefinition.getLevelRequirement() > ClassPathRules.CLASS_CHANGE_LEVEL) {
            throw new IllegalArgumentException(
                    "Class path weapon requires a level above the ceremony level: "
                            + pathDefinition.getWeaponItemId());
        }

        if (armor.getLevelRequirement() > ClassPathRules.CLASS_CHANGE_LEVEL) {
            throw new IllegalArgumentException(
                    "Class path armor requires a level above the ceremony level: "
                            + pathDefinition.getArmorItemId());
        }

        if (armor.getArmorWeight() != classDefinition.getArmorWeight()) {
            throw new IllegalArgumentException(
                    "Class path armor weight is incompatible with playable class "
                            + classId
                            + ": "
                            + pathDefinition.getArmorItemId());
        }
    }

    private static void validateCompleteCoverage(
            ClassPathCatalog classPathCatalog, PlayableClassCatalog playableClassCatalog) {
        if (!classPathCatalog.containsClassId(ClassPathRules.FINAL_FLAGELO_CLASS_ID)) {
            throw new IllegalArgumentException(
                    "Required final Flagelo has no class path definition: "
                            + ClassPathRules.FINAL_FLAGELO_CLASS_ID);
        }

        for (PlayableClassDefinition classDefinition : playableClassCatalog.getAll()) {
            if (classDefinition.getId().equals(ClassPathRules.BEGINNER_CLASS_ID)) {
                continue;
            }

            if (!classPathCatalog.containsClassId(classDefinition.getId())) {
                throw new IllegalArgumentException(
                        "Final playable class has no class path definition: "
                                + classDefinition.getId());
            }
        }

        int expectedPathCount = playableClassCatalog.getAll().size() - 1;

        if (classPathCatalog.getAll().size() != expectedPathCount) {
            throw new IllegalArgumentException(
                    "Class path count must match every non-beginner playable class. Expected "
                            + expectedPathCount
                            + " but found "
                            + classPathCatalog.getAll().size()
                            + ".");
        }
    }
}
