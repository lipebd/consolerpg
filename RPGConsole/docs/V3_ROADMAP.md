# RPGConsole V3 Roadmap

## Release goal

V3 expands RPGConsole from a data-driven combat prototype into a data-driven world with structured exploration, external scenario, item and reward content, city services, shops, scenario discovery and paid city travel.

The implementation must preserve stable IDs, strict startup validation and separation between immutable definitions and mutable runtime state.

Character progression, combat-system expansion, equipment, character creation, full-party rescue and persistence are outside this release. Their requirements are preserved in [`V4_ROADMAP.md`](V4_ROADMAP.md).

## Confirmed design decisions

* Common scenario data is stored separately from combat-scenario and dungeon-specific data.
* Long descriptions in external files use the literal `\n` sequence for line breaks.
* IDs remain stable, lowercase and without accents.
* Adjacent scenario travel is free.
* Fast travel between discovered cities is separate from adjacent travel and always costs gold.
* Fast-travel cost is fixed by destination city.
* Paid city travel may leave a city, field or dungeon.
* Leaving an incomplete exploration through paid city travel discards only its current session and preserves all permanent gains.
* Currency display name and value formatting are centralized through `GameCurrency`.
* Cities may provide multiple named exits with their own flavor text.
* Cities do not use exploration sessions.
* Exploration follows the current-position-first model:

  * The party arrives at position N.
  * The current position and its available paths are displayed.
  * Exploring resolves the event at position N.
  * After resolution, the party advances to position N + 1.
* A fixed branch is available while the party remains at its configured position.
* Exploring past a branch position makes that branch unavailable during the current session.
* Entering another scenario through a branch ends the current exploration session.
* The world travel action is displayed only when the current position provides a valid destination.
* Exploration rewards use weighted, scenario-specific pools.
* Normal and special areas may use different reward pools.
* Special areas remain eligible for the `Nothing` event.
* One reward event selects exactly one pool entry.
* The selected entry grants either an item or gold, never both simultaneously.
* Reward quantity is randomized between the configured minimum and maximum.
* Shops and merchants may have different stocks according to their city or location.
* The initial scenario ID may remain configured in `Main` until save data determines the current location.
* World interfaces display the contextual header before narrative output.
* Current state is displayed before the available actions.
* Action results are grouped beneath their scenario context.
* Menus are displayed after narrative and state information.
* Internal execution order must not leak into the player-facing presentation.

---

## 1. V3 foundation and scenario data

* [x] Create the `feature/v3-data-driven-scenarios` branch.
* [x] Create `ScenarioType`.
* [x] Create `ScenarioDefinition`.
* [x] Create `ScenarioDefinitionCatalog`.
* [x] Create `ScenarioLoader`.
* [x] Create `data/scenarios.txt`.
* [x] Externalize scenario IDs, types, names and descriptions.
* [x] Support literal `\n` line breaks in scenario descriptions.
* [x] Create `CombatScenarioDefinition`.
* [x] Create `CombatScenarioDefinitionCatalog`.
* [x] Create `CombatScenarioLoader`.
* [x] Create `data/combat_scenarios.txt`.
* [x] Externalize base rewards and exploration settings.
* [x] Reject combat data associated with cities.
* [x] Reject combat data associated with unknown scenarios.
* [x] Create `DungeonDefinition`.
* [x] Create `DungeonDefinitionCatalog`.
* [x] Create `DungeonLoader`.
* [x] Create `data/dungeons.txt`.
* [x] Externalize mandatory boss and optional epic encounter IDs.
* [x] Reject dungeon data associated with non-dungeon scenarios.
* [x] Reject dungeon data without corresponding combat data.
* [x] Validate boss and epic enemy references during startup.
* [x] Create `ScenarioFactory`.
* [x] Create runtime `City`, `Field` and `Dungeon` objects from loaded definitions.
* [x] Remove Bree, Verdes Ermos and Gruta Relamparrocha definitions from `Main`.
* [x] Register generated scenarios in `ScenarioCatalog`.
* [x] Keep the complete scenario loading and creation pipeline inside startup error handling.
* [x] Run the initial scenario-loading smoke test.
* [x] Mark the V2 build and tag tasks as completed in `V2_ROADMAP.md`.
* [x] Create `V3_ROADMAP.md`.
* [x] Reject epic encounter chances configured for non-dungeon scenarios.
* [x] Validate that the mandatory dungeon boss uses the expected enemy rank.
* [x] Validate that a configured epic enemy uses the expected enemy rank.
* [x] Reject duplicate enemy definition IDs.
* [x] Require every dungeon to define one epic enemy.
* [x] Keep epic encounters optional for the player.

