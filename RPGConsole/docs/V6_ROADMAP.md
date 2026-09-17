# RPGConsole V6 Roadmap

> **Planning status:** V5 was released as `v5.0.0`. V6 is in frozen-scope planning and has not entered implementation. This roadmap records the approved quest-system contract, the first five main quests as the release content boundary, and the remaining decisions that must be resolved only when their implementation block is reached.

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

## 2. Quest objectives and synchronous progress

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
* The first narrative-boss victory emits dungeon completion; recurring-boss victories do not.
* Progress results are returned to the interface instead of being printed by the domain service.
* Quest notifications are transient and remain outside the save schema.

### Implementation checklist

* [ ] Implement and validate all eight objective types.
* [ ] Create mutable active-objective counters separate from immutable definitions.
* [ ] Implement `QuestProgressService` with explicit result objects.
* [ ] Integrate confirmed NPC conversations, battle outcomes, Analyze, inventory acquisition, scenario arrival and narrative dungeon completion.
* [ ] Implement transactional item delivery.
* [ ] Prevent one event from progressing both the completed step and the step it just activated.
* [ ] Allow later ordered events to affect a newly activated step.
* [ ] Return ordered, groupable progress results to the caller.
* [ ] Load and validate immutable objective definitions.
* [ ] Reject steps without objectives after objective loading is complete.
* [ ] Load complete quest definitions before mutable quest progress is created.

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

* [ ] Create immutable flag and requirement definitions.
* [ ] Create mutable set-only campaign flag state.
* [ ] Implement pure requirement evaluation against party, inventory, quests and flags.
* [ ] Validate requirement types, values, order and catalog references during startup.
* [ ] Reject quest-availability self-dependencies and cycles.
* [ ] Integrate requirement evaluation with quest offers, narrative locations and NPC placements.
* [ ] Audit existing route, branch, portal and service data before choosing their exact requirement columns.
* [ ] Add physical-access integration only where the first five quests require it.
* [ ] Avoid a generic `access_rules.txt` layer unless the code audit demonstrates a concrete need.
* [ ] Validate every quest availability requirement-set reference after both catalogs are available.

---

## 4. Quest rewards and permanent effects

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
* PARTY_EXP is awarded to every current party member.
* ITEM accepts any canonical entry supported by the shared inventory.
* Reward order starts at 1 and is unique and consecutive for each quest.
* Duplicate `(rewardType, rewardId)` entries for one quest are rejected.
* A quest may have no material reward.
* Reward descriptions are derived from actual reward definitions and are not duplicated as free text.
* Quest rewards are all-or-nothing; partial reward granting is not used for quest completion.
* EXP follows the existing character-level and maximum-level rules.

### Permanent effects

Use `data/quest_effects.txt`:

```text
# questId;step;trigger;order;effectType;targetId;displayText
```

The initial triggers are:

* `QUEST_START`
* `STEP_COMPLETE`
* `QUEST_COMPLETE`

The initial effect types are:

* `SET_FLAG`
* `UNLOCK_ROUTE`

Frozen rules:

* Quest-level triggers use step 0.
* `STEP_COMPLETE` uses the real positive step number.
* Effect order starts at 1 and is unique and consecutive per trigger location.
* `displayText` is optional and describes a visible unlock to the player.
* Step effects occur atomically with the step transition.
* Quest-completion effects occur atomically with deliveries, rewards and final status mutation.
* `DISCOVER_SCENARIO` is not introduced unless an audit of current world discovery proves that the first five quests need a separate effect.
* Class change and ally recruitment are dedicated typed ceremony behavior, not generic rewards or generic effects.

### Completion transaction

* The complete plan is validated before campaign state changes.
* Required delivery items, receiving limits, rewards, effects, special ceremony changes and final quest status belong to the same transaction when they resolve together.
* A capacity or state failure leaves items, characters, flags, routes, quest progress and rewards unchanged.
* The player may correct the blocking state and retry.
* Items granted by one completed quest may emit later `OBTAIN_ITEM` events for other active quests.
* The completed quest cannot consume its own reward event retroactively.

### Implementation checklist

