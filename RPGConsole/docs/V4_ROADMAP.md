# RPGConsole V4 Roadmap

> **Definitive roadmap:** This document defines the frozen scope, implementation order and release boundary of RPGConsole V4.

## Release direction

V4 develops character identity, progression and combat customization on top of the data-driven world established by V3.

The release includes external skills and combat effects, all seven playable-class definitions and progression rules, equipment, initial character and party creation, and full-party defeat and rescue.

Every new game begins with the playable character as a Flagelo. The player does not choose an initial class during character creation, and class changing is not implemented in V4.

During development, temporary party-creation shortcuts may assign any playable class directly to test its attributes, skills and progression. These shortcuts must remain separate from the normal new-game flow.

Quests, world flags, access requirements, remaining exploration decisions, save and load, the one-time class decision in Setegalhos, and Outrosgalhos are outside the V4 release boundary and are deferred to later development.

The implementation must continue to preserve stable IDs, strict startup validation and separation between immutable definitions and mutable runtime state. Validation and regression testing must accompany each system instead of being postponed to a final generic phase.

Current milestone: all definitive V4 gameplay-system blocks are functionally complete. Remaining unchecked items belong to preserved post-V4 planning, broader regression coverage or optional learning work.

---

## 1. Combat effects

### Attribute-stage rules

* [x] Define ATK, DEF, MAG, RES, VEL and LCK as stage-modifiable attributes.
* [x] Create immutable internal stage-multiplier rules.
* [x] Limit positive and negative stages independently between 0 and 3.
* [x] Calculate the effective stage by subtracting negative stages from positive stages.
* [x] Apply the frozen multiplier for each effective stage from `-3` to `+3`.
* [x] Round multiplied attributes to the nearest integer using half-up rounding.
* [x] Keep stage modifiers separate from base, natural and equipment attributes.

### Mutable combat-effect state

* [x] Create mutable positive and negative stage state for every modifiable attribute.
* [x] Store independent remaining durations for positive and negative states.
* [x] Add same-polarity stages up to the limit of 3.
* [x] Preserve the greatest remaining duration when same-polarity effects accumulate.
* [x] Preserve opposite-polarity states independently instead of destructively cancelling them.
* [x] Expose effective ATK, DEF, MAG, RES, VEL and LCK to battle calculations.
* [x] Display active positive and negative states during battle.
* [x] Clear all temporary character effects immediately upon defeat.
* [x] Clear all remaining temporary character effects when the battle ends.
* [x] Prevent effects from leaking between unrelated battles.

### Effect duration and action order

* [x] Decrease effect duration only after the affected character consumes one of its own turns.
* [x] Keep durations unchanged after menus, cancellations and actions that produce no effect.
* [x] Support ordered skill components applied before or after the primary damage or recovery component.
* [x] Apply pre-action effects before calculating the primary component.
* [x] Decrease the acting character's existing effect durations at the end of its consumed turn.
* [x] Apply deferred post-action effects after the duration decrease.
* [x] Prevent newly applied post-action effects from losing duration immediately.
* [x] Treat applied, resisted and non-triggered applicable components as valid attempts.
* [x] Consume skill resources and the acting character's turn once when at least one component constitutes a valid attempt.
* [x] Keep failed conditions and inapplicable components outside action and resource consumption.

### Speed-stage lifecycle

* [x] Keep VEL stages pending for the next turn-order calculation.
* [x] Keep the current round order unchanged when VEL changes.
* [x] Calculate the next round order using pending VEL stages.
* [x] Remove the VEL stages immediately after generating the affected round order.
* [x] Keep the generated round order unchanged after the VEL stages are removed.
* [x] Allow pending positive VEL stages to be dissipated before order generation.
* [x] Allow pending negative VEL stages to be purified before order generation.
* [x] Preserve VEL stages applied after order generation for the subsequent round.

### Purification and dissipation

* [x] Remove all negative attribute states when purification succeeds.
* [x] Remove all positive attribute states when dissipation succeeds.
* [x] Treat purification or dissipation as ineffective when no eligible state exists.
* [x] Keep special battle states outside automatic purification and dissipation.
* [x] Connect recovery-item purification to active negative states.

### Component execution, resistance and calculations

