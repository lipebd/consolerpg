# RPGConsole — Project State

## Current version

V7 is complete and preserved as the current released version.

Current stage:

`V8 enemy combat — the V7 INN carryover, enemy levels, reusable enemy skills, initial enemy action choice, individual target profiles and Bestiary integration are implemented and verified. Remaining regional balance, regression and release checks remain pending.`

Current Git branch:

`feature/v8-enemy-combat`

Preserved release tags:

* `v1.0-mmmvp`
* `v2.0-mmvp`
* `v3.0.0`
* `v4.0.0`
* `v5.0.0`
* `v6.0.0`
* `v7.0.0`

## Project objective

Console RPG developed in Java as an academic project.

The project uses object-oriented programming, including inheritance, abstraction, encapsulation, polymorphism and external text files for game data.

Code identifiers and technical comments are written in English. Texts displayed to the player are written in Brazilian Portuguese.

## Implemented in V1

- Title screen and console clearing.
- Initial scenario: Verdes Ermos.
- Interactive world loop.
- Random exploration encounters.
- One player against one enemy.
- Attack, defend, special skill, analyze, items and flee actions.
- HP, MP and SP.
- Skills with MP and SP costs.
- Enemy ranks and reward multipliers.
- EXP, gold and level progression with excess EXP preservation.
- Battle result screen.
- Item inventory and Merumã healing item.
- Items usable inside and outside battle.
- Game Over screen.
- Executable distribution with bundled Java runtime.

## V2 implemented features

### Data-driven enemies

- Complete immutable `CharacterStats` definitions.
- External enemy definitions in `data/enemies.txt`.
- `EnemyLoader`, `EnemyCatalog` and `EnemyDefinition`.
- Stable enemy and scenario IDs.
- Runtime `Enemy` instances created from reusable definitions.
- Enemy ranks: common, veteran, leader, boss, epic and legendary.

Enemy creation flow:

```text
enemies.txt
    -> EnemyLoader
    -> EnemyCatalog
    -> EnemyDefinition
    -> Enemy
```

Runtime `Enemy` instances retain their canonical definition ID, allowing battle actions and outcomes to emit identity-based quest progress without relying on display names.

### Data-driven encounters

- External encounter entries in `data/encounters.txt`.
- Entries grouped by scenario ID.
- Exploration-position ranges.
- Relative selection weights.
- Normal, special or unrestricted area availability.
- Per-entry maximum copies.
- Per-scenario minimum and maximum enemy counts.
- Weighted enemy-group generation.
- Groups containing one to three enemies.

### Linear exploration

- Exploration advances through numbered positions.
- Each combat scenario defines its exploration length and event chances.
- Scenario-specific messages for normal and special areas.
- Supported random events: nothing, battle, reward, merchant and optional epic encounter.
- Special areas always resolve to content: direct nothing and repeated merchant results become rewards.
- Unavailable epic results become regular battles using the current area's encounter pool.
- Merchant limited to one appearance per exploration session.
- Epic encounter limited to one offer per dungeon session.
- Epic eligibility uses minimum position, area restriction, session state and persistent cooldown.
- Exploration progress survives battles.
- Exploration sessions reset when a new combat scenario session begins.
- Active exploration position and session flags remain transient across application executions.

### Scenarios and travel

- Scenario inheritance hierarchy with `Scenario`, `CombatScenario`, `Field`, `Dungeon` and `City`.
- `ScenarioCatalog` stores scenarios by stable ID.
- `TravelCatalog` stores directed routes between scenarios.
- `RouteMenu` supports multiple unlocked adjacent destinations.
- Adjacent travel from combat scenarios remains locked until exploration is complete.
- Bree, Verdes Ermos and Gruta de Relamparrocha are connected.
- Scenario descriptions are displayed on scenario entry; returning from a battle in the same scenario does not reactivate them.

### Dungeon progression

- Every dungeon defines narrative, recurring and epic enemy groups.
- Narrative-boss victory records permanent first completion.
- Completed dungeons remain available during recurring-boss cooldowns.
- A ready recurring boss becomes the mandatory final encounter.
- A dungeon exploration ends without a final boss while the recurring boss remains in cooldown.
- Boss and epic availability is evaluated when the encounter position is reached.
- Victories begin cooldowns after the battle's world-time advancement.
- Refusal, escape and defeat do not begin cooldowns.
- Respawn hours remain hidden from the player.
- `DungeonProgress` stores narrative completion and absolute availability hours separately from transient `ExplorationSession` state.

### Gruta de Relamparrocha

- Eleven-position dungeon session.
- Random normal and special-area encounters.
- Optional Relampagolem epic encounter available from position 1 in either area type.
- Relampagolem uses a six-hour victory cooldown.
- Mestre Eletrogoblin is the narrative and recurring final boss.
- The recurring boss uses a four-hour victory cooldown.
- Fleeing from the boss keeps the party at the final position.
- First completion permanently unlocks the expected travel access.

### External enemy drops

- External drop definitions in `data/enemy_drops.txt`.
- `EnemyDropLoader` and `EnemyDropCatalog`.
- Item lookup through stable IDs in `ItemCatalog`.
- Independent chance and quantity range for each drop.
- Multiple possible drops per enemy.
- Empty drop tables for enemies without registered drops.
- Equal items consolidated across all defeated enemies.
- Rewards granted from lists of defeated enemies.

Drop flow:

```text
enemy_drops.txt
    -> EnemyDropLoader
    -> EnemyDropCatalog
    -> Enemy
    -> BattleRewardService
    -> BattleRewards
    -> Party Inventory
```

### Party system

- `Player` renamed to `PlayableCharacter`.
- `Party` supports one to four playable characters.
- Inventory and gold belong to the party.
- HP, MP, SP, EXP, level and skills remain individual.
- Every living party member is controlled by the player.
- Defeated party members cannot act.
- Items may be used by one ally on another.
- Defeated party members may still receive EXP.
- Every party member receives the complete EXP reward.
- Gold and items are granted once to the party.
- Multiple level-up results are displayed compactly.
- Surviving characters fully restore HP, MP and SP after leveling up.
- Defeated characters remain defeated even after leveling up.
- Party and individual character status menus are available outside battle.
- Shared gold is displayed in the party status menu.
- `Party.fullyRestoreMembers()` centralizes revival and complete HP, MP and SP restoration for INN stays and full-party rescue.

### Recovery skills

V2 introduced healing and revival as single-purpose hardcoded skill fields. V4 replaced that precursor with external ordered components. HP recovery and revival now scale through `RecoveryCalculator`; MP and SP recovery remain fixed-power components. Targeting and applicability belong to each component, and revived allies still enter the initiative queue only in the following round.

### Multiple enemies

- Battles support one to three enemies.
- Duplicate enemies receive numbered battle names.
- Defeated enemies remain visible and are marked as defeated.
- Defeated enemies cannot act or be targeted.
- Attacks, skills and analysis support enemy target selection.
- Rewards and equal item drops are consolidated across the enemy group.
- Enemy groups respect scenario limits and per-enemy copy limits.
- Enemies currently target a living ally using a simple selection rule.

### Battle initiative

- Allies and enemies share one unified round queue.
- Greater effective Speed acts first.
- Greater effective Luck resolves the first tie.
- Remaining ties are resolved randomly.
- Turn order is rebuilt at the beginning of every round.
- Speed changes affect only the following round.
- Participants may receive multiple actions according to Speed.
- Additional actions are distributed through decreasing initiative scores.
- Defeated future entries are skipped.
- The current and next participants are displayed.
- `Tática` → `Ordem` previews up to five remaining actions without consuming the current action.

### Battle escape

- The acting playable character performs the flee attempt.
- The first check uses effective Luck directly as a percentage.
- A failed first check triggers a comparative Speed and Luck calculation.
- Living enemy averages are used by the comparative check.
- Comparative chance has configured minimum and maximum limits.
- A failed attempt consumes the acting character's action.
- A successful attempt removes the entire party from battle.
- Fleeing from a dungeon boss keeps the exploration session at the final position without completing the dungeon or beginning a cooldown.

