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

~Deferred fixed branches:~ MOVED TO V3

~- [ ] Create fixed exploration branching points.~
~- [ ] Allow a branching point to load another scenario.~
~- [ ] Make a missed branch unavailable after advancing past it.~
~- [ ] End the current exploration session when entering a branch.~

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

## Phase 5 — Party system

- [x] Rename `Player` to `PlayableCharacter`.
- [x] Create `Party`.
- [x] Support one to four party members.
- [x] Move inventory ownership to `Party`.
- [x] Move gold ownership to `Party`.
- [x] Display all party members during battle.
- [x] Give every living party member an action.
- [x] Prevent defeated allies from acting.
- [x] Allow allies to use items on each other.
- [x] Add self-healing.
- [x] Add single-ally healing.
- [x] Add area healing.
- [x] Add all-but-self healing.
- [x] Add single-target revive.
- [x] Add area revive.
- [x] Grant complete EXP to every party member.
- [x] Grant EXP to defeated party members.
- [x] Display multiple level-up results.
- [ ] Display the complete party outside battle.
- [ ] Adapt the temporary defeat screen to the party.

## Phase 6 — Multiple enemies

- [x] Support one to three enemies.
- [x] Add enemy battle slots.
- [x] Number repeated enemies.
- [x] Add enemy target selection.
- [x] Prevent defeated enemies from acting.
- [x] Prevent defeated enemies from being targeted.
- [x] Aggregate rewards from multiple enemies.
- [x] Aggregate equal item drops.
- [x] Add per-enemy maximum copy limits.
- [x] Add per-scenario enemy count limits.

## Phase 7 — Battle initiative

- [x] Create a unified ally and enemy turn order.
- [x] Order first actions by effective Speed.
- [x] Use effective Luck as the first tie-breaker.
- [x] Use a random final tie-breaker.
- [x] Rebuild the queue every round.
- [x] Allow multiple actions during one round.
- [x] Compare Speed against the opposing average.
- [x] Limit actions per participant.
- [x] Distribute additional actions using initiative scores.
- [x] Skip defeated queue entries.
- [x] Add revived allies only to the following round.
- [x] Display the current participant.
- [x] Display the next participant.
- [x] Add the `Round` inspection action.
- [x] Limit the detailed queue to five visible entries.

## Phase 8 — Battle escape

- [x] Use the acting character for a flee attempt.
- [x] Add the direct effective Luck check.
- [x] Add the comparative attribute check.
- [x] Compare against living enemy averages.
- [x] Add minimum and maximum chances.
- [x] Consume the action after a failed attempt.
- [x] Remove the entire party after success.
- [x] Preserve contextual escape messages.
- [x] Return one dungeon position after fleeing from a boss.

## Phase 9 — V2 completion

- [ ] Complete the party status interface.
- [ ] Remove remaining temporary leader references.
- [ ] Adapt the temporary Game Over screen.
- [ ] Test solo battles.
- [ ] Test full-party battles.
- [ ] Test repeated enemies.
- [ ] Test multiple actions per round.
- [ ] Test defeated queue entries.
- [ ] Test revives and the following round.
- [ ] Test both escape checks.
- [ ] Test failed escape attempts.
- [ ] Test boss retreat.
- [ ] Test rewards with living and defeated allies.
- [ ] Test items inside and outside battle.
- [ ] Test every exploration event.
- [ ] Test scenario travel.
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

## V3 — Data-driven game content

- [ ] Create `data/scenarios.txt`.
- [ ] Create `ScenarioDefinition`.
- [ ] Create `ScenarioCatalog` loading from external data.
- [ ] Create `ScenarioLoader`.
- [ ] Create a factory for `Field`, `Dungeon` and `City`.
- [ ] Remove Bree from `Main`.
- [ ] Remove Verdes Ermos from `Main`.
- [ ] Remove Gruta Relamparrocha from `Main`.
- [ ] Create fixed exploration branching points.
- [ ] Allow a branching point to load another scenario.
- [ ] Make a missed branch unavailable after advancing past it.
- [ ] End the current exploration session when entering a branch.
- [ ] Create `data/skills.txt`.
- [ ] Load playable skills externally.
- [ ] Create `data/items.txt`.
- [ ] Load consumables, materials and equipment externally.
- [ ] Implement fixed branching exploration points.
- [ ] Implement party titles.
- [ ] Use the party title in collective narrative texts.
- [ ] Persist the party title in save data.
- [ ] Implement character creation.
- [ ] Implement the seven playable classes.
- [ ] Implement save and load.