* [x] Evaluate each component in the order condition, applicability, trigger, resistance when applicable, then application.
* [x] Store an optional `triggerChancePercent` on each external component, with an empty field equivalent to 100%.
* [x] Roll the trigger once per component and share its result across all applicable targets.
* [x] Roll resistance independently for every eligible target.
* [x] Centralize resistance based on the user's effective LCK and the target's effective RES, with `KR = 1` and an 85% application cap.
* [x] Make negative attribute stages and dissipation resistible.
* [x] Keep negative attribute stages resistible when the user applies them to itself.
* [x] Distinguish `APPLIED`, `RESISTED` and `NOT_TRIGGERED` in skill-effect results.
* [x] Implement reversible, non-resistible Distorção Ancestral outside purification and dissipation.
* [x] Display Distorção Ancestral while it remains active.
* [x] Centralize physical and magical damage for skills and basic attacks through `DamageCalculator`.
* [x] Centralize ATK- or MAG-scaled HP recovery and revival through `RecoveryCalculator`.
* [x] Keep MP and SP recovery as fixed-power components.
* [x] Apply `DAMAGE_POWER_MODIFIER` to every later compatible damage component against the same target during one skill execution.
* [x] Accumulate multiple damage-power modifiers with overflow protection.
* [x] Implement direct non-lethal recoil as a percentage of the actual damage dealt by the immediately preceding single-enemy damage component.
* [x] Validate recoil ordering, targeting, percentage and source-component requirements.

### Combat-effect and skill validation

* [x] Test every stage and its attribute multiplier.
* [x] Test half-up rounding of effective attributes.
* [x] Test same-polarity accumulation, stage limits and duration renewal.
* [x] Test independent positive and negative durations on the same attribute.
* [x] Test duration changes after consumed and non-consumed actions.
* [x] Test pre-action and post-action skill effects.
* [x] Test pending VEL application, removal and turn-order generation.
* [x] Test purification and dissipation with and without eligible states.
* [x] Test immediate character-effect cleanup after defeat and subsequent revival.
* [x] Test remaining battle-effect cleanup after victory, full-party defeat and escape.
* [x] Complete regression tests for damage, HP/MP/SP recovery, revival and effect skills after externalization.
* [x] Test skills with multiple components when components are resisted, not triggered or applied in different phases.
* [x] Test conditional components before and after damage, conditional stage values and self-debuff resistance after a pre-action LCK increase.
* [x] Test recoil source damage, independent trigger chance, half-up rounding and the non-lethal HP limit.
* [x] Test global and individual reward modifiers.

## 2. Playable classes, external skills and level progression

* [x] Define external playable-class data.
* [x] Create stable IDs for all seven playable classes.
* [x] Create immutable playable-class definitions.
* [x] Create the playable-class loader.
* [x] Create the playable-class catalog.
* [x] Store base HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK.
* [x] Store each class's primary and secondary attributes.
* [x] Store class weight categories.
* [x] Store class descriptions and lore.
* [x] Support literal `\n` in class descriptions.
* [x] Validate base attributes, primary and secondary attributes, weight and descriptive data.
* [x] Validate per-stat limits and total base-stat distribution according to the playable-class balance rules.

### External skill definitions

* [x] Define `skills.txt`.
* [x] Create stable skill IDs.
* [x] Create immutable skill definitions.
* [x] Define external ordered skill-effect components.
* [x] Create the skill loader.
* [x] Create the skill catalog.
* [x] Validate targeting, damage, recovery, resource and effect data.
* [x] Validate effect attributes, stages, durations and execution order.
* [x] Support literal `\n` in skill descriptions.
* [x] Support skills containing multiple effect components.
* [x] Support reversible special battle states required by individual skills.
* [x] Externalize all 30 approved playable skills across the seven class definitions.
* [x] Replace hardcoded skill creation in `Main`.
* [x] Use loaded class and skill definitions when assembling development parties.
* [x] Complete the approved external skill data for all seven playable classes.
* [x] Redesign the learned-skills screen to display main type, power, targeting, resource cost and the external narrative description.
* [x] Add global EXP and gold reward modifiers.
* [x] Add individual character EXP modifiers.

### Character progression

