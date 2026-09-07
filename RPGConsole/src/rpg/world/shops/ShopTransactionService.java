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

		try {
			Math.addExact(inventory.getQuantity(item), quantity);
		} catch (ArithmeticException exception) {
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
			party.addGold(totalPrice);

			throw new IllegalStateException("Validated item could not be added during shop purchase.");
		}

		try {
			selectedOffer.decreaseStock(quantity);

		} catch (RuntimeException exception) {
			boolean inventoryRestored = party.getInventory().removeItem(item, quantity);

			party.addGold(totalPrice);

			if (!inventoryRestored) {
				throw new IllegalStateException("Shop purchase rollback failed.", exception);
			}

			throw new IllegalStateException("Validated shop stock changed during purchase.", exception);
		}
	}
}