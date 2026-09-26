# RPGConsole V7 Roadmap

> **Approved general scope checklist:** The release boundary and implementation order below are frozen. Each part receives its detailed behavior and implementation decisions when that part begins. A checked box means the described work has been implemented and verified, except in the explicitly study-only part.

## Release goal

V7 polishes the released V6 game: readable and consistent console screens, clearer battle feedback, smoother transitions, and selected additions to the existing item and skill effects. Enemy decision making is studied during V7 but is not implemented in this release.

Continue using the existing UI, battle, item and skill responsibilities. Reuse methods with the same semantics before adding another one. Add a Java class only when an independent responsibility cannot reasonably live in the existing structure. Keep code and technical identifiers in English and player-facing text in Brazilian Portuguese.

The checklist fixes **what belongs to V7**, not every rule for implementing it. At the start of each part, examine only its relevant code, discuss alternatives and consequences, freeze its detailed contract, then implement and test it. New requests discovered during a frozen part go to the end of the queue unless they block or critically break that part. Update affected documentation after the part's behavior is approved.

## 1. Shared text and screen transitions

### Frozen text-wrapping contract

* [x] Keep text wrapping owned by `ConsoleUI` with a fixed visible width of 75 characters.
* [x] Route player-facing prose and feedback whose length may vary through the shared wrapping behavior throughout the UI and the presentation paths in `World` and `Battle`.
* [x] Keep headings, numbered options, prompts, blank lines, separators, status values, aligned columns and tables under direct `print`, `println` or `printf` control.
* [x] Preserve explicit line breaks as authored paragraph or structural breaks; remove manual line breaks used only to fit a sentence within the screen width.
* [x] Preserve consecutive intentional blank lines.
* [x] Keep a single word longer than the available width intact on its own line without hyphenation or truncation.
* [x] Calculate wrapping and alignment from visible text width, ignoring ANSI escape sequences through one shared internal rule in `ConsoleUI`.
* [x] Support a stable line prefix for wrapped descriptions that require indentation, including current route and scenario-branch descriptions.
* [x] Preserve the prefix on every automatically wrapped line and every explicit line after the first.
* [x] Keep the wrapping algorithm reusable by later animated output; dialogue animation remains deferred to Part 4.

### Frozen clearing and flushing contract

* [x] Keep `ConsoleUI.clearScreen()` responsible for moving the cursor, clearing the screen and flushing the clear command.
* [x] Present ordinary screens in the order `clearScreen` → headings → content → actions and prompt.
* [x] Correct existing clear operations that erase a heading or execute between a heading and its intended content.
* [x] Do not scatter additional explicit flush calls through ordinary screens; gradual dialogue, battle and transition output is handled in Parts 4, 5 and 7.

### Part boundary and verification

* [x] Keep location-heading standardization, dialogue geometry and animation, battle-screen reconstruction, animated travel/saving transitions, and broad text-polish work in their later approved parts.
* [x] Verify one long text without an explicit break, one text with intentional paragraphs, one ANSI-colored text, one indented description, one aligned menu and one corrected clear/flush transition.
* [x] Update only the documentation affected by the implemented behavior after the part is tested and approved.

> **Part 1 closed on 2026-09-23.**
>
> `ConsoleUI` now owns wrapping at 75 visible columns, ANSI-insensitive width calculation, stable wrapped prefixes and screen clearing with its required flush. Variable player-facing prose and feedback were migrated across the existing UI and the presentation paths in `World` and `Battle`.
>
> Iterative standalone screens now redraw through the stable order of clearing, headings, content, primary actions, feedback, return action and prompt. Invalid input no longer disappears during the following clear or produces a growing screen log. Existing menus were reused and reorganized without adding another UI class.
>
> `CityMenu`, `CombatScenarioMenu` and `ScenarioBranchMenu` remain coupled to context currently printed by `World`; their complete redraw is carried into Part 2 together with location-heading composition. Part 2 will also provide the location context required for the final epic-encounter heading. `ClassPathTieMenu` intentionally retains its one-time narrative rendering so invalid input does not replay timed pauses.

## 2. Consistent screen headings

### Frozen heading contract