### Battle rewards and progression

- Base EXP and gold come from the current combat scenario.
- Enemy rank applies its reward multiplier.
- Rewards from multiple enemies are aggregated.
- Every party member receives the complete EXP amount.
- Defeated party members also receive EXP.
- Excess EXP is preserved after leveling.
- Individual level progression is stored in `CharacterProgressionResult`.
- Surviving characters who level up fully restore HP, MP and SP.
- Defeated characters may level up but remain defeated.
- Gold and collected items are added once to shared party resources.

### World and UI refactoring

Responsibilities previously concentrated in `World` were extracted into dedicated components, including:

- `CombatScenarioMenu`
- `CityMenu`
- `CityLocationMenu`
- `InnMenu`
- `RouteMenu`
- `CityTransportMenu`
- `WorldProgress`
- `EpicEncounterMenu`
- `BattleCoordinator`
- `BattleRewardService`
- `BattleRewards`
- `BattleRewardScreen`
- `EnemyTargetMenu`
- `AllyTargetMenu`
- `InventoryMenu`
- `BattleItemMenu`
- `RecoveryItemUseMenu`
- `ItemMenuFormatter`
- `BattleTurnOrderScreen`
- `PartyStatusMenu`
- `CharacterStatusMenu`

`World` coordinates scenario state, exploration events and the consequences of battle results. It no longer directly controls console input.

`ConsoleInput` now replaces direct `Scanner` usage throughout the application. It centralizes UTF-8 decoding, recoverable Windows-console EOF handling and queued-command control. Timed battle and skill-scroll pauses discard complete commands entered before their following prompts, preventing delayed actions from leaking into later menus.

`World` dispatches fields and dungeons to `CombatScenarioMenu` and cities to `CityMenu`. Each action list is built incrementally from the current scenario state. Both menus preserve continuous numbering, omit unavailable actions and use whitespace to separate action groups without adding unnecessary submenu layers.

World, combat-scenario, city, city-location, INN, travel, branch, inventory and status menus use iterative navigation and explicit back commands.

## City structure and services

Cities use a dedicated action flow instead of the combat-scenario menu.

The city menu currently supports:

- Visiting a functional city location, including INNs and fixed city shops.
- Leaving through an unlocked adjacent route.
- Opening the shared inventory.
- Viewing party status.
- Explicitly ending the game.

Unavailable actions are omitted. Paid city transport appears only when at least one configured and discovered destination city is available.

City locations are treated as a navigation concept rather than a generic service hierarchy. `CityLocationMenu` displays only functional location names, while each selected service owns its details and interaction flow.

Every registered city has exactly one externally configured INN. `InnCatalog` prevents multiple definitions for the same city, while `InnLoader` rejects initialization when any city has no INN.

City INNs:

- Charge an externally configured fixed price for the whole party.
- Remain accessible when the party cannot afford a stay.
- Prevent unnecessary, unaffordable and cancelled payments.
- Request confirmation before charging.
- Delegate revival and complete HP, MP and SP restoration to `Party.fullyRestoreMembers()`.
- Keep the player inside the INN after resting.
- Display action feedback inside the current INN screen.

Bree additionally provides one fixed city shop. `CityLocationMenu` combines the available location names without owning their service-specific behavior.

The Bree market uses the same shop definitions, runtime state, interface and transaction rules as exploration merchants. City shops remain accessible when they have no remaining purchase offers.

City exits reuse unlocked `TravelRouteDefinition` entries. Their names and descriptions act as creative exit labels and flavor text, and adjacent travel remains free.

## Shops and merchants

V3 implements functional fixed shops and exploration merchants.

External shop configuration is divided between:

```text
shops.txt
    -> ShopLoader
    -> ShopCatalog

shop_stock.txt
    -> ShopStockLoader
    -> ShopStockCatalog
```

`shops.txt` defines each shop's stable identity, associated scenario, access type, number of active offer slots, name and description.

`shop_stock.txt` defines the possible weighted offers for each shop, including the referenced item, sale-price percentage, initial stock and relative selection weight.

Startup validation covers:

- Stable and unique definition and offer IDs.
- Valid shop, item and scenario references.
- Compatibility between access type and scenario type.
- Positive offer-slot counts, price percentages, stock quantities and weights.
- Required base sale prices for stocked items.
- Commercially duplicate offers.
- Shops without possible offers.
- Missing exploration merchants when a scenario has a positive merchant chance.
- Price-calculation overflow.
- Per-item anti-arbitrage rules.

`ShopStateGenerator` creates every shop's initial active offers during application initialization and remains available to `World` for runtime restocking. Every generation performs weighted selection without replacement and safely accumulates selection weights.

The generated runtime structure is:

```text
ShopStateCatalog
    -> ShopState
        -> ShopOfferState
```

Each `ShopOfferState` preserves its remaining quantity. Active offers and quantities remain unchanged between repeated accesses during the current restocking cycle.

Exhausted offers disappear from the purchase list but are not replaced by unused entries during the same cycle. Shops remain accessible when all active offers are exhausted.

Whenever world time crosses midnight, `World` requests a complete replacement `ShopStateCatalog`. Every registered shop receives a new weighted offer selection with canonical initial stock. Old offers and remaining quantities are discarded instead of being merged into the new cycle.

The replacement catalog is generated before the current clock or shop-state reference changes. A generation failure therefore preserves the complete previous runtime state.

City shops:

- Are displayed as functional city locations.
- Support multiple named shops in the same city.
- Resolve their existing state through `ShopStateCatalog`.

Exploration merchants:

- Reuse the existing merchant exploration event.
- Remain limited to one encounter per exploration session.
- Are selected uniformly among eligible merchants associated with the current scenario.
- Reuse the same shop state in later exploration sessions.
- Preserve the interaction style established by epic encounters.

`ShopMenu` is shared by both access types and provides:

- Shop name, description and current party gold.
- Purchase and sale actions.
- Deterministically sorted and paginated item lists.
- Quantity selection.
- Complete transaction summaries.
- Explicit confirmation and cancellation.
- Feedback without closing the shop.

Purchases validate the selected offer, requested quantity, remaining stock, total price, party gold and resulting inventory quantity before changing any state.

Sales validate the selected item, purchase price, requested quantity, owned inventory, total value and resulting party gold before changing any state.

`ShopTransactionService` applies both operations atomically. Invalid, cancelled, unaffordable or overflowing transactions preserve gold, inventory and shop stock.

Items sold by the player are removed from inventory but are not added to shop stock. Buyback is not implemented.

`KEY_ITEM` definitions cannot declare `shopPurchasePrice`, and the category does not appear in the shop sale menu.

## External item system

Base item definitions are loaded from `data/items.txt`.

Recovery behavior is loaded separately from `data/recovery_items.txt`, preventing unrelated item categories from requiring empty recovery fields.

The current construction flow is:

The current construction flow is:

```text
items.txt
    -> ItemLoader
    -> ItemDefinitionCatalog

recovery_items.txt
    -> RecoveryItemLoader
    -> RecoveryItemDefinitionCatalog

skill_scrolls.txt
SkillCatalog
PlayableClassCatalog
    -> SkillScrollLoader
    -> SkillScrollDefinitionCatalog

ItemDefinitionCatalog
RecoveryItemDefinitionCatalog
SkillScrollDefinitionCatalog
SkillCatalog
    -> ItemFactory
    -> ItemCatalog
```

`ItemDefinitionCatalog`, `RecoveryItemDefinitionCatalog` and `SkillScrollDefinitionCatalog` contain immutable data loaded during application initialization.

`ItemFactory` combines the base and specialized definitions. It currently creates:

- `BasicItem` for items without specialized runtime behavior.
- `RecoveryItem` for items with a recovery definition.
- `SkillScroll` for items with a validated skill-scroll definition.

