# RPGConsole — V2 Roadmap

## V2 objective

Transform the V1 prototype into a small but extensible RPG with
data-driven enemies, linear exploration, multiple areas and support for
parties and multiple enemies.

## Phase 1 — Data-driven enemies

- [x] Create `CharacterStats`.
- [x] Add all core character attributes.
- [x] Remove the obsolete character healing power.
- [x] Create `EnemyDefinition`.
- [x] Create `EnemyCatalog`.
- [x] Create `EnemyLoader`.
- [x] Create `data/enemies.txt`.
- [x] Load the Lobermo from the external file.
- [x] Pass `EnemyCatalog` to `World`.
- [x] Replace the hardcoded Lobermo attributes.
- [x] Add stable IDs to scenarios.
- [x] Create area encounter entries.
- [x] Create `data/encounters.txt`.
- [x] Create the encounter data loader.
- [x] Create the random encounter generator.
- [x] Remove `startLobermoEncounter()`.
- [x] Allow each area to define its available enemies.

## Phase 2 — Linear exploration

Exploration works like movement through spaces on a board.

Each use of the Explore action advances the player by one position.

Possible events:

- Nothing
- Battle
- Item
- Merchant

Tasks:

- [x] Create `ExplorationEventType`.
- [x] Create `ExplorationSession`.
- [x] Store the current exploration position.
- [x] Configure the length of each explorable area.
- [x] Generate one event for each position.
- [x] Limit the merchant to one appearance per area session.
- [x] Allow an exploration session without a merchant.
- [x] Complete Verdes Ermos after five exploration actions.
- [x] Unlock travel after reaching the final position.
- [ ] Create fixed exploration branching points.
- [ ] Allow a branching point to load another scenario.
- [ ] End the current exploration session when entering a branch.

## Phase 3 — Additional area

- [ ] Add Gruta Relamparrocha.
- [ ] Define its description and available actions.
- [ ] Define its base EXP reward.
- [ ] Define its base gold reward.
- [ ] Add enemies belonging to the area.
- [ ] Allow travel from Verdes Ermos to Gruta Relamparrocha.
- [ ] Preserve scenario descriptions when entering or returning to maps.

## Phase 4 — External drop tables

- [ ] Create stable IDs for items.
- [ ] Allow items to be retrieved by ID.
- [ ] Create `data/enemy_drops.txt`.
- [ ] Create the enemy drop loader.
- [ ] Remove the Lobermo drop table from `World`.
- [ ] Preserve Merumã drops.
- [ ] Support multiple possible drops per enemy.

## Phase 5 — Party system

- [ ] Create `Party`.
- [ ] Support up to four allied characters.
- [ ] Display party members in battle.
- [ ] Track defeated and active allies.
- [ ] Distribute EXP among party members.
- [ ] Display compact level-up results for multiple characters.

## Phase 6 — Multiple enemies

- [ ] Replace the single battle enemy with an enemy group.
- [ ] Support up to three enemies.
- [ ] Display enemies with selection numbers.
- [ ] Implement target selection.
- [ ] Prevent defeated enemies from acting.
- [ ] End battle when all enemies are defeated.
- [ ] Aggregate rewards and drops from all defeated enemies.

## Phase 7 — V2 completion

- [ ] Remove temporary hardcoded encounter data.
- [ ] Validate all external data files.
- [ ] Improve initialization error messages.
- [ ] Test victory, defeat and fleeing.
- [ ] Test item usage inside and outside battle.
- [ ] Test level progression and excess EXP.
- [ ] Test exploration and area transitions.
- [ ] Test parties and multiple enemies.
- [ ] Update project documentation.
- [ ] Create the V2 release build.
- [ ] Create the V2 Git tag.

## Future defeat and rescue flow

This phase must be implemented only after Bree is fully functional.

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