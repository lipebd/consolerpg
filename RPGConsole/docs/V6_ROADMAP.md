# RPGConsole V6 Roadmap

> **Implementation status:** V5 was released as `v5.0.0`. V6 development is active on `feature/v6-quests`. Parts 1 through 9 are complete. The first five canonical quests and their normal project playthrough are complete in Part 10; documentation, final verification and the portable Windows release remain pending.

> **Scope rule:** The complete V6 release boundary is approved, but each remaining block is reviewed and frozen again when its implementation begins. Roadblocks may reopen a specific decision without expanding unrelated scope.

> **Checklist convention:** Contract bullets describe approved behavior and do not mean that code already exists. Unchecked boxes represent implementation, verification, documentation or release work. A box is checked only after the corresponding behavior has been implemented and approved.

## Release direction

V6 introduces a data-driven quest foundation and uses it to implement the first five main quests. Content authors must be able to add further quests that use the supported objective, requirement, reward, effect, location and dialogue types without changing Java code.

The implementation must continue to favor:

* External data for content definitions.
* Stable canonical IDs and catalog-backed reference resolution.
* Immutable definitions separated from mutable campaign state.
* Strict startup and save-data validation.
* Explicit synchronous integration instead of a global event bus.
* Atomic state-changing operations.
* Derived availability and interface markers instead of duplicated persisted state.
* Small abstractions with concrete current responsibilities.
* Incremental implementation and representative regression testing.
* Eclipse as the preferred interface for editing, compiling and running the project.

## V6 release boundary

V6 includes:

* A reusable linear quest system.
* Permanent flags and reusable requirement sets.
* Narrative city locations and fixed NPC placements.
* External linear dialogues.
* Quest objectives, rewards and permanent effects.
* Quest menus, progress notifications and `[!]` interaction markers.
* Save-format integration for quest, flag and recruitment state.
* The level-7 class ceremony.
* The internal transition from beginner Flagelo to a final class path.
* The first persistent recruitment candidate and first playable ally.
* The first five main quests.
* A non-technical quest implementation guide.

V6 does not include:

* Branching quest-step trees.
* Repeatable, failed, timed or abandoned quests.
* Optional or alternative objectives inside one step.
* A global event bus or scripting engine.
* A generic branching-dialogue engine.
* Manual quest tracking.
* Bestiary implementation.
* The second and third ally quests.
* Dynamic NPC movement.
* Quest checkpoints or saving outside INNs.
* Compatibility or migration for V5 development or release saves.
* Immediate reorganization of `data/` into subdirectories.

Reorganizing external data into subdirectories may be evaluated only after the V6 formats are stable. Any such move must update loader paths, validation messages, packaging and documentation together.

---

## 1. Quest definitions and lifecycle

### Frozen lifecycle contract

* Quests use the runtime statuses `NOT_STARTED`, `ACTIVE` and `COMPLETED`.
* Availability is derived from current requirements and is never stored as a fourth status.
* Quest steps are sequential and linear.
* Narrative choices may change campaign state but never create alternative step trees in V6.
* A quest is offered by its configured starting NPC only while its requirements are satisfied.
* Accepting an offer activates the first step.
* Declining an offer changes nothing and leaves the quest available.
* Multiple quests may be active at the same time.
* Only the current step of an active quest receives progress.
* Events that occurred before activation are not applied retroactively.
* Completed earlier steps are derived from the current step and do not need independent mutable state.
* Future steps remain inactive.
* Completed quests never regress, repeat or become available again.
* V6 quests cannot fail, expire, be abandoned or be restarted.
* Each of the first five main quests concludes through an NPC interaction.

### External definitions

Use `data/quests.txt`:

```text
# id;title;description;startingNpcId;availabilityRequirementSetId
```

Use `data/quest_steps.txt`:

```text
# questId;step;title;description
```

* Quest and step definitions use stable canonical IDs.
* Quest declaration order controls their stable presentation order.
* Every quest stores a player-facing title and description.
* Every quest references one starting NPC.
* A blank availability requirement-set ID means unrestricted availability.
* Minimum-level requirements use `LEADER_LEVEL_AT_LEAST` instead of a dedicated quest column.
* Step numbers start at 1 and are unique and consecutive within each quest.
* The highest configured step is the final step.
* Titles reject placeholders.
* Descriptions support the approved placeholders and the `\n` line-break sequence; every other use of a backslash is invalid.
* Objectives, dialogues, rewards and effects remain in their dedicated files.

### Implementation checklist

* [x] Create immutable quest and step definitions.
* [x] Create a catalog that resolves definitions by canonical ID without duplicating content state.
* [x] Reject duplicated quest IDs and structurally duplicated steps.
* [x] Reject missing, non-positive or non-consecutive step numbers.
* [x] Reject quests without steps.
* [x] Reject malformed data before partially populating the catalogs.
* [x] Keep quest definitions independent from mutable campaign progress.

---

## 2. Quest objectives and core progress

### Objective data

Use `data/quest_objectives.txt`:

```text
# id;questId;step;order;objectiveType;targetId;requiredQuantity;text
```

Frozen rules:

* Objective IDs are stable and are used by save data.
* `order` controls presentation only.
* Objective order starts at 1 and is unique and consecutive within each step.
* Every objective in the current step is mandatory.
* Objectives in the same step may progress in any order.
* Content that requires an order must use separate steps.
* Objective text is externalized in Brazilian Portuguese.
* Java adds progress counters and presentation markers.
* Required quantities are positive.
* Progress is capped at the required quantity and uses overflow-safe arithmetic.
* Duplicate `(objectiveType, targetId)` pairs within the same step are rejected.

The initial objective types are exactly:

* `TALK_TO_NPC`
* `DEFEAT_ENEMY`
* `ANALYZE_ENEMY`
* `WIN_BATTLE`
* `OBTAIN_ITEM`
* `DELIVER_ITEM`
* `REACH_SCENARIO`
* `COMPLETE_DUNGEON`

Quantity and target rules:

* `TALK_TO_NPC`, `ANALYZE_ENEMY`, `REACH_SCENARIO` and `COMPLETE_DUNGEON` require quantity 1.
* `DEFEAT_ENEMY`, `WIN_BATTLE`, `OBTAIN_ITEM` and `DELIVER_ITEM` allow any positive required quantity.
* `WIN_BATTLE` has a blank target and counts one completed battle victory regardless of enemy count.
* `ANALYZE_ENEMY` targets one canonical enemy and completes as soon as Analyze succeeds, regardless of the later battle result.
* Target IDs must resolve through the appropriate NPC, enemy, item, scenario or dungeon catalog.

### Delivery steps

* A delivery step contains exactly one `TALK_TO_NPC` objective identifying the receiver.
* It contains one or more `DELIVER_ITEM` objectives.
* Delivery is an explicit transactional NPC interaction, not an ordinary item-acquisition event.
* Required items are checked before any item is removed.
* Item removal, objective completion and step transition occur atomically.

### Progress integration

* `QuestProgressService` processes gameplay events synchronously and explicitly.
* No global event bus, reflection-based listener system or asynchronous quest processing is introduced.
* A gameplay event contains its type, an optional canonical target ID and a positive quantity.
* Events are emitted only after the corresponding gameplay operation is confirmed.
* Refused, cancelled or rolled-back operations produce no quest progress.
* One event may update multiple active quests.
* Events from one gameplay resolution are ordered and their notifications are grouped.
* An event is not reapplied after it advances a quest to a new step.
* A later event from the same gameplay resolution may affect the newly active step.
* Enemy defeat and battle victory are not emitted after escape, defeat or a refused encounter.
* Item acquisition is non-retroactive and does not regress after the item is consumed or sold.
* Every confirmed narrative- or recurring-boss victory emits dungeon completion so a quest activated after the first narrative victory cannot become permanently blocked.
* Progress results are returned to the interface instead of being printed by the domain service.
* Quest notifications are transient and remain outside the save schema.

The initial synchronous gameplay event types are exactly:

* `NPC_TALKED`
* `ENEMY_DEFEATED`
* `ENEMY_ANALYZED`
* `BATTLE_WON`
* `ITEM_OBTAINED`
* `SCENARIO_REACHED`
* `DUNGEON_COMPLETED`

`DELIVER_ITEM` has no generic gameplay event and remains reserved for the transactional NPC-delivery flow. `NPC_TALKED` does not progress a delivery step before that transaction succeeds.

### Part 2A — Objective-definition checklist

* [x] Create and validate all eight immutable objective-definition types.
* [x] Load objective definitions through the complete quest aggregate.
* [x] Require every quest step to contain at least one objective.
* [x] Validate global objective IDs, step-local order and type-target uniqueness.
* [x] Validate target presence and required quantities by objective type.
* [x] Validate transactional delivery-step composition.
* [x] Keep objectives nested in their owning steps without a separate catalog.

### Part 2B — Mutable-progress checklist

* [x] Create mutable active-objective counters separate from immutable definitions.
* [x] Represent `NOT_STARTED`, `ACTIVE` and `COMPLETED` quest status.
* [x] Require a complete `QuestCatalog` when mutable quest progress is created.
* [x] Enforce activation, capped progress, linear step transition and irreversible completion.
* [x] Present active and completed quests in stable catalog order.

### Part 2C — Core-service checklist

* [x] Implement `QuestProgressService` with explicit input and result objects.
* [x] Prevent one event from progressing both the completed step and the step it just activated.
* [x] Allow later ordered events to affect a newly activated step.
* [x] Return ordered, groupable progress results to the caller.
* [x] Validate complete event batches before mutation and preserve event and catalog order.

---

## 3. Permanent flags and reusable requirements

### Flag definitions

Use `data/flags.txt`:

```text
# id;description
```

Frozen rules:

* Flags represent stable permanent world consequences.
* Every flag starts absent.
* Flags are set-only and are never removed in V6.
* Setting an already-set flag is idempotent.
* Quest lifecycle is not duplicated through flags.

### Requirement definitions

Use `data/requirements.txt`:

```text
# setId;order;requirementType;targetId;requiredValue
```

The initial requirement types are exactly:

* `LEADER_LEVEL_AT_LEAST`
* `ITEM_QUANTITY_AT_LEAST`
* `QUEST_NOT_STARTED`
* `QUEST_ACTIVE`
* `QUEST_COMPLETED`
* `FLAG_SET`
* `FLAG_UNSET`

Frozen rules:

* Every row in one requirement set is combined through logical AND.
* V6 has no OR groups, nested expressions, scripts or references from one set to another.
* `order` starts at 1 and is unique and consecutive inside each set.
* A blank requirement-set reference means unrestricted access.
* Item requirements inspect current ownership without reserving or consuming items.
* Evaluation is pure, uncached and repeated immediately before the protected action.
* Derived requirement results are never saved.
* Unmet quest offers, narrative locations and NPC placements are hidden.
* Physical accesses may later choose `HIDE` or `SHOW_BLOCKED`, with an external player-facing message.
* Quest-availability self-dependencies and dependency cycles are rejected.

### Implementation checklist

### Part 3A — Permanent-flag checklist

* [x] Create immutable flag definitions and a stable ordered catalog.
* [x] Load and validate external flag IDs and descriptions.
* [x] Create mutable set-only campaign flag state.
* [x] Keep repeated flag assignment idempotent.
* [x] Reject unknown flag IDs before mutating campaign state.

### Part 3B — Requirement-definition checklist

* [x] Create immutable requirement and requirement-set definitions.
* [x] Load requirement sets in stable declared order.
* [x] Validate requirement types, values, order and catalog references.
* [x] Reject duplicated and contradictory requirements within one set.
* [x] Reject malformed requirement data before partially populating the catalog.

### Part 3C — Evaluation and quest-availability checklist