Equipment items remain `BasicItem` runtime objects. Their specialized immutable data is loaded independently from `weapons.txt`, `armors.txt` and `trinkets.txt` into `EquipmentCatalog` using the same stable item IDs.

`ItemCatalog` contains the complete runtime objects used by enemy drops, exploration rewards, shop offers, menus and inventory.

Base item definitions may independently declare:

- `baseShopSalePrice`, used to calculate offers sold by shops.
- `shopPurchasePrice`, used when shops buy owned items from the party.

The presence of `shopPurchasePrice` determines whether an item is sellable to shops. `playerTradable` identifies items eligible for the future player-to-player trading system. Trading-post behavior is not implemented by the local item integration.

The current recovery-item model supports:

- HP recovery.
- MP recovery.
- SP recovery.
- Revival.
- Combined effects.
- Single-ally targeting.
- Whole-party targeting.
- Purification of active negative attribute states.
- Fixed or maximum-resource percentage amounts for HP, MP, SP and revival.
- Selective purification of negative ATK, DEF, MAG, RES, VEL and LCK stages.
- Battle-only +1 and +3 positive attribute-stage fruits and potions, applied after the acting character's consumed turn.

Revival is applied before ordinary HP recovery. Items are removed only when at least one configured component produces an effect.

Purification is applied only when the target has an eligible negative attribute state. An ineffective item attempt does not consume the item or the acting character's battle action.

## External exploration reward pools

Exploration reward entries are loaded from `data/exploration_rewards.txt`.

The current flow is:

```text
exploration_rewards.txt
    -> ExplorationRewardLoader
    -> ExplorationRewardCatalog
    -> ExplorationRewardGenerator
    -> ExplorationReward
    -> Party Inventory or gold
```

The system currently supports:

- Item rewards resolved through stable IDs in `ItemCatalog`.
- Gold rewards without reward IDs.
- Inclusive minimum and maximum quantity ranges.
- Relative weighted selection.
- Normal-only, special-only and unrestricted area entries.
- Exactly one selected entry per reward event.
- Repeatable rewards.
- Independent selection from enemy battle drops.

`ExplorationRewardLoader` validates combat-scenario references, item references, reward-type requirements, quantities, weights, exact duplicates and required area pools during application initialization.

`ExplorationAreaRestriction` is shared by encounter and reward entries.

Verdes Ermos currently combines area-specific Merumã entries with unrestricted Pelo Lupino and gold entries. `pelo_lupino` is a basic material item used to validate rewards from a category different from consumables.

Combat scenarios with zero `rewardChance`, such as Gruta de Relamparrocha, do not require reward entries.

## Inventory interface

The general inventory and battle item selection use separate menus.

`InventoryMenu`:

- Displays shared party gold.
- Displays every item category in deterministic enum order.
- Keeps empty categories selectable.
- Reports an empty category only after it is selected.
- Keeps empty inventory navigation open until an explicit back or close command.
- Uses deterministic item ordering.
- Paginates lists with nine item rows per page.
- Keeps list height stable on partially filled pages.
- Truncates long names only in list views.
- Opens a details screen before offering a contextual action.

`BattleItemMenu` lists only consumables that can currently be used during battle.

`RecoveryItemUseMenu` coordinates effect application and conditional item removal in inventory and battle. `AllyTargetMenu` handles out-of-battle target selection, group confirmation and animated result presentation. Ineffective uses show feedback without consuming the item; successful uses return to the refreshed consumable list.

`SkillScrollUseMenu` resolves the unique class-compatible learner, reports unavailable learning, handles the two-slot replacement flow and delegates successful mutation and consumption to `SkillScrollService`.

Skill-scroll details reuse `SkillDetailFormatter`, display the compatible learner beside the action and remain available only through the general inventory. Successful learning refreshes the scroll category instead of closing the inventory.

An ineffective item attempt does not consume the item or the acting character's battle action.

The shared inventory has no weight or capacity limit.

## V4 combat effects and external skills

The first V4 gameplay-system block is complete.

### External playable-class and skill definitions

- All seven playable classes have stable external definitions, one validated Unicode presentation symbol and startup validation.
- `skills.txt` stores immutable skill headers with `LEVEL` or `SCROLL` acquisition types and level requirements validated against the supported playable-character range.
- `skill_effects.txt` stores ordered effect components.
- `SkillLoader` combines and validates both files and registers complete `SkillDefinition` objects in `SkillCatalog`.
- `PlayableCharacter` references its class and skill definitions, stores learned skills through stable IDs and automatically learns eligible non-scroll skills.
- The old hardcoded `Skill` and `RecoveryType` types were removed.
- All 30 approved natural class-progression skills are registered externally: two beginner Flagelo `LEVEL` skills and 28 final-path `LEVEL` skills.
- Skill powers and resource costs remain provisional until the general balance pass.

### Skill execution

- `SkillExecutionContext` preserves the user, explicitly selected targets and equipped weapon base power; `SkillUseState` preserves mutable state for one use.
- `SkillExecutor` processes `PRE_ACTION`, `PRIMARY` and deferred `POST_ACTION` components in sequence.
- Existing temporary-effect durations decrease after the acting character consumes its turn and before `POST_ACTION` components are applied.
- Each component selects its targets, evaluates its condition and applicability, then performs its trigger, resistance when applicable and application.
- `RANDOM_ENEMY` and `RANDOM_ALLY` select independently for each component from currently eligible participants. A paired `PRIMARY` damage-power modifier and damage component share one selection. Deferred random targets reserved before the turn ends are reused by their `POST_ACTION` components.
- `triggerChancePercent` belongs to the component. A blank field means 100%, and one trigger roll is shared by all its applicable targets.
- Resistance is calculated separately for each target from the user's effective LCK and the target's effective RES.
- `SkillEffectOutcome` distinguishes `APPLIED`, `RESISTED` and `NOT_TRIGGERED` for presentation.
- A resisted or non-triggered applicable component is still a valid attempt and consumes the action and resources. A failed condition or inapplicable component is not a valid attempt.
- Conditional components are evaluated against runtime state when their ordered position is reached, so later conditions can observe damage or attribute changes produced earlier in the same execution.

### Combat calculations and effects

- `DamageCalculator` owns physical and magical damage formulas for skills and basic attacks, with `KF = 20` and `KM = 20`.
- `RecoveryCalculator` owns ATK- or MAG-scaled HP recovery and revival; MP and SP recovery remain fixed.
- `EffectResistanceCalculator` owns the LCK-against-RES formula, `KR = 1` and the 85% application cap.
- Negative attribute stages and dissipation are resistible. Distorção Ancestral is not.
- Negative stages applied to the user are also resistible; each such component compares the user's effective LCK with its own effective RES when it executes.
- Attribute stages track positive and negative values and durations independently.
- Distorção Ancestral swaps the structural values of ATK/MAG and DEF/RES while stages remain attached to their named attributes.
- Distorção is reversible, persists visibly across turns, stays outside purification and dissipation and is removed by battle cleanup.
- `DAMAGE_POWER_MODIFIER` affects every later compatible damage component against the same target during one execution. Multiple modifiers use overflow-protected accumulation.
- `RECOIL_DAMAGE` derives a percentage from the actual damage of the immediately preceding single-enemy damage component and applies direct non-lethal damage to the user.
- Recovery-item purification is connected to active negative attribute states.
- Damage uses ATK for physical or MAG for magical attacks by default; a skill damage component may choose another offensive attribute while preserving its physical or magical defense.
- Partial purification and dissipation remove up to the specified number of randomly selected affected attributes; blank power removes all.
- HP sacrifice uses a nonlethal percentage of the user's current HP. HP balance averages the current HP of living allies and caps each result at that ally's maximum HP.

## V4 natural character progression

The natural-growth and level-based skill-availability block is complete.