## 2. External travel routes

* [x] Define the `travel_routes.txt` format.
* [x] Give each route a stable ID.
* [x] Store source and destination scenario IDs.
* [x] Store a player-facing route name.
* [x] Store route flavor text with literal `\n` support.
* [x] Create the travel-route definition.
* [x] Create the travel-route loader.
* [x] Create the travel-route catalog.
* [x] Reject duplicate route IDs.
* [x] Reject routes referencing unknown scenarios.
* [x] Reject routes whose source and destination are identical.
* [x] Load adjacent routes during startup.
* [x] Remove hardcoded travel registrations from `Main`.
* [x] Preserve free adjacent travel between scenarios.
- [x] Create mutable travel-route progress.
- [x] Initialize route unlock state from external data.
- [x] Hide locked routes until they are unlocked.
- [x] Support scenario-specific exit menus.
* [x] Support multiple named exits from the same city.
- [x] Keep locked and unknown exits hidden until they become available.
* [x] Test all routes between Bree, Verdes Ermos and Gruta Relamparrocha.

## 3. Current-position-first exploration

* [x] Replace `Explorar` with `Enfrentar o boss` at the final dungeon position.
* [x] Keep the party at the final dungeon position after fleeing.
* [x] Hide travel actions when the current position has no valid destination.
* [x] Refactor exploration so the event belongs to the current position.
* [x] Display the current position before resolving its event.
* [x] Advance only after the current position has been resolved.
* [x] Preserve exploration progress after regular battles.
* [x] Treat fleeing from a regular battle as resolving the current position.
* [x] Keep the party at the final dungeon position after fleeing from the boss.
* [x] Prevent the final position from advancing beyond the scenario length.
* [x] Revalidate normal and special area selection under the new flow.
* [x] Revalidate first-position and final-position restrictions.
* [x] Test exploration-session completion under the new model.

## 4. Fixed exploration branches

* [x] Define `scenario_branches.txt`.
* [x] Give every branch a stable ID.
* [x] Store the source scenario ID.
* [x] Store the exact exploration position.
* [x] Store the destination scenario ID.
* [x] Store a player-facing action name.
* [x] Store branch flavor text with literal `\n` support.
* [x] Create the branch definition.
* [x] Create the branch loader.
* [x] Create the branch catalog.
* [x] Reject branches referencing unknown scenarios.
* [x] Reject positions outside the source scenario.
* [x] Display `Seguir outro caminho` only at a position containing a valid branch.
* [x] Hide the branch action when no destination is available.
* [x] Allow the party to enter the branch destination before exploring past it.
* [x] End the current exploration session after entering another scenario.
* [x] Invalidate the missed branch after the party advances.
* [x] Add the fixed Verdes Ermos branch leading to Setegalhos.
* [x] Test entering Setegalhos.
* [x] Test ignoring the branch and continuing through Verdes Ermos.
* [x] Test returning to the branch in a new exploration session.
* [x] Store destination exploration positions in external travel routes.
* [x] Validate destination positions against combat-scenario exploration length.
* [x] Add the Setegalhos route entering Verdes Ermos after the branch.
* [x] Test entering Verdes Ermos at position 4.

## 5. External item data

