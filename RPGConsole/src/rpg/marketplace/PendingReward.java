package rpg.marketplace;

public class PendingReward {

    private final String id;
    private final String transactionId;
    private final String sellerId;
    private final int amount;

    private RewardStatus status;

    public PendingReward(
            String id,
            String transactionId,
            String sellerId,
            int amount) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Reward ID cannot be null or blank.");
        }

        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or blank.");
        }

        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller ID cannot be null or blank.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Reward amount must be greater than zero.");
        }

        this.id = id;
        this.transactionId = transactionId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.status = RewardStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public int getAmount() {
        return amount;
    }

    public RewardStatus getStatus() {
        return status;
    }

    public void claim() {
        if (status != RewardStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending rewards can be claimed.");
        }

        status = RewardStatus.CLAIMED;
    }
}