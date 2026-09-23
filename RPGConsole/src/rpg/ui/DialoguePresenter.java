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

	private static final int DIALOGUE_LINES_PER_PAGE = 2;
	private static final long CHARACTER_DELAY_MILLIS = 25;

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

	private void displayLine(
			RenderedDialogueLine renderedLine,
			String headingContext,
			String headingQualifier
	) {
		List<String> wrappedLines = ConsoleUI.wrapText(renderedLine.text());

		for (int start = 0; start < wrappedLines.size(); start += DIALOGUE_LINES_PER_PAGE) {
			int end = Math.min(start + DIALOGUE_LINES_PER_PAGE, wrappedLines.size());

			displayPage(
					renderedLine.speakerName(),
					wrappedLines.subList(start, end),
					headingContext,
					headingQualifier
			);
		}
	}

	private void
			displayPage(String speakerName, List<String> pageLines, String headingContext, String headingQualifier) {
		ConsoleUI.clearScreen();
		displayPageHeading(speakerName, headingContext, headingQualifier);

		consoleInput.discardPendingInput();

		boolean animationInterrupted = false;

		for (int row = 0; row < DIALOGUE_LINES_PER_PAGE; row++) {
			String line = row < pageLines.size() ? pageLines.get(row) : "";

			if (animateLine(line)) {
				animationInterrupted = true;
				break;
			}
		}

		if (animationInterrupted) {
			// Remove the terminal's echoed Enter and redraw the complete page.
			ConsoleUI.clearScreen();
			displayPageHeading(speakerName, headingContext, headingQualifier);

			for (int row = 0; row < DIALOGUE_LINES_PER_PAGE; row++) {
				System.out.println(row < pageLines.size() ? pageLines.get(row) : "");
			}
		}

		// Narrator text starts higher; this keeps the prompt at the same height.
		if (speakerName == null) {
			System.out.println();
			System.out.println();
		}

		System.out.println();
		System.out.print("[Enter] Continuar");
		System.out.flush();

		consoleInput.discardPendingInput();
		consoleInput.readLine();
	}

	private static void displayPageHeading(String speakerName, String headingContext, String headingQualifier) {
		if (headingQualifier == null) {
			ConsoleUI.displayHeading(headingContext);
		} else {
			ConsoleUI.displayHeading(headingContext, headingQualifier);
		}

		System.out.println();

		if (speakerName != null) {
			System.out.println(speakerName);
			System.out.println();
		}
	}

	private boolean animateLine(String line) {
		for (int offset = 0; offset < line.length();) {
			int codePoint = line.codePointAt(offset);

			System.out.print(new String(Character.toChars(codePoint)));
			System.out.flush();

			if (consoleInput.pollLine(CHARACTER_DELAY_MILLIS).isPresent()) {
				return true;
			}

			offset += Character.charCount(codePoint);
		}

		System.out.println();
		return false;
	}

	private record RenderedDialogueLine(String speakerName, String text) {
		private RenderedDialogueLine {
			if (text == null || text.isBlank()) {
				throw new IllegalArgumentException("Rendered dialogue text cannot be empty.");
			}
		}
	}
}