* [x] Define `items.txt`.
* [x] Create stable item IDs.
* [x] Define the initial item categories.
* [x] Create immutable item definitions.
* [x] Create the item loader.
* [x] Create the item catalog.
* [x] Validate duplicate IDs and invalid item data.
* [x] Support literal `\n` in long item descriptions.
* [x] Externalize the current healing item and support data-driven revival components.
* [x] Support external basic items such as materials and future equipment definitions.
* [x] Replace hardcoded item creation with catalog resolution.
* [x] Preserve shared party inventory.
* [x] Test item quantities and inventory stacking.
* [x] Include `SKILL_SCROLL` in the initial item categories.
* [x] Keep player trading eligibility separate from NPC selling eligibility.
* [x] Define composable specialized item data.
* [x] Define `recovery_items.txt`.
* [x] Support combined HP, MP, SP, revival effects.
* [x] Reject recovery items without any effective component.
* [x] Consume a recovery item only when at least one component succeeds.
* [x] Store `SINGLE_ALLY` or `ALL_ALLIES` target scope for recovery items.
* [x] Apply group-item effects independently to every eligible party member.
* [x] Consume a group item only when at least one party member is affected.
* [x] Refactor the inventory screen after completing the external item migration.
* [x] Separate the general inventory from the battle item menu.
* [x] Show only battle-usable consumables during battle.
* [x] Prevent unavailable item use from consuming the battle turn.
* [x] Organize the general inventory by item categories.
* [x] Keep category counts hidden from the category menu.
* [x] Sort items deterministically inside each category.
* [x] Add pagination to item lists.
* [x] Use fixed-width item and quantity columns.
* [x] Truncate long item names without leaving whitespace before the ellipsis.
* [x] Display full item names in the details screen.
* [x] Open every selected item through a details screen.
* [x] Display descriptions and relevant item data only in the details screen.
* [x] Use contextual actions according to the runtime item type.
* [x] Keep prices and commercial information out of the regular inventory.
* [x] Preserve unlimited shared inventory capacity.
* [x] Revalidate item use inside and outside battle.


## 6. Exploration reward pools

* [x] Convert unavailable merchant rolls to `NOTHING`.
* [x] Rename or generalize the current `ITEM` exploration event to `REWARD`.
* [x] Generalize encounter area restrictions for reuse by reward pools.
* [x] Define `exploration_rewards.txt`.
* [x] Store the scenario ID.
* [x] Store the normal, special or unrestricted area restriction.
* [x] Support `ITEM` reward entries.
* [x] Support `GOLD` reward entries.
* [x] Store relative selection weights.
* [x] Store minimum and maximum quantities.
* [x] Require an item ID for item rewards.
* [x] Reject an item ID for gold rewards.
* [x] Validate referenced item IDs.
* [x] Reject invalid quantities and weights.
* [x] Build independent pools for each scenario and area type.
* [x] Select exactly one weighted entry per reward event.
* [x] Randomize its quantity within the configured range.
* [x] Add item rewards directly to shared inventory.
* [x] Add gold rewards directly to shared party gold.
* [x] Create distinct normal and special-area pools for Verdes Ermos.
* [x] Require no reward pool when a scenario has zero reward chance.
* [x] Test item and gold rewards independently.
* [x] Test pools whose weights do not total 100.
* [x] Rename `itemChance` to `rewardChance` across settings, loaders and external scenario data.
* [x] Rename `EncounterAreaRestriction` to `ExplorationAreaRestriction`.
* [x] Move the generalized area restriction to the exploration package.
* [x] Include `ANY` entries in both normal and special eligible pools.
* [x] Treat reward pools as repeatable and exclude unique, narrative and quest rewards.
* [x] Reject only completely identical reward entries.
* [x] Allow the same item in multiple entries when another field differs.
* [x] Reject zero-weight entries and use comments to disable rewards temporarily.
* [x] Reject a missing normal pool when reward events are possible.
* [x] Reject a missing special pool when special reward events are possible.
* [x] Keep reward selection independent from battle-drop rolls.
* [x] Preserve independent rolls for every enemy drop entry.
* [x] Add `pelo_lupino` as a basic material item for pool validation.
* [x] Test `ANY` entries combined with area-specific entries.
* [x] Test exact duplicate rejection.
* [x] Test missing-pool validation.

## 7. World and scenario menus

* [x] Rename the world action label from `Itens` to `Inventário`.
* [x] Keep every inventory category visible in deterministic enum order.
* [x] Keep empty inventory categories selectable.
* [x] Display an empty-category message only after the category is selected.
* [x] Keep the inventory open when the entire inventory is empty.
* [x] Require explicit back or close commands throughout empty-inventory navigation.
* [x] Preserve item sorting, pagination, details and contextual item actions.
* [x] Refactor world scenario actions into logical visual groups.
* [x] Separate scenario actions, travel actions and party-resource actions visually.
* [x] Keep continuous numeric action selection across displayed groups.
* [x] Separate logical action groups with spacing instead of additional player-facing headings.
* [x] Add the `[X] Status` shortcut outside battle.
* [x] Keep party status accessible from every world scenario.
* [x] Add inventory access outside battle.
* [x] Keep the grouped world menu extensible for future equipment access without adding a placeholder action.
* [x] Display travel only when an unlocked route exists from the current scenario.
* [x] Keep exploration branches separate from travel actions.
* [x] Build available world actions incrementally instead of duplicating complete action lists for each state.
* [x] Keep unavailable actions hidden instead of displaying non-functional placeholders.
* [x] Preserve consistent back and exit commands across menu levels.
* [x] Keep menu navigation iterative and avoid recursive menu calls.
* [x] Test representative world states and inventory navigation paths.

