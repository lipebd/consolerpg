# RPGConsole V5 Roadmap

> **Planning status:** Part 1 records the completed integration of the contributed crafting system. Parts 2 through 5 preserve requirements transferred from the post-V4 backlog and remain open to refinement before their implementation boundaries are frozen.

## Release direction

V5 begins by integrating data-driven crafting into the V4 architecture while preserving the project's existing catalog, inventory, world and interface conventions.

The remaining V5 scope continues with save and load, world-progression decisions, deferred validation work and optional unit-testing study.

The implementation should continue to favor:

* External data for content definitions.
* Stable IDs and catalog-based reference resolution.
* Immutable definitions separated from mutable runtime state.
* Strict startup validation.
* Atomic state-changing operations.
* Explicit player-facing failure messages.
* Incremental implementation and regression testing.
* Eclipse as the preferred interface for editing, compiling and running the Java project.

---

## 1. Data-driven crafting

### Recipe definitions

* [x] Adapt the contributed recipe definition into `CraftingRecipe`.
* [x] Give every recipe a stable ID.
* [x] Store a player-facing recipe name.
* [x] Store a narrative recipe description.
* [x] Store the output item ID.
* [x] Store one or more required ingredient IDs and quantities.
* [x] Represent recipe ingredients through `CraftingMaterial`.
* [x] Keep recipe definitions immutable after loading.
* [x] Allow ingredients from any item category.
* [x] Allow outputs from any item category.
* [x] Produce exactly one output item per successful crafting operation.
* [x] Load recipe definitions from `data/crafting_recipes.txt`.
* [x] Resolve every ingredient and output ID through `ItemCatalog`.
* [x] Reject duplicate recipe IDs.
* [x] Reject recipes with missing output items.
* [x] Reject recipes with missing ingredient items.
* [x] Reject recipes without ingredients.
* [x] Reject zero or negative ingredient quantities.
* [x] Reject malformed recipe records during startup.
* [x] Store loaded definitions in `CraftingCatalog`.

### Crafter definitions

* [x] Adapt the contributed crafter definition into `CrafterDefinition`.
* [x] Give every crafter a stable ID.
* [x] Store the crafter's player-facing name and description.
* [x] Associate every crafter with a city.
* [x] Associate every crafter with an ordered list of recipe IDs.
* [x] Keep crafter definitions immutable after loading.
* [x] Load crafter definitions from `data/crafters.txt`.
* [x] Resolve every configured recipe through `CraftingCatalog`.
* [x] Reject duplicate crafter IDs.
* [x] Reject unknown city IDs.
* [x] Reject unknown recipe IDs.
* [x] Reject crafters without recipes.
* [x] Reject malformed crafter records during startup.
* [x] Store loaded definitions in `CrafterCatalog`.
* [x] Support multiple crafting NPCs in the same city.
* [x] Keep each crafter's recipe collection independent.

### Crafting transaction

* [x] Adapt the contributed crafting operation into `CraftingService`.
* [x] Create `CraftingResult` to represent the outcome of one crafting attempt.
* [x] Resolve recipes and items through their catalogs instead of duplicating definitions.
* [x] Check the party inventory for every required ingredient before mutating it.
* [x] Report every insufficient ingredient without changing the inventory.
* [x] Remove ingredients only after the complete recipe has been validated.
* [x] Add exactly one output item after successful ingredient removal.
* [x] Keep one crafting operation atomic from the player's perspective.
* [x] Prevent partial ingredient consumption when crafting cannot be completed.
* [x] Restore removed ingredients if output insertion unexpectedly fails.
* [x] Reuse the existing inventory mutation behavior.
* [x] Reject quantity changes that would overflow existing inventory limits.
* [x] Keep crafting independent from item trading and shop prices.
* [x] Keep crafting independent from the `MATERIAL` item category.

### Crafting interface

* [x] Adapt the contributed menu into `CraftingMenu`.
* [x] Display the selected crafter's name and description.
* [x] Display only the recipes configured for the selected crafter.
* [x] Display the selected recipe's name and narrative description.
* [x] Preserve the recipe order declared by the crafter definition.
* [x] Display the output item using data resolved from `ItemCatalog`.
* [x] Display every required ingredient and quantity.
* [x] Display the quantity currently owned for every ingredient.
* [x] Allow the player to inspect a recipe before confirming fabrication.
* [x] Allow the player to cancel recipe selection.
* [x] Allow the player to cancel the final crafting confirmation.
* [x] Display a clear success message after crafting.
* [x] Display which ingredients are insufficient after a failed attempt.
* [x] Return to the crafter's recipe list after completing or cancelling an attempt.
* [x] Keep input parsing and console presentation outside `CraftingService`.

