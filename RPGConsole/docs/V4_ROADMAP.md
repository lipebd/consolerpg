# RPGConsole V4 Roadmap

> **Definitive roadmap:** This document defines the frozen scope, implementation order and release boundary of RPGConsole V4.

## Release direction

V4 develops character identity, progression and combat customization on top of the data-driven world established by V3.

The release includes external skills and combat effects, all seven playable-class definitions and progression rules, equipment, initial character and party creation, and full-party defeat and rescue.

Every new game begins with the playable character as a Flagelo. The player does not choose an initial class during character creation, and class changing is not implemented in V4.

During development, temporary party-creation shortcuts may assign any playable class directly to test its attributes, skills and progression. These shortcuts must remain separate from the normal new-game flow.

Quests, world flags, access requirements, remaining exploration decisions, save and load, the one-time class decision in Setegalhos, and Outrosgalhos are outside the V4 release boundary and are deferred to later development.

The implementation must continue to preserve stable IDs, strict startup validation and separation between immutable definitions and mutable runtime state. Validation and regression testing must accompany each system instead of being postponed to a final generic phase.

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
* [ ] Connect recovery-item purification to active negative effects.
* [ ] Test damage, healing, revival, resource recovery and effect skills after externalization.
* [ ] Test effect application, duration, expiration and battle cleanup.
* [ ] Test global and individual reward modifiers.

## 2. Playable classes and level progression

* [ ] Define external playable-class data.
* [ ] Create stable IDs for all seven playable classes.
* [ ] Create immutable playable-class definitions.
* [ ] Create the playable-class loader.
* [ ] Create the playable-class catalog.
* [ ] Store base HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK.
* [ ] Store each class's primary and secondary attributes.
* [ ] Store class weight categories.
* [ ] Store class descriptions and lore.
* [ ] Support literal `\n` in class descriptions.
* [ ] Validate base attributes, primary and secondary attributes, weight and descriptive data.
* [ ] Validate per-stat limits and total base-stat distribution according to the playable-class balance rules.
* [ ] Create immutable global character-growth rules.
* [ ] Define integer growth values for HP, MP, SP and regular attributes.
* [ ] Define the primary-attribute growth interval.
* [ ] Define the secondary-attribute growth interval.
* [ ] Keep internal character-growth balance constants out of external data files.
* [ ] Apply the configured general growth to every natural character stat after level 1.
* [ ] Apply one additional primary-attribute growth unit every two levels.
* [ ] Apply one additional secondary-attribute growth unit every three levels.
* [ ] Apply defensive specialization to HP growth.
* [ ] Apply MAG specialization to MP growth.
* [ ] Apply ATK specialization to SP growth.
* [ ] Keep natural level progression separate from equipment bonuses.
* [ ] Define class-to-skill progression.
* [ ] Validate referenced skill IDs and skill-unlock levels.
* [ ] Assign a playable class to every playable character.
* [ ] Calculate a character's natural attributes from class and level.
* [ ] Grant the correct initial skills for the character's class and level.
* [ ] Apply attribute and resource growth during level-up.
* [ ] Grant newly unlocked class skills during level-up.
* [ ] Prevent the same skill from being learned more than once.
* [ ] Display the class in individual status.
* [ ] Display attribute growth and newly learned skills during level-up.
* [ ] Preserve a temporary development shortcut for assigning any playable class during party creation.
* [ ] Keep the normal new-game flow restricted to the Flagelo class.
* [ ] Test the base attributes of all seven playable classes.
* [ ] Test attribute and resource growth at levels before, during and after primary and secondary growth thresholds.
* [ ] Test natural progression independently from equipment bonuses.
* [ ] Test initial and level-based skill learning for all seven playable classes.

### Skill-scroll integration

* [ ] Define external skill-scroll links to skill and class IDs.
* [ ] Validate skill-scroll references against skill and playable-class catalogs.
* [ ] Allow a compatible character to learn an unknown skill from a scroll.
* [ ] Consume a skill scroll only after successful learning.
* [ ] Reject duplicate or class-incompatible skill learning without consuming the item.
* [ ] Test successful and rejected skill-scroll use.