* [x] Give every independent interactive or informational screen one bracketed heading as its first visible line after `clearScreen()`.
* [x] Do not stack multiple bracketed headings on one screen. Context and qualifier must share one heading when both are required.
* [x] Use `[CONTEXT]` for a single heading and `[CONTEXT - QUALIFIER]` for a composed heading.
* [x] Use the regular hyphen surrounded by spaces in composed headings. Broader dash review remains deferred to Part 7.
* [x] Centralize heading output in `ConsoleUI` through `displayHeading(String heading)` and `displayHeading(String context, String qualifier)`.
* [x] Make `ConsoleUI` validate non-null, non-blank heading components, convert them with `Locale.ROOT` and own the brackets and separator.
* [x] Keep vertical spacing outside `displayHeading(...)`; the caller remains responsible for blank lines between the heading and its content.
* [x] Preserve `TitleScreen` as the application-branding exception. Ordinary campaign screens do not repeat `[RPGCONSOLE]`.

### Location and functional contexts

* [x] Use only the current city or scenario name for the root world screen, such as `[BREE]`, `[VERDES ERMOS]` or `[RELAMPARROCHA]`.
* [x] Combine a city or scenario with a physical sublocation, service or localized event, such as `[BREE - MERCADO DE BREE]`, `[BREE - TAVERNA DO PÔNEI SALTITANTE]`, `[VERDES ERMOS - MERCADOR VIAJANTE]`, `[VERDES ERMOS - ENCONTRO ÉPICO]` and `[VERDES ERMOS - BATALHA]`.
* [x] Use only the current location heading on route, branch, city-transport and other destination-selection screens. Their question and options provide the action context.
* [x] Combine narrative city and location context into one heading for narrative-location, NPC, quest-offer and dialogue screens. Dialogue geometry and animation remain deferred to Part 4.
* [x] Keep functional screens independent from map context. Inventory, quest journal and current group status use `[INVENTÁRIO]`, `[DIÁRIO]` and `[STATUS]`.
* [x] Do not implement group naming in V7. A future group name may reuse the composed format as `[NOME DO GRUPO - STATUS]`.
* [x] Use entity-first functional headings for individual character screens, including `[NOME - STATUS]`, `[NOME - EQUIPAMENTOS]` and `[NOME - HABILIDADES]`.
* [x] Preserve concise dedicated headings for confirmations, results, empty states and other self-explanatory flows, such as `[CONFIRMAR COMPRA]`, `[MISSÃO CONCLUÍDA]` and `[DERROTA]`.
* [x] Ensure screens currently beginning only with a question, description or option list receive the appropriate heading.

### Campaign-flow headings

* [x] Use `[CONTINUAR]` when a saved campaign is loaded successfully.
* [x] Use `[NOVA CAMPANHA]` when no campaign can be continued, including missing, unreadable or invalid saves.
* [x] Keep `[NOVA CAMPANHA]` throughout new-campaign confirmation, name-selection, manual-name and final character-confirmation screens.
* [x] Replace the player-facing expression `Novo Jogo` with `Nova Campanha` in this flow.
* [x] Let each screen's content explain its current step instead of creating more specific headings such as `[NOME]` or `[CRIAÇÃO DE PERSONAGEM]`.
* [x] Include the direct instruction `Digite seu nome.` on the manual-name screen before its validation rules.

### Root world-screen ownership

* [x] Keep `World` responsible for orchestration, current-scenario state and domain decisions, but move root city and combat-scenario rendering into `CityMenu` and `CombatScenarioMenu`.
* [x] Do not pass `World` or a rendering callback into UI classes.
* [x] Let the root menus receive the current scenario data and read-only exploration state required for presentation. They may inspect that state but must not mutate world or exploration rules.
* [x] Keep boss availability and other domain decisions in `World`; pass only their resolved state when required for presentation.
* [x] Pass a simple context-name string to other location-aware menus rather than introducing a new heading-context class.
* [x] Remove the obsolete `displayCurrentScenario()`, root scenario-name/progress presentation and `scenarioNameAlreadyDisplayed` flow from `World` once the root menus own complete redraws.
* [x] Keep unrelated world-event presentation outside this ownership change unless its heading must use the shared `ConsoleUI` contract.

### Scenario-description lifecycle

* [x] Define `shouldDisplayScenarioDescription` as meaning that the next opening of the root scenario menu includes the scenario description.
* [x] Initialize it as active when a campaign begins or is restored, and reactivate it whenever `changeScenario(...)` completes.
* [x] Consume the world-level flag when opening the next root menu, while passing its captured value into that menu.
* [x] Preserve the captured value throughout invalid-input redraws in the same menu call so the description does not disappear after the first invalid command.
* [x] Do not reactivate the description after inventory, journal, status, facilities, ordinary exploration, victory or escape when the current scenario has not changed.
* [x] Keep exploration-area messages independent from the general scenario-description lifecycle.
* [x] Remove the current battle-result behavior that reactivates the scenario description after victory or escape.

