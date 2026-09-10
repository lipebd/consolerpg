# RPGConsole — Project State

## Current version

V4 gameplay-systems implementation in progress.

Current stage:

`Combat effects, external skill execution and natural character progression completed`

Current Git branch:

`feature/v4-gameplay-systems`

The V1 MMMVP is preserved by the tag:

`v1.0-mmmvp`

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
- Merchant limited to one appearance per exploration session.
- Epic encounter limited to one offer per dungeon session.
- Exploration progress survives battles.
- Exploration sessions reset when a new combat scenario session begins.

### Scenarios and travel

- Scenario inheritance hierarchy with `Scenario`, `CombatScenario`, `Field`, `Dungeon` and `City`.
- `ScenarioCatalog` stores scenarios by stable ID.
- `TravelCatalog` stores directed routes between scenarios.
- `RouteMenu` supports multiple unlocked adjacent destinations.
- Adjacent travel from combat scenarios remains locked until exploration is complete.
- Bree, Verdes Ermos and Gruta de Relamparrocha are connected.
- Scenario descriptions are displayed when entering a map and after returning from battle.

### Gruta de Relamparrocha

- Eleven-position dungeon session.
- Random normal and special-area encounters.
- Optional Relampagolem epic encounter.
- Epic encounter may be accepted or avoided.
- Mestre Eletrogoblin forced as the final boss.
- Fleeing from the boss returns the party to the previous position and keeps travel locked.
- Victory unlocks travel from the dungeon.

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
- The `Round` action displays up to five queue entries without consuming an action.

### Battle escape

- The acting playable character performs the flee attempt.
- The first check uses effective Luck directly as a percentage.
- A failed first check triggers a comparative Speed and Luck calculation.
- Living enemy averages are used by the comparative check.
- Comparative chance has configured minimum and maximum limits.
- A failed attempt consumes the acting character's action.
- A successful attempt removes the entire party from battle.
- Fleeing from a dungeon boss returns the exploration session by one position.

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

`World` dispatches fields and dungeons to `CombatScenarioMenu` and cities to `CityMenu`. Each action list is built incrementally from the current scenario state. Both menus preserve continuous numbering, omit unavailable actions and use whitespace to separate action groups without adding unnecessary submenu layers.

World, combat-scenario, city, city-location, INN, travel, branch, inventory and status menus use iterative navigation and explicit back commands.

## City structure and Bree services

Cities now use a dedicated action flow instead of the combat-scenario menu.

The city menu currently supports:

- Visiting a functional city location, including INNs and fixed city shops.
- Leaving through an unlocked adjacent route.
- Opening the shared inventory.
- Viewing party status.
- Explicitly ending the game.

Unavailable actions are omitted. Paid city transport appears only when at least one configured and discovered destination city is available.

City locations are treated as a navigation concept rather than a generic service hierarchy. `CityLocationMenu` displays only functional location names, while each selected service owns its details and interaction flow.

Bree currently provides one externally configured INN and one fixed city shop. `CityLocationMenu` combines the available location names without owning their service-specific behavior.

The Bree INN:

- Charges a provisional fixed price of 25 gold for the whole party.
- Remains accessible when the party cannot afford a stay.
- Prevents unnecessary, unaffordable and cancelled payments.
- Requests confirmation before charging.
- Revives defeated members before restoring resources.
- Fully restores HP, MP and SP for the entire party.
- Keeps the player inside the INN after resting.
- Displays action feedback inside the current INN screen.

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

`ShopStateGenerator` creates every shop's active offers once during application initialization. It performs weighted selection without replacement and safely accumulates selection weights.

The generated runtime structure is:

```text
ShopStateCatalog
    -> ShopState
        -> ShopOfferState
```

Each `ShopOfferState` preserves its remaining quantity. Active offers and quantities remain unchanged between repeated accesses during the current execution.

Exhausted offers disappear from the purchase list but are not replaced by unused entries. Shops remain accessible when all active offers are exhausted. Restocking and shop-state persistence remain postponed until save and load are implemented.

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

```text
items.txt
    -> ItemLoader
    -> ItemDefinitionCatalog

recovery_items.txt
    -> RecoveryItemLoader
    -> RecoveryItemDefinitionCatalog

ItemDefinitionCatalog
RecoveryItemDefinitionCatalog
    -> ItemFactory
    -> ItemCatalog
```

`ItemDefinitionCatalog` and `RecoveryItemDefinitionCatalog` contain immutable data loaded during application initialization.

`ItemFactory` combines the base and specialized definitions. It currently creates:

- `BasicItem` for items without specialized runtime behavior.
- `RecoveryItem` for items with a recovery definition.

`ItemCatalog` contains the complete runtime objects used by enemy drops, exploration rewards, shop offers, menus and inventory.

Base item definitions may independently declare:

- `baseShopSalePrice`, used to calculate offers sold by shops.
- `shopPurchasePrice`, used when shops buy owned items from the party.

The presence of `shopPurchasePrice` determines whether an item is sellable to shops. `playerTradable` remains reserved for future player-to-player trading.

The current recovery-item model supports:

- HP recovery.
- MP recovery.
- SP recovery.
- Revival.
- Combined effects.
- Single-ally targeting.
- Whole-party targeting.
- Purification of active negative attribute states.

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
- Paginates lists with eight item rows per page.
- Keeps list height stable on partially filled pages.
- Truncates long names only in list views.
- Opens a details screen before offering a contextual action.

`BattleItemMenu` lists only consumables that can currently be used during battle.

`RecoveryItemUseMenu` centralizes target selection, effect application, result display and conditional item removal for both contexts.

An ineffective item attempt does not consume the item or the acting character's battle action.

The shared inventory has no weight or capacity limit.

