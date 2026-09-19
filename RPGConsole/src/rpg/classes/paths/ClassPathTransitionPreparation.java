package rpg.classes.paths;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Stores either a validated class-path transition plan or expected failures.
public final class ClassPathTransitionPreparation {

    private final Optional<ClassPathTransitionPlan> plan;
    private final List<ClassPathTransitionFailure> failures;

    private ClassPathTransitionPreparation(
            Optional<ClassPathTransitionPlan> plan, List<ClassPathTransitionFailure> failures) {
        if (plan == null) {
            throw new IllegalArgumentException("Class path transition plan cannot be null.");
        }

        this.plan = plan;
        this.failures =
                List.copyOf(
                        Objects.requireNonNull(
                                failures, "Class path transition failures cannot be null."));

        if (plan.isPresent() == !this.failures.isEmpty()) {
            throw new IllegalArgumentException(
                    "Class path transition preparation must contain either "
                            + "one plan or failures.");
        }
    }

    public static ClassPathTransitionPreparation ready(ClassPathTransitionPlan plan) {
        return new ClassPathTransitionPreparation(
                Optional.of(
                        Objects.requireNonNull(plan, "Class path transition plan cannot be null.")),
                List.of());
    }

    public static ClassPathTransitionPreparation blocked(
            List<ClassPathTransitionFailure> failures) {
        if (failures == null || failures.isEmpty()) {
            throw new IllegalArgumentException(
                    "Blocked class path transition must contain failures.");
        }

        return new ClassPathTransitionPreparation(Optional.empty(), failures);
    }

    public boolean isReady() {
        return plan.isPresent();
    }

    public ClassPathTransitionPlan getPlan() {
        return plan.orElseThrow(
                () -> new IllegalStateException("Blocked class path transition has no plan."));
    }

    public List<ClassPathTransitionFailure> getFailures() {
        return failures;
    }
}
