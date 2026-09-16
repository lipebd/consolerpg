# RPGConsole V5 Roadmap

> **Planning status:** Parts 1 and 2 record the completed crafting integration and save-and-load implementation. Parts 3 through 5 preserve requirements transferred from the post-V4 backlog and remain open to refinement before their implementation boundaries are frozen.

## Release direction

V5 began by integrating data-driven crafting into the V4 architecture while preserving the project's existing catalog, inventory, world and interface conventions.

Save and load are complete. The remaining V5 scope continues with world-progression decisions, deferred validation work and optional unit-testing study.

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

* [x] Define the complete version-1 property schema before implementing serialization.
* [x] Represent the decrypted logical payload through Java `Properties`.
* [x] Encode and decode the decrypted payload explicitly as UTF-8.
* [x] Add an explicit save-format version inside the encrypted payload.
* [x] Define stable property names and indexed collection conventions.
* [x] Document required, optional and repeated properties.
* [x] Reject missing required properties.
* [x] Reject duplicated or structurally inconsistent indexed entries.
* [x] Save every playable character in party order.
* [x] Save each character's identity and creation data required to reconstruct it.
* [x] Save each character's level, EXP and playable class.
* [x] Save learned skills.
* [x] Save current HP, MP and SP.
* [x] Save equipped weapon, armor and trinket item IDs, including empty slots.
* [x] Save shared GOLD.
* [x] Save every owned inventory item and quantity.
* [x] Save the current scenario ID.
* [x] Require the saved current scenario to be the city containing the INN used to save.
* [x] Save discovered scenarios and world progress.
* [x] Save explicit route and travel progress.
* [x] Exclude transient exploration-session position and event flags from the save payload.
* [x] Save persistent reward modifiers that must survive application restart.
* [x] Save every generated shop offer and its remaining finite stock.
* [x] Preserve shops whose active offers all have zero remaining stock.
* [x] Preserve offers whose finite stock has reached zero when required to prevent regeneration.
* [x] Prepare the schema for completed quests, world flags and permanent unlocks after those systems exist.
* [x] Confirm that transient battle and menu state is not serialized.
* [x] Confirm that an active exploration session cannot exist while saving inside an INN.

### Character-level boundary

> **Frozen behavior:** The current playable-character progression ceiling is level 20. This value must have one shared source of truth so future content may raise the ceiling without requiring scattered code changes.

* [x] Set the minimum playable-character level to `1`.
* [x] Set the current maximum playable-character level to `20`.
* [x] Require current EXP to be non-negative and lower than the next-level requirement at levels 1 through 19.
* [x] Keep current EXP at `0` while a character is at level 20.
* [x] Discard EXP exceeding the amount required to reach level 20.
* [x] Ignore further EXP gains while a character remains at the maximum level.
* [x] Allow saves created at the previous maximum to remain valid when the maximum level is increased.
* [x] Centralize the minimum and maximum playable-character levels in one domain rule.
* [x] Make `PlayableCharacter` stop progression at the shared maximum level.
* [x] Prevent EXP arithmetic overflow before it mutates character progression.
* [x] Display `Nível máximo` instead of next-level progress at the maximum level.
* [x] Reject skill and equipment definitions whose level requirements exceed the supported maximum.
* [x] Validate saved level and current EXP against the shared progression rules.
* [x] Test reaching level 20 with exact and excess EXP.
* [x] Test additional EXP rewards at level 20.
* [x] Test save and load at levels 1, 19 and 20.

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
* [x] Centralize the maximum GOLD and per-item quantity values.
* [x] Apply the GOLD limit to every normal gameplay mutation.
* [x] Apply the per-item quantity limit to every normal gameplay mutation.
* [x] Update free-reward flows to report partially granted and discarded quantities.
* [x] Update shop purchases to reject limit violations before deducting GOLD.
* [x] Update crafting to reject output-limit violations before consuming ingredients.
* [x] Ensure inventory transfers and rollback operations never destroy items because of a quantity limit.
* [ ] Preserve mandatory quest rewards until they can be granted instead of discarding them after the quest system exists.

> **Deferred wealth extension:** A future exchange item may represent `100000000` GOLD and be bought and sold for the same amount, allowing total player wealth to exceed `Integer.MAX_VALUE` without increasing the liquid-GOLD limit. This extension is outside the current save/load implementation and would require an exchange mechanism compatible with the finite-stock shop model.

### World time and shop restocking

> **Frozen behavior:** Shop restocking is controlled exclusively by a persistent fictional world clock. A new campaign starts on day 1 at 08:00. Only a battle completed with victory or defeat advances world time, currently by one hour. Escaping or avoiding an encounter does not advance time. Every midnight replaces all registered shop states through complete new weighted offer selections.