## 8. City structure and Bree services

* [x] Rename `ScenarioActionMenu` to `CombatScenarioMenu`.
* [x] Rename `ScenarioAction` to `CombatScenarioAction`.
* [x] Keep the renamed combat-scenario menu exclusive to fields and dungeons.
* [x] Create a dedicated `CityMenu`.
* [x] Provide separate city actions for visiting a location and leaving through an adjacent exit.
* [x] Keep inventory and party status accessible from the city menu.
* [x] Keep continuous numeric selection across city-menu action groups.
* [x] Omit unavailable city actions instead of displaying non-functional placeholders.
* [x] Keep the future city fast-travel action absent until city transport is implemented.
* [x] Add a dedicated menu for selecting functional city locations.
* [x] Display location names in the selection menu and details only after entering a location.
* [x] Keep unimplemented shops, taverns and other services out of the city-location menu.
* [x] Treat city locations as a navigation concept instead of creating a generic service hierarchy.
* [x] Define `inns.txt`.
* [x] Store a stable INN ID.
* [x] Associate each INN with a city scenario ID.
* [x] Store each INN's name, description and fixed party price.
* [x] Support literal `\n` in INN descriptions.
* [x] Create immutable INN definitions.
* [x] Create the INN loader and catalog.
* [x] Validate referenced scenario IDs and require the `CITY` scenario type.
* [x] Reject empty IDs, names and descriptions.
* [x] Reject invalid prices and duplicate INN IDs.
* [x] Allow at most one INN definition per city.
* [x] Configure Bree's INN with a provisional price of 25 gold.
* [x] Keep INN prices externally configurable for future balancing.
* [x] Keep the INN accessible when the party cannot afford a stay.
* [x] Prevent resting when every party member is already fully restored.
* [x] Prevent resting when the party cannot afford the configured price.
* [x] Request confirmation before charging the party.
* [x] Preserve party gold when resting is unnecessary, unaffordable or cancelled.
* [x] Deduct gold only after every requirement and confirmation succeeds.
* [x] Revive defeated party members before restoring their resources.
* [x] Fully restore HP, MP and SP for every party member.
* [x] Preserve the current behavior of `GameCharacter.restoreAllResources()` outside the INN.
* [x] Display immersive messages for unnecessary, unaffordable and successful stays.
* [x] Keep the player inside the INN after resting until an explicit back command.
* [x] Reuse unlocked `TravelRouteDefinition` entries as city exits.
* [x] Use route display names for gates, roads, portals and other creative exit labels.
* [x] Preserve each route description as the exit's flavor and destination text.
* [x] Keep adjacent travel through city exits free.
* [x] Keep city exits separate from future paid city transport.
* [x] Preserve iterative navigation across city, location, INN and exit menus.
* [x] Test city navigation, INN loading, payment, restoration and free adjacent exits.

## 9. Shops and merchants

### Commercial item data

* [x] Remove the redundant `npcSellable` property from base item definitions and runtime items.
* [x] Add optional `baseShopSalePrice` and `shopPurchasePrice` fields to `items.txt`.
* [x] Define `baseShopSalePrice` as the base amount charged by shops before an offer-specific percentage is applied.
* [x] Define `shopPurchasePrice` as the fixed amount paid by any shop when purchasing that item from the player.
* [x] Allow either commercial price to remain empty when the corresponding transaction is unavailable.
* [x] Require every present commercial price to be positive.
* [x] Use the presence of `shopPurchasePrice`, rather than a separate boolean property, to determine whether an item can be sold to shops.
* [x] Keep `playerTradable` exclusive to future player-to-player trading.
* [x] Keep all shop-specific availability, price variation and stock outside `ItemDefinition`.