* [x] Implement pure uncached requirement evaluation against party, inventory, quests and flags.
* [x] Validate every quest availability requirement-set reference after both catalogs are available.
* [x] Reject quest-availability self-dependencies and dependency cycles.
* [x] Keep requirement evaluation pure and uncached so later consumers reevaluate immediately before protected actions.

---

## 4. Quest rewards and permanent effects

> **Frozen V6 scope:** Material rewards and permanent effects remain external definitions associated with quests. Ordinary quest lifecycle transitions use validated all-or-nothing plans. A confirmed gameplay event is not rolled back when the resulting quest completion is temporarily blocked.

### Material rewards

Use `data/quest_rewards.txt`:

```text
# questId;order;rewardType;rewardId;quantity
```

The initial reward types are exactly:

* `GOLD`
* `PARTY_EXP`
* `ITEM`

Frozen rules:

* GOLD is added to the shared party balance.
* PARTY_EXP is awarded to every party member present when the quest completes.
* Characters recruited later do not receive quest EXP retroactively.
* ITEM accepts any canonical entry supported by the shared inventory.
* Reward quantities are positive and respect the supported GOLD and inventory limits.
* GOLD and PARTY_EXP require a blank `rewardId`.
* ITEM requires a canonical item ID.
* Reward order starts at 1 and is unique and consecutive for each quest.
* Duplicate `(rewardType, rewardId)` entries for one quest are rejected.
* A quest may have no material reward.
* Reward descriptions are derived from actual reward definitions and are not duplicated as free text.
* Quest rewards are all-or-nothing; partial reward granting is not used for quest completion.
* EXP follows the existing character-level and maximum-level rules.
* Reward definitions remain separate from the central quest-definition loader.

### Permanent effects

Use `data/quest_effects.txt`:

```text
# questId;step;trigger;order;effectType;targetId;displayText
```

The initial triggers are exactly:

* `QUEST_START`
* `STEP_COMPLETE`
* `QUEST_COMPLETE`

The initial effect types are exactly:

* `SET_FLAG`
* `UNLOCK_ROUTE`

Frozen rules:

* Quest-level triggers use step 0.
* `STEP_COMPLETE` uses a real positive step number belonging to the quest.
* Effect order starts at 1 and is unique and consecutive per trigger location.
* Duplicate `(effectType, targetId)` entries inside one trigger location are rejected.
* The same target may appear at different trigger locations.
* SET_FLAG requires a canonical permanent-flag ID.
* UNLOCK_ROUTE requires a canonical travel-route ID.
* Setting an existing flag and unlocking an existing route are idempotent.
* `displayText` is optional and describes a visible unlock to the player.
* Visible effect text is returned only when the effect changes campaign state.
* Effect text supports only the literal `\n` line-break escape; other backslash uses are invalid.
* Step effects occur atomically with the step transition.
* The final step's STEP_COMPLETE effects join the same transaction as quest completion.
* Quest-completion effects occur atomically with deliveries, rewards and final status mutation.
* Quest-start effects occur atomically with quest activation.
* Permanent-effect definitions remain separate from the central quest-definition loader.
* `DISCOVER_SCENARIO` is not introduced unless an audit of current world discovery proves that the first five quests need a separate effect.
* Class change and ally recruitment are dedicated typed ceremony behavior, not generic rewards or generic effects.

### Completion transaction

* A confirmed gameplay event updates its objective before a resulting transition is attempted.
* The complete transition or completion plan is validated before its campaign mutations begin.
* Required delivery items, receiving limits, rewards, effects, special ceremony changes and final quest status belong to the same transaction when they resolve together.
* A capacity or state failure leaves deliveries, characters, flags, routes, rewards and quest lifecycle state unchanged from the moment the transition attempt began.
* Objective progress already earned through the confirmed triggering event is preserved.
* A blocked final step remains fully complete while the quest stays ACTIVE.
* No additional pending or ready quest status is introduced.
* The player may correct the blocking state and retry.
* NPC interactions may naturally retry their associated completion.
* The quest interface later provides an explicit `Concluir quest` fallback for a blocked completion outside NPC interaction.
* Opening the quest interface alone never retries or mutates the campaign.
* Items granted by one completed quest may emit later `OBTAIN_ITEM` events for other active quests.
* Reward-caused events are emitted only after the source quest is completed successfully.
* The completed source quest cannot consume its own reward event retroactively.

### Derived reward presentation

Player-facing reward summaries are derived from canonical data, for example:

```text
- 100 GOLD
- 50 EXP para cada integrante
- 2x Merumã
```

Names and quantities come from the loaded reward and item definitions. No independent reward-description field is stored.

### Implementation checklist

* [x] Load and validate reward definitions.
* [x] Load and validate permanent-effect definitions.
* [x] Derive player-facing reward summaries from actual data.
* [x] Build and validate a complete completion plan before mutation.
* [x] Apply ordinary quest rewards and effects atomically.
* [x] Return explicit completion failures without partially changing the campaign.
* [x] Emit reward-caused item acquisition only after successful completion.

---

## 5. Narrative locations, NPCs and dialogue

### Narrative locations

Use `data/narrative_locations.txt`:

```text
# id;cityId;name;description;requirementSetId
```

Frozen rules:

* These locations are city narrative spaces containing NPC interactions.
* INNs, shops and crafters preserve their existing systems and definitions.
* A visible narrative location may contain zero NPCs.
* An empty location displays `Não há ninguém aqui.`
* Location visibility is derived from its requirement set.

### NPC identity and placement

Use `data/npcs.txt`:

```text
# id;name;description;defaultDialogueId
```

Use `data/npc_placements.txt`:

```text
# npcId;narrativeLocationId;requirementSetId
```

Frozen rules:

* NPC identity is independent from city placement because NPCs may also speak in combat scenarios.
* Every ordinary NPC has a description and default dialogue.
* An NPC may be unplaced.
* A placed NPC has at most one fixed narrative location in V6.
* V6 does not support dynamic NPC movement.
* Placement visibility is derived from its requirement set.

### Linear dialogues