### Part boundary and verification

* [x] Defer `[MAP - BATALHA]` and the remaining battle-menu headings to Part 5 so they are implemented together with the complete battle-screen reconstruction.
* [x] Limit dialogue work in this part to the unified contextual heading; fixed geometry and animated text remain frozen for Part 4.
* [x] Do not add group naming, new world rules, new navigation behavior or a general screen-context class.
* [x] Verify representative city-root, exploration-root, facility, narrative, functional, campaign, epic-encounter and battle headings.
* [x] Verify that a newly entered scenario retains its description across invalid commands, loses it after a valid action and does not redisplay it after returning from a nested functional screen or same-scenario battle.
* [x] Update only the documentation affected by the implemented behavior after the part is tested and approved.

> **Deferred discussion:** routine quest-progress screens may later become contextual notifications on the following world or narrative screen. The replacement is not committed yet; its architectural cost and interaction with synchronous quest presentation must be evaluated before changing the current flow.

## 3. City destinations

* [x] Display functional city destinations and quest/narrative locations in two columns on the existing city-destination screen.
* [x] Include the currently available shop, crafting and INN destinations on the functional side; allow the same presentation to accommodate a future trading post without implementing one in V7.
* [x] Preserve one selection flow, stable option-to-destination mapping, visibility requirements and actionable quest markers.
* [x] Handle one empty column and long or varying destination names without breaking the menu layout.
* [x] Verify destination selection, return, hidden locations and quest markers.

## 4. Dialogue presentation

* [x] Keep dialogue screens at a stable height with space for two wrapped text lines per page and consistent spacing around heading, speaker and action prompt.
* [x] Present longer utterances across consecutive two-line pages without truncating text or changing speakers.
* [x] Animate dialogue one Unicode code point at a time, preserving accents, symbols, manual paragraph breaks and shared wrapping.
* [x] Make Enter during animation reveal the complete current page; require another Enter to advance to the next page or utterance.
* [x] Verify narrator and named-speaker segments, short and long segments, and transitions between consecutive pages and utterances.

## 5. Battle screen and action feedback

* [x] Keep enemy and party HP details visible and consistent while battle actions and their outcomes are presented.
* [x] Show an explicit defeat message when a participant reaches zero HP while the battle continues.
* [x] Replace the action menu with the current action's messages during resolution instead of extending a running log below it.
* [x] Reuse battle-status formatting and coordinate panel redraw/flush behavior without cursor positioning in battle panels and menus. Keep cursor positioning confined to the battle-entry animation.
* [x] Move battle presentation and input responsibilities to the existing `rpg.ui` screens and menus where appropriate, while `Battle.java` coordinates actions and results.
* [x] Show active buffs and debuffs on the attack target-selection screen.
* [x] Identify which effect was resisted in resistance feedback.
* [x] Suppress the player-facing message for an effect that did not trigger, without changing the effect's rules or resource/turn consumption.
* [x] Verify a continuing battle with a defeated enemy or ally, multi-effect skill output, target selection and the final battle result.

> **Part 5 closed on 2026-09-25.**
>
> The battle panel shows up to three enemies and four allies with fixed-width columns, resource values and compact effect summaries. Enemy names are truncated to preserve the panel width. During target selection, eligible enemies or allies receive yellow numbered indicators in their existing panel positions; cancelling selection returns without reporting an invalid target.
>
> Action results appear in the battle panel's message area. Battle item use and skill results use that presentation instead of printing below their menus. Battle item lists, item details and the tactical screens redraw with fixed height and inline feedback. Tactical status has resource and effect views plus a legend; enemy analysis has its own screen; turn order previews the next five actions and reports any remaining actions.
>
> Normal, boss and epic battles use the corresponding contextual headings. A brief battle-entry animation replaces the old text introduction. This animation alone positions the cursor directly; battle panels and menus continue to redraw normally. The victory screen now separates the reward summary from conditional level-growth details and paginates exceptional overflow. The adjacent out-of-battle item details and ally selection were aligned to the same 17-line presentation contract after Part 5's main battle commit.

## 6. Skill presentation and focused correction

