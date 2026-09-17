package rpg.persistence;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

// Reads required properties and tracks unrecognized fields.
final class StrictPropertyReader {

        private final Properties properties;
        private final Set<String> unreadPropertyNames;

        StrictPropertyReader(Properties properties) {
                this.properties = Objects.requireNonNull(properties, "Properties cannot be null.");

                validateEntryTypes();

                this.unreadPropertyNames = new HashSet<>(properties.stringPropertyNames());
        }

        String readRequired(String propertyName) {
                if (!unreadPropertyNames.remove(propertyName)) {
            throw new IllegalArgumentException("Required property is missing: " + propertyName);
                }

                String value = properties.getProperty(propertyName);

                if (value == null) {
            throw new IllegalArgumentException("Property value is not textual: " + propertyName);
                }

                return value;
        }

        int readInt(String propertyName) {
                String value = readRequired(propertyName);

                validateDecimalInteger(value, propertyName);

                try {
                        return Integer.parseInt(value);
                } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                    "Property value is outside the supported integer range: " + propertyName,
                    exception);
                }
        }

        long readLong(String propertyName) {
                String value = readRequired(propertyName);

                validateDecimalInteger(value, propertyName);

                try {
                        return Long.parseLong(value);
                } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                    "Property value is outside the supported long range: " + propertyName,
                    exception);
                }
        }

    boolean readBoolean(String propertyName) {
        String value = readRequired(propertyName);

        if (value.equals("true")) {
            return true;
        }

        if (value.equals("false")) {
            return false;
        }

        throw new IllegalArgumentException(
                "Property value is not a valid boolean: " + propertyName);
    }

        int readCount(String propertyName) {
                int count = readInt(propertyName);

                if (count < 0) {
                        throw new IllegalArgumentException(
                    "Collection count cannot be negative: " + propertyName);
                }

                if (count > properties.size()) {
                        throw new IllegalArgumentException(
                    "Collection count exceeds the available property structure: " + propertyName);
                }

                return count;
        }

        String readNullableId(String propertyName) {
                String value = readRequired(propertyName);

                return value.isEmpty() ? null : value;
        }

        void rejectUnreadProperties() {
                if (unreadPropertyNames.isEmpty()) {
                        return;
                }

        String firstUnexpectedProperty =
                unreadPropertyNames.stream().sorted().findFirst().orElseThrow();

        throw new IllegalArgumentException("Unexpected property: " + firstUnexpectedProperty);
        }

        private void validateEntryTypes() {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                        if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof String)) {
                throw new IllegalArgumentException("Property names and values must be strings.");
                        }
                }
        }

        private void validateDecimalInteger(String value, String propertyName) {
                if (!value.matches("-?[0-9]+")) {
                        throw new IllegalArgumentException(
                    "Property value is not a valid base-10 integer: " + propertyName);
                }
        }
}