* [x] Add a fictional world clock to the mutable campaign state.
* [x] Persist the world-clock state in `save.txt`.
* [x] Keep world time unchanged when opening menus, visiting shops, saving, loading or restarting the application.
* [x] Make shop restocking depend on world-time progression instead of shop visits.
* [x] Restock all registered shops globally at each midnight.
* [x] Replace every shop's active offers through a complete new weighted selection.
* [x] Restore the initial stock of every offer selected for the new cycle.
* [x] Do not preserve old offers or merge depleted offers with newly selected entries during restocking.
* [x] Keep shop offers unchanged while the player is interacting with an open shop menu.
* [x] Restore the saved world time and shop states without generating new offers during loading.
* [x] Start every new campaign on day 1 at 08:00.
* [x] Advance world time only after a battle ends in victory or defeat.
* [x] Advance zero hours after escaping or avoiding an encounter.
* [x] Advance one hour per currently supported finished-battle event.
* [x] Centralize the finished-battle duration through `HOURS_PER_FINISHED_BATTLE`.
* [x] Pass the applicable hour quantity to the general world-clock advancement operation.
* [x] Use a 24-hour day and perform global shop restocking whenever an advancement crosses midnight.
* [x] Display the current day and time above city and combat-scenario action menus.
* [x] Display city and combat-scenario actions horizontally across two rows.
* [x] Display the next restocking day and time only inside the shop's main menu.
* [x] Display `☼` from 06:00 through 17:59 and `☾` from 18:00 through 05:59.
* [x] Do not display time advancement as a battle reward or separate post-battle message.
* [x] Add the mutable world-clock state after its progression rules are frozen.
* [x] Store elapsed world hours as the authoritative clock value.
* [x] Derive the displayed day and hour from elapsed world hours.
* [x] Reject non-positive advancement amounts and arithmetic overflow.
* [x] Make `ShopStateGenerator` available for both new-game initialization and runtime restocking.
* [x] Generate a complete replacement `ShopStateCatalog` before modifying the active world state.
* [x] Replace the active shop-state catalog only after complete generation succeeds.
* [x] Preserve the previous world time and shop states when replacement generation fails.
* [x] Ensure one world-time advancement cannot process the same midnight boundary more than once.
* [x] Save world time and active shop states in the same campaign snapshot.
* [x] Test world-time progression after victory and defeat.
* [x] Test that escape and avoided encounters do not advance world time.
* [x] Test that menus, shops, saving, loading and application restart do not advance world time.
* [x] Test the exact moment of a global shop restock.
* [x] Test that every shop receives one complete new offer selection.
* [x] Test the sun and moon boundaries at 06:00 and 18:00.
* [x] Test saving and loading immediately before and after a restocking boundary.
* [x] Test that loading never causes an additional restock.

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
* [x] Define a minimal unencrypted container header containing no campaign information.
* [x] Add an explicit encryption-container version.
* [x] Store the IV and authenticated ciphertext in the encrypted container.
* [x] Keep the internal save-format version inside the encrypted payload.
* [x] Reject unsupported encryption-container versions.
* [x] Reject missing, malformed or invalid Base64 fields.
* [x] Reject authentication failure as invalid or altered save data.
* [x] Never log the encryption key or decrypted campaign payload.

### Atomic save operation

* [x] Build and validate a complete save snapshot before writing.
* [x] Serialize the snapshot into the versioned property payload.
* [x] Encrypt the complete payload before touching the previous `save.txt`.
* [x] Write the encrypted result to a temporary file in the application root.
* [x] Replace `save.txt` only after the temporary file has been written successfully.
* [x] Prefer an atomic filesystem move when supported.
* [x] Provide a safe replacement fallback when atomic moves are unavailable.
* [x] Preserve the previous valid save when serialization, encryption or temporary writing fails.
* [x] Remove obsolete temporary save files safely when appropriate.
* [x] Return an explicit result describing save success or failure.
* [x] Display player-facing save failures without terminating the application.

### Campaign selection after the title screen

* [x] Keep the existing `TitleScreen` presentation unchanged.
* [x] Add a campaign-selection screen immediately after `TitleScreen`.
* [x] Inspect, decrypt and validate `save.txt` before offering Continue.
* [x] Display `[1] Continuar` and `[2] Novo Jogo` when a valid save exists.
* [x] Display only `[1] Novo Jogo` when no valid campaign can be continued.
* [x] Display a message stating that no active campaign exists when `save.txt` is absent.
* [x] Distinguish an absent save from an existing save that cannot be loaded.
* [x] Preserve an invalid or corrupted `save.txt` instead of deleting it automatically.
* [x] Display a basic campaign summary when a valid save exists.
* [x] Display the leader's name through `Party.getLeader()`.
* [x] Display the leader's current level.
* [x] Display the player-facing name of the saved city.
* [x] Resolve the saved city through `ScenarioCatalog` instead of displaying its ID.
* [x] Display the class icons of all saved party members in party order.
* [x] Support a summary containing between one and four class icons.
* [x] Avoid activating the saved campaign merely to produce its summary.

### New Game behavior

* [x] Start a new campaign directly when no previous `save.txt` exists.
* [x] Ask for confirmation before starting a new campaign when a previous save file exists.
* [x] Explain that the previous campaign will be lost only if the new campaign is later saved.
* [x] Allow cancellation and return to campaign selection.
* [x] Keep the previous `save.txt` untouched after New Game is confirmed.
* [x] Keep the previous campaign loadable if the new game is closed before its first save.
* [x] Replace the previous save only after the new campaign completes a successful save operation.
* [x] Never delete the previous save as a preliminary step before saving its replacement.

