package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.characters.Party;
import rpg.enemies.EnemyCatalog;
import rpg.narrative.DialogueCatalog;
import rpg.narrative.DialogueDefinition;
import rpg.narrative.DialogueLineDefinition;
import rpg.narrative.NarrativeTextProcessor;
import rpg.narrative.NpcCatalog;
import rpg.recruitment.RecruitmentCandidateStateCatalog;

// Presents external linear dialogues one utterance at a time.
public final class DialoguePresenter {

	private final RecruitmentCandidateStateCatalog candidateStateCatalog;
	private final ConsoleInput consoleInput;
	private final DialogueCatalog dialogueCatalog;
	private final NpcCatalog npcCatalog;
	private final EnemyCatalog enemyCatalog;

	public DialoguePresenter(
			ConsoleInput consoleInput,
			DialogueCatalog dialogueCatalog,
			NpcCatalog npcCatalog,
			EnemyCatalog enemyCatalog,
			RecruitmentCandidateStateCatalog candidateStateCatalog
	) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.dialogueCatalog = Objects.requireNonNull(dialogueCatalog, "Dialogue catalog cannot be null.");
		this.npcCatalog = Objects.requireNonNull(npcCatalog, "NPC catalog cannot be null.");
		this.enemyCatalog = Objects.requireNonNull(enemyCatalog, "Enemy catalog cannot be null.");
		this.candidateStateCatalog = Objects
				.requireNonNull(candidateStateCatalog, "Recruitment candidate state catalog cannot be null.");
	}

	public void display(String dialogueId, Party party, String heading) {
		displayResolvedDialogue(dialogueId, party, heading, null);
	}

	public void display(String dialogueId, Party party, String context, String qualifier) {
		displayResolvedDialogue(dialogueId, party, context, qualifier);
	}

	private void
			displayResolvedDialogue(String dialogueId, Party party, String headingContext, String headingQualifier) {
		Objects.requireNonNull(party, "Party cannot be null.");

		DialogueDefinition dialogueDefinition = dialogueCatalog.getById(dialogueId);

		List<RenderedDialogueLine> renderedLines = prepareLines(dialogueDefinition, party);

		for (RenderedDialogueLine renderedLine : renderedLines) {
			displayLine(renderedLine, headingContext, headingQualifier);
		}
	}

	private List<RenderedDialogueLine> prepareLines(DialogueDefinition dialogueDefinition, Party party) {
		List<RenderedDialogueLine> renderedLines = new ArrayList<>();

		for (DialogueLineDefinition lineDefinition : dialogueDefinition.getLines()) {
			String speakerName = resolveSpeakerName(lineDefinition, party);

			String recruitResolvedText = NarrativeTextProcessor
					.resolveRecruitNames(lineDefinition.getText(), candidateStateCatalog);

			String text = NarrativeTextProcessor.resolve(recruitResolvedText, party);

			renderedLines.add(new RenderedDialogueLine(speakerName, text));
		}

		return List.copyOf(renderedLines);
	}

	private String resolveSpeakerName(DialogueLineDefinition lineDefinition, Party party) {
		return switch (lineDefinition.getSpeakerType()) {
		case NPC -> npcCatalog.getById(lineDefinition.getSpeakerId().orElseThrow()).getName();

		case ENEMY -> enemyCatalog.getById(lineDefinition.getSpeakerId().orElseThrow()).getName();

		case LEADER -> party.getLeader().getName();

		case NARRATOR -> null;

		case RECRUIT -> candidateStateCatalog.getByCandidateId(lineDefinition.getSpeakerId().orElseThrow()).getName();
		};
	}

	private void displayLine(RenderedDialogueLine renderedLine, String headingContext, String headingQualifier) {
		ConsoleUI.clearScreen();

		if (headingQualifier == null) {
			ConsoleUI.displayHeading(headingContext);
		} else {
			ConsoleUI.displayHeading(headingContext, headingQualifier);
		}

		System.out.println();

		if (renderedLine.speakerName() != null) {
			System.out.println(renderedLine.speakerName());
			System.out.println();
		}

		ConsoleUI.displayWrappedText(renderedLine.text());

		System.out.println();
		System.out.print("[Enter] Continuar");

		consoleInput.readLine();
	}

	private record RenderedDialogueLine(String speakerName, String text) {
		private RenderedDialogueLine {
			if (text == null || text.isBlank()) {
				throw new IllegalArgumentException("Rendered dialogue text cannot be empty.");
			}
		}
	}
}