* [x] Create immutable global character-growth rules.
* [x] Define integer growth values for HP, MP, SP and regular attributes.
* [x] Define the primary-attribute growth interval.
* [x] Define the secondary-attribute growth interval.
* [x] Keep internal character-growth balance constants out of external data files.
* [x] Apply the configured general growth to every natural character stat after level 1.
* [x] Apply one additional primary-attribute growth unit every two levels.
* [x] Apply one additional secondary-attribute growth unit every three levels.
* [x] Apply defensive specialization to HP growth.
* [x] Apply MAG specialization to MP growth.
* [x] Apply ATK specialization to SP growth.
* [x] Keep natural level progression separate from temporary combat effects and equipment bonuses.
* [x] Define class-to-skill progression through each skill's class ID, acquisition type and level requirement.
* [x] Validate skill class IDs, acquisition types and level requirements.
* [x] Assign a playable-class definition to every current playable character.
* [x] Calculate a character's natural attributes from class and level.
* [x] Expose the correct available skills for the character's class and level.
* [x] Apply attribute and resource growth during level-up.
* [x] Report every class skill newly unlocked across one or more gained levels.
* [x] Keep skill availability derived from unique definitions instead of duplicating learned skills.
* [x] Display the class in individual status.
* [x] Display attribute growth and newly learned skills during level-up.
* [x] Preserve a temporary development shortcut for assigning any playable class during party creation.
* [x] Keep the normal new-game flow restricted to the Flagelo class.
* [x] Test the base attributes of all seven playable classes.
* [x] Test attribute and resource growth at levels before, during and after primary and secondary growth thresholds.
* [x] Test natural progression independently from equipment bonuses.
* [x] Test initial and level-based availability for the currently externalized skill subset.

### Skill-scroll integration

* [x] Distinguish `STARTER`, `LEVEL` and `SCROLL` skill acquisition types.
* [x] Treat a skill's configured level as its automatic acquisition level or minimum scroll-use level according to its acquisition type.
* [x] Classify the Flagelo's level-1 and level-3 skills as temporary `STARTER` skills.
* [x] Keep `STARTER` skill retirement deferred to the level-7 class-choice flow.
* [ ] Keep every post-choice playable class limited to four permanent `LEVEL` skills and two learned `SCROLL` skills.
* [x] Define external skill-scroll links to item, skill and playable-class IDs.
* [x] Require skill-scroll items to use the `SKILL_SCROLL` item category.
* [x] Require scroll-linked skills to use the `SCROLL` acquisition type.
* [x] Validate skill-scroll references against item, skill and playable-class catalogs.
* [x] Reject mismatches between a scroll's compatible class and its linked skill class.
* [x] Create immutable skill-scroll definitions, external loading and catalog lookup.
* [x] Create the specialized runtime skill-scroll item type.
* [x] Store learned skills through stable IDs while keeping `SkillDefinition` instances unique.
* [x] Centralize successful skill acquisition through character-domain learning behavior.
* [x] Allow skill scrolls to be used only outside battle.
* [x] Reject scroll use by a class-incompatible or underleveled character.
* [x] Reject learning an already known skill without consuming the scroll.
* [x] Allow at most two learned `SCROLL` skills per character.
* [x] Require replacement of one learned `SCROLL` skill when both scroll slots are occupied.
* [x] Prevent `STARTER` and `LEVEL` skills from being replaced through scroll use.
* [x] Preserve the current learned skills and inventory when replacement is cancelled.
* [x] Consume exactly one skill scroll only after successful learning or confirmed replacement.
* [x] Keep replaced scroll skills forgotten without returning their original scroll.
* [x] Display permanent level skills before scroll-learned skills.
* [x] Preserve scroll-learning order among learned scroll skills.
* [x] Reuse the existing skill definition, component, detail and execution structures for scroll skills.
* [x] Support low-chance skill-scroll drops through the existing enemy-drop system.
* [x] Keep trading-post behavior outside the current integration block.
* [x] Test successful learning, duplicate rejection, class rejection, level rejection, limit handling, cancelled replacement and confirmed replacement.

## 3. Equipment

* [x] Define the weapon, armor and trinket equipment slots.
* [x] Define external weapon data.
* [x] Define external armor data.
* [x] Define external trinket data.
* [x] Create immutable weapon, armor and trinket definitions.
* [x] Create the shared equipment loader.
* [x] Create the shared equipment catalog.
* [x] Link every equipment definition to a stable item ID.
* [x] Validate equipment references against the item catalog.
* [x] Prevent the same item ID from defining more than one equipment category.
* [x] Store an integer level requirement for every equipment item.
* [x] Reject equipment use below its level requirement.

### Weapons