### World integration

* [x] Load crafting recipes during world initialization.
* [x] Load crafter definitions after their referenced catalogs are available.
* [x] Expose the crafting catalogs through `World`.
* [x] Add the crafting service to the application composition root.
* [x] Display available crafting NPCs in the current city.
* [x] Open the crafting menu for the selected NPC.
* [x] Support more than one crafting NPC in one city.
* [x] Preserve the normal city menu when no crafter is available.
* [x] Preserve all V4 world initialization and gameplay behavior.
* [x] Include the new external data files in the portable Windows build.

### Initial content

* [x] Add an initial weapon crafter.
* [x] Add an initial alchemist.
* [x] Add initial crafting recipes for both NPCs.
* [x] Add initial `MATERIAL` item definitions.
* [x] Demonstrate that a recipe may consume `CONSUMABLE` items.
* [x] Demonstrate that a recipe may produce an existing catalog item.
* [x] Preserve declared recipe order in each crafter's menu.
* [ ] Review crafting names, descriptions and requirements against the project's canonical setting and intended balance.
* [x] Document that some initial crafting materials do not yet have a normal acquisition path.

### Documentation, validation and contribution preservation

* [x] Document the formats of `crafting_recipes.txt` and `crafters.txt`.
* [x] Document the crafting package and its architectural responsibilities.
* [x] Document the relationship between catalogs, the service and the menu.
* [x] Credit the original contributor whose implementation formed the basis of the system.
* [x] Preserve the useful abstractions and behavior of the contributed implementation.
* [x] Adapt the contribution to the project's naming, validation and package conventions.
* [x] Validate malformed crafting and crafter data during startup.
* [x] Test successful crafting.
* [x] Test crafting with insufficient ingredients.
* [x] Test cancellation during recipe selection.
* [x] Test cancellation during final confirmation.
* [x] Test crafting with quantities greater than the required amount.
* [x] Confirm that crafting consumes the exact required quantities.
* [x] Confirm that failed crafting leaves the inventory unchanged.
* [x] Complete general V4 regression after modifying `Main` and `World`.
* [x] Verify the portable Windows build with the new data files.

---

## 2. Save and load

> **Planning required:** Freeze the persistence boundary and numeric resource rules before implementing serialization. Loading must be validated completely before replacing the current runtime state.

### Persistence contract

* [ ] Define the save-file format.
* [ ] Add an explicit save-format version.
* [ ] Define where save files are stored.
* [ ] Decide whether the game supports one save slot or multiple slots.
* [ ] Decide whether saving is manual, automatic or checkpoint-based.
* [ ] Define how missing, unsupported and newer save-format versions are reported.
* [ ] Define the maximum owned gold quantity.
* [ ] Define the maximum owned quantity of each item.
* [ ] Keep total inventory capacity unlimited while enforcing numeric quantity limits.
* [ ] Decide whether gains exceeding a limit are rejected, capped or partially granted.
* [ ] Prevent integer overflow during gold and inventory quantity changes.

### Saved state

* [ ] Save party identity and title when the party-title system exists.
* [ ] Save every playable character.
* [ ] Save each character's level, EXP and class.
* [ ] Save learned skills.
* [ ] Save current HP, MP and SP.
* [ ] Save shared gold and inventory.
* [ ] Save equipped items.
* [ ] Save the current scenario ID.
* [ ] Save world and scenario discovery.
* [ ] Save relevant exploration progress.
* [ ] Save generated shop offers and their remaining finite stock.
* [ ] Save completed quests, world flags and explicit route unlocks after those systems exist.
* [x] Confirm that the current crafting system has no independent mutable state beyond the shared inventory.

### Loading and validation

* [ ] Validate every loaded class, skill, item, equipment, scenario and world ID against the current catalogs.
* [ ] Validate saved gold and item quantities before restoring state.
* [ ] Reject negative or out-of-range resource quantities.
* [ ] Reject invalid character levels, EXP and current resource values.
* [ ] Reject incompatible equipment assignments.
* [ ] Reject or safely report malformed and unsupported save data.
* [ ] Construct and validate a complete candidate state before replacing the active state.
* [ ] Prevent partial state changes when loading fails.
* [ ] Preserve the current game state when a load attempt is rejected.

