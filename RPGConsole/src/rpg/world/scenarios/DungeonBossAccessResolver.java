package rpg.world.scenarios;

import java.util.Optional;

import rpg.characters.Party;
import rpg.flags.CampaignFlags;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestProgress;
import rpg.quests.QuestStepDefinition;
import rpg.requirements.RequirementCatalog;
import rpg.requirements.RequirementEvaluator;
import rpg.requirements.RequirementSetDefinition;

// Resolves dungeon boss availability and contextual gate reactions.
public final class DungeonBossAccessResolver {

	private final DungeonBossGateCatalog gateCatalog;
	private final CombatScenarioDialogueCatalog dialogueCatalog;
	private final DungeonProgress dungeonProgress;
	private final QuestProgress questProgress;
	private final RequirementCatalog requirementCatalog;
	private final RequirementEvaluator requirementEvaluator;
	private final Party party;
	private final CampaignFlags campaignFlags;

	public DungeonBossAccessResolver(
			DungeonBossGateCatalog gateCatalog,
			CombatScenarioDialogueCatalog reactionCatalog,
			DungeonProgress dungeonProgress,
			QuestProgress questProgress,
			RequirementCatalog requirementCatalog,
			RequirementEvaluator requirementEvaluator,
			Party party,
			CampaignFlags campaignFlags
	) {
		this.gateCatalog = requireNonNull(gateCatalog, "Dungeon boss gate catalog");
		this.dialogueCatalog = requireNonNull(reactionCatalog, "Dungeon boss gate reaction catalog");
		this.dungeonProgress = requireNonNull(dungeonProgress, "Dungeon progress");
		this.questProgress = requireNonNull(questProgress, "Quest progress");
		this.requirementCatalog = requireNonNull(requirementCatalog, "Requirement catalog");
		this.requirementEvaluator = requireNonNull(requirementEvaluator, "Requirement evaluator");
		this.party = requireNonNull(party, "Party");
		this.campaignFlags = requireNonNull(campaignFlags, "Campaign flags");
	}

	public DungeonBossAccess resolve(Dungeon dungeon, long elapsedHours) {
		if (dungeon == null) {
			throw new IllegalArgumentException("Dungeon cannot be null.");
		}

		String dungeonId = dungeon.getId();

		if (dungeonProgress.isNarrativeBossDefeated(dungeonId)) {
			return dungeonProgress.isRecurringBossAvailable(dungeonId, elapsedHours) ? DungeonBossAccess.AVAILABLE
					: DungeonBossAccess.UNAVAILABLE;
		}

		Optional<DungeonBossGateDefinition> optionalGate = gateCatalog.findByDungeonId(dungeonId);

		if (optionalGate.isEmpty()) {
			return DungeonBossAccess.AVAILABLE;
		}

		DungeonBossGateDefinition gate = optionalGate.orElseThrow();

		RequirementSetDefinition requirementSet = requirementCatalog.getById(gate.getAvailabilityRequirementSetId());

		return requirementEvaluator.isSatisfied(requirementSet, party, questProgress, campaignFlags)
				? DungeonBossAccess.AVAILABLE
				: DungeonBossAccess.GATED;
	}

	public DungeonBossGateDefinition getGateDefinition(Dungeon dungeon) {
		if (dungeon == null) {
			throw new IllegalArgumentException("Dungeon cannot be null.");
		}

		return gateCatalog.findByDungeonId(dungeon.getId()).orElseThrow(
				() -> new IllegalStateException("Dungeon does not have a registered " + "boss gate: " + dungeon.getId())
		);
	}

	public String resolveBlockedDialogueId(Dungeon dungeon) {
		DungeonBossGateDefinition gate = getGateDefinition(dungeon);

		return resolveConfiguredDialogueId(dungeon, CombatScenarioDialogueDefinition.Trigger.BOSS_GATED)
				.orElse(gate.getDefaultBlockedDialogueId());
	}

	public Optional<String> resolveEncounterDialogueId(Dungeon dungeon, boolean narrativeBoss) {
		return resolveConfiguredDialogueId(
				dungeon,
				narrativeBoss ? CombatScenarioDialogueDefinition.Trigger.BOSS_NARRATIVE
						: CombatScenarioDialogueDefinition.Trigger.BOSS_RECURRING
		);
	}

	private Optional<String>
			resolveConfiguredDialogueId(Dungeon dungeon, CombatScenarioDialogueDefinition.Trigger trigger) {
		String selectedDialogueId = null;

		for (QuestDefinition activeQuest : questProgress.getActiveQuests()) {
			QuestStepDefinition currentStep = questProgress.getCurrentStep(activeQuest.getId()).orElseThrow();

			Optional<CombatScenarioDialogueDefinition> matchingDialogue = dialogueCatalog
					.find(dungeon.getId(), trigger, activeQuest.getId(), currentStep.getStep());

			if (matchingDialogue.isEmpty()) {
				continue;
			}

			if (selectedDialogueId != null) {
				throw new IllegalStateException(
						"Multiple combat scenario dialogues match: " + dungeon.getId() + ", " + trigger
				);
			}

			selectedDialogueId = matchingDialogue.orElseThrow().dialogueId();
		}

		if (selectedDialogueId != null) {
			return Optional.of(selectedDialogueId);
		}

		return dialogueCatalog.findGeneral(dungeon.getId(), trigger).map(CombatScenarioDialogueDefinition::dialogueId);
	}

	private static <T> T requireNonNull(T value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " cannot be null.");
		}

		return value;
	}
}