Use `data/dialogues.txt`:

```text
# dialogueId;line;speakerType;speakerId;text
```

The initial speaker types are:

* `NPC`
* `ENEMY`
* `LEADER`
* `NARRATOR`
* `RECRUIT`

Speaker rules:

* `NPC` requires a canonical NPC ID.
* `ENEMY` requires a canonical enemy ID.
* `LEADER` and `NARRATOR` require a blank speaker ID.
* `RECRUIT` requires a canonical recruitment-candidate ID and resolves its persisted name.
* A non-hostile character speaking outside a narrative location still uses `NPC`.
* An enemy speaking as an enemy uses `ENEMY`.

Dialogue rules:

* Dialogues are external linear sequences.
* One physical record represents one utterance.
* Line numbers start at 1 and are unique and consecutive inside a dialogue.
* The only supported text sequence is literal `\n`, which creates an intentional line break.
* A backslash not immediately followed by `n` is invalid in external V6 text.
* Semicolons remain reserved field separators.
* Long text may wrap naturally in the console.
* Each utterance advances only after Enter.
* V6 has no generic choices, conditions, branching, timing, color or audio inside dialogue definitions.

Supported placeholders are:

* `{leaderName}`
* `{groupMember2}`
* `{groupMember3}`
* `{groupMember4}`

Placeholder rules:

* They are accepted in quest, step and objective descriptions and dialogue text.
* They are rejected in technical IDs, names and titles.
* A missing referenced party member fails before mutation instead of producing a silent fallback.
* `{groupName}` is reserved conceptually for future support and is rejected in V6.

### Quest dialogue contexts

Use `data/quest_dialogues.txt`:

```text
# questId;step;context;dialogueId
```

The initial contexts are:

* `OFFER`
* `ACCEPT`
* `DECLINE`
* `PENDING`
* `COMPLETE`

Frozen rules:

* Every quest has an offer dialogue.
* Acceptance and decline dialogues are optional.
* A step completed through NPC interaction has a completion dialogue.
* A delivery step may have a pending dialogue for insufficient or missing delivery state.
* Accept and decline choices are generated by the interface and are not a branching-dialogue language.
* An NPC with multiple quest interactions opens a stable submenu containing quest titles and `Conversar`.
* `Conversar` always preserves access to the NPC's default dialogue.

Interaction rules:

* `OFFER`, `ACCEPT` and `DECLINE` use step 0; `PENDING` and `COMPLETE` use a real positive quest step.
* Every quest has exactly one `OFFER`; `ACCEPT` and `DECLINE` remain optional.
* Every step containing `TALK_TO_NPC` has exactly one `COMPLETE`.
* `PENDING` is optional and restricted to steps containing `DELIVER_ITEM`.
* A quest interaction becomes available only after every ordinary non-dialogue objective required before the NPC interaction is complete.
* Delivery interactions remain available with insufficient items so they can present their pending state.
* Applicable quests follow stable quest-catalog order, followed by `Conversar`.
* Quest markers exist only when at least one quest interaction is currently available.
* Required dialogue text and placeholders are resolved before the related campaign mutation.
* Completion is prevalidated before its dialogue; the already-validated transaction is applied only after the dialogue finishes.
* `RECRUIT` remains a recognized speaker type, but its data rows are rejected until recruitment-candidate definitions are introduced and can validate the canonical reference.

### Implementation checklist

* [x] Implement narrative-location, NPC, placement, dialogue and quest-dialogue definitions and loaders.
* [x] Validate every reference, line order, context, speaker and placeholder during startup.
* [x] Decode supported text escapes consistently with existing external text behavior.
* [x] Display empty narrative locations correctly.
* [x] Present fixed NPC placement and visibility through current requirements.
* [x] Implement linear Enter-driven dialogue presentation.
* [x] Implement the stable NPC interaction submenu without a generic dialogue-choice engine.
* [x] Validate every quest starting NPC reference after both catalogs are available.
* [x] Integrate confirmed NPC conversations and transactional item delivery with quest progress.
* [x] Integrate requirement evaluation with quest offers, narrative locations and NPC placements.

---

## 6. Gameplay integration, quest interface and interaction markers

### Quest menu

* The quest menu is available from the stable top-level city and combat-scenario menus.
* It is not exposed from transient or nested flows such as battle, dialogue, shops, crafting, INNs or NPC interaction.
* Opening and navigating the quest menu does not advance time or alter campaign state.
* The first selection separates active and completed quests.
* Quests that have not started are hidden.
* Active and completed quests preserve stable catalog order.
* An active quest displays its resolved description, current step number, title and resolved description, only the current objectives and their counters, and material rewards derived through `QuestRewardSummaryFormatter`.
* A completed quest displays only its resolved description.
* `Concluir missão` is available only when the final step is complete and does not depend on `TALK_TO_NPC`.
* Manual completion retry delegates to the same atomic completion validation used by automatic progression.
* V6 does not allow the player to select a manually tracked quest.

### Progress notifications

* `QuestProgressCoordinator` is the interface boundary shared by confirmed gameplay emitters.
* Partial objective progress, objective completion and step completion remain grouped in event order.
* An objective-completion notification does not repeat the full objective text.
* Simple battle progress is appended inline after the battle result without clearing the screen or requiring another confirmation.
* Quest completion, material rewards, permanent effects and completion failures use a dedicated screen.
* Notifications produced by the same gameplay resolution are grouped.
* Notifications are transient and are never persisted.

### Gameplay event sources