* [x] Classify every weapon as physical or magical.
* [x] Store the weapon type and normal-attack name.
* [x] Store a mandatory integer base weapon damage value (`DBA`, displayed as `Poder`).
* [x] Support optional HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK bonuses.
* [x] Reject weapons with missing or invalid base weapon damage.
* [x] Use the equipped weapon's damage type and `DBA` in basic-attack calculations.
* [x] Keep weapon `DBA` separate from effective character attributes.
* [x] Add the equipped weapon's full `DBA` to every damaging skill.
* [x] Allow every playable class to equip every weapon that satisfies the level requirement.

### Armor

* [x] Define light, medium and heavy armor weights.
* [x] Support optional HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK bonuses.
* [x] Require the armor weight to match the playable class's weight exactly.
* [x] Keep armor defense entirely attribute-based without introducing a separate base-armor value.

### Trinkets

* [x] Support optional HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK bonuses.
* [x] Support an individual EXP reward multiplier for the equipped character.
* [x] Support a party-wide gold reward multiplier from an individually equipped trinket.
* [x] Keep individual equipment EXP modifiers separate from global EXP modifiers.
* [x] Keep equipment-based gold modifiers separate from temporary global gold modifiers.
* [x] Additively combine `M.EXP` with other EXP modifiers applicable to the equipped character.
* [x] Additively combine all equipped `M.GOLD` bonuses with other global gold modifiers.
* [x] Define consistent integer rounding for modified EXP and gold rewards.

### Equipment-data validation

* [x] Validate equipment IDs, required fields and duplicate definitions.
* [x] Validate positive level requirements.
* [x] Validate the common nine-stat bonus schema for every equipment type.
* [x] Validate weapon categories, weapon types and normal-attack names.
* [x] Validate armor weights.
* [x] Validate integer attribute bonuses.
* [x] Validate EXP and gold reward multipliers.

### Equipment state and interface

* [x] Equip and unequip one weapon, one armor and one trinket per playable character.
* [x] Prevent equipment from being placed in an incompatible slot.
* [x] Prevent the party from equipping more copies of an item than the quantity it owns.
* [x] Prevent equipped item quantities from being sold by transferring equipped units out of inventory.
* [x] Recalculate effective attributes safely after equipment changes.
* [x] Preserve natural attributes separately from equipment bonuses.
* [x] Update the character's basic-attack presentation when its equipped weapon changes.
* [x] Display equipped items, requirements, `Poder`, bonuses and multipliers in inventory and individual status flows.
* [x] Integrate equipment base items with shops, drops and exploration rewards.
* [x] Support equipping from inventory and from a character's status screen through one shared menu.
* [x] Display a change-only comparison before confirming equipment.
* [x] Test equipping, replacing and removing every equipment type.
* [x] Test equipment level requirements.
* [x] Test physical and magical basic attacks after weapon changes.
* [x] Test effective attributes before and after equipment changes.
* [x] Test equipment ownership across multiple party members.
* [x] Test equipment purchases, rewards and sale restrictions.
* [x] Test exact armor-weight compatibility for light, medium and heavy classes.
* [x] Test individual EXP modifiers from equipped trinkets.
* [x] Test party-wide gold modifiers from equipped trinkets.
* [x] Test additive stacking between multiple reward modifiers.

## 4. Initial character and party creation

* [x] Centralize playable-character name normalization and validation.
* [x] Limit playable-character names to 16 complete characters.
* [x] Allow Unicode letters, single spaces, hyphens and apostrophes in character names.
* [x] Reject blank, malformed, control-containing or overlong character names.
* [x] Compare party-member names case-insensitively for duplication.
* [x] Define external random character names.
* [x] Load and validate unique random names through the shared character-name rules.
* [x] Require enough valid external names to support a complete party.
* [x] Select random character names uniformly after excluding names already in use.
* [x] Create the initial character-creation flow.
* [x] Allow the player to type the protagonist's name or ask the game to choose one.
* [x] Allow repeated random-name selection before confirmation.
* [x] Allow character creation to be cancelled back to the title screen.
* [x] Display the Flagelo's class name, primary and secondary specializations and description before character confirmation.
* [x] Allow the player to change the proposed name before confirmation.
* [x] Create the confirmed protagonist as a level-1 Flagelo with full resources.
* [x] Create the initial `Party` with only the protagonist.
* [x] Keep the protagonist as the party leader.
* [x] Replace temporary hardcoded party creation in `Main`.
* [x] Preserve a `--dev-party` shortcut through a separate `DevelopmentPartyFactory`.
* [x] Allow the development factory to configure distinct classes, levels and party sizes from one to four.
* [x] Use the centralized maximum name length in naming instructions and keep character interfaces compatible with it.
* [x] Display complete valid character names without truncation.
* [x] Store one validated Unicode symbol for every playable class.
* [x] Display playable-class symbols in compact party and individual status views.
* [x] Test manual naming, random naming, repeated randomization, invalid input, cancellation and confirmation.
* [x] Test initial-party creation and configurable development parties.

