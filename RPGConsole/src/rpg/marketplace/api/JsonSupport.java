package rpg.marketplace.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Minimal JSON support for the API's flat request and response records.
final class JsonSupport {

	private JsonSupport() {
	}

	static String object(Map<String, ?> fields) {
		StringBuilder json = new StringBuilder("{");
		boolean first = true;

		for (Map.Entry<String, ?> entry : fields.entrySet()) {
			if (!first) {
				json.append(',');
			}
			first = false;
			json.append(quote(entry.getKey())).append(':').append(value(entry.getValue()));
		}

		return json.append('}').toString();
	}

	static Map<String, Object> parseObject(String json) {
		Object parsed = new Parser(json).parseDocument();

		if (!(parsed instanceof Map<?, ?> rawMap)) {
			throw new IllegalArgumentException("Expected a JSON object.");
		}

		return stringKeyMap(rawMap);
	}

	static List<Map<String, Object>> parseObjectArray(String json) {
		Object parsed = new Parser(json).parseDocument();

		if (!(parsed instanceof List<?> rawList)) {
			throw new IllegalArgumentException("Expected a JSON array.");
		}

		List<Map<String, Object>> result = new ArrayList<>();

		for (Object element : rawList) {
			if (!(element instanceof Map<?, ?> rawMap)) {
				throw new IllegalArgumentException("Expected an object inside the JSON array.");
			}
			result.add(stringKeyMap(rawMap));
		}

		return List.copyOf(result);
	}

	static String requiredString(Map<String, Object> object, String field) {
		Object value = object.get(field);

		if (!(value instanceof String text) || text.isBlank()) {
			throw new IllegalArgumentException("Missing JSON string field: " + field);
		}

		return text;
	}

	public static boolean requiredBoolean(Map<String, Object> object, String key) {
		if (object.get(key) instanceof Boolean value) {
			return value;
		}
		throw new IllegalArgumentException("Missing or invalid JSON boolean: " + key);
	}

	static int requiredInt(Map<String, Object> object, String field) {
		Object value = object.get(field);

		if (!(value instanceof Long number)) {
			throw new IllegalArgumentException("Missing JSON integer field: " + field);
		}

		return Math.toIntExact(number);
	}

	private static Map<String, Object> stringKeyMap(Map<?, ?> rawMap) {
		Map<String, Object> result = new LinkedHashMap<>();

		for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
			if (!(entry.getKey() instanceof String key)) {
				throw new IllegalArgumentException("JSON object key is not text.");
			}
			result.put(key, entry.getValue());
		}

		return result;
	}

	private static String value(Object value) {
		if (value instanceof String text) {
			return quote(text);
		}
		if (value instanceof Integer || value instanceof Long) {
			return value.toString();
		}
		if (value instanceof Boolean) {
			return value.toString();
		}
		if (value == null) {
			return "null";
		}

		throw new IllegalArgumentException("Unsupported JSON value type: " + value.getClass());
	}

	private static String quote(String text) {
		StringBuilder result = new StringBuilder("\"");

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);

			switch (character) {
				case '\"' -> result.append("\\\"");
				case '\\' -> result.append("\\\\");
				case '\b' -> result.append("\\b");
				case '\f' -> result.append("\\f");
				case '\n' -> result.append("\\n");
				case '\r' -> result.append("\\r");
				case '\t' -> result.append("\\t");
				default -> {
					if (character < 0x20) {
						result.append(String.format("\\u%04x", (int) character));
					} else {
						result.append(character);
					}
				}
			}
		}

		return result.append('\"').toString();
	}

	private static final class Parser {

		private final String input;
		private int position;

		private Parser(String input) {
			if (input == null) {
				throw new IllegalArgumentException("JSON input cannot be null.");
			}
			this.input = input;
		}

		private Object parseDocument() {
			skipWhitespace();
			Object value = parseValue();
			skipWhitespace();

			if (position != input.length()) {
				throw error("Unexpected trailing JSON content.");
			}

			return value;
		}

		private Object parseValue() {
			if (position >= input.length()) {
				throw error("Unexpected end of JSON.");
			}

			return switch (input.charAt(position)) {
				case '{' -> parseObjectValue();
				case '[' -> parseArrayValue();
				case '\"' -> parseString();
				case 't' -> parseLiteral("true", Boolean.TRUE);
				case 'f' -> parseLiteral("false", Boolean.FALSE);
				case 'n' -> parseLiteral("null", null);
				default -> parseNumber();
			};
		}

		private Map<String, Object> parseObjectValue() {
			expect('{');
			skipWhitespace();
			Map<String, Object> object = new LinkedHashMap<>();

			if (consume('}')) {
				return object;
			}

			while (true) {
				skipWhitespace();
				String key = parseString();
				skipWhitespace();
				expect(':');
				skipWhitespace();

				if (object.put(key, parseValue()) != null) {
					throw error("Duplicate JSON field: " + key);
				}

				skipWhitespace();
				if (consume('}')) {
					return object;
				}
				expect(',');
			}
		}

		private List<Object> parseArrayValue() {
			expect('[');
			skipWhitespace();
			List<Object> array = new ArrayList<>();

			if (consume(']')) {
				return array;
			}

			while (true) {
				array.add(parseValue());
				skipWhitespace();
				if (consume(']')) {
					return array;
				}
				expect(',');
				skipWhitespace();
			}
		}

		private String parseString() {
			expect('\"');
			StringBuilder result = new StringBuilder();

			while (position < input.length()) {
				char character = input.charAt(position++);

				if (character == '\"') {
					return result.toString();
				}

				if (character != '\\') {
					result.append(character);
					continue;
				}

				if (position >= input.length()) {
					throw error("Incomplete JSON escape.");
				}

				char escaped = input.charAt(position++);
				switch (escaped) {
					case '\"', '\\', '/' -> result.append(escaped);
					case 'b' -> result.append('\b');
					case 'f' -> result.append('\f');
					case 'n' -> result.append('\n');
					case 'r' -> result.append('\r');
					case 't' -> result.append('\t');
					case 'u' -> result.append(parseUnicodeEscape());
					default -> throw error("Invalid JSON escape.");
				}
			}

			throw error("Unterminated JSON string.");
		}

		private char parseUnicodeEscape() {
			if (position + 4 > input.length()) {
				throw error("Incomplete Unicode escape.");
			}

			String digits = input.substring(position, position + 4);
			position += 4;

			try {
				return (char) Integer.parseInt(digits, 16);
			} catch (NumberFormatException exception) {
				throw error("Invalid Unicode escape.");
			}
		}

		private Long parseNumber() {
			int start = position;
			consume('-');

			while (position < input.length() && Character.isDigit(input.charAt(position))) {
				position++;
			}

			if (start == position || (input.charAt(start) == '-' && start + 1 == position)) {
				throw error("Invalid JSON value.");
			}

			try {
				return Long.parseLong(input.substring(start, position));
			} catch (NumberFormatException exception) {
				throw error("JSON number is outside the supported range.");
			}
		}

		private Object parseLiteral(String literal, Object value) {
			if (!input.startsWith(literal, position)) {
				throw error("Invalid JSON literal.");
			}
			position += literal.length();
			return value;
		}

		private void skipWhitespace() {
			while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
				position++;
			}
		}

		private boolean consume(char expected) {
			if (position < input.length() && input.charAt(position) == expected) {
				position++;
				return true;
			}
			return false;
		}

		private void expect(char expected) {
			if (!consume(expected)) {
				throw error("Expected '" + expected + "'.");
			}
		}

		private IllegalArgumentException error(String message) {
			return new IllegalArgumentException(message + " Position: " + position);
		}
	}
}