### External shop definitions

* [x] Create `ShopAccessType` with `CITY` and `EXPLORATION`.
* [x] Create immutable shop definitions, catalogs and loaders.
* [x] Define `shops.txt` using `id;scenarioId;accessType;offerSlots;name;description`.
* [x] Require positive `offerSlots`.
* [x] Associate `CITY` shops only with city scenarios.
* [x] Associate `EXPLORATION` merchants only with fields and dungeons.
* [x] Support multiple shops or merchants in the same scenario.
* [x] Create immutable shop-stock entries, catalogs and loaders.
* [x] Define `shop_stock.txt` using `id;shopId;itemId;shopSalePricePercent;stock;weight`.
* [x] Require stable unique offer IDs and valid shop and item references.
* [x] Require positive price percentages, stock quantities and weights without limiting their totals to 100.
* [x] Require every stocked item to define `baseShopSalePrice`.
* [x] Require every shop to have at least one possible sale offer.
* [x] Reject offers from the same shop that repeat the same item, price percentage and stock quantity, regardless of their stable IDs or weights.
* [x] Allow distinct offers to reference the same item when their price percentages or stock quantities differ.
* [x] Calculate final shop sale prices by applying `shopSalePricePercent` to `baseShopSalePrice`.
* [x] Round final shop sale prices to the nearest integer, with half values rounded up.
* [x] Reject any calculated shop sale price below one.
* [x] Use overflow-safe arithmetic when calculating final shop sale prices.
* [x] For each item referenced by at least one sale offer, reject configurations in which `shopPurchasePrice` exceeds that item's lowest rounded final shop sale price.
* [x] Allow items that define `shopPurchasePrice` without appearing in any shop sale offer.
* [x] Permit equality between `shopPurchasePrice` and the lowest final shop sale price, producing zero resale profit.

### Runtime shop state

* [ ] Use finite per-offer stock in V3, superseding the earlier plan to use infinite shop stock.
* [x] Create separate mutable runtime state for each shop.
* [x] Generate every shop's active offers once during application initialization.
* [x] Select active offers by relative weight without replacement.
* [x] Accumulate selection weights using overflow-safe arithmetic.
* [x] Select up to `offerSlots` entries or every possible entry when fewer are available.
* [x] Allow separate active offers for the same item when their price percentages or stock quantities differ.
* [x] Store the remaining quantity of every selected offer.
* [ ] Reduce remaining stock only after a successful purchase.
* [x] Remove exhausted offers from the visible purchase list.
* [ ] Keep a shop accessible when all of its sale offers are exhausted.
* [x] Preserve selected offers and remaining quantities for the entire program execution.
* [x] Do not restock shops or merchants during the current execution.
* [x] Generate fresh shop state when a new program execution starts.
* [ ] Do not add items sold by the player to shop stock.
* [ ] Do not implement item buyback.
* [ ] Postpone shop-state persistence and restocking rules until save and load are implemented.

### Shop and merchant access

* [ ] Add `CITY` shops to the functional locations displayed by `CityLocationMenu`.
* [ ] Support multiple named shops in one city.
* [ ] Use the existing merchant exploration event to access `EXPLORATION` shops.
* [x] Require at least one eligible `EXPLORATION` merchant when a combat scenario has `merchantChance` greater than zero.
* [x] Ignore inaccessible `EXPLORATION` definitions associated with scenarios whose `merchantChance` is zero.
* [x] Select uniformly among all eligible exploration merchants associated with the current scenario.
* [x] Preserve the existing one-merchant-per-exploration-session rule.
* [x] Reuse the same shop runtime state whenever that exploration merchant is encountered in later exploration sessions.
* [ ] Preserve the interaction and navigation style established by `EpicEncounterMenu` for exploration merchants.
* [ ] Share shop definitions, runtime state, transaction rules and menus between city shops and exploration merchants whenever their behavior is identical.
* [ ] Allow every `CITY` and `EXPLORATION` shop to purchase any owned item that defines `shopPurchasePrice`.

### Shop interface