- `CharacterGrowthCalculator` derives a complete natural `CharacterStats` snapshot from one playable-class definition and target level.
- Base growth after level 1 is `HP +25`, `MP +10`, `SP +10` and `+1` to every regular attribute per level.
- The primary attribute gains one additional point every two levels; the secondary attribute gains one additional point every three levels.
- ATK specialization adds SP growth, MAG adds MP growth, and DEF or RES adds HP growth on the same schedules. Defensive specializations are cumulative.
- Growth is calculated directly from class base stats and final level, avoiding incremental drift when several levels are gained at once.
- Natural stats remain separate from temporary effects and implemented equipment bonuses.
- `PlayableCharacter` keeps its immutable class and skill definitions while storing the stable IDs of skills it has actually learned.
- Eligible `LEVEL` skills are learned during construction, after level gains and when a controlled class transition installs a new natural skill roster; `SCROLL` skills are never granted automatically.
- `BattleRewardService` records natural stats before and after leveling, detects every newly learned non-scroll skill and restores surviving characters only after the new maximums are installed.
- `BattleRewardScreen` displays compact stat deltas and newly learned skill names; `CharacterStatusMenu` displays the character's complete class name and symbol.
- Experience and level arithmetic reject overflow instead of wrapping silently.

## V5 playable-character level boundary

The playable-character progression boundary required for save validation is implemented.

- `PlayableCharacterLevelRules` centralizes the minimum level 1, current maximum level 20 and next-level experience formula.
- `CharacterGrowthCalculator` rejects target levels outside the supported range.
- `DevelopmentPartyFactory` rejects invalid configured levels before constructing a partial party.
- `PlayableCharacter` stops at level 20, keeps maximum-level EXP at zero and ignores later EXP gains.
- EXP crossing the maximum is partially accepted only up to level 20; the remainder is discarded.
- EXP addition uses `long` intermediate arithmetic to prevent integer overflow before progression is updated.
- `CharacterStatusMenu` displays `Nível máximo` instead of requesting progress toward level 21.
- Normal progression, exact arrival at level 20, excess EXP, later maximum-level rewards, invalid development level 21 and out-of-range skill and equipment requirements were manually validated.
- `SkillDefinition` and `EquipmentDefinition` reject level requirements outside the shared supported range.
- Seven final armor definitions inherited from the earlier level-25 progression were aligned with the current level-20 ceiling.

## V4 skill-detail presentation

- `CharacterStatusMenu` presents available skills as a compact selectable list instead of expanding every description at once.
- `SkillDetailFormatter` derives level, main type, power, action target and resource cost from the immutable skill definition.
- The main display component is the first effective component in `PRIMARY`; secondary components remain described by the external narrative text.
- Character status and battle confirmation reuse the same formatted skill sheet.
- Narrative descriptions avoid promising final damage or recovery amounts that depend on effective attributes and combat formulas.
- The displayed level represents automatic acquisition for `LEVEL` skills and the minimum scroll-use requirement for `SCROLL` skills.
- Skill-scroll details reuse the same formatter instead of maintaining a separate technical presentation.

## V4 skill-scroll integration

The local skill-scroll architecture and behavior are complete.

- `skill_scrolls.txt` links item, skill and playable-class IDs.
- `SkillScrollLoader` validates item categories, acquisition types, catalog references, class agreement, duplicate links and complete specialization.
- `SkillScrollDefinition` and `SkillScrollDefinitionCatalog` store immutable specialized data.
- `ItemFactory` creates the specialized runtime `SkillScroll` using the canonical `SkillDefinition`.
- `PlayableCharacter` stores learned skill IDs in deterministic order.
- `SkillLearningResult` reports successful and rejected learning outcomes.
- `SkillScrollService` consumes one owned scroll only after successful learning or replacement.
- `SkillScrollUseMenu` supports class and level rejection, duplicate rejection, a maximum of two learned scroll skills, cancellation and confirmed replacement.
- Only learned `SCROLL` skills may be replaced; `LEVEL` skills remain protected.
- Forgotten scroll skills do not return their original item.
- Scroll-learned skills reuse normal status display, battle selection and skill execution.
- Skill scrolls remain outside battle inventory and can use the existing enemy-drop system.
- Temporary synthetic scrolls validated loading, learning, consumption, rejection, replacement and battle execution before being removed.

No permanent scroll item, scroll skill or curated scroll drop is currently registered. The seven initial scroll-skill concepts remain draft content and some require additional combat-effect mechanics.

## V4 equipment

The equipment block is complete and contains 67 weapons, 25 armors and 22 trinkets, totaling 114 specialized definitions linked to base items.

- `EquipmentLoader` reads all three external files and registers definitions in one `EquipmentCatalog`.
- `EquipmentDefinition` centralizes level requirements and all nine possible stat bonuses.
- `WeaponDefinition`, `ArmorDefinition` and `TrinketDefinition` add their category-specific fields.
- `ArmorWeight` replaced the former `PlayableClassWeight` and is shared by class and armor compatibility.
- `DamageType` moved to `rpg.battle`, where it serves skills and weapon attacks.
- `CharacterEquipment` owns one weapon, one armor and one trinket per playable character.
- `EquipmentService` transfers units between the shared inventory and character slots, including replacement and removal.
- Equipped units leave inventory, so no `(E)` marker or special sale filtering is required.
- Every class may use every weapon and trinket after meeting the level requirement; armor requires exact weight compatibility.
- Every equipment category may grant HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK bonuses.
- Natural stats remain unchanged by equipment. Public maximums and attributes add structural equipment bonuses before temporary combat effects.
- Lower maximums clamp current HP, MP and SP after equipment changes; higher maximums do not heal.
- One shared `EquipmentMenu` supports equipping from inventory and managing slots from character status.
- `EquipmentDetailFormatter` presents details and change-only confirmation comparisons with `Poder` as the player-facing weapon-power label.

## Equipment-aware combat

- A player's basic attack uses the equipped weapon's damage type, normal-attack name and base power.
- An unarmed basic attack falls back to physical damage with zero weapon power.
- Every damaging skill adds 100% of the equipped weapon's base power.
- A skill's declared damage type remains independent from the equipped weapon's physical or magical classification.
- `SkillExecutionContext` captures weapon base power for one skill use.
- Enemy basic attacks remain physical with zero weapon power.
- Skill powers and resource costs remain provisional until the general balance pass.

## Reward modifiers

- `PartyRewardModifierState` supports one temporary EXP modifier and one temporary GOLD modifier at the same time.
- Durations decrease only after a rewarded victory; escape and defeat do not consume them.
- An equipped trinket's EXP modifier applies only to its wearer.
- GOLD modifiers from every equipped trinket are summed for the party.
- Temporary and equipment percentages combine additively before one rounding.
- Character status displays total individual M.EXP.
- Party status and battle rewards display temporary global M.EXP and total effective M.GOLD.
- Battle rewards intentionally do not list every ally's individual M.EXP, preserving readability for a four-member party.
- Ascensão, Prosperidade and Sucesso are outside-battle consumables loaded from `reward_modifier_items.txt`; they grant +50% EXP, +50% GOLD or +25% to both for 15 rewarded victories.
- Activation never replaces an occupied modifier slot. Sucesso activates both slots atomically or preserves the item and both existing states.
- The inventory confirms use, reports a blocked slot on the item sheet and returns to the refreshed consumable list after success.

## Character attributes

The complete character attribute set is:

- HP — Health Points
- MP — Mana Points
- SP — Stamina Points
- ATK — Physical Attack
- DEF — Physical Defense
- MAG — Magic
- RES — Magic Resistance
- VEL — Speed
- LCK — Luck

Initial values are grouped in immutable `CharacterStats`. Maximum HP and all six regular attributes must be positive; maximum MP and SP may be zero. Enemy and playable-class loaders report the source line and content ID when invalid values are found.

Current HP, MP and SP remain inside `GameCharacter` because they change during gameplay.

Temporary attribute stages and special battle states belong to each character's `CombatEffectState`. Battle formulas consume effective attributes rather than repeating stage logic.

