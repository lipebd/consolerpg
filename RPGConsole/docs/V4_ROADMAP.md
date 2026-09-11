# RPGConsole V4 Roadmap

> **Definitive roadmap:** This document defines the frozen scope, implementation order and release boundary of RPGConsole V4.

## Release direction

V4 develops character identity, progression and combat customization on top of the data-driven world established by V3.

The release includes external skills and combat effects, all seven playable-class definitions and progression rules, equipment, initial character and party creation, and full-party defeat and rescue.

Every new game begins with the playable character as a Flagelo. The player does not choose an initial class during character creation, and class changing is not implemented in V4.

During development, temporary party-creation shortcuts may assign any playable class directly to test its attributes, skills and progression. These shortcuts must remain separate from the normal new-game flow.

Quests, world flags, access requirements, remaining exploration decisions, save and load, the one-time class decision in Setegalhos, and Outrosgalhos are outside the V4 release boundary and are deferred to later development.

The implementation must continue to preserve stable IDs, strict startup validation and separation between immutable definitions and mutable runtime state. Validation and regression testing must accompany each system instead of being postponed to a final generic phase.

Current milestone: combat effects, external skills, natural progression, reward modifiers and the complete equipment system are implemented. Remaining unchecked validation items represent broader regression coverage, not missing core architecture.

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

* [ ] Test every stage and its attribute multiplier.
* [x] Test half-up rounding of effective attributes.
* [x] Test same-polarity accumulation, stage limits and duration renewal.
* [x] Test independent positive and negative durations on the same attribute.
* [ ] Test duration changes after consumed and non-consumed actions.
* [x] Test pre-action and post-action skill effects.
* [ ] Test pending VEL application, removal and turn-order generation.
* [ ] Test purification and dissipation with and without eligible states.
* [ ] Test immediate character-effect cleanup after defeat and subsequent revival.
* [ ] Test remaining battle-effect cleanup after victory, full-party defeat and escape.
* [ ] Complete regression tests for damage, HP/MP/SP recovery, revival and effect skills after externalization.
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
* [x] Use loaded class and skill definitions when assembling the temporary development party.
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
* [x] Define class-to-skill progression through each skill's class ID and acquisition level.
* [x] Validate skill class IDs and acquisition levels.
* [x] Assign a playable-class definition to every current playable character.
* [x] Calculate a character's natural attributes from class and level.
* [x] Expose the correct available skills for the character's class and level.
* [x] Apply attribute and resource growth during level-up.
* [x] Report every class skill newly unlocked across one or more gained levels.
* [x] Keep skill availability derived from unique definitions instead of duplicating learned skills.
* [x] Display the class in individual status.
* [x] Display attribute growth and newly learned skills during level-up.
* [x] Preserve a temporary development shortcut for assigning any playable class during party creation.
* [ ] Keep the normal new-game flow restricted to the Flagelo class.
* [x] Test the base attributes of all seven playable classes.
* [x] Test attribute and resource growth at levels before, during and after primary and secondary growth thresholds.
* [x] Test natural progression independently from equipment bonuses.
* [x] Test initial and level-based availability for the currently externalized skill subset.

### Skill-scroll integration

* [ ] Define external skill-scroll links to skill and class IDs.
* [ ] Validate skill-scroll references against skill and playable-class catalogs.
* [ ] Allow a compatible character to learn an unknown skill from a scroll.
* [ ] Consume a skill scroll only after successful learning.
* [ ] Reject duplicate or class-incompatible skill learning without consuming the item.
* [ ] Test successful and rejected skill-scroll use.


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

## 4. Character and party creation

* [ ] Create the initial character-creation flow.
* [ ] Allow the player to choose a character name.
* [ ] Create every new-game playable character as a Flagelo.
* [ ] Display the Flagelo's description and initial attributes before character confirmation.
* [ ] Replace temporary hardcoded party creation in `Main`.
* [ ] Define party-title rules.
* [ ] Use the party title in world narration.
* [ ] Display character history and lore.
* [ ] Preserve a temporary development-party shortcut for testing arbitrary classes, levels and party compositions.
* [ ] Define the maximum character-name length.
* [ ] Reject character names exceeding the supported length.
* [ ] Align party and individual status layouts using the supported name length.
* [ ] Display complete character names without truncation.

## 5. Full-party defeat and rescue

* [ ] Replace the temporary `GameOverScreen`.
* [ ] Send the defeated party to Bree.
* [ ] Fully restore HP, MP and SP through the rescue flow.
* [ ] Revive all defeated party members through rescue.
* [ ] Preserve EXP.
* [ ] Preserve gold.
* [ ] Preserve inventory.
* [ ] Preserve equipment.
* [ ] Preserve scenario discovery and progression.
* [ ] Keep normal post-battle behavior from automatically reviving defeated members.
* [ ] Test defeat in fields.
* [ ] Test defeat in dungeons.
* [ ] Test defeat during mandatory and optional encounters.

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
* [ ] Test character creation.
* [ ] Test full-party rescue.
* [ ] Test save and load after restarting the application.
* [ ] Test world flags and access requirements when their supporting systems exist.
* [ ] Test gold and per-item quantity boundaries, overflow attempts and invalid save values.

## 9. Optional post-release learning

> **Optional:** This section does not block V4 or any earlier release.

* [ ] Study the purpose and structure of unit tests.
* [ ] Evaluate adding JUnit to the project.
* [ ] Create a small isolated unit-test suite as a learning exercise.
* [ ] Compare automated unit tests with the project's existing manual functional and regression tests.
