# RPGConsole — V2 Roadmap

## V2 objective

Transform the V1 prototype into a small but extensible RPG with data-driven enemies and drops, linear exploration, multiple areas, enemy groups and support for parties.

## Phase 1 — Data-driven enemies and encounters

- [x] Create `CharacterStats`.
- [x] Add all core character attributes.
- [x] Remove the obsolete character healing power.
- [x] Create `EnemyDefinition`.
- [x] Create `EnemyCatalog`.
- [x] Create `EnemyLoader`.
- [x] Create `data/enemies.txt`.
- [x] Load enemies from the external file.
- [x] Add stable IDs to enemies and scenarios.
- [x] Create `EncounterEntry` and `EncounterCatalog`.
- [x] Create `data/encounters.txt`.
- [x] Create `EncounterLoader`.
- [x] Create weighted random encounter selection.
- [x] Filter encounters by scenario and exploration position.
- [x] Add normal, special and unrestricted area availability.
- [x] Store the maximum number of copies per enemy.
- [x] Remove `startLobermoEncounter()`.
- [x] Remove hardcoded enemy attributes from `World`.

## Phase 2 — Linear exploration

Exploration works like movement through spaces on a board. Each use of the Explore action advances the player by one position.

- [x] Create `ExplorationEventType`.
- [x] Create `ExplorationSettings`.
- [x] Create `ExplorationSession`.
- [x] Store the current exploration position.
- [x] Configure the length of each explorable area.
- [x] Configure battle, item, merchant and epic event chances.
- [x] Validate that total random event chance does not exceed 100.
- [x] Generate one event for each position.
- [x] Add normal and special-area messages.
- [x] Prevent special areas on the first and last positions.
- [x] Limit the merchant to one appearance per session.
- [x] Allow a session without a merchant.
- [x] Complete Verdes Ermos after its configured exploration length.
- [x] Unlock travel after reaching the final position.

Deferred fixed branches:

- [ ] Create fixed exploration branching points.
- [ ] Allow a branching point to load another scenario.
- [ ] Make a missed branch unavailable after advancing past it.
- [ ] End the current exploration session when entering a branch.

## Phase 3 — Gruta de Relamparrocha and travel

- [x] Create `Dungeon` and `DungeonSettings`.
- [x] Add Gruta de Relamparrocha.
- [x] Define its description and available actions.
- [x] Define its base EXP and gold rewards.
- [x] Add its normal and special-area encounters.
- [x] Configure its exploration length and event chances.
- [x] Add the optional Relampagolem epic encounter.
- [x] Limit the epic encounter to one offer per session.
- [x] Allow the player to accept or avoid the epic encounter.
- [x] Force Mestre Eletrogoblin at the final dungeon position.
- [x] Return the player to the previous position after fleeing the boss.
- [x] Keep travel locked until the boss is defeated.
- [x] Create `ScenarioCatalog` and `TravelCatalog`.
- [x] Support multiple travel destinations.
- [x] Connect Bree, Verdes Ermos and Gruta de Relamparrocha.
- [x] Display scenario descriptions when entering or returning to maps.

## Phase 4 — External drop tables

- [x] Add stable IDs to items.
- [x] Implement `Item.equals()` and `hashCode()` using item IDs.
- [x] Allow items to be retrieved by ID.
- [x] Create `EnemyDropCatalog`.
- [x] Create `data/enemy_drops.txt`.
- [x] Create `EnemyDropLoader`.
- [x] Validate enemy and item references during loading.
- [x] Remove the Lobermo drop table from `World`.
- [x] Preserve Merumã drops.
- [x] Support multiple independent drops per enemy.
- [x] Support minimum and maximum drop quantities.
- [x] Consolidate equal item drops in battle results.
- [x] Prepare reward calculation for a list of defeated enemies.

## Phase 5 — World and battle-flow refactoring

- [x] Create `TravelMenu`.
- [x] Create `ScenarioActionMenu`.
- [x] Create `EpicEncounterMenu`.
- [x] Create `BattleRewards`.
- [x] Create `BattleRewardService`.
- [x] Create `BattleRewardScreen`.
- [x] Create `BattleCoordinator`.
- [x] Remove direct console input from `World`.
- [x] Remove reward calculation and display from `World`.
- [x] Remove enemy construction from `World`.
- [ ] Review the remaining exploration responsibilities in `World`.
- [ ] Extract additional responsibilities only where a clear boundary exists.

## Phase 6 — Enemy groups and multiple enemies

- [ ] Generate a random enemy-group size using scenario minimum and maximum values.
- [ ] Select every group member using eligible weighted encounter entries.
- [ ] Enforce `maxCopies` for each enemy definition.
- [ ] Handle pools that cannot fill the requested group size.
- [ ] Replace the single battle enemy with an enemy group.
- [ ] Support up to three enemies.
- [ ] Display enemies with selection numbers.
- [ ] Implement target selection.
- [ ] Prevent defeated enemies from acting.
- [ ] Execute the turn of every living enemy.
- [ ] End battle when all enemies are defeated.
- [ ] Aggregate EXP, gold and drops from all defeated enemies.
- [ ] Display consolidated battle rewards.

## Phase 7 — Party system

- [ ] Create `Party`.
- [ ] Support up to four allied characters.
- [ ] Display party members in battle.
- [ ] Implement ally selection for items and supportive skills.
- [ ] Track defeated and active allies.
- [ ] End in defeat only when all allies are defeated.
- [ ] Distribute EXP among party members.
- [ ] Display compact level-up results for multiple characters.

## Phase 8 — V2 completion

- [ ] Replace the hardcoded exploration item reward with area data.
- [ ] Decide whether items will be externalized during V2 or postponed.
- [ ] Validate all external data files.
- [ ] Improve initialization error messages.
- [ ] Test victory, defeat and fleeing.
- [ ] Test optional epic and forced boss encounters.
- [ ] Test item usage inside and outside battle.
- [ ] Test level progression and excess EXP.
- [ ] Test exploration and every area transition.
- [ ] Test encounter weights, area restrictions and maximum copies.
- [ ] Test parties and multiple enemies.
- [ ] Update project documentation.
- [ ] Create the V2 release build.
- [ ] Create the V2 Git tag.

## Future defeat and rescue flow

This phase must be implemented only after Bree is fully functional and defeat presents a meaningful risk.

- [ ] Remove the Game Over flow.
- [ ] Create a full HP, MP and SP restoration operation.
- [ ] Display a defeat and rescue message.
- [ ] End the current exploration session after defeat.
- [ ] Return the player or party to Bree.
- [ ] Preserve EXP, gold, items and character progression.
- [ ] Support full-party defeat after the party system is implemented.

## Outside the V2 scope

Planned for V3:

- Character creation.
- Seven playable classes.
- Class-specific progression.
- Continued use of Flagelo as the primary test character.
- Save and load system.
- No class-changing system yet.
