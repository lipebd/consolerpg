package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.paths.ClassPathAnswerDefinition;
import rpg.classes.paths.ClassPathDefinition;
import rpg.classes.paths.ClassPathQuestionnaireCatalog;
import rpg.classes.paths.ClassPathRecommendationResult;
import rpg.classes.paths.ClassPathRecommendationService;

// Coordinates the transient questionnaire and final class-path choice.
public final class ClassPathChoiceCoordinator {

    private final Party party;
    private final PlayableClassCatalog playableClassCatalog;
    private final ClassPathQuestionnaireCatalog questionnaireCatalog;
    private final ClassPathRecommendationService recommendationService;
    private final ClassPathQuestionnaireMenu questionnaireMenu;
    private final ClassPathTieMenu tieMenu;
    private final ClassPathRecommendationMenu recommendationMenu;
    private final ClassPathSelectionMenu selectionMenu;
    private final ClassPathConfirmationMenu confirmationMenu;

    public ClassPathChoiceCoordinator(
            Party party,
            PlayableClassCatalog playableClassCatalog,
            ClassPathQuestionnaireCatalog questionnaireCatalog,
            ClassPathRecommendationService recommendationService,
            ClassPathQuestionnaireMenu questionnaireMenu,
            ClassPathTieMenu tieMenu,
            ClassPathRecommendationMenu recommendationMenu,
            ClassPathSelectionMenu selectionMenu,
            ClassPathConfirmationMenu confirmationMenu) {
        this.party = Objects.requireNonNull(party, "Party cannot be null.");
        this.playableClassCatalog =
                Objects.requireNonNull(
                        playableClassCatalog, "Playable class catalog cannot be null.");
        this.questionnaireCatalog =
                Objects.requireNonNull(
                        questionnaireCatalog, "Class path questionnaire catalog cannot be null.");
        this.recommendationService =
                Objects.requireNonNull(
                        recommendationService, "Class path recommendation service cannot be null.");
        this.questionnaireMenu =
                Objects.requireNonNull(
                        questionnaireMenu, "Class path questionnaire menu cannot be null.");
        this.tieMenu = Objects.requireNonNull(tieMenu, "Class path tie menu cannot be null.");
        this.recommendationMenu =
                Objects.requireNonNull(
                        recommendationMenu, "Class path recommendation menu cannot be null.");
        this.selectionMenu =
                Objects.requireNonNull(selectionMenu, "Class path selection menu cannot be null.");
        this.confirmationMenu =
                Objects.requireNonNull(
                        confirmationMenu, "Class path confirmation menu cannot be null.");
    }

    public PlayableClassDefinition chooseClassPath() {
        List<ClassPathAnswerDefinition> selectedAnswers =
                questionnaireMenu.open(questionnaireCatalog, party);

        ClassPathRecommendationResult recommendationResult =
                recommendationService.evaluate(selectedAnswers);

        Optional<PlayableClassDefinition> resolvedRecommendation =
                resolveRecommendation(recommendationResult);

        if (resolvedRecommendation.isPresent()
                && recommendationMenu.accept(resolvedRecommendation.get())) {
            return resolvedRecommendation.get();
        }

        return chooseManually();
    }

    private Optional<PlayableClassDefinition> resolveRecommendation(
            ClassPathRecommendationResult recommendationResult) {
        if (recommendationResult.hasSingleRecommendation()) {
            return Optional.of(resolveClass(recommendationResult.getSingleRecommendation()));
        }

        List<PlayableClassDefinition> tiedClasses = new ArrayList<>();

        for (ClassPathDefinition pathDefinition : recommendationResult.getLeadingPaths()) {
            tiedClasses.add(resolveClass(pathDefinition));
        }

        return tieMenu.select(tiedClasses);
    }

    private PlayableClassDefinition chooseManually() {
        while (true) {
            PlayableClassDefinition selectedClass = selectionMenu.select();

            if (confirmationMenu.confirm(selectedClass)) {
                return selectedClass;
            }
        }
    }

    private PlayableClassDefinition resolveClass(ClassPathDefinition pathDefinition) {
        return playableClassCatalog.getById(pathDefinition.getClassId());
    }
}
