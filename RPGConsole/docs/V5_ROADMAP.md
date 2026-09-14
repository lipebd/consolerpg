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

> **Frozen requirement:** The game must save campaign data exclusively through INNs. The campaign must be loadable from the initial application flow. All campaign information must be stored in an encrypted file named `save.txt`.

> **Implementation boundary:** Define and validate the complete persistence schema before implementing runtime-state replacement. A failed save must preserve the previous file, and a failed load must preserve the current runtime state.

> **Checklist convention:** Checked items in the frozen-planning subsections represent completed design decisions, not implemented code. Implementation and verification items remain open until they are completed.

### Frozen persistence contract

* [x] Use a single campaign slot.
* [x] Store the campaign in a file named exactly `save.txt`.
* [x] Store `save.txt` in the application root.
* [x] Keep the save location independent from the current temporary Windows launcher.
* [x] Exclude `save.txt` from Git.
* [x] Exclude user save data from newly generated release packages.
* [x] Allow saving only through INNs.
* [x] Keep saving free of any GOLD cost.
* [x] Do not add automatic or checkpoint-based saving.
* [x] Load an existing campaign from the application flow immediately after `TitleScreen`.
* [x] Keep `TitleScreen` itself unchanged.
* [x] Preserve the previous save while playing a new campaign until that campaign is successfully saved.
* [x] Use the player-created protagonist as the permanent party leader.
* [x] Resolve the protagonist through the existing `Party.getLeader()` behavior.
* [x] Preserve party order so the protagonist remains the first party member.
* [x] Keep the current crafting system outside the persistence model because it has no independent mutable state.

### Save-data schema

* [ ] Define the complete version-1 property schema before implementing serialization.
* [ ] Represent the decrypted logical payload through Java `Properties`.
* [ ] Encode and decode the decrypted payload explicitly as UTF-8.
* [ ] Add an explicit save-format version inside the encrypted payload.
* [ ] Define stable property names and indexed collection conventions.
* [ ] Document required, optional and repeated properties.
* [ ] Reject missing required properties.
* [ ] Reject duplicated or structurally inconsistent indexed entries.
* [ ] Save every playable character in party order.
* [ ] Save each character's identity and creation data required to reconstruct it.
* [ ] Save each character's level, EXP and playable class.
* [ ] Save learned skills.
* [ ] Save current HP, MP and SP.
* [ ] Save equipped weapon and armor items.
* [ ] Save shared GOLD.
* [ ] Save every owned inventory item and quantity.
* [ ] Save the current scenario ID.
* [ ] Require the saved current scenario to be the city containing the INN used to save.
* [ ] Save discovered scenarios and world progress.
* [ ] Save explicit route and travel progress.
* [ ] Save relevant persistent exploration progress.
* [ ] Save persistent reward modifiers that must survive application restart.
* [ ] Save every generated shop offer and its remaining finite stock.
* [ ] Preserve shops whose generated offer collection is empty.
* [ ] Preserve offers whose finite stock has reached zero when required to prevent regeneration.
* [ ] Prepare the schema for completed quests, world flags and permanent unlocks after those systems exist.
* [ ] Confirm that transient battle and menu state is not serialized.
* [ ] Confirm that an active exploration session cannot exist while saving inside an INN.

### Numeric resource boundaries

> **Frozen behavior:** Resource limits must prevent overflow without silently destroying the value exchanged by the player. Free rewards may be partially granted, while operations involving a cost must remain atomic.

* [x] Set the maximum owned GOLD quantity to `999999999`.
* [x] Set the maximum inventory quantity of each item ID to `999`.
* [x] Keep the total number of different item IDs in the inventory unlimited.
* [x] Keep one quantity entry per item ID instead of creating additional stacks.
* [x] Partially grant free GOLD and item rewards when the complete amount would exceed a limit.
* [x] Explicitly report the granted and discarded portions of a partially granted reward.
* [x] Reject the complete operation when a purchase, crafting operation or other cost-bearing transaction would exceed a receiving limit.
* [x] Preserve all GOLD, items and ingredients when a cost-bearing operation is rejected.
* [x] Reject save data containing negative or out-of-range GOLD or item quantities.
* [x] Never cap or correct invalid loaded quantities silently.
* [x] Use `long` for intermediate arithmetic before validating and storing bounded `int` quantities.
* [ ] Centralize the maximum GOLD and per-item quantity values.
* [ ] Apply the GOLD limit to every normal gameplay mutation.
* [ ] Apply the per-item quantity limit to every normal gameplay mutation.
* [ ] Update free-reward flows to report partially granted and discarded quantities.
* [ ] Update shop purchases to reject limit violations before deducting GOLD.
* [ ] Update crafting to reject output-limit violations before consuming ingredients.
* [ ] Ensure inventory transfers and rollback operations never destroy items because of a quantity limit.
* [ ] Preserve mandatory quest rewards until they can be granted instead of discarding them after the quest system exists.

