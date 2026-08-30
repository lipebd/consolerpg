package rpg.ui;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelCatalog;

// Displays travel destiantions and returns the selected scenarios
public final class TravelMenu {

	private final Scanner scanner;
	private final ScenarioCatalog scenarioCatalog;
	private final TravelCatalog travelCatalog;

	public TravelMenu(Scanner scanner, ScenarioCatalog ScenarioCatalog, TravelCatalog TravelCatalog) {
		this.scanner = scanner;
		this.scenarioCatalog = ScenarioCatalog;
		this.travelCatalog = TravelCatalog;
	}
	
	public Optional<Scenario> open(Scenario currentScenario){
		List<String> destinationIds = travelCatalog.getDestinationIds(currentScenario.getId());
		
		if(destinationIds.isEmpty()) {
			System.out.println("\nNão há destinos possíveis.");
			return Optional.empty();
		}
		
		while(true) {
			System.out.println("\nEscolha seu destino:");
			
			for (int index = 0; index < destinationIds.size(); index++) {
				String destinationId = destinationIds.get(index);
				Scenario destination = scenarioCatalog.getById(destinationId);
				System.out.println("[" + (index + 1) + "] " + destination.getName());
			}
			
			System.out.println("[0] Voltar");
			System.out.print("> ");
			
			String choice = scanner.nextLine();
			
			if (choice.equals("0")) {
				return Optional.empty();
			}
			
			try {
				int selectedIndex = Integer.parseInt(choice) - 1;
				
			if (selectedIndex < 0 || selectedIndex >= destinationIds.size()) {
				System.out.println("Destino inválido.");
				
				continue;
			}
				
				String destinationId = destinationIds.get(selectedIndex);
				Scenario destination = scenarioCatalog.getById(destinationId);
				
				return Optional.of(destination);
				
			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}
	
}