# RPGConsole — Save State

> **Planning status:** This document defines the planned V5 persistence contract. Save and load are not implemented yet. Confirmed rules are authoritative for implementation; explicitly pending decisions remain open until their gameplay integration is designed.

## Purpose

This document is the detailed reference for RPGConsole campaign persistence.

It separates:

* Persistent campaign state.
* Values reconstructed from external definitions.
* Temporary runtime state that must not be saved.
* Encrypted file structure.
* Decrypted property structure.
* Validation and reconstruction order.
* Player-facing save and load behavior.
* Versioning and future-feature integration.

`ARCHITECTURE.md`, `DATA_FORMATS.md`, `MECHANICS.md` and `PROJECT_STATE.md` will contain shorter summaries after the corresponding behavior is implemented.

## Academic requirement

The game must:

* Provide saving through INNs.
* Allow loading from the initial application flow.
* Store save information in `save.txt`.
* Encrypt the saved information.

The project implements this requirement as one encrypted campaign slot.

## Core persistence principles

* External `.txt` files remain the source of immutable game definitions.
* Save data stores stable IDs and mutable campaign values.
* Java runtime objects are not serialized directly.
* Derived values are recalculated from current canonical definitions.
* Loading validates a complete candidate state before creating `World`.
* Failed saving preserves the previous valid file.
* Failed loading never creates a partially restored campaign.
* Save and load never depend on object identity from a previous execution.

## Save-file location

The campaign file is named exactly:

```text
save.txt
```

It is stored in the application root.

In the current portable Windows distribution, it is located beside `RPGConsole.cmd`. A future launcher or executable may replace the command file without changing the save location.

`save.txt` must:

* Be ignored by Git.
* Be absent from newly generated release packages.
* Be created only after a successful INN save.
* Never be deleted merely because the player starts a new campaign.

## Campaign-slot behavior

The game supports one campaign slot.

Starting a new campaign while `save.txt` exists does not immediately delete or alter the previous campaign. The existing file remains loadable until the new campaign is successfully saved.

A failed first save from the new campaign preserves the previous campaign.

## Initial application flow

The normal application flow is:

1. Load and validate all immutable external definitions and catalogs.
2. Create campaign-independent services.
3. Display the unchanged `TitleScreen`.
4. Inspect `save.txt`.
5. Display the campaign-selection screen.
6. Create a new campaign or reconstruct the saved campaign.
7. Construct `World` once with the selected state.
8. Start the world loop.

Mutable new-campaign state must not be generated before the player selects New Game.

### Valid saved campaign

When a valid save exists, the campaign-selection screen displays:

* Leader name.
* Leader level.
* Saved city name.
* Class symbols for all party members in party order.
* `[1] Continuar`.
* `[2] Novo Jogo`.

The summary contains between one and four class symbols.

The saved city name and class symbols are resolved through current catalogs. Raw internal IDs are not displayed to the player.

Inspecting the save for this summary must not activate or partially install the campaign.

### Missing save

When `save.txt` does not exist:

* The game states that no active campaign exists.
* Only `[1] Novo Jogo` is displayed.

### Invalid save

When `save.txt` exists but cannot be authenticated, decrypted, parsed or validated:

* Continue is unavailable.
* The game reports that the saved campaign could not be loaded.
* New Game remains available.
* The invalid file is not deleted automatically.

### New Game confirmation

When an existing save would eventually be replaced, selecting New Game displays a confirmation explaining that:

* Starting the new campaign does not immediately remove the old save.
* Saving the new campaign will permanently replace the previous campaign.

Cancelling returns to campaign selection.

Confirming begins the new campaign while preserving the existing `save.txt`.

### Development-party flow

`--development-party` changes only the New Game campaign factory.

With this argument:

* `TitleScreen` and campaign selection still appear.
* Continue loads the existing campaign normally.
* New Game follows the normal replacement confirmation.
* Confirmed New Game creates `DevelopmentPartyFactory` state instead of opening protagonist creation.
* The resulting campaign uses the same save and load system.

No separate development save format or file is created.

## INN save flow

Saving is available in every INN and costs no GOLD.

The INN provides:

```text
[1] Descansar
[2] Salvar e continuar
[3] Salvar e encerrar
[0] Voltar
```

Resting remains independent from saving and retains its configured GOLD cost.