* [x] Display six fixed skill slots in character status and battle selection: four natural slots and two scroll slots. Keep empty slots visible and non-selectable.
* [x] Present skill attribute-stage changes with repeated arrows in their descriptions, preserving conditions, durations and next-round VEL timing.
* [x] Record the previously corrected Grito de Guerra behavior: its effects execute in `POST_ACTION`, and a skill does not require a `PRIMARY` component.
* [x] Verify empty slots, a scroll skill learned through the inventory, stage descriptions and the already corrected Grito de Guerra.

> **Part 6 closed on 2026-09-25.**
>
> `PlayableCharacter` supplies six stable optional positions. Character status lists them in numerical order; battle selection groups them by column as `1 3 5 / 2 4 6`. Empty positions cannot be selected. Battle selection abbreviates names longer than its available width while preserving complete names in skill details.
>
> The character-status skill sheet uses shared text wrapping and fixed 17-line pages, with a reserved line for invalid-input feedback. Skill descriptions use one `↑` or `↓` per attribute stage. A temporary Bardo scroll was learned through the normal inventory flow and displayed in slot 5 in status and battle; its data definitions were removed after verification. Grito de Guerra had already been corrected before this part.

* [x] Add a brief transition during travel and exploration that communicates movement without changing travel cost, destination or world-state rules.
* [x] Add a brief saving transition without changing when a save succeeds, fails or becomes persistent.
* [x] Review player-facing dash usage and remove accidental string concatenations such as `"texto" + "texto"` in the touched presentation paths.
* [x] Verify travel cancellation and completion, successful saving and saving failure, and the resulting screens.

> **Part 7 presentation verified on 2026-09-26; text polish pending.**
>
> Exploration narration and its result animate within the combat-scenario screen before the actions appear. The exploration position shown during narration belongs to the event being resolved; the next menu redraw shows the advanced position. City descriptions use a bounded preview and `[M] Mais`; combat scenarios retain a three-line message area and offer the complete map description separately. These root screens respect the 77-column, 17-line terminal boundary.
>
> Adjacent routes and scenario branches display their configured passage names alongside destination names resolved from `ScenarioCatalog`. Their descriptions animate only after a valid selection. Paid city transport animates its existing description after confirmation and payment. Transitions use `[SOURCE - DESTINATION]`; Enter reveals the complete text and removes terminal echo.
>
> Saving remains inside the INN screen. Its options give way to an animated message, the actual save result and `aperte ENTER para continuar`. A failed save returns to the INN even when the player selected `Salvar e encerrar`. Battle action messages now also accept Enter during animation and redraw the complete final three-line message window.

## 8. Additional consumable and skill effects

* [ ] Select the specific additional consumable effects for V7 and freeze their rules before implementing them.
* [ ] Select the specific additional skill effects for V7 and freeze their rules before implementing them.
* [ ] Extend the existing definition, validation, execution and presentation paths only as required by the approved effects.
* [ ] Verify each distinct new effect behavior and relevant failure boundary without repeating the same execution path for every item or skill.

This part reserves room for selected new effects; it does not approve an unlimited effect catalog. Enemy action selection and enemy use of magic, buffs or debuffs are handled as a study in Part 9.

## 9. Enemy decision-making study — no V7 implementation

* [ ] Map the current enemy turn, available attacks, targeting rules and battle-effect execution paths.
* [ ] Compare practical approaches to target selection, target persistence and attack/action selection for a deliberately simple enemy AI.
* [ ] Study what enemy magical attacks, buffs and debuffs would require in definitions, execution, feedback and validation.
* [ ] Record a bounded recommendation, implementation order and open decisions for a later version.
* [ ] Make no enemy-AI or enemy-action expansion in V7 as a consequence of this study alone.

## 10. V7 verification, documentation and release

* [ ] Run focused tests when each part closes, covering distinct branches and boundaries rather than every data combination.
* [ ] Run representative regressions for city navigation, dialogue, travel, saving, combat, items and skills after their affected parts close.
* [ ] Update the roadmap and only the architecture, mechanics, data-format and project-state documents actually affected by the approved behavior.
* [ ] Review the finished V7 diff for duplicate responsibilities, unintended temporary code and unrelated changes.
* [ ] Build and check the portable Windows package, including Unicode presentation and an extracted-package playthrough of the changed flows.
* [ ] Prepare the V7 release notes, tag and publish the release after the final verification and documentation are approved.