### Player flow and verification

* [ ] Add New Game and Continue options to the title screen.
* [ ] Disable or clearly report Continue when no valid save exists.
* [ ] Display clear success and failure messages for save operations.
* [ ] Test saving and loading after restarting the application.
* [ ] Test missing and malformed save files.
* [ ] Test unsupported save-format versions.
* [ ] Test gold and per-item quantity boundaries.
* [ ] Test overflow attempts and invalid saved resource values.
* [ ] Test save and load with equipped items, learned skills and finite shop stock.
* [ ] Verify save compatibility in the portable Windows build.

---

## 3. Exploration and world progression

### Exploration behavior decisions

* [ ] Decide whether some special areas guarantee a specific event.
* [ ] Evaluate exploration checkpoints.
* [ ] Evaluate camps and temporary recovery.
* [ ] Decide how completed dungeons behave when revisited.
* [ ] Define which exploration state persists between application executions.
* [ ] Align persistent exploration state with the save-file contract.

### Level-7 class change

> **Deferred design:** The complete class-path flow must be defined before implementation. Its equipment consequence is already frozen.

* [ ] Define the complete level-7 class-path decision.
* [ ] Define where and when the decision becomes available.
* [ ] Define the consequences of changing class or remaining a Flagelo.
* [ ] Return all three equipped items to inventory before applying the class decision.
* [ ] Grant the chosen class's level-7 starter weapon and armor, including when the character remains a Flagelo.
* [ ] Display narrative guidance telling the player to open the equipment menu and equip the new set.
* [ ] Prevent the class decision from being applied more than once to the same character.
* [ ] Prepare the chosen class path for save data.

### World flags and access requirements

> **Deferred:** Implement these requirements only after quests and relevant progression systems provide real state to evaluate. Do not introduce placeholder access abstractions before then.

* [ ] Create stable world-progress flags for quests, story events and permanent unlocks.
* [ ] Define reusable access requirements for routes, branches, portals and services.
* [ ] Evaluate access requirements against party level, inventory, completed quests and world flags.
* [ ] Support multiple simultaneous requirements for the same access point.
* [ ] Decide whether unmet requirements hide an action or display it as blocked with a player-facing reason.
* [ ] Replace blanket route unlocking with explicit progress triggers where required.
* [ ] Prepare world flags, completed quests and route unlocks for save data.
* [ ] Test access requirements with every supported requirement type.
* [ ] Test combinations of multiple simultaneous requirements.
* [ ] Test persistence of permanent unlocks after restarting the application.

---

## 4. Deferred validation and maintainability

> Crafting-specific validation is tracked in Part 1. Save/load and world-progression validation remain with their respective feature sections.

### Gameplay regression

* [ ] Test malformed skill and playable-class data.
* [x] Test battles after skill, effect, class and equipment integration.
* [x] Test party rewards and level-ups after class progression.
* [ ] Test equipment changes during normal progression.
* [x] Test character creation.
* [x] Test full-party rescue.
* [ ] Test the future level-7 class decision and equipment replacement flow.

### Data validation consistency

* [ ] Standardize remaining external-data loaders so validation errors report the source file, line number and content ID whenever available.
* [ ] Standardize duplicate-ID reporting across catalogs.
* [ ] Standardize unknown-reference reporting across external data files.
* [ ] Confirm that every loader rejects invalid data before partially populating its catalog.

### Maintainability review

* [ ] Review compact functional-style code and replace it with clearer imperative code when this improves maintainability without changing behavior.
* [ ] Review duplicated parsing and validation patterns after all V5 data formats are stable.
* [ ] Keep presentation, orchestration, immutable definitions and mutable runtime state separated.
* [ ] Update architecture, mechanics, data-format and project-state documentation as V5 systems are frozen.

---

## 5. Optional unit-testing study

> **Optional:** This part does not block V5 unless automated testing is explicitly adopted as a release requirement.

* [ ] Study the purpose and structure of unit tests.
* [ ] Evaluate adding JUnit to the project.
* [ ] Create a small isolated unit-test suite as a learning exercise.
* [ ] Compare automated unit tests with the project's existing manual functional and regression tests.
* [ ] Evaluate crafting transactions and save-data validation as possible first automated-test subjects.