* [ ] Display the shop name, configured description and current party gold through `GameCurrency`.
* [ ] Provide the player-facing actions `Comprar`, `Vender` and `Voltar`.
* [ ] Display purchase offers with item name, formatted unit price and remaining stock.
* [ ] Keep separate offers selectable when they reference the same item but differ in price percentage or stock quantity.
* [ ] Sort purchase offers deterministically by item category and name.
* [ ] Break purchase-offer sorting ties by final unit price and stable offer ID.
* [ ] Paginate shop lists consistently with the existing inventory interface.
* [ ] Allow the player to select a purchase quantity up to the remaining stock.
* [ ] Skip quantity selection when only one unit is available.
* [ ] Display the complete item name, description, quantity, unit price and total price before purchase confirmation.
* [ ] Organize the sale interface by the existing item categories.
* [ ] Display only owned items that define `shopPurchasePrice` as sellable.
* [ ] Allow the player to select a sale quantity up to the owned quantity.
* [ ] Skip quantity selection when the player owns only one unit.
* [ ] Display the complete item name, description, quantity, unit price and total value before sale confirmation.
* [ ] Keep shops open after successful, cancelled or failed transactions until the player explicitly leaves.
* [ ] Display clear feedback for successful, cancelled and rejected transactions.

### Transaction rules and validation

* [ ] Validate purchase selection, quantity, remaining stock, calculated total and party gold before changing any state.
* [ ] Deduct party gold, add items to the shared inventory and reduce stock only after a confirmed valid purchase.
* [ ] Validate sale selection, quantity, owned inventory, calculated total and resulting party gold before changing any state.
* [ ] Remove items from the shared inventory and add party gold only after a confirmed valid sale.
* [ ] Preserve gold, inventory and shop stock when a transaction is invalid, unaffordable or cancelled.
* [ ] Use overflow-safe arithmetic for unit-price calculations, transaction totals and resulting gold balances.
* [ ] Prevent negative values, integer overflow, unintended item duplication and item loss during transactions.
* [ ] Keep every purchase and sale atomic from the player's perspective.
* [ ] Use `GameCurrency` for every displayed price, total and balance.

### Validation and manual tests

* [ ] Test malformed shop definitions, stock entries, prices, percentages, quantities, weights and references.
* [x] Test invalid scenario types and missing merchants for positive exploration merchant chances.
* [x] Test rejection of commercially duplicate offers regardless of stable ID or weight.
* [x] Test distinct offers for the same item with different price percentages or stock quantities.
* [x] Test weighted offer selection without replacement.
* [x] Test overflow protection when accumulating selection weights.
* [x] Test `offerSlots` below, equal to and above the number of possible offers.
* [ ] Test shop state preservation across repeated menu access and later merchant encounters.
* [ ] Test purchases, cancellations, insufficient gold, insufficient stock and exhausted shops.
* [ ] Test sales, cancellations, unsellable items and insufficient inventory quantities.
* [x] Test price rounding and per-item anti-arbitrage validation.
* [ ] Test overflow protection in price calculation, transaction totals and gold updates.
* [ ] Test fixed city shops and exploration merchants separately.


## 10. Scenario discovery and paid city travel

