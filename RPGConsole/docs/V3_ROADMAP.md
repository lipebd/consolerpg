# RPGConsole V3 Roadmap

## Release goal

V3 expands RPGConsole from a data-driven combat prototype into a data-driven world with structured exploration, external game content, city services, character progression and persistent game state.

The implementation must preserve stable IDs, strict startup validation and separation between immutable definitions and mutable runtime state.

## Confirmed design decisions

* Common scenario data is stored separately from combat-scenario and dungeon-specific data.
* Long descriptions in external files use the literal `\n` sequence for line breaks.
* IDs remain stable, lowercase and without accents.
* Adjacent scenario travel is free.
* Fast travel between discovered cities is separate from adjacent travel and always costs gold.
* Fast-travel cost is fixed by destination city.
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

> **On hold:** Initial implementation is being developed separately and will be reviewed before this section begins.

* [ ] Define fixed shops separately from their stock.
* [ ] Define `shops.txt`.
* [ ] Define `shop_stock.txt`.
* [ ] Associate each shop with a scenario.
* [ ] Support multiple shops in one city.
* [ ] Support different stocks in different cities.
* [ ] Validate all referenced item IDs.
* [ ] Implement buying.
* [ ] Implement selling.
* [ ] Validate party gold before purchases.
* [ ] Update shared inventory and gold after transactions.
* [ ] Decide whether each stock entry is finite or infinite.
* [ ] Decide when finite stock is restored.
* [ ] Decide whether modified stock is persisted.
* [ ] Define how exploration merchants select their stock.
* [ ] Preserve the one-merchant-per-exploration-session rule.
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

## 11. External skills and combat effects

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

## 12. Playable classes

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

## 13. Equipment

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

## 14. Character and party creation

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

## 15. Full-party defeat and rescue

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

## 16. Save and load

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

## 17. Exploration decisions still under study

* [x] Keep special areas eligible for the `Nothing` event.
* [ ] Decide whether some special areas guarantee a specific event.
* [x] Preserve permanent gains when leaving a scenario while discarding its current exploration session.
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

## 18. Validation and regression tests

* [ ] Test malformed scenario data.
* [ ] Test duplicate scenario IDs.
* [ ] Test missing combat-scenario definitions.
* [ ] Test missing dungeon definitions.
* [ ] Test invalid enemy references.
* [ ] Test invalid route and branch references.
* [ ] Test malformed item, reward, shop, skill and class data.
* [ ] Test battles after all data externalization.
* [ ] Test party rewards and level-ups.
* [ ] Test defeated-character behavior.
* [ ] Test normal and special exploration areas.
* [ ] Test mandatory dungeon bosses.
* [ ] Test optional epic encounters.
* [ ] Test adjacent travel.
* [ ] Test fixed branches.
* [ ] Test paid city transport.
* [ ] Test city services and shops.
* [ ] Test character creation.
* [ ] Test full-party rescue.
* [ ] Test save and load.
* [ ] Audit every external-data loader that accepts player-facing text for literal `\n` conversion.
* [ ] Run a complete new-game playthrough.
* [ ] Test the portable build outside the repository.
* [ ] Test gold and per-item quantity boundaries, overflow attempts and invalid save values.

## 19. Documentation and V3 release

* [ ] Update `DATA_FORMATS.md`.
* [ ] Document the literal `\n` convention for all external descriptions.
* [ ] Update `ARCHITECTURE.md`.
* [ ] Update `MECHANICS.md`.
* [ ] Update `PROJECT_STATE.md`.
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
