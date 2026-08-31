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

* [ ] Refactor exploration so the event belongs to the current position.
* [ ] Display the current position before resolving its event.
* [ ] Advance only after the current position has been resolved.
* [ ] Preserve the current position after regular battles.
* [ ] Preserve the current position after fleeing from regular battles.
* [ ] Preserve dungeon boss rollback behavior after fleeing.
* [ ] Prevent the final position from advancing beyond the scenario length.
* [ ] Revalidate normal and special area selection under the new flow.
* [ ] Revalidate first-position and final-position restrictions.
* [ ] Test exploration-session completion under the new model.

## 4. Fixed exploration branches

* [ ] Define `scenario_branches.txt`.
* [ ] Give every branch a stable ID.
* [ ] Store the source scenario ID.
* [ ] Store the exact exploration position.
* [ ] Store the destination scenario ID.
* [ ] Store a player-facing action name.
* [ ] Store branch flavor text with literal `\n` support.
* [ ] Create the branch definition.
* [ ] Create the branch loader.
* [ ] Create the branch catalog.
* [ ] Reject branches referencing unknown scenarios.
* [ ] Reject positions outside the source scenario.
* [ ] Display the travel action only at a position containing a valid branch.
* [ ] Hide or disable `[4] Viajar` when no destination is available.
* [ ] Allow the party to enter the branch destination before exploring past it.
* [ ] End the current exploration session after entering another scenario.
* [ ] Mark the destination as discovered.
* [ ] Invalidate the missed branch after the party advances.
* [ ] Add the fixed Verdes Ermos branch leading to Setegalhos.
* [ ] Test entering Setegalhos.
* [ ] Test ignoring the branch and continuing through Verdes Ermos.
* [ ] Test returning to the branch in a new exploration session.

## 5. External item data

* [ ] Define `items.txt`.
* [ ] Create stable item IDs.
* [ ] Define the initial item categories.
* [ ] Create immutable item definitions.
* [ ] Create the item loader.
* [ ] Create the item catalog.
* [ ] Validate duplicate IDs and invalid item data.
* [ ] Support literal `\n` in long item descriptions.
* [ ] Externalize current healing and revival items.
* [ ] Externalize materials and future equipment items.
* [ ] Replace hardcoded item creation with catalog resolution.
* [ ] Preserve shared party inventory.
* [ ] Test item quantities and inventory stacking.

## 6. Exploration reward pools

* [ ] Rename or generalize the current `ITEM` exploration event to `REWARD`.
* [ ] Generalize encounter area restrictions for reuse by reward pools.
* [ ] Define `exploration_rewards.txt`.
* [ ] Store the scenario ID.
* [ ] Store the normal, special or unrestricted area restriction.
* [ ] Support `ITEM` reward entries.
* [ ] Support `GOLD` reward entries.
* [ ] Store relative selection weights.
* [ ] Store minimum and maximum quantities.
* [ ] Require an item ID for item rewards.
* [ ] Reject an item ID for gold rewards.
* [ ] Validate referenced item IDs.
* [ ] Reject invalid quantities and weights.
* [ ] Build independent pools for each scenario and area type.
* [ ] Select exactly one weighted entry per reward event.
* [ ] Randomize its quantity within the configured range.
* [ ] Add item rewards directly to shared inventory.
* [ ] Add gold rewards directly to shared party gold.
* [ ] Create distinct normal and special-area pools for Verdes Ermos.
* [ ] Create distinct normal and special-area pools for Gruta Relamparrocha.
* [ ] Test item and gold rewards independently.
* [ ] Test pools whose weights do not total 100.

## 7. World and scenario menus

* [ ] Refactor field and dungeon menus into logical groups.
* [ ] Add the `[X] Status` shortcut outside battle.
* [ ] Keep party status accessible from every world scenario.
* [ ] Add inventory access outside battle.
* [ ] Prepare equipment access outside battle.
* [ ] Display travel only when a valid adjacent route or branch exists.
* [ ] Separate city locations from city exits.
* [ ] Separate adjacent travel from paid city transport.
* [ ] Prevent world menus from growing into a single flat list.
* [ ] Preserve consistent back and exit commands across submenus.
* [ ] Test menu navigation without recursive menu calls.

## 8. City structure and Bree services

* [ ] Create a dedicated city menu.
* [ ] Add city locations.
* [ ] Add custom city exits and gates.
* [ ] Add flavor text for each exit.
* [ ] Implement Bree's initial locations.
* [ ] Implement the Bree INN.
* [ ] Restore surviving and defeated party members through the INN flow.
* [ ] Define the INN price rules.
* [ ] Prevent payment when the party cannot afford the service.
* [ ] Preserve free adjacent travel through Bree's exits.
* [ ] Prepare city services for additional cities.

## 9. Shops and merchants

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

## 10. Scenario discovery and city transport

* [ ] Create runtime world-progression state.
* [ ] Record discovered scenario IDs.
* [ ] Mark scenarios as discovered when first entered.
* [ ] Define city transport data and destination costs.
* [ ] Assign a fixed transport cost to each destination city.
* [ ] Keep early-game destinations cheaper than late-game destinations.
* [ ] List only discovered destination cities.
* [ ] Exclude the current city from its own destination list.
* [ ] Keep city transport separate from adjacent exits.
* [ ] Require payment for every city-to-city transport.
* [ ] Prevent transport when the party cannot afford it.
* [ ] Deduct gold only after a valid destination is selected.
* [ ] Move the party directly to the destination city.
* [ ] Preserve free adjacent travel to fields and dungeons.
* [ ] Prepare discovered-scenario state for save data.
* [ ] Test transport between Bree and another discovered city.

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
* [ ] Implement temporary buffs.
* [ ] Implement temporary debuffs.
* [ ] Implement effect duration.
* [ ] Display active effects during battle.
* [ ] Prevent effects from leaking between unrelated battles.
* [ ] Add global EXP and gold reward modifiers.
* [ ] Add individual character EXP modifiers.
* [ ] Test healing, revival and effect skills after externalization.

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

## 17. Exploration decisions still under study

* [ ] Decide whether special areas may generate the `Nothing` event.
* [ ] Decide whether some special areas guarantee a specific event.
* [ ] Evaluate exploration checkpoints.
* [ ] Evaluate camps and temporary recovery.
* [ ] Decide which exploration state must survive leaving a scenario.
* [ ] Decide how completed dungeons behave when revisited.
* [ ] Decide whether optional epic encounters can return after being declined or defeated.

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
* [ ] Run a complete new-game playthrough.
* [ ] Test the portable build outside the repository.

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