### Save and continue

`Salvar e continuar`:

1. Builds and validates the complete campaign snapshot.
2. Writes the new save atomically.
3. Displays a success message.
4. Returns to the INN flow.

If saving fails, the game remains open and reports the failure.

### Save and exit

`Salvar e encerrar`:

1. Builds and validates the complete campaign snapshot.
2. Writes the new save atomically.
3. Displays a success message.
4. Terminates the world loop.

The application terminates only after saving succeeds.

### Exit without saving

Every gameplay option that currently exits directly must request confirmation.

The confirmation explains that campaign progress after the last successful save will be lost.

Confirming:

* Terminates without saving.
* Leaves the existing `save.txt` unchanged.

Cancelling returns to the previous gameplay flow.

The warning is displayed consistently without requiring an unsaved-change tracker.

## Party ownership

The player-created protagonist is permanently the party leader.

`Party.getLeader()` returns the first party member. Party order is therefore persistent state.

A valid saved party must:

* Contain between one and four members.
* Contain the protagonist as its first member.
* Contain no null members.
* Contain no repeated character names under `CharacterNameRules`.
* Preserve member order during reconstruction.

`Party.removeMember()` is an unused remnant of the discarded mutable-party design and is not part of the persistence contract.

## Persisted state inventory

### Party

Persist:

* Shared GOLD.
* Ordered playable-character collection.
* Ordered inventory contents.
* Temporary party-wide EXP modifier percentage.
* Remaining rewarded victories for the EXP modifier.
* Temporary party-wide GOLD modifier percentage.
* Remaining rewarded victories for the GOLD modifier.

Do not persist derived equipment reward totals.

### Playable character

Persist for every member:

* Name.
* Playable-class ID.
* Level.
* Current EXP within the level.
* Ordered learned-skill IDs.
* Equipped weapon item ID or an empty slot.
* Equipped armor item ID or an empty slot.
* Equipped trinket item ID or an empty slot.
* Current HP.
* Current MP.
* Current SP.

### Inventory

Persist every inventory entry in its current display order:

* Canonical item ID.
* Quantity.

The inventory uses one quantity entry per item ID. Additional stacks of the same item are not supported.

An equipped item ID may also appear in the inventory because equipment slots and inventory quantities represent separate owned copies rather than individual item instances.

### World progress

Persist:

* Current city scenario ID.
* Discovered scenario IDs.
* Unlocked travel-route IDs.
* Persistent world-clock state after its exact representation is frozen.

The current city must already be included among the discovered scenarios.

### Shops

Persist for every registered shop:

* Shop ID.
* Ordered active offers.
* Stable `ShopStockEntry` ID for each offer.
* Remaining stock for each offer, including zero.

Persist the world-time/restocking state required to associate the shop states with their current restocking cycle after that representation is frozen.

## Derived state

The following values are reconstructed and must not be duplicated as authoritative save values:

* Party leader identity, derived from the first member.
* Character natural statistics, derived from class and level.
* Maximum HP, MP and SP, derived from natural stats and equipment.
* Equipment bonuses.
* Equipment EXP and GOLD modifiers.
* Normal level-acquired skills made automatically available by class and level.
* Item category, prices, name and description.
* Equipment requirements and bonuses.
* Shop item, price, percentage, weight and initial stock.
* Scenario names, descriptions and types.
* Travel-route definitions.
* INN definitions.
* Crafting definitions.
* Catalog contents.

Learned-skill IDs remain persisted for complete state validation. During reconstruction, automatically acquired skills are derived normally, while saved scroll-acquired skills are reapplied explicitly.

## Transient state

The following state is intentionally discarded:

* Active battles.
* Runtime enemies.
* Battle turn order.
* Defending state.
* Attribute stages and their remaining turns.
* Distorção Ancestral and other temporary battle effects.
* Pending speed stages.
* Per-skill execution state.
* Open menus and current menu selections.
* UI feedback messages.
* `World.running`.
* Scenario-description display flags.
* `ExplorationSession`.
* Exploration position.
* Merchant encounter flag for the current exploration.
* Epic-encounter offer flag for the current exploration.
* Random-generator state.

Saving inside an INN guarantees that no active exploration session needs to be serialized.

## Character-level boundary

The shared playable-character level range is:

```text
minimumLevel = 1
maximumLevel = 20
```

For levels 1 through 19:

```text
0 <= currentExperience < experienceRequiredForNextLevel
```

At level 20:

```text
currentExperience = 0
```

When a reward reaches level 20:

* Only the EXP required to reach level 20 is applied.
* Remaining EXP from that reward is discarded.
* Later EXP gains are ignored while level 20 remains the maximum.
* Player-facing progression displays `Nível máximo`.

`PlayableCharacterLevelRules` is the shared domain source of truth for the minimum level, current maximum level and next-level experience formula. Runtime progression already enforces these boundaries. Save reconstruction must reuse the same rule instead of defining an independent valid range.

Raising the maximum level in the future must not require changes to save-property names or the save-format version.

A level-20 save remains valid if a later version raises the maximum. The character resumes future progression from zero current EXP.

## Numeric resource boundaries

### GOLD

```text
0 <= gold <= 999999999
```

### Inventory quantity

For every item ID:

```text
1 <= quantity <= 999
```

Zero-quantity inventory entries are not serialized.

The number of different item IDs remains unlimited.

### Normal gameplay gains

Free rewards may be partially granted up to the applicable limit.

The player must be informed of:

* The amount granted.
* The amount discarded.

Excess must never disappear silently.

### Cost-bearing operations

Purchases, crafting and other operations that consume player resources are rejected completely when their result would exceed a receiving limit.

A rejected operation must preserve:

* GOLD.
* Ingredients.
* Inventory.
* Shop stock.
* Every other participating state.

### Loaded values

Negative or out-of-range saved values invalidate the save. Loading never caps, discards or corrects them silently.

Intermediate arithmetic uses `long` before validated values are stored as `int`.

### Deferred wealth extension

A future exchange item may represent `100000000` GOLD and be bought and sold for the same amount.

With an inventory limit of 999 units, this item can represent wealth above `Integer.MAX_VALUE` while liquid GOLD remains bounded.

This item and its exchange mechanism are outside the current save/load implementation.

## Reward-modifier validation

EXP and GOLD modifier states are validated independently.

Each modifier must be either inactive:

```text
modifierPercent = 0
victoriesRemaining = 0
```

or active:

```text
modifierPercent > 0
victoriesRemaining > 0
```

Mixed zero/positive combinations and negative values are invalid.

Inactive state is reconstructed by leaving a new `PartyRewardModifierState` unchanged. Active state is reconstructed through its existing activation operation.

## World discovery validation

For every saved discovered scenario ID:

* The ID must be non-empty.
* The ID must exist in `ScenarioCatalog`.
* The ID must appear only once.

Discovery order is not meaningful state. IDs may be serialized in a stable sorted order.

A new `WorldProgress` is reconstructed by calling `discover()` for every validated ID.

The saved current scenario must:

* Exist.
* Be a city.
* Provide a valid INN.
* Already be present in the discovered set.

## Travel-progress validation

For every saved unlocked route ID:

* The ID must be non-empty.
* The ID must exist in `TravelCatalog`.
* The ID must appear only once.

Route-unlock order is not meaningful state. IDs may be serialized in a stable sorted order.

A new `TravelProgress` automatically registers routes currently defined as initially unlocked. Saved IDs are then reapplied through `unlock()`.

## Shop-state validation

Each saved shop state must:

* Reference a current `ShopDefinition`.
* Appear no more than once.
* Contain at least one active offer.
* Contain no more offers than its configured slot count.

Each active offer must:

* Reference a current `ShopStockEntry`.
* Belong to the containing shop.
* Appear no more than once in that shop state.
* Store remaining stock from zero through its canonical initial stock.

Offer order is persistent because it controls menu presentation.

A restored `ShopOfferState` is created with canonical initial stock. The difference between initial and saved stock is then applied through `decreaseStock()`.

Prices, weights and initial quantities are never trusted from the save.

## World time and shop restocking

A fictional world clock will control shop restocking.

The following behavior is frozen:

* World time belongs to mutable campaign state.
* World time is persisted.
* Opening menus does not advance time.
* Visiting a shop does not advance time.
* Saving and loading do not advance time.
* Restarting the application does not advance time.
* Shop restocking depends on world-time progression.
* All registered shops restock globally at the configured boundary.
* Restocking performs a complete new weighted offer selection.
* New offers begin with their canonical initial stock.
* Old and new offer sets are not merged.
* Shop state never changes while its menu is open.
* The player receives enough clock information to anticipate restocking.
* Loading restores saved shop states without a new roll.

