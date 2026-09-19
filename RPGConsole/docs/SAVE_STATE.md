# RPGConsole — Save State

> **Implementation status:** Internal save format version 3 is fully integrated. The game creates one coordinated immutable snapshot containing party, world, shop, quest, permanent-flag and recruitment-candidate state; serializes and encrypts it into `save.txt`; atomically replaces the previous save; and validates and reconstructs an isolated complete campaign before runtime activation. Internal version-2 saves are rejected without migration. The empty-narrative V3 restart round trip and version-2 rejection have completed manual verification. Non-empty narrative round trips remain scheduled with the canonical quest content that can produce those states.

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

`ARCHITECTURE.md`, `DATA_FORMATS.md`, `MECHANICS.md` and `PROJECT_STATE.md` contain shorter summaries, while this document remains the authoritative persistence reference.

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
* Authoritative elapsed world hours through `world.elapsedHours`.

The current city must already be included among the discovered scenarios.

### Dungeon progress

Persist for every registered dungeon:

* Canonical dungeon scenario ID.
* Whether the narrative boss has been defeated.
* Absolute elapsed world hour when the recurring boss next becomes available.
* Absolute elapsed world hour when the epic encounter next becomes available.

Every registered dungeon must appear exactly once.

Before narrative completion, recurring-boss availability must be zero. After narrative completion, it must be defined as a positive elapsed-hour value. Epic availability is initially zero and becomes positive after the first epic victory.

Availability values are authoritative absolute world hours. They may be lower than, equal to or greater than the saved `world.elapsedHours`, representing an encounter already available, becoming available now or remaining in cooldown.

### Shops

Persist for every registered shop:

* Shop ID.
* Ordered active offers.
* Stable `ShopStockEntry` ID for each offer.
* Remaining stock for each offer, including zero.

`world.elapsedHours` and every active shop state must belong to the same campaign snapshot. The current restocking cycle is derived from elapsed hours, so no separate cycle identifier is persisted.

### Quest progress

Persist for every active quest:

* Canonical quest ID.
* Current sequential step number.
* Every stable objective ID belonging to that current step.
* Current quantity for every current objective.

Persist every completed quest through its canonical quest ID.

A quest absent from both collections is `NOT_STARTED`. Completed previous steps, future steps and derived availability are not serialized.

### Permanent campaign flags

Persist the canonical ID of every currently set flag.

Absent flags remain unset. Flag values are set-only, so no independent `false` entry is serialized.

### Recruitment candidates

Persist every candidate already introduced to the campaign:

* Canonical candidate ID.
* Persistent generated name.
* Recruited state.

Quest ID, recruitment step, configured ally entry level and every other recruitment definition remain immutable external data resolved through `QuestRecruitmentCatalog`.

## Derived state

The following values are reconstructed and must not be duplicated as authoritative save values:

* Party leader identity, derived from the first member.
* Character natural statistics, derived from class and level.
* Maximum HP, MP and SP, derived from natural stats and equipment.
* Equipment bonuses.
* Normal level-acquired skills made automatically available by class and level.
* Item category, prices, name and description.
* Equipment requirements and bonuses.
* Shop item, price, percentage, weight and initial stock.
* Scenario names, descriptions and types.
* Dungeon enemy groups, cooldown durations, epic minimum positions and area restrictions.
* Travel-route definitions.
* INN definitions.
* Crafting definitions.
* Catalog contents.
* Completed quest steps before the current active step.
* Future quest steps and objective definitions.
* Quest availability and `[!]` marker state.
* Permanent flags that are not set.
* Recruitment quest ID, step and configured ally level.

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
* Open quest, NPC, dialogue, questionnaire and confirmation menus.
* Queued quest notifications and temporary event batches.
* Questionnaire answers, recommendation scores and ties.
* Unconfirmed protagonist class choices.
* Random uncommitted ally classes and constructed uncommitted allies.
* `ClassPathTransitionPlan`, `AllyRecruitmentPlan`, `ClassPathRecruitmentPlan` and other prepared transaction state.
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

`Party.MAXIMUM_GOLD` is the shared runtime and load-validation source for this boundary.

With an inventory limit of 999 units, this item can represent wealth above `Integer.MAX_VALUE` while liquid GOLD remains bounded.

`Inventory.MAXIMUM_QUANTITY_PER_ITEM` is the shared runtime and load-validation source for this boundary.

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

A fictional world clock controls global shop restocking. It belongs to mutable campaign state and never reads the operating-system date or time.

### Authoritative clock representation

The authoritative persisted clock value is `world.elapsedHours`, representing the number of complete world hours elapsed since 00:00 on day 1.

A new campaign starts with:

```properties
world.elapsedHours=8
```

