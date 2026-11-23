package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.WorldProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.transport.CityTransportDefinition;

public class MapPrototype {

	private static final int MAP_WIDTH = 76;
	private static final String CURRENT_LOCATION_MARKER = "\u001B[33m◉\u001B[0m";

	private static final String MAP = """
			                                   Vales do       Reino de      Sanctum
			 Zona de                           Paradoxo ----- Altaneve ---- Zenithi
			 Quarentena                            |
			  |                                    |     Minas do Além Túmulo
			 Mist ---------- Monte                 |              |
			 Suburb          Malferve --------- Bree -------- Entrelinhas
			  |                                    |              |          Praia de
			 Bosque de ----- Povoado de ---- Verdes Ermos --- Entrelugar --- Sanguemar
			 Verdesvivos     Setegalhos            |              |             |
			                /                Gruta de         Fenda de       Ruínas de
			               /                 Relamparrocha    Entrequando    Sanguemar
			 Mata do Agouro --- O Vão                                           |
			                                                                 Deep Valley
			""";

	// Coordinates are zero-based. Hidden fragments preserve the existing road anchors.
	private static final List<MapLocation> MAP_LOCATIONS = List.of(
			new MapLocation("vales_paradoxo", 0, 35, 8, "", " [???] -"),
			new MapLocation("reino_altaneve", 0, 50, 8, "", "[???] --"),
			new MapLocation("sanctum_zenithi", 0, 64, 7, "", "[???]"),
			new MapLocation("zona_quarentena", 1, 1, 10, "[???]", " |"),
			new MapLocation("minas_alem_tumulo", 3, 45, 20, "       [???]"),
			new MapLocation("mist_suburb", 4, 1, 6, "[???] ", " |"),
			new MapLocation("monte_malferve", 4, 17, 8, "--¬", "[???] --"),
			new MapLocation("bree", 5, 36, 5, "[???]"),
			new MapLocation("entrelinhas", 5, 50, 11, "- [???]"),
			new MapLocation("praia_sanguemar", 6, 65, 9, "", "[???]"),
			new MapLocation("bosque_verdesvivos", 7, 1, 11, "[???] -----", ""),
			new MapLocation("setegalhos", 7, 17, 10, "[???] ----", "/"),
			new MapLocation("verdes_ermos", 7, 33, 12, "-- [???] ---"),
			new MapLocation("entrelugar", 7, 50, 10, "- [???] --"),
			new MapLocation("gruta_relamparrocha", 9, 33, 13, "   [???]", ""),
			new MapLocation("fenda_entrequando", 9, 50, 11, "  [???]", ""),
			new MapLocation("ruinas_sanguemar", 9, 65, 9, "[???]", "   |"),
			new MapLocation("mata_agouro", 11, 1, 14, "         [???]"),
			new MapLocation("o_vao", 11, 20, 5, "[???]"),
			new MapLocation("deep_valley", 12, 65, 11, "[???]"));

	private record MapLocation(String scenarioId, int row, int column, int width, String... hiddenLines) {
	}

	private final ConsoleInput consoleInput;
	private final CityTransportMenu cityTransportMenu;

	public MapPrototype(ConsoleInput consoleInput, CityTransportMenu cityTransportMenu) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.cityTransportMenu = Objects.requireNonNull(cityTransportMenu, "City transport menu cannot be null.");
	}

	public Optional<CityTransportDefinition> open(
			Scenario currentScenario,
			List<CityTransportDefinition> availableDestinations,
			int destinationSlotCount,
			Set<String> markedDestinationScenarioIds,
			WorldProgress worldProgress) {
		String feedbackMessage = null;
		boolean showLegend = false;
		String currentScenarioId = currentScenario.getId();

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Mapa " + currentScenarioId);
			if (!showLegend) {
				openMap(currentScenarioId, worldProgress);
			} else {
				System.out.println();
				ConsoleUI.displayMapDescriptionPreview(currentScenario.getDescription(), 7);
				System.out.println();
				System.out.println("""
						[???] Lugar não descoberto
						 ---  Estrada
						 | /  Estrada
						  \u001B[33m◉\u001B[0m   Sua localização atual.""");
			}
			System.out.println(feedbackMessage != null ? feedbackMessage : "");

			StringBuilder navigationOptions = new StringBuilder();
			navigationOptions.append("[0] Voltar");
			navigationOptions.append(showLegend ? "   [1] Mapa" : "   [1] Sobre o mapa");
			navigationOptions.append(!availableDestinations.isEmpty() ? "   [2] Viajar" : "");
			System.out.println(navigationOptions);
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {

			case "1" -> {
				showLegend = !showLegend;
				feedbackMessage = null;
			}

			case "2" -> {
				if (!availableDestinations.isEmpty()) {
					Optional<CityTransportDefinition> selectedDestination = cityTransportMenu.open(
							currentScenario.getName(),
							availableDestinations,
							destinationSlotCount,
							markedDestinationScenarioIds);

					if (selectedDestination.isPresent()) {
						return selectedDestination;
					}

					showLegend = false;
					feedbackMessage = null;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}

			case "0" -> {
				feedbackMessage = null;
				return Optional.empty();
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private static void openMap(String currentScenarioId, WorldProgress worldProgress) {
		StringBuilder[] rows = MAP.lines()
				.map(line -> new StringBuilder(line + " ".repeat(MAP_WIDTH - line.length())))
				.toArray(StringBuilder[]::new);
		MapLocation currentLocation = null;

		for (MapLocation location : MAP_LOCATIONS) {
			if (location.scenarioId().equalsIgnoreCase(currentScenarioId)) {
				currentLocation = location;
				continue;
			}
			if (worldProgress.isDiscovered(location.scenarioId())) {
				continue;
			}
			for (int i = 0; i < location.hiddenLines().length; i++) {
				String fragment = location.hiddenLines()[i];
				String paddedFragment = fragment + " ".repeat(location.width() - fragment.length());
				rows[location.row() + i].replace(
						location.column(), location.column() + location.width(), paddedFragment);
			}
		}

		// Join adjacent horizontal road fragments without moving labels or road anchors.
		for (StringBuilder row : rows) {
			for (int column = 1; column < row.length() - 1; column++) {
				if (row.charAt(column) != ' ' || row.charAt(column - 1) != '-') {
					continue;
				}
				int nextColumn = column;
				while (nextColumn < row.length() && row.charAt(nextColumn) == ' ') {
					nextColumn++;
				}
				if (nextColumn < row.length() && row.charAt(nextColumn) == '-') {
					for (int gapColumn = column; gapColumn < nextColumn; gapColumn++) {
						row.setCharAt(gapColumn, '-');
					}
				}
				column = nextColumn;
			}
		}

		// Add ANSI formatting only after every fixed-column replacement is complete.
		if (currentLocation != null) {
			rows[currentLocation.row()].replace(
					currentLocation.column() - 1, currentLocation.column(), CURRENT_LOCATION_MARKER);
		}
		for (StringBuilder row : rows) {
			System.out.println(row.toString().stripTrailing());
		}
	}
}