* Battle analysis is stored transiently in first-analysis order and deduplicated by canonical enemy ID.
* Runtime `Enemy` instances retain the canonical ID of their `EnemyDefinition`.
* Battle events are submitted after the battle ends in this order: analyzed enemy IDs, defeated-enemy quantities grouped by ID, one battle victory and items actually collected.
* Escape and defeat preserve confirmed analysis events but do not emit enemy defeat, battle victory or item-drop acquisition.
* Successful shop purchases and crafting emit item acquisition only after their transactions commit.
* Exploration rewards emit only the quantity actually added to inventory.
* Battle drops emit only collected quantities; discarded quantities do not progress quests.
* Quest item rewards already emit acquisition events through the completion transaction.
* Equipment returns, rollbacks, save restoration and transfers of already owned items do not emit acquisition events.
* `SCENARIO_REACHED` is emitted only after `World.changeScenario(...)` confirms the destination transition.
* A confirmed narrative- or recurring-boss victory emits `DUNGEON_COMPLETED` after boss progress and exploration completion are recorded.
* Regular and epic encounters do not emit dungeon completion.

### `[!]` marker

* `[!]` is the only quest-interaction symbol in V6.
* The same symbol represents offer, progress, completion and immediate quest-relevant travel.
* Actionable NPC markers are derived through the visible NPC, narrative location and current-city hierarchy.
* A pending incomplete `REACH_SCENARIO` objective marks only an already unlocked route or currently available city-transport destination leading directly to its target.
* The corresponding parent action is also marked: `Sair da cidade`, `Seguir caminho` or `Viajar`.
* Route and transport menus receive only destination IDs to mark and do not depend on quest-domain objects.
* Markers do not reveal locked, undiscovered, hidden or future destinations.
* Markers do not perform global pathfinding or point through multiple routes.
* A completed objective no longer produces a marker even if its quest remains active because final completion is blocked.
* Marker state is derived from current definitions and campaign state and is never saved.

NPC-driven grouped progress presentation and local actionable-marker derivation were required to validate the complete Part 5 interaction flow and were therefore delivered with that block. Part 6 reuses those components while adding the general quest menu, remaining gameplay event sources and immediate travel-destination propagation.

### Implementation checklist

* [x] Add quest-menu access to every applicable stable menu.
* [x] Implement active and completed quest presentation.
* [x] Display current objective counters and derived reward summaries.
* [x] Support eligible manual completion retry through the atomic quest service.
* [x] Present grouped progress, step-completion and quest-completion results.
* [x] Present simple battle progress inline while preserving dedicated important-result screens.
* [x] Derive actionable NPC markers.
* [x] Propagate markers to visible locations, cities, parent travel actions and immediate destinations.
* [x] Confirm that markers do not expose unmet requirements or inaccessible content.
* [x] Integrate confirmed battle outcomes and Analyze with quest progress.
* [x] Integrate committed item acquisition from shops, crafting, exploration, battle and quest rewards.
* [x] Integrate confirmed scenario arrival.
* [x] Emit dungeon completion after narrative and recurring boss victories.

---

## 7. Level-7 class ceremony

### Beginner and final Flagelo definitions

* Introduce `flagelo_iniciante` as the protagonist's initial internal class.
* Preserve `flagelo` as the final Flagelo path.
* Both definitions display the same player-facing name and icon.
* Both use the same base attributes, growth rules and heavy armor weight category.
* The player experiences this path as remaining a Flagelo, not changing between two visible classes.
* New V6 campaigns begin with `flagelo_iniciante`.
* `flagelo_iniciante` is never displayed as one of the seven final paths.
* There are eight internal class definitions but only seven final selectable paths.
* V5 saves are not converted from `flagelo` to `flagelo_iniciante`.

Natural skill ownership becomes:

```text
investida;flagelo_iniciante;LEVEL;1;...
quebra_armadura;flagelo_iniciante;LEVEL;3;...
golpe_experiente;flagelo;LEVEL;7;...
```

* Reaching level 7 as `flagelo_iniciante` does not grant `golpe_experiente`.
* Choosing the final Flagelo path replaces beginner natural skills with the final class's natural roster.
* Scroll-learned skills are preserved across the protagonist's class transition.
* `STARTER` has no remaining definition or runtime consumer and is removed; natural beginner skills use `LEVEL`.

### Introductory class equipment

Use `data/class_paths.txt`:

```text
# classId;weaponItemId;armorItemId
```

Frozen rules:

* Every non-beginner playable class has exactly one final class-path definition.
* `flagelo_iniciante` cannot be registered as a final path.
* Final Flagelo remains a required selectable path.
* Every introductory weapon and armor must be equippable at level 7.
* Introductory armor must match the final class's armor-weight category.
* The introductory equipment is granted to the shared inventory and is not automatically equipped.
* No introductory trinket is granted.
* The beginner and final Flagelo definitions are required canonical content and are validated before dependent data is loaded.
* They must share player-facing name, symbol, base statistics, attribute specializations and armor weight.

### Questionnaire data

Use `data/class_path_questions.txt`:

```text
# id;order;text
```

Use `data/class_path_answers.txt`:

```text
# questionId;order;text;classId
```

Frozen rules:

* The questionnaire is narrative and does not modify attributes, probability or ally behavior.
* Questions are sequential and cannot be revisited during one attempt.
* Every question has at least two answers.
* Invalid input repeats the current question without replaying earlier questions.
* Every selected answer gives exactly one point to exactly one final class.
* Flagelo is absent from questionnaire answers and can never be recommended.
* Flagelo remains freely selectable as the protagonist's personal rejection of the offered paths.
* The highest score determines the directly recommended path or the set of tied leading paths.
* Ties are never resolved randomly.
* A two-way or three-way tie presents every tied mark in stable class-path order.
* The player may choose one tied mark or reject every tied mark and open free selection.
* Choosing a tied mark produces the same final revelation used by a direct winner.
* The initial canonical questionnaire uses seven questions: four with three answers and three with two answers.
* Each of the six recommendable classes appears once among the binary questions and twice among the ternary questions.
* This distribution gives every recommendable class three scoring opportunities and prevents ties among more than three classes.
* The recommendation does not restrict the final choice.
* Question and answer text supports the `\n` line-break sequence and the approved placeholders.
* Every questionnaire template is resolved before the first question is presented.
* Questionnaire answers, scores, tied results and the confirmed choice remain transient until the complete Quest 5 transaction succeeds.

### Selection and confirmation