## 3. Equipment

* [ ] Define the weapon, armor and trinket equipment slots.
* [ ] Define external weapon data.
* [ ] Define external armor data.
* [ ] Define external trinket data.
* [ ] Create immutable weapon, armor and trinket definitions.
* [ ] Create the equipment loaders.
* [ ] Create the equipment catalogs.
* [ ] Link every equipment definition to a stable item ID.
* [ ] Validate equipment references against the item catalog.
** [ ] Prevent the same item ID from defining more than one equipment category.
* [ ] Store an integer level requirement for every equipment item.
* [ ] Reject equipment use below its level requirement.

### Weapons

* [ ] Classify every weapon as physical or magical.
* [ ] Store the weapon type and normal-attack name.
* [ ] Store a mandatory integer base weapon damage value (`DBA`).
* [ ] Support optional ATK, MAG, SP, MP, VEL and LCK bonuses.
* [ ] Reject weapons with missing or invalid base weapon damage.
* [ ] Use the equipped weapon's category and `DBA` in basic-attack calculations.
* [ ] Keep weapon `DBA` separate from effective character attributes.
* [ ] Allow every playable class to equip every weapon that satisfies the level requirement.

### Armor

* [ ] Define light, medium and heavy armor weights.
* [ ] Support optional HP, DEF, RES, VEL and LCK bonuses.
* [ ] Require the armor weight to match the playable class's weight exactly.
* [ ] Keep armor defense entirely attribute-based without introducing a separate base-armor value.

### Trinkets

* [ ] Support optional RES and LCK bonuses.
* [ ] Support an individual EXP reward multiplier for the equipped character.
* [ ] Support a party-wide gold reward multiplier from an individually equipped trinket.
* [ ] Keep individual equipment EXP modifiers separate from global EXP modifiers.
* [ ] Keep equipment-based gold modifiers separate from temporary global gold modifiers.
* [ ] Additively combine `M.EXP` with other EXP modifiers applicable to the equipped character.
* [ ] Additively combine all equipped `M.GOLD` bonuses with other global gold modifiers.
* [ ] Define consistent integer rounding for modified EXP and gold rewards.

### Equipment-data validation

* [ ] Validate equipment IDs, required fields and duplicate definitions.
* [ ] Validate positive level requirements.
* [ ] Validate that each equipment type contains only its supported bonuses.
* [ ] Validate weapon categories, weapon types and normal-attack names.
* [ ] Validate armor weights.
* [ ] Validate integer attribute bonuses.
* [ ] Validate EXP and gold reward multipliers.

### Equipment state and interface

* [ ] Equip and unequip one weapon, one armor and one trinket per playable character.
* [ ] Prevent equipment from being placed in an incompatible slot.
* [ ] Prevent the party from equipping more copies of an item than the quantity it owns.
* [ ] Prevent equipped item quantities from being sold.
* [ ] Recalculate effective attributes safely after equipment changes.
* [ ] Preserve natural attributes separately from equipment bonuses.
* [ ] Update the character's basic-attack presentation when its equipped weapon changes.
* [ ] Display equipped items, requirements, `DBA`, bonuses and multipliers in individual status.
* [ ] Integrate equipment with shops and exploration rewards.
* [ ] Test equipping, replacing and removing every equipment type.
* [ ] Test equipment level requirements.
* [ ] Test physical and magical basic attacks after weapon changes.
* [ ] Test effective attributes before and after equipment changes.
* [ ] Test equipment ownership across multiple party members.
* [ ] Test equipment purchases, rewards and sale restrictions.
* [ ] Test exact armor-weight compatibility for light, medium and heavy classes.
* [ ] Test individual EXP modifiers from equipped trinkets.
* [ ] Test party-wide gold modifiers from equipped trinkets.
* [ ] Test additive stacking between multiple reward modifiers.

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
