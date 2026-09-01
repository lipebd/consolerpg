package rpg.world.scenarios;

import java.util.List;
import java.util.Scanner;

import rpg.characters.Player;

public abstract class City extends Scenario {

	protected Player player;
	protected Scanner scanner;

	public City(
			String name,
			String description,
			Player player,
			Scanner scanner
			) {

		super(name, description);

		this.player = player;
		this.scanner = scanner;
	}

	@Override
	public abstract List<ScenarioAction> getAvailableActions();
}