Still pending:

* Initial world day and time.
* Actions that advance time.
* Hours advanced by each action.
* Hours between restocking boundaries.
* Exact persisted clock representation.
* Exact player-facing clock presentation.

A restock must generate a complete replacement `ShopStateCatalog` before replacing the active catalog. Failed generation preserves the previous shop states.

## Encrypted container

`save.txt` is a textual encrypted container.

The planned outer fields are:

| Field | Purpose |
|---|---|
| `container` | Identifies an RPGConsole save container |
| `containerVersion` | Selects the supported encryption-container contract |
| `iv` | Base64-encoded random GCM initialization vector |
| `ciphertext` | Base64-encoded authenticated ciphertext and GCM tag |

The initial container uses:

```text
container=RPGCONSOLE_SAVE
containerVersion=1
```

The campaign payload is encrypted with:

```text
AES/GCM/NoPadding
```

Cryptographic parameters:

* Stable 256-bit application key.
* New secure random 12-byte IV for every save.
* 128-bit GCM authentication tag.
* Basic Base64 encoding without semantic campaign information outside the ciphertext.

The key is not stored in `save.txt` and its real value must not appear in documentation or logs.

### Threat boundary

Because the offline client must decrypt without a player password and the application is distributed to users, a determined person can extract the application key.

The intended protection is:

* Prevent casual reading.
* Prevent casual editing.
* Detect corruption.
* Detect unauthenticated modification.

The system does not claim protection against determined reverse engineering of the local client.

## Decrypted logical payload

The authenticated plaintext uses Java `Properties` encoded explicitly as UTF-8.

The payload contains:

```text
save.formatVersion
```

independently from the outer `containerVersion`.

### Property conventions

* Property names use dot-separated English identifiers.
* Repeated structures use zero-based contiguous indexes.
* Every repeated structure declares its count.
* Missing indexes are invalid.
* Indexes outside the declared count are invalid.
* Repeated IDs are invalid unless the schema explicitly permits them.
* Optional equipment slots use an explicitly documented empty value.
* Numeric values use decimal notation without grouping separators.
* Boolean values, when introduced, use only `true` or `false`.
* IDs are resolved through current catalogs.
* Unknown properties are rejected unless a later format version explicitly permits them.
* Duplicate property keys must be detected before ordinary `Properties` loading can overwrite them silently.

### Planned property groups

| Prefix | Content |
|---|---|
| `save.*` | Internal save-format metadata |
| `party.*` | Shared party resources and ordered members |
| `party.member.<index>.*` | One playable character |
| `party.inventory.*` | Ordered inventory entries |
| `party.rewardModifier.*` | Temporary global reward modifiers |
| `world.*` | Current city, discovery and persistent clock |
| `travel.*` | Unlocked route IDs |
| `shop.*` | Ordered shop states and offer stock |

The complete version-1 property list will be frozen before serialization code is implemented.

## Save-format versioning

The two versions have different responsibilities:

* `containerVersion` defines how bytes are decoded, authenticated and decrypted.
* `save.formatVersion` defines how decrypted campaign properties are interpreted.

Rules:

* Unsupported container versions are rejected before payload parsing.
* Unsupported save-format versions are rejected after successful decryption.
* Newer unknown versions are never guessed.
* Older versions require an explicit compatible reader or migration.
* Missing required properties are never supplied silently unless an older-version migration defines the default.
* Incompatible catalog-ID changes may require migration or intentional save incompatibility.
* Raising the playable-character maximum level alone does not require a save-format change.

## Atomic save operation

Saving follows this order:

1. Verify that the party is inside a city INN.
2. Verify that no exploration session is active.
3. Capture a complete campaign snapshot.
4. Validate the snapshot.
5. Serialize the UTF-8 property payload.
6. Encrypt the complete payload.
7. Build the textual outer container.
8. Write a temporary file in the application root.
9. Complete and close the temporary file.
10. Replace `save.txt` only after every earlier step succeeds.
11. Prefer an atomic move when supported.
12. Use a safe replacement fallback when atomic moves are unavailable.

