package rpg.narrative;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import rpg.enemies.EnemyCatalog;
import rpg.recruitment.QuestRecruitmentCatalog;

// Loads complete linear dialogues from external utterance data.
public final class DialogueLoader {

    private static final int EXPECTED_FIELD_COUNT = 5;

	public DialogueCatalog load(
			Path filePath,
			NpcCatalog npcCatalog,
			EnemyCatalog enemyCatalog,
			QuestRecruitmentCatalog recruitmentCatalog
			) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("Dialogue file path cannot be null.");
        }

        if (npcCatalog == null) {
            throw new IllegalArgumentException("NPC catalog cannot be null.");
        }

        if (enemyCatalog == null) {
            throw new IllegalArgumentException("Enemy catalog cannot be null.");
        }

		if (recruitmentCatalog == null) {
			throw new IllegalArgumentException(
					"Quest recruitment catalog cannot be null."
					);
		}

        Map<String, List<DialogueLineDraft>> draftsByDialogueId = new LinkedHashMap<>();

        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
				DialogueLineDefinition definition =
						parseDefinition(
								line,
								npcCatalog,
								enemyCatalog,
								recruitmentCatalog);

                draftsByDialogueId
                        .computeIfAbsent(definition.getDialogueId(), ignored -> new ArrayList<>())
                        .add(new DialogueLineDraft(definition, index + 1, line));

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(filePath, index + 1, line, exception);
            }
        }

        return buildCatalog(filePath, draftsByDialogueId);
    }

    private DialogueLineDefinition parseDefinition(
			String line,
			NpcCatalog npcCatalog,
			EnemyCatalog enemyCatalog,
			QuestRecruitmentCatalog recruitmentCatalog
			) {
        String[] fields = line.split(";", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected "
                            + EXPECTED_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ".");
        }

        DialogueSpeakerType speakerType = parseSpeakerType(fields[2]);

        String speakerIdValue = fields[3].trim();

        Optional<String> speakerId =
                speakerIdValue.isEmpty() ? Optional.empty() : Optional.of(speakerIdValue);

        DialogueLineDefinition definition =
                new DialogueLineDefinition(
                        fields[0].trim(),
                        parseInteger(fields[1], "dialogue line number"),
                        speakerType,
                        speakerId,
                        fields[4]);

		validateSpeakerReference(
				definition,
				npcCatalog,
				enemyCatalog,
				recruitmentCatalog);

		validateRecruitmentPlaceholders(
		        definition,
		        recruitmentCatalog);

        return definition;
    }

    private void validateSpeakerReference(
			DialogueLineDefinition definition,
			NpcCatalog npcCatalog,
			EnemyCatalog enemyCatalog,
			QuestRecruitmentCatalog recruitmentCatalog) {
        switch (definition.getSpeakerType()) {
            case NPC -> npcCatalog.getById(definition.getSpeakerId().orElseThrow());

            case ENEMY -> enemyCatalog.getById(definition.getSpeakerId().orElseThrow());

		case RECRUIT ->
		recruitmentCatalog.getByCandidateId(definition.getSpeakerId().orElseThrow());

            case LEADER, NARRATOR -> {
                // These speaker types do not reference a catalog entry.
            }
        }
    }

    private void validateRecruitmentPlaceholders(
            DialogueLineDefinition definition,
            QuestRecruitmentCatalog recruitmentCatalog) {
        for (String candidateId :
                NarrativeTextProcessor.getRecruitmentCandidateIds(
                        definition.getText())) {
            recruitmentCatalog.getByCandidateId(candidateId);
        }
    }

    private DialogueCatalog buildCatalog(
            Path filePath, Map<String, List<DialogueLineDraft>> draftsByDialogueId) {
        DialogueCatalog dialogueCatalog = new DialogueCatalog();

        for (Map.Entry<String, List<DialogueLineDraft>> entry : draftsByDialogueId.entrySet()) {
            List<DialogueLineDraft> sortedDrafts = new ArrayList<>(entry.getValue());

            sortedDrafts.sort(Comparator.comparingInt(draft -> draft.definition.getLine()));

            List<DialogueLineDefinition> lineDefinitions = new ArrayList<>();

            for (int index = 0; index < sortedDrafts.size(); index++) {
                DialogueLineDraft draft = sortedDrafts.get(index);

                int expectedLine = index + 1;

                if (draft.definition.getLine() != expectedLine) {
                    throw invalidDefinition(
                            filePath,
                            draft.physicalLine,
                            draft.sourceLine,
                            new IllegalArgumentException(
                                    "Dialogue lines must start at 1 and be consecutive for "
                                            + entry.getKey()
                                            + ". Expected line "
                                            + expectedLine
                                            + " but found "
                                            + draft.definition.getLine()
                                            + "."));
                }

                lineDefinitions.add(draft.definition);
            }

            dialogueCatalog.register(new DialogueDefinition(entry.getKey(), lineDefinitions));
        }

        return dialogueCatalog;
    }

    private DialogueSpeakerType parseSpeakerType(String value) {
        String normalizedValue = value.trim();

        try {
            return DialogueSpeakerType.valueOf(normalizedValue);

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid dialogue speaker type: " + normalizedValue, exception);
        }
    }

    private int parseInteger(String value, String fieldName) {
        String normalizedValue = value.trim();

        try {
            return Integer.parseInt(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + ": " + normalizedValue, exception);
        }
    }

    private IllegalArgumentException invalidDefinition(
            Path filePath, int lineNumber, String line, IllegalArgumentException cause) {
        return new IllegalArgumentException(
                "Invalid dialogue definition in "
                        + filePath
                        + " at line "
                        + lineNumber
                        + ": "
                        + line
                        + ". "
                        + cause.getMessage(),
                cause);
    }

    private static final class DialogueLineDraft {

        private final DialogueLineDefinition definition;
        private final int physicalLine;
        private final String sourceLine;

        private DialogueLineDraft(
                DialogueLineDefinition definition, int physicalLine, String sourceLine) {
            this.definition = definition;
            this.physicalLine = physicalLine;
            this.sourceLine = sourceLine;
        }
    }
}
