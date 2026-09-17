package rpg.persistence;

// Identifies the outcome of one campaign load attempt.
public enum CampaignLoadStatus {

        SUCCESS,
        SAVE_NOT_FOUND,
        FILE_READ_FAILED,
        INVALID_SAVE
}