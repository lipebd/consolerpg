package rpg.world.shops;

import java.util.Objects;

import rpg.characters.Party;
import rpg.items.Inventory;
import rpg.items.Item;

// Applies validated shop transactions to party and shop state.
public final class ShopTransactionService {

	private final Party party;

	public ShopTransactionService(Party party) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
	}

	public ShopPurchaseResult purchase(ShopState shopState, ShopOfferState selectedOffer, int quantity) {
		Objects.requireNonNull(shopState, "Shop state cannot be null.");

		if (selectedOffer == null || !shopState.getActiveOffers().contains(selectedOffer)) {
			return ShopPurchaseResult.INVALID_SELECTION;
		}

		if (quantity <= 0) {
			return ShopPurchaseResult.INVALID_QUANTITY;
		}

		if (quantity > selectedOffer.getRemainingStock()) {
			return ShopPurchaseResult.INSUFFICIENT_STOCK;
		}

		int unitPrice = selectedOffer.getStockEntry().getFinalShopSalePrice();

		int totalPrice;

		try {
			totalPrice = Math.multiplyExact(unitPrice, quantity);
		} catch (ArithmeticException exception) {
			return ShopPurchaseResult.TOTAL_OVERFLOW;
		}

		if (party.getGold() < totalPrice) {
			return ShopPurchaseResult.INSUFFICIENT_GOLD;
		}

		Item item = selectedOffer.getStockEntry().getItem();

		Inventory inventory = party.getInventory();

		if (quantity > inventory.getRemainingCapacity(item)) {
			return ShopPurchaseResult.INVENTORY_CAPACITY_EXCEEDED;
		}

		applyPurchase(selectedOffer, item, quantity, totalPrice);

		return ShopPurchaseResult.SUCCESS;
	}

	private void applyPurchase(ShopOfferState selectedOffer, Item item, int quantity, int totalPrice) {
		if (!party.spendGold(totalPrice)) {
			throw new IllegalStateException("Party gold changed before shop purchase.");
		}

		if (!party.getInventory().addItem(item, quantity)) {
			boolean goldRestored = party.addGold(totalPrice);

			if (!goldRestored) {
				throw new IllegalStateException("Shop purchase rollback failed.");
			}

			throw new IllegalStateException("Validated item could not be added during shop purchase.");
		}

		try {
			selectedOffer.decreaseStock(quantity);

		} catch (RuntimeException exception) {
			boolean inventoryRestored = party.getInventory().removeItem(item, quantity);

			boolean goldRestored = party.addGold(totalPrice);

			if (!inventoryRestored || !goldRestored) {
				throw new IllegalStateException("Shop purchase rollback failed.", exception);
			}

			throw new IllegalStateException("Validated shop stock changed during purchase.", exception);
		}
	}

	private void applySale(Item selectedItem, int quantity, int totalValue) {
		if (!party.getInventory().removeItem(selectedItem, quantity)) {
			throw new IllegalStateException("Party inventory changed before shop sale.");
		}

		if (!party.addGold(totalValue)) {
			boolean inventoryRestored = party.getInventory().addItem(selectedItem, quantity);

			if (!inventoryRestored) {
				throw new IllegalStateException("Shop sale rollback failed.");
			}

			throw new IllegalStateException("Validated GOLD could not be added during shop sale.");
		}
	}

	public ShopSaleResult sell(Item selectedItem, int quantity) {
		if (selectedItem == null) {
			return ShopSaleResult.INVALID_SELECTION;
		}

		Integer unitPrice = selectedItem.getShopPurchasePrice();

		if (unitPrice == null) {
			return ShopSaleResult.ITEM_NOT_SELLABLE;
		}

		if (quantity <= 0) {
			return ShopSaleResult.INVALID_QUANTITY;
		}

		Inventory inventory = party.getInventory();

		if (inventory.getQuantity(selectedItem) < quantity) {
			return ShopSaleResult.INSUFFICIENT_INVENTORY;
		}

		int totalValue;

		try {
			totalValue = Math.multiplyExact(unitPrice, quantity);
		} catch (ArithmeticException exception) {
			return ShopSaleResult.TOTAL_OVERFLOW;
		}

		if (totalValue > party.getRemainingGoldCapacity()) {
			return ShopSaleResult.GOLD_CAPACITY_EXCEEDED;
		}

		applySale(selectedItem, quantity, totalValue);

		return ShopSaleResult.SUCCESS;
	}

}