World time uses 24-hour days. The player-facing values are derived as follows:

```text
day = floor(elapsedHours / 24) + 1
hourOfDay = elapsedHours % 24
```

For example:

```text
elapsedHours = 8  -> day 1 at 08:00
elapsedHours = 23 -> day 1 at 23:00
elapsedHours = 24 -> day 2 at 00:00
```

The displayed day, hour, period symbol and next-restocking time are derived values and are not persisted independently.

Loaded elapsed hours must be integral, must represent at least the initial campaign time and must fit the supported runtime representation. Invalid values reject the complete save instead of being capped or corrected.

Opening menus, visiting shops, saving, loading and restarting the application do not advance world time. Loading restores the exact saved elapsed-hour value and active shop states without consulting real time or generating new offers.

### Time advancement

The world clock exposes a general operation that receives the positive number of hours to advance. It does not know which gameplay action caused the advancement.

The current gameplay integration advances time only after a battle ends in:

* `VICTORY`.
* `DEFEAT`.

`World` owns the current battle-duration configuration:

```java
private static final int HOURS_PER_FINISHED_BATTLE = 1;
```

The value is passed to the general clock-advancement operation. This keeps the clock reusable if another gameplay action later advances a different configured number of hours.

The following outcomes do not advance world time:

* `FLED`.
* Avoiding an optional encounter before battle.
* Any event that does not start a battle.
* A battle attempt that does not produce a finished victory or defeat result.

Time advancement is not displayed as a battle reward and does not add a separate post-battle message.

### Restocking boundary

Every midnight begins a new global shop-restocking cycle.

The current cycle is derived from:

```text
restockingCycle = floor(elapsedHours / 24)
```

A time advancement crosses a restocking boundary when its resulting cycle differs from its previous cycle.

When an advancement crosses midnight:

1. Generate a complete candidate `ShopStateCatalog`.
2. Perform a new weighted offer selection independently for every registered shop.
3. Give every selected offer its canonical initial stock.
4. Do not merge old and new offers.
5. Commit the new elapsed world time and replacement shop states together.

The previous world time and complete previous shop states remain unchanged if replacement generation fails.

Shop state never changes while a shop menu is open. With the currently supported progression rules, only a finished battle can cross midnight, and battles cannot occur during shop interaction.

Saving stores world time and all active shop states in the same campaign snapshot. Loading restores both from that snapshot and never interprets loading itself as a crossed boundary.

### Player-facing presentation

Current world time uses this format:

```text
Dia 1 — 08:00 ☼
Dia 1 — 19:00 ☾
```

The period symbol is derived from the displayed hour:

* `☼` from 06:00 through 17:59.
* `☾` from 18:00 through 05:59.

The current day and time appear above the horizontally arranged action menus in cities and combat scenarios.

City and combat-scenario actions are displayed horizontally across two rows, following the compact organization already used by battle actions.

The shop's main menu displays both the current world time and the exact next global restocking boundary:

```text
Dia 1 — 13:00 ☼
Próxima reposição: Dia 2 — 00:00 ☾
```

The next-restocking line is not repeated in ordinary city or combat-scenario menus.

The presentation symbols are runtime formatting details. They are not stored in `save.txt` and do not affect clock progression or shop behavior.

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
* Boolean values use only the canonical lowercase forms `true` and `false`.
* IDs are resolved through current catalogs.
* Unknown properties are rejected unless a later format version explicitly permits them.
* Duplicate property keys must be detected before ordinary `Properties` loading can overwrite them silently.

### Version-3 property schema

The decrypted version-3 payload contains exactly the following property structures.