Recovery power is not a character attribute. Recovery values belong to skill components or items. `RecoveryCalculator` calculates scaled skill recovery, while `GameCharacter.recoverHp()`, `recoverMp()`, `recoverSp()` and `revive()` apply changes to character resources.

## V4 initial character and party creation

The initial character-creation block is complete.

- `CharacterNameRules` centralizes normalization, validation, the 16-code-point limit and case-insensitive comparison.
- Valid names use Unicode letters separated only by single spaces, hyphens or apostrophes.
- Names are normalized to NFC, outer spaces are removed and repeated ordinary spaces are collapsed.
- `PlayableCharacter` accepts only normalized valid names.
- `Party` rejects a new member whose name duplicates an existing member without regard to letter case.
- `character_names.txt` provides external names for random selection.
- `CharacterNameLoader` validates every external name, rejects case-insensitive duplicates and requires at least four valid entries.
- `CharacterNameCatalog` stores the validated names and `CharacterNameSelector` selects uniformly after exclusions.
- `CharacterCreationMenu` supports manual naming, random naming, repeated randomization, name changes, confirmation and cancellation.
- Character creation displays the Flagelo's complete class name, primary and secondary specializations and external description.
- Cancelling returns to `TitleScreen` without constructing partial character or party state.
- A confirmed normal new game creates only one level-1 Flagelo with full resources.
- The confirmed protagonist becomes the first member and leader of the new `Party`.
- `Main` no longer contains a hardcoded normal party.
- The `--dev-party` argument bypasses normal creation and delegates to `DevelopmentPartyFactory`.
- `DevelopmentPartyFactory` supports one to four configured members with distinct classes, positive levels, fully restored resources, configurable initial gold and optional temporary reward modifiers. An optional development-only skill-scroll grant remains disabled by default; temporary scroll definitions are not part of the current game data.
- Every playable class defines one visible Unicode symbol for compact interface identification.
- `PartyStatusMenu` displays complete names and symbols in a two-line layout.
- `CharacterStatusMenu` displays the complete character name together with the class symbol and name.
- Character names are not truncated in character interfaces.

The four current entries in `character_names.txt` are temporary placeholders. Final name curation belongs to the creative team.

## V4 full-party defeat and rescue

The full-party defeat and rescue block is complete.

- `Battle` displays the final defeat message and returns `BattleResult.DEFEAT` without terminating the application.
- Every combat scenario externally defines an optional preferred rescue city, a required fallback rescue city and a narrative rescue description.
- The preferred city is selected only when already discovered; otherwise, the fallback city is used.
- Every registered city has exactly one INN, and every configured rescue destination is validated against the scenario and INN catalogs during initialization.
- Rescue costs the greater value between the destination INN price and 10% of current party gold rounded upward, limited to the gold currently available.
- A party with no gold is rescued without payment.
- `World` coordinates destination selection, payment, complete restoration and movement to the selected city.
- `Party.fullyRestoreMembers()` revives every defeated member and fully restores HP, MP and SP.
- `PartyRescueScreen` presents the external narrative, selected destination and exact cost before requiring ENTER.
- The failed exploration session is discarded without advancing or completing its current position.
- Rescue preserves EXP, levels, learned skills, remaining gold, inventory, equipment, reward modifiers, world progress, unlocked routes and shop state.
- Rescue may discover an undiscovered fallback city without unlocking unrelated routes.
- Victory and escape do not activate rescue, and normal victory does not automatically revive defeated allies.

## Scenario discovery and paid city transport

V3 roadmap section 10 is complete.

Implemented scenario-discovery behavior includes:

- `WorldProgress` records stable IDs of scenarios entered during the active game.
- The initial scenario is discovered when `World` is created.
- Routes, fixed branches and paid transport all discover their destinations.
- Discovery remains separate from adjacent-route unlocking.
- Discovered IDs are exposed without exposing mutable internal state.

Paid city transport includes:

- External destination definitions loaded from `data/city_transport.txt`.
- Immutable `CityTransportDefinition` entries.
- Startup validation through `CityTransportLoader`.
- Destination lookup through `CityTransportCatalog`.
- A dedicated `CityTransportMenu`.
- Filtering by configured and discovered cities.
- Unnumbered `--` slots for undiscovered cities.
- Prices and balances formatted through `GameCurrency`.
- Availability from cities, fields and dungeons, including incomplete exploration.
- Confirmation before payment.
- Preservation of gold after cancellation or insufficient funds.
- Direct movement to the selected city and display of its travel description.
- Disposal of an abandoned incomplete `ExplorationSession` without losing permanent progress.

Adjacent routes remain free and continue to use `TravelRouteDefinition`, `TravelCatalog`, `TravelProgress` and `RouteMenu`.

## Centralized currency presentation

`GameCurrency` centralizes the visible currency name and numeric formatting. The current interface uses `GOLD` as the name and the `G` suffix without a separating space, such as `100G`.

Battle rewards, exploration rewards, inventory, party status, INNs, paid city transport and every shop price, total and balance use this shared presentation without moving gold ownership away from `Party`.

## V5 data-driven crafting

The initial V5 crafting integration is functionally complete.

The system adapts the crafting contribution created by Matheus to the architecture established through V4 while preserving its core definition, catalog, loader, service and menu responsibilities.

Implemented behavior includes:

* External recipes loaded from `data/crafting_recipes.txt`.
* External crafter NPCs loaded from `data/crafters.txt`.
* Immutable recipe, material and crafter definitions.
* Stable recipe and crafter IDs.
* Strict item, scenario and recipe reference validation during startup.
* Generic crafter terminology supporting different crafting professions.
* Multiple crafter NPCs in the same city.
* Independent ordered recipe sets for every crafter.
* Recipe names and narrative descriptions separate from output-item content.
* Ingredients and outputs from any item category.
* Positive ingredient quantities and exactly one output unit per successful operation.
* Complete ingredient validation before inventory mutation.
* Reporting of every insufficient ingredient.
* Output-quantity overflow protection.
* Atomic ingredient removal and output insertion with rollback protection.
* Integration with the shared party inventory.
* Integration with the existing city-location selection flow.
* Player-facing recipe inspection, confirmation, cancellation and feedback.
* Display of required and currently owned ingredient quantities.

Initial content includes:

* `Senhor das Armas` in Bree, offering the Espada de Ferro recipe.
* `Alquimista de Bree` in Bree, offering the Poção de Merumã recipe.
* External Madeira and Ferro material items.
* A multi-ingredient equipment recipe.
* A recovery-item recipe that consumes a `CONSUMABLE`.

The service has been validated independently for missing ingredients, exact requirements, excess owned quantities, multiple ingredients, successful consumption, single-unit output and inventory overflow. The complete interface flow has also been validated through purchase, crafting and inventory verification.

## V5 numeric resource boundaries

Numeric resource boundaries required for save validation are implemented.

- `Party.MAXIMUM_GOLD` centralizes the liquid-GOLD maximum of `999999999`.
- `Inventory.MAXIMUM_QUANTITY_PER_ITEM` centralizes the maximum of 999 units for each item ID.
- The inventory continues to support an unlimited number of different item IDs and one quantity entry per ID.
- Domain additions use `long` intermediate arithmetic and reject values that do not fit completely.
- Battle and exploration rewards grant only the portion that fits and explicitly report discarded GOLD or items.
- Shop purchases validate item capacity before deducting GOLD or reducing stock.
- Shop sales validate GOLD capacity before removing items.
- Crafting validates output capacity before consuming ingredients, including recipes whose output is also an ingredient.
- Equipment replacement and removal are rejected when the returning item cannot fit completely in inventory.
- Defensive rollback paths verify complete resource restoration.
- GOLD rewards, item rewards, purchases, sales, crafting, equipment replacement and equipment removal were manually validated at their limits.

## V5 world time and shop restocking

The fictional world clock and runtime shop-restocking cycle are functionally integrated.