* A unique highest score proceeds directly to the final class revelation.
* A tied result first asks the player to choose one of the tied marks or reject them all.
* The resolved recommendation is presented through a one-time timed narrative sequence.
* The decision screen then displays a compact class summary: icon, name, armor weight and attribute tendencies.
* The complete multiline description remains available through an explicit menu option.
* Description access does not replay the timed revelation.
* The player may embrace the resolved recommendation or reject its call.
* Rejecting the recommendation opens unrestricted selection among all seven final paths.
* Rejecting every tied mark also opens unrestricted selection.
* Manual selection uses the complete shared class presentation and permits `Confirmar` or `Escolher novamente`.
* The ceremony cannot be cancelled after reaching the questionnaire.
* Confirming a path freezes the protagonist's choice for the current uninterrupted Quest 5 resolution but does not commit partial campaign state by itself.
* A technical or capacity failure ends the interaction without mutation and allows a later complete retry.

### Protagonist transition

Preserve:

* Name.
* Level.
* Current EXP.
* Learned scroll skills.
* First position and leader identity.

Apply atomically:

* Replace the internal class, including beginner Flagelo becoming final Flagelo.
* Recalculate class-based attributes and natural skills.
* Fully restore HP, MP and SP.
* Return the currently equipped weapon, armor and amulet to the shared inventory.
* Leave all three protagonist equipment slots empty.
* Grant the selected final class's level-7 introductory weapon and armor to the shared inventory.
* Grant no introductory amulet.
* Display guidance telling the player to open the equipment menu.
* Set the permanent ceremony-completion flag.
* Prevent the ceremony from being applied more than once.

The full inventory consequence must be validated before any equipment, class, resource, flag or quest state changes.

The standalone protagonist transition plan and application are implemented and verified. Part 8 composes that validated plan with mandatory ally recruitment so Quest 5 can commit both outcomes through one complete transaction rather than applying the protagonist transition early.

### Implementation checklist

* [x] Add and validate the beginner and final Flagelo definitions and natural skills.
* [x] Audit and remove the unused `STARTER` skill-acquisition type.
* [x] Freeze and load the external schema for level-7 introductory class equipment.
* [x] Define introductory weapon and armor for every final path.
* [x] Load and validate questions, answers and recommendation targets.
* [x] Implement questionnaire scoring and ordered player-controlled tie resolution.
* [x] Reuse complete class presentation across creation, recommendation and free selection.
* [x] Implement compact recommendation presentation with optional description access.
* [x] Build, revalidate and apply the complete standalone protagonist transition atomically.
* [x] Verify capacity failure, equipment return, introductory items, class replacement, natural skills, full restoration and repeated-application prevention.
* [x] Compose the confirmed protagonist transition with mandatory recruitment in Part 8.

---

## 8. Recruitment candidate and first ally

### Persistent candidate identity

* A recruitment candidate is a small mutable campaign record for a known future ally who is not yet in the party.
* Each candidate has a stable canonical ID, one persistent generated name and a recruited state.
* The candidate is created when first introduced to the narrative.
* Before recruitment, the candidate has no battle resources, equipment or party position.
* Candidate identity is reusable for later ally content, but V6 implements only the first recruit.
* The candidate remains marked as recruited after becoming a playable character so it cannot be created or recruited twice.

Use one shared external playable-character name list:

```text
data/character_names.txt
```

Name rules:

* The same list supports every generated playable-character name.
* All four party-member names must be unique.
* Pending candidates also reserve their generated names.
* A candidate name is generated only once and is immediately part of persistent campaign state.
* Names already used by the protagonist, party or another candidate are ineligible.
* If no unique name remains, candidate creation fails before mutation.
* Separate protagonist, ally or class-specific name lists are not introduced.

### Recruitment dispatch data

Use:

```text
data/quest_recruitments.txt
```

Format:

```text
# questId;step;candidateId;allyLevel
```

Rules:

* Each record associates one real quest step with one canonical recruitment candidate and the level at which that candidate becomes playable.
* Quest and step references must exist.
* One quest-and-step combination may appear at most once.
* One candidate ID may appear at most once.
* Ally level must remain inside the supported playable-character level range.
* The catalog accepts at most three records, derived from the maximum party size minus its required leader.
* The file defines recruitment dispatch and playable-entry level; it does not store generated names, recruited state, class results, equipment or battle resources.
* The relationship itself is neutral regarding narrative orchestration. Part 8 consumes the first recruitment through the class-path ceremony, while later narrative recruitments can reuse the same candidate, class-selection, ally-construction and party-insertion infrastructure without repeating that ceremony.
* The file may remain header-only until canonical recruitment quests are authored.

### Off-screen ceremony

* The candidate performs the same narrative rite as the protagonist in parallel.
* The candidate's hunt and questionnaire occur off screen and are not simulated.
* The candidate chooses a final class before approaching the protagonist after the ceremony.
* The candidate's result is independent from the protagonist's questionnaire answers.

### Ally class draw

* The ally class is drawn only after the protagonist has confirmed a final path.
* `flagelo_iniciante` is never eligible.
* Final Flagelo is never eligible for the ally.
* The protagonist's final class is removed from the draw.
* If the protagonist chooses Flagelo, the ally is drawn uniformly from the other six classes.
* If the protagonist chooses another path, the ally is drawn uniformly from the remaining five non-Flagelo classes.
* The result cannot be rerolled, rejected or manually selected.

### Recruitment interaction

* After the protagonist's ceremony, the approved ally reports the class chosen off screen.
* The ally asks narratively to join the investigation into the disease.
* The protagonist always accepts.
* The interface presents one fixed action, `Aceitar sua companhia`, and no false refusal branch.
* Dialogue remains external; the single required action does not create a generic choice language.
* Recruitment occurs in the same uninterrupted resolution, so no separately persisted pending class result is required.

### Initial playable state

The first ally enters with:

* Fixed level 7.
* Current EXP 0.
* Attributes calculated normally for the drawn class at level 7.
* HP, MP and SP fully restored.
* Every natural class skill available through level 7.
* No scroll-learned skills.
* The class's introductory weapon already equipped.
* The class's introductory armor already equipped.
* An empty amulet slot.
* No additional items.
* The candidate's previously generated name.
* The second party position.

The generic recruitment definition stores `allyLevel` externally so later recruitment content may select another supported entry level without changing Java code. The first canonical ally remains fixed at level 7.

The ally's equipped introductory items never pass through the shared inventory.

### Atomic recruitment

* Quest 5 builds one complete final plan after the protagonist confirms a path and before campaign mutation.
* Validate the protagonist's equipment return, inventory capacity, candidate, party capacity, ally class pool, class definitions, introductory equipment, rewards and effects before the ally is accepted.
* The ceremony ending and ally conversation may be presented sequentially, but they belong to one uninterrupted resolution with no save point between them.
* The mandatory acceptance commits the protagonist transition, complete ally creation, second-position insertion, candidate recruitment state, quest advancement, rewards and effects atomically.
* A failure leaves the protagonist, candidate, party, inventory, flags, routes and quest state unchanged and ready for a later complete retry.

### Implementation checklist

* [x] Freeze the exact candidate-definition trigger or data reference after auditing the quest-effect flow.
* [x] Create the minimal persistent recruitment-candidate state.
* [x] Load and validate the shared character-name list.
* [x] Generate a unique candidate name at first narrative introduction.
* [x] Resolve `RECRUIT` dialogue speakers from candidate state.
* [x] Build the eligible ally class pool after protagonist confirmation.
* [x] Construct the complete level-7 ally with equipped introductory gear.
* [x] Present the single mandatory acceptance action.
* [x] Recruit the ally into the second party position atomically.
* [x] Prevent duplicate candidate creation and recruitment.

---

## 9. Campaign persistence

### Save-format boundary

* Increase the authenticated internal save-format version from 2 to 3.
* Keep the encrypted outer-container version unchanged.
* Reject V5 version-2 saves without migration.
* Preserve the existing encrypted, authenticated and atomic save operation.
* Preserve all-or-nothing candidate restoration before activating a loaded campaign.

### Quest progress

For active quests, persist:

* Canonical quest ID.
* Current step number.
* Stable objective IDs belonging to the current step.
* Current counters for those objectives.

For completed quests, persist:

* Canonical quest ID.
* `COMPLETED` status.

Frozen rules:

* A quest absent from saved quest state is `NOT_STARTED`.
* Completed previous steps, future steps and derived availability are not serialized.
* Unknown or duplicated quests and objectives are rejected.
* Saved counters must be non-negative and no greater than their current requirements.
* An active objective must belong to the saved quest's current step.

### Flags and candidates

Persist:

* The canonical IDs of all set permanent flags.
* Every recruitment candidate already introduced to the narrative.
* Each candidate's canonical ID, persistent name and recruited state.

Frozen rules:

* Absent flags remain unset.
* Unknown or duplicated flag IDs are rejected.
* Candidate names remain stable after their first generation.
* All party and pending-candidate names must remain unique.
* Recruited candidate state must be coherent with the restored party.
* Derived requirements and `[!]` markers are recalculated after restoration.

### Transient state

Do not persist:

* Open quest, NPC, dialogue, questionnaire or confirmation menus.
* Queued quest notifications.
* Questionnaire answers or recommendation before ceremony confirmation.
* A partially assembled completion transaction.
* Temporary event batches.
* Active exploration state already excluded by the V5 contract.

### Implementation checklist

* [x] Extend immutable snapshots with quest, flag and recruitment-candidate state.
* [x] Serialize and deserialize all new collections through stable canonical IDs.
* [x] Increase the authenticated internal version to 3.
* [x] Reject internal version 2 without migration.
* [x] Reconstruct and validate complete candidate state before runtime activation.
* [x] Supply restored quest, flag and candidate state to `World`.
* [x] Preserve atomic file replacement and all existing cryptographic behavior.

Part 9 closure verified explicit version-2 rejection and an empty-narrative version-3 save, restart, Continue, resave and second restart. The representative non-empty narrative round trip and corrupt narrative-state cases remain in Part 11 because canonical quest and recruitment content is introduced only in Part 10.

---

## 10. First five main quests

### Content boundary

* V6 covers the first five main quests, in a linear sequence.
* Narrative choices do not create alternative quest branches.
* Quest content uses the supported objectives, requirements, rewards and effects when their semantics match.
* Java behavior added in this part must serve a reusable domain operation, not encode the content of one quest.
* The class-path ceremony and first-ally recruitment belong to Quest 5, `Os Sete Caminhos`.

### Canonical sequence

| Quest | ID | Main progression |
| --- | --- | --- |
| 1. A Ordem dos Alquimistas | `ordem_dos_alquimistas` | Introduces the disease and opens the route from Bree to Verdes Ermos. |
| 2. O Discípulo | `o_discipulo` | Investigates the goblins, opens Gruta Relamparrocha and rescues the disciple. |
| 3. Entre Caminhos | `entre_caminhos` | Crosses Entrelinhas and reaches Entrelugar in search of researchers. |
| 4. Energia Residual | `energia_residual` | Investigates the Minas do Além-Túmulo, obtains an energy sample and leads the party toward Setegalhos. |
| 5. Os Sete Caminhos | `os_sete_caminhos` | Completes the seven branch trials, the class-path ceremony and the first ally's recruitment. |

The future ally has an independent motivation and participates in the story before joining the party. Recruitment occurs only at the conclusion of Quest 5.

### Technical integration completed during Part 10

