package rpg.world.scenarios;

import java.util.List;
import java.util.Scanner;

import rpg.characters.Player;

public class Bree extends City {

	public Bree(
			Player player,
			Scanner scanner
			) {

		super("Bree",
				"Uma cidade movimentada, erguida no encontro de várias estradas."
				+ "\nSeu mercado reúne comerciantes de diversas regiões, enquanto a taverna é ponto de encontro"
				+"\nde viajantes e aventureiros. É de Bree que aventureiros inconsequentes saem para arriscar"
				+"\nsuas vidas em busca de tesouros e glória.",
				player,
				scanner
				);
	}

	@Override
	public List<ScenarioAction> getAvailableActions() {

		return List.of(
				ScenarioAction.ITEM_SHOP,
				ScenarioAction.BLACKSMITH,
				ScenarioAction.TAVERN,
				ScenarioAction.LEAVE_CITY
				);
	}
}