- `WorldClock` stores one authoritative elapsed-hour value using `long`.
- New campaigns begin on day 1 at 08:00.
- Displayed day and hour values are derived using 24-hour days.
- Non-positive advancement and elapsed-hour overflow are rejected before mutation.
- `World.HOURS_PER_FINISHED_BATTLE` centralizes the current one-hour battle duration.
- Victories and defeats advance world time.
- Successful escape and avoidance before battle do not advance world time.
- Every midnight triggers one global replacement of all registered shop states.
- Every shop performs a complete new weighted offer selection with canonical initial stock.
- Old offers, depleted quantities and non-selected entries are not merged into the new cycle.
- Candidate generation completes before clock and shop-state replacement.
- Failed candidate generation preserves the previous time and shop states.
- City and combat-scenario menus display current world time above horizontally arranged action rows.
- Shop main menus additionally display the exact next midnight restocking boundary.
- `WorldTimeFormatter` displays `☼` from 06:00 through 17:59 and `☾` from 18:00 through 05:59.
- Clock progression, midnight restocking, restored stock, escape behavior and period-symbol boundaries were manually validated.

## Save and load

The version-4 persistence foundation is implemented through `rpg.persistence`.

Current completed behavior:

- Immutable snapshots represent party, character, equipment, inventory, reward-modifier, world-time, discovery, travel, permanent dungeon progression, shop, quest, permanent-flag, recruitment-candidate and bestiary-discovery state.
- The versioned campaign schema uses strict indexed Java properties encoded as UTF-8.
- Canonical booleans accept only lowercase `true` and `false`.
- Every registered dungeon requires exactly one valid progress entry.
- Earlier internal versions are rejected without migration; the encrypted outer-container version remains unchanged.
- AES-256-GCM encrypts and authenticates the complete campaign payload.
- Every encryption uses a new secure random 12-byte IV and a 128-bit authentication tag.
- The textual container exposes no campaign information outside authenticated ciphertext.
- Save replacement is performed through a completed temporary file, with atomic movement preferred and a safe fallback available.
- Save operations return distinct encoding and file-writing failures.
- Load operations distinguish absent, unreadable, invalid and successfully decoded saves.
- Invalid save files are preserved instead of being deleted or corrected.
- Snapshot, property, UTF-8, encryption, container, atomic-file and save/load service round trips were manually validated.

`CampaignSnapshotRestorer` validates every catalog-backed reference and reconstructs a complete isolated `RestoredCampaign`, including party order, progression, learned scroll skills, equipment, resources, inventory, reward modifiers, world progress, travel progress, fictional time, finite shop stock, quest progress, permanent flags, candidate state and discovered bestiary IDs. The reconstructed candidate becomes active only after every loading stage and every cross-state validation succeeds.

`QuestProgressRestorer` and `RecruitmentCandidateStateRestorer` provide single-use domain-local restoration gateways over package-private mutation APIs. The persistence orchestrator reconstructs all three narrative state holders before validating quest, flag, candidate, leader-class and party coherence.

After the unchanged title screen, `CampaignSelectionMenu` distinguishes absent, unreadable, invalid and valid saves. Valid campaigns may be continued or replaced through the confirmed New Game flow. Every INN provides atomic Save and Continue and Save and Exit operations, while ordinary exits require confirmation and never save implicitly.

Complete encrypted round trips, campaign reconstruction, valid and invalid startup flows, preservation of previous saves, INN saving, failed writes, exit-without-saving behavior and version-2 dungeon-progress restoration were manually validated before Part 9. Part 9 additionally validated explicit version-2 rejection and a complete version-3 save, restart, Continue, resave and second-restart cycle with empty narrative collections. Non-empty narrative persistence remains scheduled with canonical quest content.

## V6 quest and narrative foundation

V6 Parts 1 through 9 are implemented. Part 10 added the first five canonical quests, and their normal project playthrough has been validated, including the Quest 5 ceremony, class selection and first-ally recruitment. Portable Windows build verification remains pending.

The current foundation includes:

- Complete immutable quest definitions assembled from external quest, step and objective files.
- Sequential linear quest steps and eight closed objective types.
- Mutable campaign-level `QuestProgress` separated from immutable definitions.
- `NOT_STARTED`, `ACTIVE` and `COMPLETED` quest lifecycle.
- Explicit synchronous quest-progress events and ordered result objects.
- Permanent external flag definitions and mutable set-only `CampaignFlags`.
- Reusable external AND requirement sets.
- Pure uncached requirement evaluation against leader level, inventory, quest status and flags.
- Quest-availability validation rejecting missing sets, self-dependencies and dependency cycles.
- External material quest rewards for GOLD, party EXP and items.
- External quest effects for setting flags, unlocking directed routes or branches, granting items and transporting the party.
- Complete completion-plan validation before campaign mutation.
- Atomic delivery consumption, reward application, permanent effects and quest-state transition.
- Explicit completion failures that preserve every affected state.
- External narrative city locations with requirement-controlled visibility.
- Canonical NPC identities separated from fixed city placements.
- External linear dialogues with canonical speakers, party-name placeholders and a placeholder for a recruitment candidate's persistent generated name.
- External quest-dialogue associations for offer, acceptance, refusal, pending and completion contexts.
- Stable NPC interaction menus preserving the permanent `Conversar` option.
- Automatic narrative-text wrapping at 90 console columns.
- Read-only quest journal available from stable city and combat-scenario menus.
- Separate active and completed quest listings with contextual quest details.
- Current-step objective counters and material reward summaries derived from canonical data.
- Manual retry of an already completed final non-NPC step when atomic quest completion was previously blocked.
- `QuestProgressCoordinator` as the UI boundary between confirmed gameplay events, domain progression and contextual presentation.
- Battle progress from confirmed analysis, enemy defeat, victory and collected-drop events.
- Item-acquisition progress after successful purchases, crafting, exploration rewards, battle drops and quest rewards.
- Scenario-arrival progress emitted only after a successful scenario change.
- Dungeon-completion progress emitted after either narrative or recurring boss victory.
- Derived `[!]` markers on actionable NPCs, narrative locations and applicable city actions.
- Derived `[!]` markers on immediately reachable quest destinations and their parent travel actions without revealing locked or multi-step paths.
- Compact battle progress displayed inline, with dedicated presentation retained for quest completion, effects and failures.
- Required canonical `flagelo_iniciante` and final `flagelo` definitions, validated before dependent class data is loaded.
- Shared beginner and final Flagelo presentation, base statistics, specializations and armor weight with separate natural-skill rosters.
- External final class-path definitions associating every selectable path with level-7 introductory weapon and armor.
- Immutable external class-path questions and answers with supported narrative line breaks and party placeholders.
- Seven-question canonical questionnaire content balanced across the six recommendable final classes.
- One-point questionnaire scoring with ordered direct and tied highest-score results.
- Player-controlled resolution of two-way and three-way ties without random recommendation.
- Unrestricted access to all seven final paths after rejecting the questionnaire result.
- Shared complete class presentation across character creation, recommendation details and free selection.
- Timed one-time class revelation followed by compact acceptance, rejection and optional-description presentation.
- Transient questionnaire, recommendation and confirmation state with no partial campaign mutation.
- Validated protagonist class transition preserving identity, level, EXP and scroll skills while replacing natural class state.
- Atomic equipment return, introductory-item grant, resource restoration and permanent ceremony-flag assignment.
- Explicit inventory-capacity failure and repeated-application prevention before transition mutation.
- External quest-step recruitment definitions with canonical candidate IDs and configurable ally entry levels.
- Structural limit of three recruitment records derived from party capacity.
- Minimal mutable candidate state containing canonical ID, persistent generated name and recruited status.
- Idempotent candidate introduction at the first displayed `RECRUIT` dialogue.
- Shared unique-name selection across party members and pending candidates.
- Runtime resolution of `RECRUIT` speakers from established candidate state.
- Generic ally-class selection excluding beginner Flagelo, final Flagelo, party classes and caller-provided classes.
- Generic complete ally construction at the externally configured level.
- Natural attributes and skills calculated through the existing progression classes.
- Introductory weapon and armor equipped directly without passing through shared inventory.
- Empty ally trinket slot, zero EXP and fully restored resources.
- Generic recruitment plans and services reusable by later narrative allies.
- Ceremony-specific composition isolated in `ClassPathRecruitmentService`.
- Joint validation of protagonist inventory additions and quest rewards before mutation.
- Mandatory `Aceitar sua companhia` action without a false refusal branch.
- Ordered atomic commit of protagonist transition, quest completion, rewards, effects, ally insertion and recruited state.
- Party EXP applied before ally insertion so the newly recruited ally remains at zero EXP.
- Expected ceremony and quest failures presented without partial campaign mutation.