## V4 combat effects and external skills

The first V4 gameplay-system block is complete.

### External playable-class and skill definitions

- All seven playable classes have stable external definitions and startup validation.
- `skills.txt` stores immutable skill headers.
- `skill_effects.txt` stores ordered effect components.
- `SkillLoader` combines and validates both files and registers complete `SkillDefinition` objects in `SkillCatalog`.
- `PlayableCharacter` references its class definition and exposes skills unlocked at its current level.
- The old hardcoded `Skill` and `RecoveryType` types were removed.
- The temporary development party is still assembled in `Main`, but its class and skill content comes from loaded definitions.

### Skill execution

- One `SkillExecutionContext` preserves the user and targets selected for the complete skill use.
- `SkillExecutor` processes `PRE_ACTION`, `PRIMARY` and deferred `POST_ACTION` components in sequence.
- Existing temporary-effect durations decrease after the acting character consumes its turn and before `POST_ACTION` components are applied.
- Each component is evaluated in this order: condition, applicability, trigger, resistance when applicable, then application.
- `triggerChancePercent` belongs to the component. A blank field means 100%, and one trigger roll is shared by all its applicable targets.
- Resistance is calculated separately for each target from the user's effective LCK and the target's effective RES.
- `SkillEffectOutcome` distinguishes `APPLIED`, `RESISTED` and `NOT_TRIGGERED` for presentation.
- A resisted or non-triggered applicable component is still a valid attempt and consumes the action and resources. A failed condition or inapplicable component is not a valid attempt.

### Combat calculations and effects

- `DamageCalculator` owns physical and magical damage formulas for skills and basic attacks, with `KF = 20` and `KM = 20`.
- `RecoveryCalculator` owns ATK- or MAG-scaled HP recovery and revival; MP and SP recovery remain fixed.
- `EffectResistanceCalculator` owns the LCK-against-RES formula, `KR = 1` and the 85% application cap.
- Negative attribute stages and dissipation are resistible. Distorção Ancestral is not.
- Attribute stages track positive and negative values and durations independently.
- Distorção Ancestral swaps the structural values of ATK/MAG and DEF/RES while stages remain attached to their named attributes.
- Distorção is reversible, persists visibly across turns, stays outside purification and dissipation and is removed by battle cleanup.
- `DAMAGE_POWER_MODIFIER` affects every later compatible damage component against the same target during one execution. Multiple modifiers use overflow-protected accumulation.
- Recovery-item purification is connected to active negative attribute states.

## V4 natural character progression

The natural-growth and level-based skill-availability block is complete.

- `CharacterGrowthCalculator` derives a complete natural `CharacterStats` snapshot from one playable-class definition and target level.
- Base growth after level 1 is `HP +25`, `MP +10`, `SP +10` and `+1` to every regular attribute per level.
- The primary attribute gains one additional point every two levels; the secondary attribute gains one additional point every three levels.
- ATK specialization adds SP growth, MAG adds MP growth, and DEF or RES adds HP growth on the same schedules. Defensive specializations are cumulative.
- Growth is calculated directly from class base stats and final level, avoiding incremental drift when several levels are gained at once.
- Natural stats remain separate from temporary effects and future equipment bonuses.
- `PlayableCharacter` keeps its immutable class definition and exposes skills available for its current level dynamically.
- `BattleRewardService` records natural stats before and after leveling, detects every newly unlocked skill and restores surviving characters only after the new maximums are installed.
- `BattleRewardScreen` displays compact stat deltas and newly learned skill names; `CharacterStatusMenu` displays the character's class.
- Experience and level arithmetic reject overflow instead of wrapping silently.

## V4 skill-detail presentation

- `CharacterStatusMenu` presents available skills as a compact selectable list instead of expanding every description at once.
- `SkillDetailFormatter` derives level, main type, power, action target and resource cost from the immutable skill definition.
- The main display component is the first effective component in `PRIMARY`; secondary components remain described by the external narrative text.
- Character status and battle confirmation reuse the same formatted skill sheet.
- Narrative descriptions avoid promising final damage or recovery amounts that depend on effective attributes and combat formulas.

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

`GameCurrency` centralizes the visible currency name and numeric formatting. The current interface uses `GOLD` as the name and `G$ ` as the value prefix.

Battle rewards, exploration rewards, inventory, party status, INNs, paid city transport and every shop price, total and balance use this shared presentation without moving gold ownership away from `Party`.

## Current limitations and temporary implementations

- Equipment behavior and specialized equipment data are not implemented.
- Skill-scroll behavior and specialized skill-scroll data are not implemented.
- The development party is still assembled in `Main`; normal character and party creation are not implemented.
- Playable-class and skill definitions are external and natural class growth is integrated, but the full approved skill roster remains unfinished.
- Enemy turns currently use basic attacks; enemy skill selection is deferred.
- Tavern functionality remains unavailable.
- Defeated allies remain unavailable until revived by a skill, item or city INN.
- Full-party defeat still uses the temporary Game Over flow.
- Shop state is regenerated when the program starts and is not persisted.
- Shops do not restock during the current execution.
- Items sold by the player cannot be bought back.

## Current next step

The combat-effect, external skill-execution, natural-progression and learned-skill presentation foundations in V4 roadmap sections 1 and 2 are complete. Remaining unchecked items in those sections include the full approved skill roster and reward modifiers.

The next implementation block should be selected from those remaining section 2 dependencies before equipment work begins. Normal character creation, equipment, defeat rescue and save/load remain later V4 blocks. The level-7 class-path decision is tracked as post-V4 work. The temporary development party must not be mistaken for either final flow.

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
- Git operations are performed through Git Bash.
- Eclipse is used to edit, compile and run the Java project.
