package rpg.persistence;

// Stores one active shop offer in a campaign snapshot.
public final class ShopOfferSnapshot {

        private final String stockEntryId;
        private final int remainingStock;

        public ShopOfferSnapshot(String stockEntryId, int remainingStock) {
                if (stockEntryId == null || stockEntryId.isBlank()) {
                        throw new IllegalArgumentException("Shop stock-entry ID cannot be null or blank.");
                }

                if (remainingStock < 0) {
                        throw new IllegalArgumentException("Remaining shop stock cannot be negative.");
                }

                this.stockEntryId = stockEntryId;
                this.remainingStock = remainingStock;
        }

        public String getStockEntryId() {
                return stockEntryId;
        }

        public int getRemainingStock() {
                return remainingStock;
        }
}