The new external files are:

- `quests.txt`
- `quest_steps.txt`
- `quest_objectives.txt`
- `flags.txt`
- `requirements.txt`
- `quest_rewards.txt`
- `quest_effects.txt`
- `narrative_locations.txt`
- `npcs.txt`
- `npc_placements.txt`
- `dialogues.txt`
- `quest_dialogues.txt`
- `class_paths.txt`
- `class_path_questions.txt`
- `class_path_answers.txt`
- `quest_recruitments.txt`
- `dungeon_boss_gates.txt`
- `combat_scenario_dialogues.txt`

The complete narrative interaction was validated through one temporary quest and NPC. Representative testing covered city, location and NPC markers; default conversation; offer refusal and later acceptance; quest activation; required completion dialogue; atomic completion; a real GOLD reward; grouped notifications; marker removal after completion; and preservation of default dialogue access.

Part 6 representative testing covered journal navigation without world-time advancement; active and completed quest details; current objective counters and reward summaries; manual retry after an atomic reward-capacity failure; confirmed analysis followed by escape; grouped enemy-defeat, battle-victory and collected-item progress; immediate route and city-transport markers; scenario-arrival progress; narrative dungeon completion; marker removal after objective completion; and inline presentation of simple battle progress.

Part 7 representative testing covered required beginner and final Flagelo validation; complete final-path equipment coverage; questionnaire loading, line-break decoding and placeholder preservation; direct scoring; ordered three-way ties; player-controlled tie resolution; unrestricted rejection and free selection; shared class presentation; optional description access; invalid-input handling without replaying timed sequences; inventory-capacity rejection without mutation; equipped-item return; introductory-item grants; class and natural-skill replacement; complete resource restoration; permanent-flag assignment; and repeated-transition prevention.

Part 8 representative testing covered header-only startup; candidate creation at the first recruit dialogue; stable candidate name across repeated offer refusal and acceptance; the then-current version-2 restart boundary; multiple valid ally-class draws; exclusion of beginner Flagelo, final Flagelo and the protagonist's confirmed class; complete level-7 ally state; direct introductory equipment; empty trinket slot; second-position party insertion; mandatory acceptance; quest reward and effect application; zero ally EXP after `PARTY_EXP`; prevention of repeated recruitment; combined protagonist-item and quest-reward capacity rejection without mutation; minimum-level ceremony rejection without mutation; maximum three-record enforcement; duplicate candidate rejection; duplicate quest-step rejection; and invalid ally-level rejection.

Part 9 added immutable quest, flag and candidate snapshots; strict version-3 property serialization and deserialization; explicit version-2 incompatibility; domain-local quest and candidate restorers; complete narrative-state injection through `RestoredCampaign`; and cross-state validation for quest steps, objective counters, flags, candidates, party membership, distinct classes and the first ceremony transaction. The encrypted outer container, AES-GCM behavior and atomic file replacement remain unchanged.

## V7 interface polish

V7 interface polish is implemented, tested and released:

- Shared prose wrapping uses 75 visible columns, respects authored line breaks and ignores ANSI color codes when measuring text.
- Console clearing and ordinary screen headings are centralized in `ConsoleUI`.
- World, location and functional screens use consistent contextual headings and iterative feedback.
- Inventory, equipment, status and character-creation presentation received focused layout and navigation updates.
- Dialogue presents two lines per page with Unicode-aware character animation. Enter reveals the current page before a subsequent Enter advances.
- Battle uses a fixed participant panel for up to three enemies and four allies, with resources, compact effects, defeated participants and bounded name display.
- Action results replace the battle action menu with paged messages inside the panel; a participant reaching zero HP receives explicit feedback when combat continues.
- Battle target selection marks eligible enemies and allies directly in the panel. Battle item use and skill results return their messages to battle presentation.
- The tactical menu provides resource and effect overviews, an effect legend, dedicated enemy analysis and a preview of remaining actions.
- Normal, boss and epic battles have contextual headings. `BattleScreen` presents the entry animation separately from ordinary panel redraws.
- Out-of-battle consumable and skill-scroll details use fixed-height pages; ally selection shows resources in a dedicated fixed-height screen.
- Victory rewards use a fixed-height summary followed by level-growth details only when a character levels up, with extra pages only when content exceeds the available space.
- Character status and battle selection show four natural and two scroll skill positions, including non-selectable empty slots. Battle selection groups positions by column and abbreviates long names only in that menu.
- Character-status skill details use shared wrapping and fixed 17-line pages with reserved feedback. Skill descriptions consistently show attribute stages as repeated arrows.

Part 7 travel, exploration, INN-saving and battle-message presentation is complete and visually verified. Route and branch menus resolve destination names from scenario IDs; route, branch and paid-transport descriptions animate after valid selection. Exploration results animate within the stable combat-scenario layout. Enter reveals complete travel, exploration and battle-message screens without terminal echo. Saving presents its animation and actual success or failure inside the INN screen.

Part 8 completed fixed and percentage-based recovery and revival items, battle-only attribute-stage items, selective normalization items and persistent EXP/GOLD reward potions. Additional skill effects, conditions, attribute-based damage scaling and independent random targeting were also implemented and tested. Ineffective item attempts preserve the item, applicable confirmations remain cancellable and successful outside-battle results return to the refreshed inventory.

## V8 enemy combat

The V7 carryover for INN resting is complete. A successful rest uses a separate bounded result screen with shared wrapping and line animation; Enter reveals the complete result before a subsequent Enter returns to the INN.

The first enemy-combat block is implemented and verified:

- Every runtime `Enemy` has a positive level that remains hidden from the player.
- `EnemyDefinition` stores level-1 natural stats and independently optional primary, secondary and weakness attributes.
- `EnemyGrowthCalculator` centralizes enemy natural growth, weakness behavior, rank stat multipliers, rounding and exact arithmetic.
- Enemy natural MP and SP grow by 5 per level and remain independent from rank and specialization bonuses.
- Enemy ranks multiply HP and offensive attributes after natural level growth. `LEGENDARY` remains reserved without configured stat multipliers.
- Random encounter entries define inclusive minimum and maximum levels. `EncounterGenerator` rolls each generated copy independently and carries the selected definition and level through `EnemySpawn`.
- Dungeon data defines a fixed narrative-boss level and separate recurring-boss and epic ranges. Every enemy inside one boss or epic group shares the level selected for that encounter.
- Encounter and dungeon loaders validate growth at every configured maximum level so invalid ranges and arithmetic overflow fail during initialization.
- All current enemy species were rebased to intentional level-1 stats. Optional specializations and limited lore-specific base adjustments preserve distinct identities without using arbitrary historical values as the primary balancing mechanism.
- Verdes Ermos and Gruta Relamparrocha were rebalanced and verified through normal campaign play. The Mestre Eletrogoblin narrative encounter was approved at level 2 after completion with a level-4 protagonist using the Adaga Improvisada and Gibão Comum.

Enemy level does not change EXP, GOLD or drops in V8. Rewards continue to depend on the combat scenario, rank and independent drop entries.

Current authored dungeon levels are:

