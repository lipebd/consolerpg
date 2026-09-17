package rpg.persistence;

import java.util.Base64;

// Provides the stable application key used for local save encryption.
final class ApplicationSaveEncryptionKey {

        private static final String ENCODED_KEY = "slMPI8UW2OPoNoEPYI8J2+5+yJFirQL6zcnfOTSphRw=";

        private ApplicationSaveEncryptionKey() {
        }

        static byte[] getBytes() {
                return Base64.getDecoder().decode(ENCODED_KEY);
        }
}