* [ ] Load and validate reward definitions.
* [ ] Load and validate permanent-effect definitions.
* [ ] Derive player-facing reward summaries from actual data.
* [ ] Build and validate a complete completion plan before mutation.
* [ ] Apply ordinary quest rewards and effects atomically.
* [ ] Return explicit completion failures without partially changing the campaign.
* [ ] Emit reward-caused item acquisition only after successful completion.

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

### Implementation checklist

* [ ] Implement narrative-location, NPC, placement, dialogue and quest-dialogue definitions and loaders.
* [ ] Validate every reference, line order, context, speaker and placeholder during startup.
* [ ] Decode supported text escapes consistently with existing external text behavior.
* [ ] Display empty narrative locations correctly.
* [ ] Present fixed NPC placement and visibility through current requirements.
* [ ] Implement linear Enter-driven dialogue presentation.
* [ ] Implement the stable NPC interaction submenu without a generic dialogue-choice engine.
* [ ] Validate every quest starting NPC reference after both catalogs are available.

---

## 6. Quest interface and interaction markers

### Quest menu

* The quest menu is available from every stable menu outside battle and dialogue, including stable exploration menus.
* Opening the quest menu does not advance time or alter campaign state.
* The menu separates active and completed quests.
* Quests that have not started are hidden.
* An active quest displays its description, current step, current objectives and material rewards.
* Only objectives belonging to the current step are detailed.
* V6 does not allow the player to select a manually tracked quest.

### Progress notifications

* Partial objective progress produces a compact notification.
* Objective and step completion produce highlighted notifications.
* An objective-completion notification does not repeat the full objective text.
* Multiple completed objectives may be summarized numerically.
* Notifications produced by the same gameplay resolution are grouped.
* Quest completion receives its own reward and effect screen.
* Notifications are transient and are never persisted.

### `[!]` marker

* `[!]` is the only quest-interaction symbol in V6.
* The same symbol represents offer, progress and completion interactions.
* The marker appears only when an actionable quest interaction is available at that moment.
* It appears beside the relevant NPC and narrative location.
* It propagates to the current city's presentation when that city contains an actionable visible interaction.
* It propagates to a displayed travel destination and to `[#] Viajar` only when the reachable destination currently contains such an interaction.
* It does not reveal locked, hidden or future content.
* It does not perform global pathfinding or point through multiple routes.
* Marker state is derived from current definitions and campaign state and is never saved.

### Implementation checklist

* [ ] Add quest-menu access to every applicable stable menu.
* [ ] Implement active and completed quest presentation.
* [ ] Display current objective counters and derived reward summaries.
* [ ] Present grouped progress, step-completion and quest-completion results.
* [ ] Derive actionable NPC markers.
* [ ] Propagate markers to visible locations, cities and immediate travel presentation.
* [ ] Confirm that markers do not expose unmet requirements or inaccessible content.

---

## 7. Campaign persistence

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

* [ ] Extend immutable snapshots with quest, flag and recruitment-candidate state.
* [ ] Serialize and deserialize all new collections through stable canonical IDs.
* [ ] Increase the authenticated internal version to 3.
* [ ] Reject internal version 2 without migration.
* [ ] Reconstruct and validate complete candidate state before runtime activation.
* [ ] Supply restored quest, flag and candidate state to `World`.
* [ ] Preserve atomic file replacement and all existing cryptographic behavior.

---

## 8. Level-7 class ceremony

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
* Remove the `STARTER` acquisition type only after confirming that no remaining definition or runtime path depends on it.

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
* Invalid input repeats the current question.
* Every selected answer gives exactly one point to exactly one final class.
* Only final classes referenced by at least one answer can be recommended.
* The questionnaire is not required to cover all seven selectable paths.
* Flagelo is intentionally absent from the initial questionnaire and can never be recommended.
* Flagelo remains freely selectable as the protagonist's personal rejection of the offered paths.
* The highest score determines the recommendation.
* A tie is resolved uniformly among the tied highest-scoring classes using the application's injectable random source.
* Exactly one recommendation is presented.
* The recommendation does not restrict the final choice.
* Question and answer text supports the `\n` line-break sequence and the approved placeholders.
* Questionnaire state remains transient until a class is successfully confirmed.

### Selection and confirmation