### Loading and validation

* [x] Read and validate the encrypted container before decrypting its payload.
* [x] Authenticate and decrypt the payload before parsing campaign properties.
* [x] Reject unsupported save-format versions.
* [x] Validate every loaded class, skill, item, equipment, scenario and world ID against the current catalogs.
* [x] Validate party size as a minimum of one and maximum of four characters.
* [x] Validate that the first restored member is the player-created protagonist and party leader.
* [x] Preserve the saved party order.
* [x] Validate character levels, EXP and current HP, MP and SP.
* [x] Validate learned skills against character class and progression rules.
* [x] Validate equipped-item ownership and compatibility.
* [x] Validate saved GOLD and inventory quantities.
* [x] Validate generated shop offers and remaining stock.
* [x] Reject negative, overflowing or out-of-range numeric values.
* [x] Reject unknown, missing or duplicated IDs.
* [x] Reject structurally inconsistent collection counts and indexes.
* [x] Reject a saved current scenario that is not a valid city associated with an INN.
* [x] Construct a complete candidate runtime state before replacing the active state.
* [x] Prevent partial state changes when any loading stage fails.
* [x] Preserve the current runtime state when a load attempt is rejected.
* [x] Display a clear player-facing reason when the campaign cannot be loaded.

### INN save flow

* [x] Add save access to every INN.
* [x] Display `Salvar e continuar`.
* [x] Display `Salvar e encerrar`.
* [x] Allow the player to return without saving.
* [x] Charge no GOLD for either save option.
* [x] Make both options use the same atomic save operation.
* [x] Display a success message after `Salvar e continuar`.
* [x] Return to the appropriate INN flow after `Salvar e continuar`.
* [x] Display a success message after `Salvar e encerrar`.
* [x] Terminate the application only after `Salvar e encerrar` succeeds.
* [x] Keep the application running when `Salvar e encerrar` fails.
* [x] Never overwrite the previous save when the new operation fails.

### Exit-without-saving flow

* [x] Find every gameplay menu that currently displays `[0] Encerrar Jogo`.
* [x] Replace direct termination with a confirmation step.
* [x] Explain that campaign progress after the last save will be lost.
* [x] Allow the player to cancel and return to the previous menu.
* [x] Terminate without saving after explicit confirmation.
* [x] Leave the existing `save.txt` unchanged when exiting without saving.
* [x] Display the warning consistently even when no unsaved-change tracker exists.
* [x] Do not add implicit saving to ordinary exit operations.

### Save-state documentation

* [x] Create `docs/SAVE_STATE.md` as the detailed persistence reference.
* [x] Mark the document as a planned contract until save and load are implemented.
* [x] Separate persisted, derived and transient runtime state.
* [x] Document the encrypted outer container independently from the decrypted property schema.
* [x] Map every persisted property to its domain source and validation rules.
* [x] Document the required reconstruction and validation order.
* [x] Document the campaign-selection, New Game, Continue, INN-save and exit-without-saving flows.
* [x] Document numeric resource and playable-level boundaries.
* [x] Document world-time and shop-restocking persistence.
* [x] Document save-format and encryption-container versioning.
* [x] Add guidance for integrating future quests, flags and permanent unlocks.
* [x] Keep real encryption keys and real decrypted campaign data out of documentation.
* [x] Add concise references from `ARCHITECTURE.md`, `DATA_FORMATS.md`, `MECHANICS.md` and `PROJECT_STATE.md` as implementation milestones are completed.

### Verification

> **Verification convention:** Tests focus on distinct behavior, relevant boundaries and meaningful failure modes. Data variations that use the same validated runtime flow are covered through representative integration tests and catalog validation instead of repetitive per-entry testing.

* [x] Test startup and campaign selection without `save.txt`.
* [x] Test saving, continuing, terminating and loading through the shared INN flow.
* [x] Confirm through catalog validation that every registered city resolves exactly one INN.
* [x] Test the campaign summary with the minimum and maximum supported party sizes.
* [x] Test leader name, level, city name and party class icons in the summary.
* [x] Test cancelling, starting and abandoning New Game while preserving the previous save.
* [x] Test replacing the previous save only after the new campaign is successfully saved.
* [x] Test cancelling and confirming exit without saving.
* [x] Test representative absent, malformed, tampered and unsupported-version save files.
* [x] Test catalog-backed rejection of unknown IDs and inconsistent character data.
* [x] Test saved numeric boundaries and overflow rejection.
* [x] Test one complete campaign round trip containing equipment, learned skills, world progress, travel progress, world time and depleted shop stock.
* [x] Test that a failed save replacement preserves the previous valid file.
* [x] Confirm that a failed load does not partially modify runtime state.
* [x] Confirm that runtime output and `save.txt` are ignored by Git.
* [x] Verify that the portable Windows build contains no bundled save and supports save creation and loading.

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
