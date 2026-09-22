package rpg.classes.paths;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores the ordered class paths tied at the highest questionnaire score.
public final class ClassPathRecommendationResult {

    private final List<ClassPathDefinition> leadingPaths;

    public ClassPathRecommendationResult(List<ClassPathDefinition> leadingPaths) {
        this.leadingPaths =
                List.copyOf(
                        Objects.requireNonNull(
                                leadingPaths, "Leading class paths cannot be null."));

        if (this.leadingPaths.isEmpty()) {
            throw new IllegalArgumentException(
                    "Recommendation result must contain at least one class path.");
        }

        Set<String> classIds = new LinkedHashSet<>();

        for (ClassPathDefinition pathDefinition : this.leadingPaths) {
            Objects.requireNonNull(pathDefinition, "Leading class path cannot be null.");

            if (!classIds.add(pathDefinition.getClassId())) {
                throw new IllegalArgumentException(
                        "Recommendation result contains a duplicated class path: "
                                + pathDefinition.getClassId());
            }
        }
    }

    public boolean hasSingleRecommendation() {
        return leadingPaths.size() == 1;
    }

    public ClassPathDefinition getSingleRecommendation() {
        if (!hasSingleRecommendation()) {
            throw new IllegalStateException("Tied recommendation result has no single class path.");
        }

        return leadingPaths.get(0);
    }

    public List<ClassPathDefinition> getLeadingPaths() {
        return leadingPaths;
    }
}