> **Deferred wealth extension:** A future exchange item may represent `100000000` GOLD and be bought and sold for the same amount, allowing total player wealth to exceed `Integer.MAX_VALUE` without increasing the liquid-GOLD limit. This extension is outside the current save/load implementation and would require an exchange mechanism compatible with the finite-stock shop model.

### Encrypted file container

* [x] Encrypt all campaign information stored in `save.txt`.
* [x] Use `AES/GCM/NoPadding`.
* [x] Use a stable 256-bit application key.
* [x] Keep the encryption key outside `save.txt`.
* [x] Keep the key stable across installations so a valid save can move with the game.
* [x] Accept that client-side key distribution prevents protection against determined reverse engineering.
* [x] Use a new cryptographically secure random 12-byte IV for every save operation.
* [x] Use a 128-bit GCM authentication tag.
* [x] Encode the encrypted binary content as Base64 so `save.txt` remains a textual file.
* [ ] Define a minimal unencrypted container header containing no campaign information.
* [ ] Add an explicit encryption-container version.
* [ ] Store the IV and authenticated ciphertext in the encrypted container.
* [ ] Keep the internal save-format version inside the encrypted payload.
* [ ] Reject unsupported encryption-container versions.
* [ ] Reject missing, malformed or invalid Base64 fields.
* [ ] Reject authentication failure as invalid or altered save data.
* [ ] Never log the encryption key or decrypted campaign payload.

### Atomic save operation

* [ ] Build and validate a complete save snapshot before writing.
* [ ] Serialize the snapshot into the versioned property payload.
* [ ] Encrypt the complete payload before touching the previous `save.txt`.
* [ ] Write the encrypted result to a temporary file in the application root.
* [ ] Replace `save.txt` only after the temporary file has been written successfully.
* [ ] Prefer an atomic filesystem move when supported.
* [ ] Provide a safe replacement fallback when atomic moves are unavailable.
* [ ] Preserve the previous valid save when serialization, encryption or temporary writing fails.
* [ ] Remove obsolete temporary save files safely when appropriate.
* [ ] Return an explicit result describing save success or failure.
* [ ] Display player-facing save failures without terminating the application.

### Campaign selection after the title screen

* [ ] Keep the existing `TitleScreen` presentation unchanged.
* [ ] Add a campaign-selection screen immediately after `TitleScreen`.
* [ ] Inspect, decrypt and validate `save.txt` before offering Continue.
* [ ] Display `[1] Continuar` and `[2] Novo Jogo` when a valid save exists.
* [ ] Display only `[1] Novo Jogo` when no valid campaign can be continued.
* [ ] Display a message stating that no active campaign exists when `save.txt` is absent.
* [ ] Distinguish an absent save from an existing save that cannot be loaded.
* [ ] Preserve an invalid or corrupted `save.txt` instead of deleting it automatically.
* [ ] Display a basic campaign summary when a valid save exists.
* [ ] Display the leader's name through `Party.getLeader()`.
* [ ] Display the leader's current level.
* [ ] Display the player-facing name of the saved city.
* [ ] Resolve the saved city through `ScenarioCatalog` instead of displaying its ID.
* [ ] Display the class icons of all saved party members in party order.
* [ ] Support a summary containing between one and four class icons.
* [ ] Avoid activating the saved campaign merely to produce its summary.

### New Game behavior

* [ ] Start a new campaign directly when no previous `save.txt` exists.
* [ ] Ask for confirmation before starting a new campaign when a previous save file exists.
* [ ] Explain that the previous campaign will be lost only if the new campaign is later saved.
* [ ] Allow cancellation and return to campaign selection.
* [ ] Keep the previous `save.txt` untouched after New Game is confirmed.
* [ ] Keep the previous campaign loadable if the new game is closed before its first save.
* [ ] Replace the previous save only after the new campaign completes a successful save operation.
* [ ] Never delete the previous save as a preliminary step before saving its replacement.