* [x] Rename `TravelMenu` to `RouteMenu`.
* [x] Rename the existing `CombatScenarioAction.TRAVEL` action to `FOLLOW_ROUTE`.
* [x] Display the free adjacent-route action as `Seguir caminho`.
* [x] Rename route-menu fields and methods to remove travel ambiguity.
* [x] Keep `RouteMenu` exclusive to unlocked adjacent `TravelRouteDefinition` entries.
* [x] Add `CITY_TRANSPORT` as a separate city and combat-scenario action.
* [x] Display the paid city-travel action as `Viajar`.
* [x] Keep paid city travel separate from adjacent routes and route unlocking.
* [x] Create runtime `WorldProgress` state.
* [x] Record discovered scenario IDs.
* [x] Mark the initial scenario as discovered.
* [x] Mark every scenario as discovered when first entered.
* [x] Cover destinations entered through routes, branches and paid travel.
* [x] Keep scenario discovery separate from route availability and access requirements.
* [x] Expose discovered IDs without exposing mutable internal state.
* [x] Prepare discovered-scenario state for future save data.
* [x] Define `city_transport.txt`.
* [x] Store the destination city scenario ID, fixed price and custom travel description.
* [x] Support literal `\n` in travel descriptions.
* [x] Create immutable city-transport definitions.
* [x] Create the city-transport loader and catalog.
* [x] Require every transport destination to reference an existing `CITY`.
* [x] Reject empty required fields, invalid prices and duplicate destination cities.
* [x] Configure Bree, Setegalhos, O Vão, Entrelugar, Deep Valley and Mist Suburb as transport destinations.
* [x] Use provisional destination prices of G$ 25, G$ 50, G$ 75, G$ 100, G$ 125 and G$ 150.
* [x] Keep early-game destinations cheaper than late-game destinations.
* [x] Keep Reino de Altaneve out of runtime data until its scenario definition is approved.
* [x] Add a dedicated `CityTransportMenu`.
* [x] List only discovered and configured destination cities.
* [x] Exclude the current city when travel begins from a city.
* [x] Keep discovered destinations visible when the party cannot afford them.
* [x] Number only selectable discovered destinations.
* [x] Display unnumbered `--` slots for cities that remain undiscovered.
* [x] Derive the number of destination slots from registered `CITY` scenarios instead of a hardcoded total.
* [x] Display discovered destinations before empty slots.
* [x] Show prices and available party currency through `GameCurrency`.
* [x] Use `GOLD` as the current display name and `G$` followed by a space as the value prefix.
* [x] Keep the currency name and value formatting centralized for future changes.
* [x] Omit the `Viajar` action when no valid destination city is available.
* [x] Allow paid city travel from cities, fields and dungeons.
* [x] Allow paid travel before a combat scenario's exploration is complete.
* [x] Keep paid travel unavailable during battles.
* [x] Request confirmation before charging the party.
* [x] Preserve party gold when travel is unaffordable or cancelled.
* [x] Deduct gold only after a valid destination and confirmation succeed.
* [x] Move the party directly to the selected destination city.
* [x] Display the destination's custom narrative travel description.
* [x] Discard an incomplete exploration session when paid travel leaves its scenario.
* [x] Preserve EXP, gold, items, bestiary analysis, discoveries, unlocks and all other permanent progress already obtained.
* [x] Create a fresh exploration session from the route-defined entry position when the abandoned scenario is entered again.
* [x] Preserve free adjacent travel to fields and dungeons.
* [x] Preserve iterative navigation across route and city-transport menus.
* [x] Test discovery through routes and exploration branches.
* [x] Test paid travel from a city and from an incomplete combat scenario.
* [x] Test destination filtering, empty slots, payment, cancellation, insufficient funds and permanent-progress preservation.

## 11. V3 validation and regression tests

* [ ] Test malformed scenario data.
* [ ] Test duplicate scenario IDs.
* [ ] Test missing combat-scenario definitions.
* [ ] Test missing dungeon definitions.
* [ ] Test invalid enemy references.
* [ ] Test invalid route and branch references.
* [ ] Test malformed item, reward, inn, shop and city-transport data.
* [ ] Test battles after V3 data externalization.
* [ ] Test party rewards and level-ups.
* [ ] Test defeated-character behavior.
* [ ] Test normal and special exploration areas.
* [ ] Test mandatory dungeon bosses.
* [ ] Test optional epic encounters.
* [ ] Test adjacent travel.
* [ ] Test fixed branches.
* [ ] Test paid city transport.
* [ ] Test city services and shops.
* [ ] Test centralized currency display across rewards, inventory, status, city services and paid travel.
* [ ] Audit every external-data loader that accepts player-facing text for literal `\n` conversion.
* [ ] Run a complete new-game playthrough.
* [ ] Test the portable build outside the repository.

## 12. Documentation and V3 release

* [ ] Update `DATA_FORMATS.md`.
* [ ] Document the literal `\n` convention for all external descriptions.
* [ ] Update `ARCHITECTURE.md`.
* [ ] Update `MECHANICS.md`.
* [ ] Update `PROJECT_STATE.md`.
* [x] Verify that all post-V3 requirements remain preserved in `V4_ROADMAP.md`.
* [ ] Review this roadmap and mark completed tasks.
* [ ] Remove obsolete temporary development code.
* [ ] Generate the V3 runnable JAR.
* [ ] Generate the portable Windows application.
* [ ] Copy all required external data files into the release build.
* [ ] Test the extracted release in an isolated directory.
* [ ] Decide the V3 release name and tag.
* [ ] Merge the V3 branch into `master`.
* [ ] Create and push the V3 Git tag.
* [ ] Publish the GitHub Release and portable ZIP.
