package rpg.persistence;

// Describes the outcome of one campaign save attempt.
public enum CampaignSaveResult {

        SUCCESS,
        ENCODING_FAILED,
        FILE_WRITE_FAILED;

        public boolean isSuccess() {
                return this == SUCCESS;
        }
}