### Loading and validation

* [ ] Read and validate the encrypted container before decrypting its payload.
* [ ] Authenticate and decrypt the payload before parsing campaign properties.
* [ ] Reject unsupported save-format versions.
* [ ] Validate every loaded class, skill, item, equipment, scenario and world ID against the current catalogs.
* [ ] Validate party size as a minimum of one and maximum of four characters.
* [ ] Validate that the first restored member is the player-created protagonist and party leader.
* [ ] Preserve the saved party order.
* [ ] Validate character levels, EXP and current HP, MP and SP.
* [ ] Validate learned skills against character class and progression rules.
* [ ] Validate equipped-item ownership and compatibility.
* [ ] Validate saved GOLD and inventory quantities.
* [ ] Validate generated shop offers and remaining stock.
* [ ] Reject negative, overflowing or out-of-range numeric values.
* [ ] Reject unknown, missing or duplicated IDs.
* [ ] Reject structurally inconsistent collection counts and indexes.
* [ ] Reject a saved current scenario that is not a valid city associated with an INN.
* [ ] Construct a complete candidate runtime state before replacing the active state.
* [ ] Prevent partial state changes when any loading stage fails.
* [ ] Preserve the current runtime state when a load attempt is rejected.
* [ ] Display a clear player-facing reason when the campaign cannot be loaded.

### INN save flow

* [ ] Add save access to every INN.
* [ ] Display `Salvar e continuar`.
* [ ] Display `Salvar e encerrar`.
* [ ] Allow the player to return without saving.
* [ ] Charge no GOLD for either save option.
* [ ] Make both options use the same atomic save operation.
* [ ] Display a success message after `Salvar e continuar`.
* [ ] Return to the appropriate INN flow after `Salvar e continuar`.
* [ ] Display a success message after `Salvar e encerrar`.
* [ ] Terminate the application only after `Salvar e encerrar` succeeds.
* [ ] Keep the application running when `Salvar e encerrar` fails.
* [ ] Never overwrite the previous save when the new operation fails.

### Exit-without-saving flow

* [ ] Find every gameplay menu that currently displays `[0] Encerrar Jogo`.
* [ ] Replace direct termination with a confirmation step.
* [ ] Explain that campaign progress after the last save will be lost.
* [ ] Allow the player to cancel and return to the previous menu.
* [ ] Terminate without saving after explicit confirmation.
* [ ] Leave the existing `save.txt` unchanged when exiting without saving.
* [ ] Display the warning consistently even when no unsaved-change tracker exists.
* [ ] Do not add implicit saving to ordinary exit operations.

### Verification

* [ ] Test the first launch without `save.txt`.
* [ ] Test the campaign-selection screen without an active campaign.
* [ ] Test saving and continuing from every INN.
* [ ] Test saving and terminating from every INN.
* [ ] Test loading after restarting the application.
* [ ] Test the displayed campaign summary with one through four party members.
* [ ] Test leader name, level, city name and class icons in the summary.
* [ ] Test cancelling New Game when a previous save exists.
* [ ] Test starting New Game while preserving the previous save.
* [ ] Test closing the new campaign before saving and then continuing the previous campaign.
* [ ] Test replacing the previous save with the first successful save of a new campaign.
* [ ] Test cancelling exit without saving.
* [ ] Test confirming exit without saving.
* [ ] Test missing, empty, truncated and malformed save files.
* [ ] Test altered ciphertext and GCM authentication failure.
* [ ] Test unsupported encryption-container versions.
* [ ] Test unsupported save-format versions.
* [ ] Test unknown catalog IDs and inconsistent character data.
* [ ] Test GOLD and per-item quantity boundaries.
* [ ] Test numeric overflow attempts and invalid saved resource values.
* [ ] Test save and load with equipped items and learned skills.
* [ ] Test save and load with world, travel and exploration progress.
* [ ] Test save and load with generated shop offers and depleted finite stock.
* [ ] Test failure while replacing an existing save.
* [ ] Confirm that a failed save preserves the previous valid file.
* [ ] Confirm that a failed load does not partially modify runtime state.
* [ ] Confirm that `save.txt` is not tracked by Git.
* [ ] Confirm that a newly generated portable build contains no user save.
* [ ] Verify save creation and loading in the portable Windows build.

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