* Quest effects control access to directed routes and scenario branches; completing a combat scenario does not unlock passages automatically.
* `WIN_BATTLE` accepts either a specific combat-scenario ID or a blank target for victories anywhere.
* `GRANT_ITEM` supports item delivery during progression, and `TRANSPORT_PARTY` supports quest-triggered transport.
* Dungeon-boss gates support reusable access conditions, contextual or general blocked reactions and transport away from a blocked encounter.
* Combat-scenario dialogue associations support blocked-boss reactions and dialogue before narrative or recurring boss encounters.
* NPC placement requirements support story-dependent appearances using separate NPC IDs for the same portrayed character.
* Recruit dialogue text may use `{recruitName:candidateId}` to show the candidate's persistent generated name.
* Loading a saved campaign accommodates newly added shops and compatible changes to existing stock definitions.

### Remaining closure checks

* [x] Implement and play through Quests 1–4.
* [x] Play through Quest 5 from its offer through class selection and recruitment.
* [x] Confirm passage and branch progression across all five quests in one new campaign.
* [x] Complete the documentation and content-author guide in Part 10.

## 11. Verification

> **Verification convention:** Test distinct behavior, relevant boundaries and state-corruption risks. Use one representative content entry when multiple entries travel through the same loader, catalog or runtime branch. Do not multiply tests merely because five quests or several classes use the same implementation.

### Definition and loader validation

* [x] Test representative malformed, duplicated and unknown quest references.
* [x] Test objective type, target, quantity and order validation.
* [x] Test requirement-set validation, including one self-dependency or cycle.
* [x] Test dialogue order, speaker, escape and placeholder validation.
* [x] Test reward and effect order, reference and duplication validation.
* [x] Confirm that failed loading does not partially populate catalogs.

### Runtime progression

* [x] Test offer decline and later acceptance without unintended mutation.
* [x] Test that only the current step progresses.
* [x] Test that one event does not spill into the step it activates.
* [x] Test that a later ordered event can progress the newly activated step.
* [x] Test representative behavior for all eight objective types.
* [x] Test battle victory, enemy defeat, analyze, escape, defeat and refused encounter distinctions without redundant permutations.
* [x] Test non-retroactive item acquisition and monotonic progress after item use or sale.
* [x] Test transactional multi-item delivery success and failure.
* [x] Test narrative dungeon completion separately from recurring-boss victory.

### Transactions and interface

* [x] Test successful quest completion with rewards and permanent effects.
* [x] Test capacity failure preserving delivery items, rewards, flags, routes and quest state.
* [x] Test active and completed quest-menu presentation.
* [x] Test grouped progress and completion notifications.
* [x] Test actionable `[!]` propagation from NPC to location, city and immediate travel presentation.
* [x] Confirm that unmet and hidden content does not leak through markers.

### Ceremony and recruitment

* [x] Test questionnaire recommendation with one clear winner and one tie.
* [x] Test free selection independent from the recommendation.
* [x] Test the beginner-to-final Flagelo transition.
* [x] Test one different final-class transition as the representative shared path.
* [x] Test equipment return and inventory-capacity failure atomically.
* [x] Test that the ally draw excludes Flagelo and the protagonist's class.
* [x] Test the five-class and six-class ally pools at their distinct boundaries.
* [x] Test unique candidate naming and exhausted-name failure.
* [x] Test the recruited ally's fixed level, EXP, skills, resources, equipment and party position.
* [x] Test that failed recruitment leaves candidate, party and quest state unchanged.
* [x] Test that ceremony and recruitment cannot be applied twice.

### Save and regressions

* [x] Test one version-3 round trip containing active quest counters, a completed quest, flags, a pending candidate and a recruited ally.
* [x] Test representative invalid quest, objective, flag and candidate save state.
* [x] Confirm that internal version 2 is rejected without migration.
* [x] Confirm that failed restoration does not partially replace the active campaign.
* [x] Run focused regressions for battle, Analyze, inventory, rewards, routes, dungeons, shops, crafting, INN saving and campaign loading.
* [x] Verify the complete five-quest flow in the portable Windows build.

---

## 12. Documentation and release closure

### Quest implementation guide

Create `docs/QUEST_IMPLEMENTATION_GUIDE.md` in non-technical language for content authors. It must explain:

* Every quest-related external file and column.
* How to create a quest and its sequential steps.
* How to choose and configure objectives.
* How to write requirements, rewards and effects.
* How to create narrative locations, NPCs, placements and dialogues.
* The supported `\n` line-break sequence and placeholders.
* How to connect offers, pending interactions and completions.
* Common validation errors and how to correct them.
* A complete content-author checklist.
* How to test a new quest without modifying Java code.

General documentation is maintained incrementally when each coherent implementation block closes. The final checklist below remains unchecked until every named document also contains the later persistence, ceremony, recruitment and canonical-content behavior required for the complete V6 release.

### Documentation checklist

* [x] Create `docs/QUEST_IMPLEMENTATION_GUIDE.md` after all V6 formats are stable.
* [x] Update `ARCHITECTURE.md` with quest, narrative, requirement and recruitment responsibilities.
* [x] Update `DATA_FORMATS.md` with every new external format and save-format version 3.
* [x] Update `MECHANICS.md` with quest progression, ceremony and recruitment behavior.
* [x] Update `SAVE_STATE.md` with quest, flag and candidate properties and transient-state boundaries.
* [x] Update `CLASS_GUIDE.md` for beginner Flagelo, final paths and ceremony consequences.
* [x] Update `PROJECT_STATE.md` only after the implemented V6 state is approved.
* [ ] Update the Windows package readme if player-facing controls or save compatibility require it.
* [ ] Decide whether stable V6 data files should be reorganized into subdirectories; do not combine the move with unfinished loader work.

### Release checklist

* [ ] Review the complete V6 diff and remove every development shortcut or temporary route.
* [ ] Confirm that no save, decrypted payload, real encryption key or test artifact is packaged.
* [ ] Compile and run the final representative regression set.
* [ ] Build the portable Windows package.
* [ ] Verify a fresh V6 campaign, INN save, restart and Continue from the extracted package.
* [ ] Commit each completed roadmap block coherently instead of committing per checkbox.
* [ ] Create the consolidated V6 release commit or merge only after all blocks are complete.
* [ ] Tag and publish V6 only after documentation and portable-build verification are approved.