## 5. Full-party defeat and rescue

### Rescue destination data

* [x] Add an optional `preferredRescueScenarioId` to every external combat-scenario definition.
* [x] Add a required `fallbackRescueScenarioId` to every external combat-scenario definition.
* [x] Add a required external `rescueDescription` to every combat-scenario definition.
* [x] Define exactly one external INN for every registered city.
* [x] Reject game initialization when any registered city has no INN.
* [x] Interpret literal `\n` sequences in rescue descriptions as line breaks.
* [x] Require every configured rescue destination to reference a valid city.
* [x] Require every configured rescue destination to provide a valid INN.
* [x] Reject equal preferred and fallback rescue destinations when a preferred destination is present.
* [x] Use the preferred rescue destination only when it has already been discovered.
* [x] Use the fallback rescue destination when the preferred destination is absent or undiscovered.
* [x] Allow the fallback destination to be used regardless of its previous discovery state.
* [x] Mark the selected rescue destination as discovered without unlocking unrelated routes.

### Rescue cost

* [x] Calculate 10% of the party's current gold as the proportional rescue cost.
* [x] Round the proportional rescue cost upward.
* [x] Use the greater value between the proportional cost and the selected destination INN price.
* [x] Limit the final rescue cost to the party's current gold.
* [x] Allow rescue with no payment when the party has no gold.
* [x] Prevent rescue costs from producing negative gold or arithmetic overflow.
* [x] Display the exact gold amount deducted for rescue and medical care.

### Defeat and restoration flow

* [x] Replace and remove the temporary `GameOverScreen`.
* [x] Keep `Battle` responsible only for determining and returning `BattleResult.DEFEAT`.
* [x] Keep the final battle-defeat message before beginning the rescue flow.
* [x] Keep the application running after full-party defeat.
* [x] Make `World` resolve the rescue destination and coordinate the complete rescue flow.
* [x] Abandon the failed `ExplorationSession` without advancing or completing it.
* [x] Start a new exploration session from the normal entry position when the defeated scenario is visited again.
* [x] Move the party to the selected rescue city.
* [x] Revive every defeated party member.
* [x] Fully restore every party member's HP, MP and SP.
* [x] Centralize complete party restoration and reuse it from both rescue and INN flows.
* [x] Keep normal victory and escape behavior from automatically reviving defeated members.

### Rescue presentation

* [x] Create a dedicated `PartyRescueScreen`.
* [x] Display the defeated combat scenario's external rescue description.
* [x] Display the selected rescue city's name separately from the external narrative.
* [x] Support appropriate singular and full-party restoration messages.
* [x] Display the rescue and medical-care cost through `GameCurrency`.
* [x] Require ENTER before returning control to the normal world loop.
* [x] Resume the normal world loop in the selected rescue city after confirmation.

### State preservation

* [x] Preserve character EXP and levels.
* [x] Preserve learned skills.
* [x] Preserve all gold remaining after the explicit rescue penalty.
* [x] Preserve inventory contents without restoring items consumed during battle.
* [x] Preserve equipped items.
* [x] Preserve active temporary reward modifiers and their remaining durations.
* [x] Preserve discovered scenarios and unlocked routes.
* [x] Preserve completed permanent world progression.
* [x] Preserve current shop offers and remaining stock.
* [x] Keep defeat from granting battle rewards or rolling back previous state changes.

### Rescue validation

