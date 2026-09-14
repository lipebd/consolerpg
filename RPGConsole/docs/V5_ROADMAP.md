# RPGConsole V5 Roadmap

> **Preliminary roadmap:** This document defines the currently approved scope and implementation order of RPGConsole V5. The crafting integration described below is definitive. The broader V5 release boundary remains open until the remaining planned systems are evaluated.

## Release direction

V5 begins by integrating a data-driven crafting system contributed by another project member and adapting it to the architecture established through V4.

The contribution must be preserved wherever it remains compatible with the current project. Necessary adaptations may correct outdated V3 integration points, naming, validation, data structures and architectural inconsistencies without unnecessarily replacing the original implementation.

Crafting is represented as a generic service provided by crafter NPCs rather than as behavior exclusive to blacksmiths. This allows different characters and establishments to provide weapon forging, alchemy, cooking or other future forms of item transformation through the same system.

Each crafter belongs to one scenario and exposes its own configured set of recipes. More than one crafter may exist in the same city.

Recipes may consume and produce items from any item category. The `MATERIAL` category describes an item's role in the game but does not determine whether it may participate in crafting. A recipe may therefore consume items such as a `CONSUMABLE`, including Merumãs used to produce a Poção de Merumã.

Every completed crafting operation produces exactly one unit of its configured output item. Batch crafting, variable output quantities, crafting fees, crafting levels, recipe discovery, probabilistic results and equipment enhancement are outside the initial crafting block.

The implementation must continue to preserve stable IDs, strict startup validation, immutable definitions, centralized inventory mutation and separation between domain behavior and console presentation.

Current milestone: planning and integration of the approved crafting contribution.

---

## 1. Data-driven crafting definitions

### Crafting recipes

* [x] Define external crafting-recipe data in `crafting_recipes.txt`.
* [x] Create stable IDs for crafting recipes.
* [x] Create immutable `CraftingRecipeDefinition` instances.
* [x] Store one existing output item ID in every recipe.
* [x] Produce exactly one unit of the configured output item.
* [x] Store one or more required ingredients through immutable `CraftingMaterial` entries.
* [x] Store an existing item ID and a positive required quantity in every ingredient entry.
* [x] Allow items from any `ItemCategory` to be used as ingredients.
* [x] Allow items from any `ItemCategory` to be produced by recipes.
* [x] Store an external name and narrative descrition for every recipe.
* [x] Support literal `\n` in recipe descriptions
* [x] Keep output item properties derived from `ItemCatalog` instead of duplicating them as recipe output data.
* [x] Reject recipes with missing IDs, missing output items or no ingredients.
* [x] Reject recipes with empty names or descriptions.
* [x] Reject invalid recipe IDs according to the project ID convention.
* [x] Reject duplicated recipe IDs.
* [x] Reject unknown output and ingredient item IDs.
* [x] Reject empty ingredient IDs and non-positive ingredient quantities.
* [x] Reject duplicated ingredient entries inside the same recipe.
* [x] Create `CraftingRecipeLoader`.
* [x] Create `CraftingRecipeCatalog`.
* [x] Support recipe lookup by stable ID.
* [x] Preserve recipe declaration order for deterministic menu presentation.

### Crafter NPCs

* [x] Define external crafter data in `crafters.txt`.
* [x] Create stable IDs for crafter NPCs.
* [x] Create immutable `CrafterDefinition` instances.
* [x] Store the scenario ID, name, description and ordered recipe IDs for every crafter.
* [x] Keep crafter terminology generic instead of restricting the system to blacksmiths.
* [x] Allow multiple crafter definitions in the same city.
* [x] Allow each crafter to expose an independent set of recipes.
* [x] Reject invalid and duplicated crafter IDs.
* [x] Reject unknown or non-city scenario IDs in the initial crafting block.
* [x] Reject empty names and descriptions.
* [x] Reject crafters without recipes.
* [x] Reject unknown or duplicated recipe IDs within one crafter definition.
* [x] Create `CrafterLoader`.
* [x] Create `CrafterCatalog`.
* [x] Support crafter lookup by stable ID.
* [x] Support ordered lookup of all crafters available in one scenario.

---

## 2. Crafting transaction

### Service and results