The old `save.txt` is never deleted as a preliminary step.

## Load and reconstruction pipeline

Loading follows this order:

1. Load and validate immutable external data.
2. Read the outer save container.
3. Validate its technical structure and container version.
4. Decode Base64 fields.
5. Authenticate and decrypt the payload.
6. Parse UTF-8 properties with duplicate-key detection.
7. Validate property counts, indexes and required fields.
8. Validate numeric boundaries.
9. Resolve every persisted ID through current catalogs.
10. Reconstruct every playable character.
11. Reconstruct the ordered party.
12. Restore inventory, GOLD and reward modifiers.
13. Reconstruct world discovery.
14. Reconstruct travel progress.
15. Reconstruct shop states.
16. Reconstruct world time after its representation is frozen.
17. Validate cross-state invariants.
18. Produce the campaign summary and complete candidate state.
19. Construct `World` only after the candidate is valid.

### Character reconstruction order

For each character:

1. Resolve the playable-class definition.
2. Resolve the class skill definitions.
3. Construct the level-1 character.
4. Restore validated level and EXP through a domain-supported progression path.
5. Validate all saved learned-skill IDs.
6. Reapply saved scroll-acquired skills.
7. Resolve and equip weapon, armor and trinket definitions.
8. Calculate final maximum HP, MP and SP.
9. Validate saved current resources against those maximums.
10. Restore exact current HP, MP and SP.
11. Confirm that no temporary battle state is active.

The exact controlled API for progression and current-resource restoration remains an implementation decision. Gameplay damage and resource-spending methods must not be misused to simulate loading.

## Cross-state invariants

A valid candidate campaign must satisfy all of the following:

* Party size is between one and four.
* The first member is the permanent protagonist and leader.
* Character names are unique under current name rules.
* Every class and skill ID is valid.
* Learned skills are compatible with class, level and acquisition rules.
* Equipment slot IDs resolve to the correct equipment categories.
* Equipment is compatible with the character.
* Current resources fit the reconstructed maximums.
* GOLD and inventory quantities fit their limits.
* Inventory item IDs are unique within the ordered inventory entries.
* Current scenario is a discovered city with an INN.
* Every discovered scenario and unlocked route exists.
* Every shop has one valid restored state.
* Every active offer belongs to its shop.
* World time and shop restocking cycle are mutually consistent after their representation is frozen.

A failed invariant rejects the complete candidate.

## Future-feature integration

Every future feature must decide explicitly whether its state is:

* Immutable external definition.
* Persistent campaign state.
* Derived runtime state.
* Temporary runtime state.

A feature introducing persistent state must define:

1. Stable IDs for referenced definitions.
2. Snapshot fields.
3. Property names.
4. Numeric and structural validation.
5. Reconstruction order.
6. Cross-state invariants.
7. Behavior for older save versions.
8. Restart-based verification tests.
9. Documentation updates.

Likely future persisted state includes:

* Started quests.
* Completed quests.
* Quest stages and objective counters.
* Pending mandatory rewards.
* Story decisions.
* Permanent world flags.
* Explicit access unlocks.
* Completed-dungeon state.
* Level-7 class-path decisions.

Placeholder persistence structures must not be created before the supporting gameplay systems exist.

## Verification groups

Implementation verification must cover:

| Group | Required coverage |
|---|---|
| Initial flow | Missing, valid and invalid save |
| Party | One through four members, leader order and duplicate names |
| Progression | Levels 1, 19 and 20; valid and invalid EXP |
| Resources | Current HP/MP/SP boundaries |
| Inventory | Ordered items, quantity limits and duplicated IDs |
| Equipment | Empty and occupied slots, compatibility and unknown IDs |
| Skills | Automatic, scroll-acquired, unknown and incompatible skills |
| World | Current city, discovered scenarios and unlocked routes |
| Rewards | Active and inactive global modifiers |
| Shops | Ordered offers, zero stock, invalid entries and complete restoration |
| Clock | Saved time, advancement boundaries and no load-triggered restock |
| Cryptography | Invalid Base64, altered ciphertext, wrong tag and unsupported container |
| Atomicity | Failed save preserves old file; failed load preserves runtime state |
| Player flow | Continue, New Game confirmation, both INN save actions and exit confirmation |
| Packaging | Root save creation and no bundled user save |