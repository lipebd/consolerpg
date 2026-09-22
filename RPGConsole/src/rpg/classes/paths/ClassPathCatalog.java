package rpg.classes.paths;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores selectable final class paths in external declaration order.
public final class ClassPathCatalog {

    private final Map<String, ClassPathDefinition> pathsByClassId;

    public ClassPathCatalog() {
        this.pathsByClassId = new LinkedHashMap<>();
    }

    public void register(ClassPathDefinition pathDefinition) {
        Objects.requireNonNull(pathDefinition, "Class path definition cannot be null.");

        String classId = pathDefinition.getClassId();

        if (pathsByClassId.containsKey(classId)) {
            throw new IllegalArgumentException(
                    "Class path is already registered for playable class: " + classId);
        }

        pathsByClassId.put(classId, pathDefinition);
    }

    public ClassPathDefinition getByClassId(String classId) {
        if (classId == null || classId.isBlank()) {
            throw new IllegalArgumentException("Playable class ID cannot be empty.");
        }

        ClassPathDefinition pathDefinition = pathsByClassId.get(classId.trim());

        if (pathDefinition == null) {
            throw new IllegalArgumentException(
                    "Class path not found for playable class: " + classId.trim());
        }

        return pathDefinition;
    }

    public boolean containsClassId(String classId) {
        if (classId == null || classId.isBlank()) {
            throw new IllegalArgumentException("Playable class ID cannot be empty.");
        }

        return pathsByClassId.containsKey(classId.trim());
    }

    public List<ClassPathDefinition> getAll() {
        return List.copyOf(pathsByClassId.values());
    }
}