* [ ] Adapt the contributed crafting service into `CraftingService`.
* [ ] Create `CraftingResult` to represent the outcome of one crafting attempt.
* [ ] Resolve recipes and items through their catalogs instead of creating duplicate definitions.
* [ ] Check the party inventory for every required ingredient before mutating it.
* [ ] Report insufficient ingredients without changing the inventory.
* [ ] Remove every required ingredient only after the complete recipe has been validated.
* [ ] Add exactly one output item after successful ingredient removal.
* [ ] Keep one crafting operation atomic from the player's perspective.
* [ ] Prevent partial ingredient consumption when crafting cannot be completed.
* [ ] Reuse the existing inventory mutation behavior instead of duplicating inventory rules.
* [ ] Keep crafting independent from item trading and shop prices.
* [ ] Keep crafting independent from the `MATERIAL` item category.

### Transaction validation

* [ ] Test crafting with exactly the required ingredient quantities.
* [ ] Test crafting while holding more than the required quantities.
* [ ] Test failure when one ingredient is missing.
* [ ] Test failure when one ingredient has insufficient quantity.
* [ ] Test that failed crafting does not consume any ingredient.
* [ ] Test that successful crafting consumes every configured ingredient quantity.
* [ ] Test that successful crafting adds exactly one output item.
* [ ] Test a recipe that consumes a non-`MATERIAL` item.
* [ ] Test a recipe whose output is not equipment.
* [ ] Test recipes containing multiple ingredients.

---

## 3. Crafting interface

* [ ] Adapt the contributed menu into `CraftingMenu`.
* [ ] Display the selected crafter's name and description.
* [ ] Display only the recipes configured for the selected crafter.
* [ ] Display the selected recipe's name and narrative description.
* [ ] Preserve the recipe order declared by the crafter definition.
* [ ] Display the output item using data resolved from `ItemCatalog`.
* [ ] Display every required ingredient and quantity.
* [ ] Display the quantity currently owned for every ingredient.
* [ ] Allow the player to inspect a recipe before confirming fabrication.
* [ ] Allow the player to cancel recipe selection.
* [ ] Allow the player to cancel the final crafting confirmation.
* [ ] Display a clear success message after crafting.
* [ ] Display which ingredients are insufficient after a failed attempt.
* [ ] Return to the crafter's recipe list after completing or cancelling an attempt.
* [ ] Keep input parsing and console presentation outside the crafting service.

---

## 4. World integration

* [ ] Load crafting recipes after the item catalog is available.
* [x] Load crafter definitions after the recipe and scenario catalogs are available.
* [ ] Integrate the crafting catalogs and service into the current V4 startup flow.
* [ ] Adapt only the required sections of the contributed `Main` implementation.
* [ ] Adapt only the required sections of the contributed `World` implementation.
* [ ] Preserve all V4 initialization, character creation, combat, equipment, shop, INN, travel and rescue behavior.
* [ ] Make every crafter in the current city available through the city-location flow.
* [ ] Display multiple crafters in the same city as independent destinations.
* [ ] Open `CraftingMenu` with the selected crafter definition.
* [ ] Prevent crafter access outside its configured scenario.
* [ ] Preserve the current scenario flow after leaving the crafting menu.
* [ ] Avoid introducing crafting-specific conditionals throughout unrelated world behavior.

---

## 5. Initial content

* [x] Define the first approved crafter NPCs.
* [x] Define an initial representative recipe set.
* [x] Include at least one recipe with multiple ingredients.
* [x] Include at least one recipe that consumes a `CONSUMABLE`.
* [x] Include at least one recipe that produces a recovery item.
* [x] Include at least one recipe that produces equipment.
* [x] Confirm that every recipe output has all required specialized data when applicable.
* [x] Keep placeholder and joke content out of definitive game data.
* [ ] Review names, descriptions and recipe requirements against the project's canonical setting and balance.

---

## 6. Documentation and regression

* [ ] Document the `crafters.txt` format in `DATA_FORMATS.md`.
* [ ] Document the `crafting_recipes.txt` format in `DATA_FORMATS.md`.
* [ ] Document crafting ownership and data flow in `ARCHITECTURE.md`.
* [ ] Document player-facing crafting rules in `MECHANICS.md`.
* [ ] Update `PROJECT_STATE.md` after the integration is complete.
* [ ] Preserve English identifiers and comments.
* [ ] Preserve PT-BR player-facing text.
* [ ] Compile after every atomic implementation stage.
* [ ] Validate malformed crafting and crafter data during startup.
* [ ] Complete crafting-flow regression through the city interface.
* [ ] Complete general V4 regression after modifying `Main` and `World`.
* [ ] Verify the portable Windows build with the new data files.
* [ ] Credit the original contributor in the integration history.