* [x] Test rescue from a field encounter.
* [x] Test rescue from a dungeon encounter.
* [x] Test rescue from an accepted optional epic encounter.
* [x] Test rescue from a mandatory boss encounter.
* [x] Test selection of a discovered preferred rescue destination.
* [x] Test fallback selection when the preferred destination is absent.
* [x] Test fallback selection when the preferred destination is undiscovered.
* [x] Test fallback discovery without unrelated route unlocking.
* [x] Test zero-gold rescue.
* [x] Test gold below the destination INN price.
* [x] Test the destination INN price exceeding the proportional cost.
* [x] Test the proportional cost exceeding the destination INN price.
* [x] Test upward percentage rounding and maximum integer gold.
* [x] Test complete revival and HP, MP and SP restoration for parties of one to four members.
* [x] Test preservation of EXP, skills, remaining gold, inventory, equipment, reward modifiers, world progress and shop state.
* [x] Test that the failed exploration position is discarded.
* [x] Test that victory and escape do not activate the rescue flow.
* [x] Test that defeated members are not automatically revived after a normal victory.

---

> **Preserved post-V4 backlog:** The following requirements remain in this document temporarily to prevent planning loss. They are outside the definitive V4 release boundary and will be transferred to the V5 roadmap during its planning phase.

## 6. Save and load

* [ ] Define the save-file format.
* [ ] Add a save-format version.
* [ ] Save party identity and title.
* [ ] Save every playable character.
* [ ] Save level, EXP and class.
* [ ] Save learned skills.
* [ ] Save current HP, MP and SP.
* [ ] Save shared gold and inventory.
* [ ] Save equipped items.
* [ ] Save current scenario ID.
* [ ] Save world and scenario discovery.
* [ ] Save relevant exploration progress.
* [ ] Save persistent shop state if finite stock is adopted.
* [ ] Validate all loaded IDs against current catalogs.
* [ ] Reject or safely report invalid save data.
* [ ] Add New Game and Continue options to the title screen.
* [ ] Prevent partial state changes when loading fails.
* [ ] Test save and load after restarting the application.
* [ ] Define the maximum owned gold quantity.
* [ ] Define the maximum owned quantity of each item.
* [ ] Keep total inventory capacity unlimited while enforcing numeric quantity limits.
* [ ] Decide whether gains exceeding a limit are rejected, capped or partially granted.
* [ ] Prevent integer overflow during gold and inventory quantity changes.
* [ ] Validate saved gold and item quantities before restoring state.
* [ ] Reject negative or out-of-range resource quantities in save data.

## 7. Exploration and world-progression decisions

* [ ] Decide whether some special areas guarantee a specific event.
* [ ] Evaluate exploration checkpoints.
* [ ] Evaluate camps and temporary recovery.
* [ ] Decide how completed dungeons behave when revisited.

### Level-7 class change

> **Deferred:** The class-path decision remains outside V4, but its equipment consequence is already frozen.

* [ ] Return all three equipped items to inventory before applying the class decision.
* [ ] Grant the chosen class's level-7 starter weapon and armor, including when the character remains a Flagelo.
* [ ] Display narrative guidance telling the player to open the equipment menu and equip the new set.

### World flags and access requirements

> **Deferred:** Implement after quests and the relevant progression systems provide real state to evaluate. Do not create placeholder requirement abstractions before then.

* [ ] Create stable world-progress flags for quests, story events and permanent unlocks.
* [ ] Define reusable access requirements for routes, branches, portals and services.
* [ ] Evaluate access requirements against party level, inventory, completed quests and world flags.
* [ ] Support multiple simultaneous requirements for the same access point.
* [ ] Decide whether unmet requirements hide an action or display it as blocked with a player-facing reason.
* [ ] Replace blanket route unlocking with explicit progress triggers where required.
* [ ] Prepare world flags, completed quests and route unlocks for save data.

## 8. Future validation requirements

* [ ] Test malformed skill and class data.
* [ ] Test battles after skill, effect, class and equipment integration.
* [x] Test party rewards and level-ups after class progression is implemented.
* [ ] Test equipment changes during normal progression.
* [x] Test character creation.
* [x] Test full-party rescue.
* [ ] Test save and load after restarting the application.
* [ ] Test world flags and access requirements when their supporting systems exist.
* [ ] Test gold and per-item quantity boundaries, overflow attempts and invalid save values.
* [ ] Standardize remaining external-data loaders so validation errors report the source file, line number and content ID whenever available.
* [ ] Review compact functional-style code and replace it with clearer imperative code when this improves maintainability without changing behavior.

## 9. Optional post-release learning

> **Optional:** This section does not block V4 or any earlier release.

* [ ] Study the purpose and structure of unit tests.
* [ ] Evaluate adding JUnit to the project.
* [ ] Create a small isolated unit-test suite as a learning exercise.
* [ ] Compare automated unit tests with the project's existing manual functional and regression tests.