* The player may accept the recommended class or inspect and choose any of the seven final paths.
* The ceremony cannot be cancelled after reaching this decision.
* Final confirmation offers only `Confirmar` and `Escolher novamente`.
* The confirmation screen reuses the complete class presentation from character creation: icon, name, weight, attribute tendencies and multiline description.
* Confirming the presentation freezes the protagonist's choice for the current uninterrupted Quest 4 resolution but does not commit partial campaign state by itself.
* A technical or capacity failure ends the interaction without mutation and allows a later retry.

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

### Implementation checklist

* [ ] Add and validate the beginner Flagelo definition and natural skills.
* [ ] Audit and remove the `STARTER` skill-acquisition type if it has no remaining consumer.
* [ ] Freeze the exact external schema for level-7 introductory class equipment.
* [ ] Define introductory weapon and armor for every final path.
* [ ] Load and validate questions, answers and recommendation targets.
* [ ] Implement questionnaire scoring and deterministic tie testing.
* [ ] Reuse class presentation for recommendation, free selection and confirmation.
* [ ] Build and validate the complete protagonist transition before mutation.
* [ ] Apply the confirmed transition exactly once and atomically.

---

## 9. Recruitment candidate and first ally

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

The ally's equipped introductory items never pass through the shared inventory.

### Atomic recruitment

* Quest 4 builds one complete final plan after the protagonist confirms a path and before campaign mutation.
* Validate the protagonist's equipment return, inventory capacity, candidate, party capacity, ally class pool, class definitions, introductory equipment, rewards and effects before the ally is accepted.
* The ceremony ending and ally conversation may be presented sequentially, but they belong to one uninterrupted resolution with no save point between them.
* The mandatory acceptance commits the protagonist transition, complete ally creation, second-position insertion, candidate recruitment state, quest advancement, rewards and effects atomically.
* A failure leaves the protagonist, candidate, party, inventory, flags, routes and quest state unchanged and ready for a later complete retry.

### Implementation checklist

* [ ] Freeze the exact candidate-definition trigger or data reference after auditing the quest-effect flow.
* [ ] Create the minimal persistent recruitment-candidate state.
* [ ] Load and validate the shared character-name list.
* [ ] Generate a unique candidate name at first narrative introduction.
* [ ] Resolve `RECRUIT` dialogue speakers from candidate state.
* [ ] Build the eligible ally class pool after protagonist confirmation.
* [ ] Construct the complete level-7 ally with equipped introductory gear.
* [ ] Present the single mandatory acceptance action.
* [ ] Recruit the ally into the second party position atomically.
* [ ] Prevent duplicate candidate creation and recruitment.

---

## 10. First five main quests

### Content boundary

* V6 ships exactly the first five main quests of the approved campaign structure.
* Their step sequences remain linear.
* Narrative choices do not create alternative quest branches.
* Each quest ends through an NPC interaction.
* Content uses the generic supported objective, requirement, reward and effect types whenever their semantics match.
* New Java behavior is added only for a genuine unsupported domain operation, not to encode one quest's ordinary content.
* Quest 4, `Os Sete Caminhos`, uses the dedicated ceremony and recruitment behavior defined in Parts 8 and 9.
* Exact narrative text, objectives, rewards, flags and unlocks must be reviewed and approved before each quest's data is implemented.

### Quest 4 frozen narrative sequence

```text
Caçada simples
→ retorno ao mestre
→ questionário
→ recomendação
→ escolha livre da classe do protagonista
→ encerramento da cerimônia
→ revelação da classe do aliado
→ aceitação obrigatória do aliado
→ recrutamento
→ conclusão
```

The companion participates in the story before joining the party and shares the protagonist's interest in investigating the disease.

### Implementation checklist

* [ ] Freeze the content contract for Quest 1 before implementing its data.
* [ ] Implement and validate Quest 1.
* [ ] Freeze the content contract for Quest 2 before implementing its data.
* [ ] Implement and validate Quest 2.
* [ ] Freeze the content contract for Quest 3 before implementing its data.
* [ ] Implement and validate Quest 3.
* [ ] Freeze the final content contract for Quest 4 against the ceremony architecture.
* [ ] Implement and validate Quest 4.
* [ ] Freeze the content contract for Quest 5 before implementing its data.
* [ ] Implement and validate Quest 5.
* [ ] Confirm that permanent route and scenario progression matches the approved campaign order.
* [ ] Confirm that all five quests are completable from a new campaign without development shortcuts.