```properties
save.formatVersion=3

party.gold=1250

party.rewardModifier.experience.percent=20
party.rewardModifier.experience.remainingVictories=3
party.rewardModifier.gold.percent=0
party.rewardModifier.gold.remainingVictories=0

party.member.count=1

party.member.0.name=Aragorn
party.member.0.classId=flagelo_iniciante
party.member.0.level=7
party.member.0.experience=120
party.member.0.currentHp=85
party.member.0.currentMp=20
party.member.0.currentSp=30

party.member.0.equipment.weaponItemId=espada_de_ferro
party.member.0.equipment.armorItemId=
party.member.0.equipment.trinketItemId=

party.member.0.learnedSkill.count=1
party.member.0.learnedSkill.0.id=golpe_aprendido

party.inventory.count=2
party.inventory.0.itemId=pocao
party.inventory.0.quantity=4
party.inventory.1.itemId=espada_de_ferro
party.inventory.1.quantity=1

world.currentScenarioId=bree
world.elapsedHours=31

world.discoveredScenario.count=2
world.discoveredScenario.0.id=bree
world.discoveredScenario.1.id=campos_de_bree

travel.unlockedRoute.count=1
travel.unlockedRoute.0.id=bree_campos
world.dungeon.count=1
world.dungeon.0.id=gruta_relamparrocha
world.dungeon.0.narrativeBossDefeated=true
world.dungeon.0.recurringBossAvailableAtHour=35
world.dungeon.0.epicAvailableAtHour=40

shop.count=1
shop.0.id=mercado_de_bree
shop.0.offer.count=2
shop.0.offer.0.stockEntryId=mercado_pocao
shop.0.offer.0.remainingStock=3
shop.0.offer.1.stockEntryId=mercado_espada
shop.0.offer.1.remainingStock=0

quest.active.count=1
quest.active.0.id=os_sete_caminhos
quest.active.0.currentStep=2
quest.active.0.objective.count=1
quest.active.0.objective.0.id=falar_com_mestre
quest.active.0.objective.0.currentQuantity=0

quest.completed.count=1
quest.completed.0.id=a_ordem_dos_alquimistas

flag.count=1
flag.0.id=acesso_entrelugar

recruitment.candidate.count=1
recruitment.candidate.0.id=primeiro_aliado
recruitment.candidate.0.name=Lina
recruitment.candidate.0.recruited=false
```

The values above are illustrative and are not canonical campaign data.

All properties shown by the applicable count structures are required. Counts may be zero where the persistence contract permits an empty collection.

Equipment-slot properties are always present. An empty value represents an unequipped slot. A non-empty value must be a canonical item ID resolving to the correct equipment category.

`party.member.<index>.learnedSkill.*` contains only explicitly learned scroll-skill IDs. Skills acquired automatically from class and level are reconstructed from current definitions and are not serialized.

Party members, learned skills, inventory entries, shops, active offers, active quests, completed quests, current objectives, set flags and recruitment candidates preserve their stable runtime or catalog order. Discovered scenarios, unlocked routes and dungeon-progress entries are serialized in stable ID order.

Every repeated index starts at zero, is contiguous and is lower than its declared count. Missing indexes, additional indexes, duplicate IDs and duplicate property names invalidate the complete payload.

`world.dungeon.count` must cover every dungeon registered in the current `ScenarioCatalog`. Unknown dungeon IDs, missing entries, duplicated IDs and non-dungeon scenario IDs invalidate the complete payload.

`world.dungeon.<index>.narrativeBossDefeated` accepts only `true` or `false`. Recurring-boss availability must be zero while this value is `false` and nonzero while it is `true`. Both availability-hour properties must be non-negative.

Active and completed quest IDs must be unique across both collections. Every active quest step must exist in the current definition, and its objective collection must match that step's canonical objective set exactly. Objective counters must be non-negative and no greater than their current required quantities.

Every saved flag ID must resolve through `FlagCatalog` and appear at most once.

Every saved candidate ID must resolve through `QuestRecruitmentCatalog` and appear at most once. Candidate names must satisfy `CharacterNameRules` and remain unique among candidate states. `recruited` accepts only canonical `true` or `false`.

Properties outside the version-3 schema invalidate the payload. This prevents misspelled or unsupported fields from being silently ignored.

All numeric values use base-10 ASCII digits and must satisfy their documented domain boundaries.

The four reward-modifier properties are always present. An inactive modifier is represented by zero percent and zero remaining victories.

`world.elapsedHours`, all `world.dungeon.*` structures and all `shop.*` structures belong to the same snapshot. No separate restocking-cycle property is stored.

`shop.<index>.offer.count` must be positive because every valid runtime `ShopState` contains at least one active offer. Offers with zero remaining stock remain serialized.

The version-3 payload contains no real-world timestamps, formatted values, derived period symbols, cryptographic fields, immutable quest or recruitment definitions, derived availability or transient runtime state.

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

The current implementation supports only internal format version 3. Internal versions 1 and 2 are rejected without migration. The encrypted outer-container format remains version 1 and is unchanged by this schema evolution.

### V6 Part 9 persistence boundary

V6 Part 9 evolves the authenticated internal payload from version 2 to version 3 as one coordinated contract. It adds quest progress, set permanent flags and introduced recruitment-candidate state to the existing party, world, dungeon, clock and shop snapshot.

The version increase is intentionally incompatible. A version-2 party could structurally contain multiple playable characters but could not prove the quest, flag and candidate transaction that produced a recruited ally. Supplying defaults or loading only the party would permit repeated recruitment or disagreement between narrative and character state, so version 2 is rejected explicitly instead of migrated.

The outer container, application encryption key usage, AES-GCM parameters, duplicate-key protection and atomic replacement flow remain unchanged.

`quest_recruitments.txt` remains immutable external definition data. Quest ID, step and configured ally level are reloaded from the current catalog and are not copied into the save payload.

