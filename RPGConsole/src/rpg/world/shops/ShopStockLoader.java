package rpg.world.shops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.items.Item;
import rpg.items.ItemCatalog;

// Loads possible shop-stock entries from external data.
public final class ShopStockLoader {

	private static final int EXPECTED_FIELD_COUNT = 6;

	public ShopStockCatalog load(
			Path filePath,
			ShopCatalog shopCatalog,
			ItemCatalog itemCatalog
	) throws IOException {
		ShopStockCatalog stockCatalog = new ShopStockCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				ShopStockEntry stockEntry = parseEntry(
						line,
						shopCatalog,
						itemCatalog
				);

				stockCatalog.register(stockEntry);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid shop stock entry at line "
								+ (index + 1)
								+ ": " + line
								+ ". " + exception.getMessage(),
						exception
				);
			}
		}

		validateEveryShopHasOffers(shopCatalog, stockCatalog);
		validateAntiArbitrage(itemCatalog, stockCatalog);

		return stockCatalog;
	}

	private ShopStockEntry parseEntry(
			String line,
			ShopCatalog shopCatalog,
			ItemCatalog itemCatalog
	) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Expected " + EXPECTED_FIELD_COUNT
							+ " fields but found " + fields.length + "."
			);
		}

		String id = fields[0].trim();
		String shopId = fields[1].trim();
		String itemId = fields[2].trim();
		int shopSalePricePercent =
				parseInteger(fields[3], "shop sale price percent");
		int stock = parseInteger(fields[4], "shop stock quantity");
		int weight = parseInteger(fields[5], "shop stock weight");

		shopCatalog.getById(shopId);
		Item item = itemCatalog.getById(itemId);

		return new ShopStockEntry(
				id,
				shopId,
				item,
				shopSalePricePercent,
				stock,
				weight
		);
	}

	private int parseInteger(String value, String fieldName) {
		String normalizedValue = value.trim();

		try {
			return Integer.parseInt(normalizedValue);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid " + fieldName + ": " + normalizedValue,
					exception
			);
		}
	}

	private void validateEveryShopHasOffers(
			ShopCatalog shopCatalog,
			ShopStockCatalog stockCatalog
	) {
		for (ShopDefinition shopDefinition : shopCatalog.getAll()) {
			if (stockCatalog
					.getByShopId(shopDefinition.getId())
					.isEmpty()) {
				throw new IllegalArgumentException(
						"Shop must have at least one possible sale offer: "
								+ shopDefinition.getId()
				);
			}
		}
	}

	private void validateAntiArbitrage(
			ItemCatalog itemCatalog,
			ShopStockCatalog stockCatalog
	) {
		for (Item item : itemCatalog.getAll()) {
			int lowestShopSalePrice = Integer.MAX_VALUE;

			for (ShopStockEntry stockEntry : stockCatalog.getAll()) {
				if (stockEntry.getItem().equals(item)) {
					lowestShopSalePrice = Math.min(
							lowestShopSalePrice,
							stockEntry.getFinalShopSalePrice()
					);
				}
			}

			if (lowestShopSalePrice == Integer.MAX_VALUE) {
				continue;
			}

			Integer shopPurchasePrice = item.getShopPurchasePrice();

			if (shopPurchasePrice != null
					&& shopPurchasePrice > lowestShopSalePrice) {
				throw new IllegalArgumentException(
						"Shop purchase price cannot exceed the lowest shop sale price for item: " + item.getId()
				);
			}
		}
	}
}