---

## 11. Verification

> **Verification convention:** Test distinct behavior, relevant boundaries and state-corruption risks. Use one representative content entry when multiple entries travel through the same loader, catalog or runtime branch. Do not multiply tests merely because five quests or several classes use the same implementation.

### Definition and loader validation

* [ ] Test representative malformed, duplicated and unknown quest references.
* [ ] Test objective type, target, quantity and order validation.
* [ ] Test requirement-set validation, including one self-dependency or cycle.
* [ ] Test dialogue order, speaker, escape and placeholder validation.
* [ ] Test reward and effect order, reference and duplication validation.
* [ ] Confirm that failed loading does not partially populate catalogs.

### Runtime progression

* [ ] Test offer decline and later acceptance without unintended mutation.
* [ ] Test multiple simultaneously active quests receiving one applicable event.
* [ ] Test that only the current step progresses.
* [ ] Test that one event does not spill into the step it activates.
* [ ] Test that a later ordered event can progress the newly activated step.
* [ ] Test representative behavior for all eight objective types.
* [ ] Test battle victory, enemy defeat, Analyze, escape, defeat and refused encounter distinctions without redundant permutations.
* [ ] Test non-retroactive item acquisition and monotonic progress after item use or sale.
* [ ] Test transactional multi-item delivery success and failure.
* [ ] Test narrative dungeon completion separately from recurring-boss victory.

### Transactions and interface

* [ ] Test successful quest completion with rewards and permanent effects.
* [ ] Test capacity failure preserving delivery items, rewards, flags, routes and quest state.
* [ ] Test a reward-generated item event progressing another active quest.
* [ ] Test active and completed quest-menu presentation.
* [ ] Test grouped progress and completion notifications.
* [ ] Test actionable `[!]` propagation from NPC to location, city and immediate travel presentation.
* [ ] Confirm that unmet and hidden content does not leak through markers.

### Ceremony and recruitment

* [ ] Test questionnaire recommendation with one clear winner and one tie.
* [ ] Test free selection independent from the recommendation.
* [ ] Test the beginner-to-final Flagelo transition.
* [ ] Test one different final-class transition as the representative shared path.
* [ ] Test equipment return and inventory-capacity failure atomically.
* [ ] Test that the ally draw excludes Flagelo and the protagonist's class.
* [ ] Test the five-class and six-class ally pools at their distinct boundaries.
* [ ] Test unique candidate naming and exhausted-name failure.
* [ ] Test the recruited ally's fixed level, EXP, skills, resources, equipment and party position.
* [ ] Test that failed recruitment leaves candidate, party and quest state unchanged.
* [ ] Test that ceremony and recruitment cannot be applied twice.

### Save and regressions

* [ ] Test one version-3 round trip containing active quest counters, a completed quest, flags, a pending candidate and a recruited ally.
* [ ] Test representative invalid quest, objective, flag and candidate save state.
* [ ] Confirm that internal version 2 is rejected without migration.
* [ ] Confirm that failed restoration does not partially replace the active campaign.
* [ ] Run focused regressions for battle, Analyze, inventory, rewards, routes, dungeons, shops, crafting, INN saving and campaign loading.
* [ ] Verify the complete five-quest flow in the portable Windows build.

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

### Documentation checklist

* [ ] Create `docs/QUEST_IMPLEMENTATION_GUIDE.md` after all V6 formats are stable.
* [ ] Update `ARCHITECTURE.md` with quest, narrative, requirement and recruitment responsibilities.
* [ ] Update `DATA_FORMATS.md` with every new external format and save-format version 3.
* [ ] Update `MECHANICS.md` with quest progression, ceremony and recruitment behavior.
* [ ] Update `SAVE_STATE.md` with quest, flag and candidate properties and transient-state boundaries.
* [ ] Update `CLASS_GUIDE.md` for beginner Flagelo, final paths and ceremony consequences.
* [ ] Update `PROJECT_STATE.md` only after the implemented V6 state is approved.
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