Questionnaire answers, recommendation scores, uncommitted selections, random uncommitted ally classes, constructed pending allies and prepared transaction plans remain transient in version 3.

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
15. Reconstruct permanent dungeon progress.
16. Reconstruct world time.
17. Reconstruct shop states.
18. Reconstruct active and completed quest progress through `QuestProgressRestorer`.
19. Reconstruct set permanent flags through `CampaignFlags`.
20. Reconstruct introduced candidate state through `RecruitmentCandidateStateRestorer`.
21. Validate narrative and party cross-state invariants.
22. Produce the campaign summary and complete isolated `RestoredCampaign`.
23. Construct `World` only after the complete candidate campaign is valid.

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
* Every registered dungeon has exactly one progress entry.
* Every saved dungeon ID resolves to a runtime `Dungeon`.
* Narrative completion and recurring-boss availability are structurally consistent.
* Recurring-boss and epic availability hours are non-negative.
* Every shop has one valid restored state.
* Every active offer belongs to its shop.
* Validated world time, complete dungeon progress and the complete validated shop catalog come from the same candidate snapshot and are installed together without load-triggered events.
* Active and completed quest IDs are mutually exclusive and resolve through the current `QuestCatalog`.
* Every active quest uses a valid current step and the exact canonical objective set for that step.
* Every objective counter fits its current canonical requirement.
* Every set flag resolves through the current `FlagCatalog` and appears only once.
* Every candidate resolves through `QuestRecruitmentCatalog`, uses a valid persistent name and appears only once.
* Candidate names are unique among recruitment candidates.
* A pending candidate matches no party member under `CharacterNameRules`.
* A recruited candidate matches exactly one non-leader party member under `CharacterNameRules`.
* A recruited ally is at least the externally configured entry level and does not use beginner or final Flagelo.
* Every recruitment step already passed by quest progress corresponds to a recruited candidate, and every recruited candidate corresponds to a passed recruitment step.
* The first ceremony state agrees across completed recruitment quest, permanent ceremony flag, leader class transition and recruited candidate.
* When the first candidate is recruited, the matching ally occupies the second party position.
* Every party member uses a distinct playable class.
* Additional non-leader party members without candidate state remain valid for the isolated `--dev-party` development path.

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

Internal format version 3 adds the implemented V6 campaign state as one coordinated contract:

* Active quest IDs.
* Current quest steps.
* Current objective counters.
* Completed quest IDs.
* Permanent world flags.
* Recruitment-candidate IDs.
* Recruitment-candidate persistent names.
* Recruitment-candidate recruited states.

Pending transaction plans, random uncommitted choices and derived availability remain excluded.

The protagonist's confirmed class already uses the persisted canonical playable-class ID. Version-3 restoration validates that class against the permanent ceremony-completion flag and the restored recruitment transaction state instead of serializing a second independent class-path decision.

Version-3 restoration validates:

* Every restored candidate ID exists in `QuestRecruitmentCatalog`.
* Candidate IDs are unique.
* Candidate names satisfy current character-name rules.
* Candidate names are unique among candidates.
* A pending candidate does not already appear as its recruited party member.
* A recruited candidate corresponds to exactly one restored non-leader ally.
* The first recruited candidate is consistent with the completed ceremony flag, protagonist final class and recruitment quest state.
* The protagonist and every ally retain distinct playable classes.
* Party order remains leader first followed by recruited allies.
* No transient preparation or random-selection state is reconstructed.

Every quest, flag and candidate object must be reconstructed and cross-validated in an isolated candidate campaign before `World` receives the restored state.

Future schema evolution must continue to add snapshot structures only after their supporting gameplay systems exist.

## Verification groups

Implementation verification must cover:

V6 Part 9 manually confirmed explicit version-2 rejection and a complete version-3 restart round trip with empty quest, flag and candidate collections. A non-empty narrative round trip and representative invalid narrative payloads remain pending until canonical quest content can produce coherent runtime fixtures in Parts 10 and 11. This deferral does not weaken the format contract or permit placeholder production content.

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
| Dungeons | Complete catalog coverage, narrative completion, recurring-boss availability, epic availability and invalid state |
| Shops | Ordered offers, zero stock, invalid entries and complete restoration |
| Clock | Saved time, advancement boundaries and no load-triggered restock |
| Narrative | Active counters, completed quests, set flags, pending candidates, recruited candidates and party coherence |
| Cryptography | Invalid Base64, altered ciphertext, wrong tag and unsupported container |
| Atomicity | Failed save preserves old file; failed load preserves runtime state |
| Player flow | Continue, New Game confirmation, both INN save actions and exit confirmation |
| Packaging | Root save creation and no bundled user save |
