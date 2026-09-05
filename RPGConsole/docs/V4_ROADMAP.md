# RPGConsole V4 Roadmap

> **Preliminary roadmap:** This document preserves requirements currently planned beyond V3. Its order, release boundary and detailed scope will be reviewed only after V3 is completed and released.

## Proposed release direction

V4 develops character identity, progression and combat systems on top of the data-driven world established by V3.

The initial direction includes external skills and effects, playable classes, equipment, character and party creation, and full-party defeat and rescue.

Save and load, remaining exploration decisions, world flags and access requirements are provisionally associated with V4, but may be moved wholly or partially to V5 during the definitive V4 planning phase.

The implementation must continue to preserve stable IDs, strict startup validation and separation between immutable definitions and mutable runtime state.

---

## 1. External skills and combat effects

* [ ] Define `skills.txt`.
* [ ] Create stable skill IDs.
* [ ] Create immutable skill definitions.
* [ ] Create the skill loader.
* [ ] Create the skill catalog.
* [ ] Validate targeting, damage, recovery and resource data.
* [ ] Support literal `\n` in skill descriptions.
* [ ] Externalize current playable skills.
* [ ] Replace hardcoded skill creation in `Main`.
* [ ] Redesign the learned-skills screen to display complete targeting, resource-cost and effect information.
* [ ] Implement temporary buffs.
* [ ] Implement temporary debuffs.
* [ ] Implement effect duration.
* [ ] Display active effects during battle.
* [ ] Prevent effects from leaking between unrelated battles.
* [ ] Add global EXP and gold reward modifiers.
* [ ] Add individual character EXP modifiers.
* [ ] Test healing, revival and effect skills after externalization.
* [ ] Define external skill-scroll links to skill and class IDs.
* [ ] Validate skill-scroll references against skill and class catalogs.
* [ ] Allow a compatible character to learn an unknown skill from a scroll.
* [ ] Consume a skill scroll only after successful learning.
* [ ] Reject duplicate or class-incompatible skill learning without consuming the item.
* [ ] Connect recovery-item purification to active negative effects.

## 2. Playable classes

* [ ] Define external playable-class data.
* [ ] Create stable IDs for all seven playable classes.
* [ ] Store base attributes.
* [ ] Store attribute-growth rules.
* [ ] Store class descriptions and lore.
* [ ] Support literal `\n` in class descriptions.
* [ ] Define class-to-skill progression.
* [ ] Validate referenced skill IDs.
* [ ] Assign a playable class to every playable character.
* [ ] Apply class growth during level-up.
* [ ] Display the class in individual status.
* [ ] Support the Flagelo's complete initial progression.
* [ ] Prepare Setegalhos for class training and class changes.
* [ ] Preserve IDs required by future save data.

## 3. Equipment

* [ ] Define equipment item types.
* [ ] Define equipment slots.
* [ ] Add weapon damage.
* [ ] Add armor bonuses.
* [ ] Add attribute bonuses.
* [ ] Equip and unequip items.
* [ ] Prevent one inventory instance from being equipped twice.
* [ ] Recalculate effective attributes safely.
* [ ] Preserve base attributes separately from equipment bonuses.
* [ ] Add equipment inspection to individual status.
* [ ] Integrate equipment with shops and exploration rewards.
* [ ] Test equipment changes during normal progression.

## 4. Character and party creation

* [ ] Create the initial character-creation flow.
* [ ] Allow the player to choose a character name.
* [ ] Allow the player to choose from available starting classes.
* [ ] Display class descriptions and attributes before confirmation.
* [ ] Replace temporary hardcoded party creation in `Main`.
* [ ] Define party-title rules.
* [ ] Use the party title in world narration.
* [ ] Display character history and lore.
* [ ] Prepare character and party identity for save data.
* [ ] Preserve temporary development-party shortcuts when needed for testing.
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

> **Provisional release boundary:** The following requirements are currently associated with V4 only to preserve planning continuity. They may be moved wholly or partially to V5 when the definitive V4 roadmap is prepared.

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
* [ ] Test party rewards and level-ups after class progression is implemented.
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