| Dungeon | Narrative boss | Recurring boss | Epic |
|---|---:|---:|---:|
| Gruta Relamparrocha | 2 | 2–3 | 4–5 |
| Minas do Além-Túmulo | 10 | 9–11 | 12–14 |
| Mata do Agouro | 13 | 12–14 | 14–16 |
| Ruínas de Sanguemar | 15 | 14–16 | 16–18 |

All four dungeon records pass startup validation at their configured maximum levels. Automated boundary coverage creates every narrative boss and both endpoints of each recurring and epic range. Random encounter ranges outside the verified opening regions remain preliminary content values for later representative regional play.

The second enemy-combat block is also implemented and verified:

- `enemy_skills.txt` stores reusable enemy skill headers with stable IDs, MP/SP costs and player-facing names.
- `enemy_skillsets.txt` links enemy species to skills through positive minimum enemy levels.
- `skill_effects.txt` now serves both playable and enemy skill catalogs through globally unique IDs and shared ordered components.
- `CombatSkillDefinition` provides the minimal common execution contract implemented by `SkillDefinition` and `EnemySkillDefinition`.
- `SkillExecutionContext`, `SkillExecutor` and `SkillUseState` accept the shared combat contract and any acting `GameCharacter` without generalizing playable progression, learning or slot ownership.
- `EnemySkillsetLoader` validates cross-catalog references, duplicate links, complete enemy-skill coverage and resource capacity at each link's minimum level.
- `EnemyRank` defines runtime loadout ceilings. `BattleCoordinator` filters links by level and selects per-instance skills without repetition before constructing each runtime `Enemy`.
- Enemy MP and SP now gain 5 points per level without rank or specialization bonuses.
- Enemy action selection is uniform between the basic attack and every currently affordable installed skill.
- Resources are spent only after a shared execution can consume the turn. Ineffective selected skills preserve resources and fall back to the basic attack.
- Enemy target meanings are resolved relative to the acting side while preserving the shared effect calculations, resistance, ordered phases, pending VEL and defeat behavior.
- Enemy basic attacks remain physical, use ATK against DEF and receive zero weapon base damage. Enemy skills also receive zero weapon base damage and instead use their component power and declared physical or magical type.
- All 47 current enemy species have authored skill content. The first pass contains 83 reusable enemy skill headers, 90 links and 123 effect components.
- Current enemy content intentionally uses clear single-enemy or self-targeted damage, attribute stages and self-recovery. Related goblins, herbalísticos, lobermos, crustaceans and mine-security robots reuse appropriate general skills.

Representative gameplay verified enemy physical skill damage, MP/SP consumption and exhaustion, playable-skill regression, self-buffing, resistible debuffs, pending VEL, multiple installed skills and basic-attack fallback. Negative startup tests verified global skill-ID uniqueness, unknown effect references, enemy skills without links and resource capacity at the linked minimum level.

The third enemy-combat block is implemented and verified:

- `EnemyTargetProfile` defines `INSTINCTIVE`, `ERRATIC`, `PREDATOR` and `TACTICAL` behavior independently from rank.
- Every runtime `Enemy` retains its profile, while `EnemyTargetSelector` owns independent per-instance target memory during the battle.
- Instinctive enemies acquire a uniform random living target and hold it until defeat.
- Erratic enemies redraw uniformly before every individual action and may repeat their previous target.
- Predator and Tactical enemies use positive weighted acquisition, missing-HP percentage and a penalty against defending targets for damaging actions.
- Tactical enemies additionally compare the effective DEF or RES relevant to the selected action.
- Scored profiles preserve their current valid target until the best alternative exceeds the frozen retarget margin.
- Non-damaging and mixed-damage actions do not compare DEF/RES; mixed damage still considers Defend.
- Self, ally, group and explicitly random target types preserve their previous resolution.
- All 47 current species have one authored profile: 12 instinctive, 15 erratic, 9 predator and 11 tactical.
- Enemy analysis exposes that profile as the player-facing personality Instintiva, Errática, Predadora or Tática while retaining the fixed 17-line battle layout.

The contributed Bestiary has been adapted to the current V8 architecture:

- `BestiaryStateCatalog` stores discovered canonical enemy IDs as mutable campaign state.
- A successful analysis reuses the IDs already collected by `Battle`; `BattleCoordinator` records both bestiary discovery and quest analysis progress without changing `Battle`'s analysis rule.
- `Diário` opens a paginated `BestiaryMenu`. Undiscovered entries show `???`; discovered entries show name, rank, target personality, optional specializations, optional weakness, description, height and weight.
- All 47 V8 enemy records retain their current stats, growth attributes, skills and target profiles while receiving the contributed height and weight values.
- `BestiarySnapshot` participates in snapshot creation, strict property serialization, isolated restoration and runtime injection.
- Internal save format version 4 stores discovered enemy IDs and validates them through `EnemyCatalog`. Earlier internal versions are rejected; the encrypted outer container remains version 1.
- The Bestiary and its feedback remain within the established 17-line interface boundary.

## Current limitations and temporary implementations

- Permanent skill-scroll content and curated scroll drops remain absent while the draft skills await required combat mechanics and final content review.
- Playable-class definitions, natural growth and the complete approved 30-skill roster are external; skill powers and resource costs remain provisional pending the general balance pass.
- Enemy action selection is intentionally uniform and does not yet evaluate HP thresholds, tactical usefulness, action weights or species-specific preferences. Target profiles affect only the selected individual opponent after the action is chosen.
- More complex enemy use of ally targeting, group targeting, purification, dissipation and other shared effects remains deferred until justified by concrete content.
- Current enemy skill pools stay at or below their rank ceilings. Guaranteed moves for future pools above those ceilings remain undesigned.
- Enemy-level reward scaling is intentionally absent from V8; any future scaling requires a separate economic balance decision.
- Random encounter ranges outside Verdes Ermos and Gruta Relamparrocha remain preliminary until their regions receive representative playtesting. Dungeon narrative, recurring and epic ranges have a complete first authored pass but may still receive later tuning.
- `RECRUIT` references are now validated through `QuestRecruitmentCatalog` and resolved through candidate state.
- After a normal victory, defeated allies remain unavailable until revived by a skill, item or city INN; full-party defeat instead activates rescue and complete restoration.
- Items sold by the player cannot be bought back.
- Normal new games currently begin with only the protagonist. The first-ally runtime behavior is implemented; later allies remain future narrative content.
- Candidate naming reuses the implemented shared external-name selection and preserves one generated name across the active campaign and version-4 save/load cycles.
- The external character-name catalog has been expanded and can continue receiving content additions without code changes.
- Party-title acquisition and character-history presentation remain future narrative work.
- Initial crafting content is limited to two Bree crafters and two recipes pending broader creative and balance curation.
- Crafters are currently restricted to city scenarios.
- Batch crafting, variable output quantities, crafting fees, recipe discovery, crafting levels, random failure and equipment enhancement are not implemented.


## Current next step

V7 was released as `v7.0.0` and integrated into `master`. V8 development continues on `feature/v8-enemy-combat`.

The INN rest-result carryover, enemy levels, encounter strength, reusable enemy skills, first-pass skill content, initial uniform enemy action choice, individual target profiles and Bestiary integration are implemented and verified.

V8 is feature-complete. The next work is the remaining representative regional regression, complete diff review, portable-package verification and release procedure defined by the active roadmap. Later contextual action intelligence remains separate from the completed target-profile block.

## Important conventions

- Stable IDs use lowercase letters and underscores.
- Display names may be translated or changed without changing IDs.
- External data fields are separated by semicolons.
- Lines beginning with `#` are comments.
- Definitions describe reusable immutable data.
- Runtime objects represent individual mutable game instances.
- External references are validated during game initialization.
- Inventory and gold belong to `Party`.
- Character resources and progression belong to `PlayableCharacter`.
- Git operations and manual compilation are performed through Git Bash.
- Eclipse is used to edit, compile and run the Java project.
