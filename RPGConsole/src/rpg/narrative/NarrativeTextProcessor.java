package rpg.narrative;

import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import rpg.recruitment.RecruitmentCandidateStateCatalog;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;

// Validates, decodes and resolves external narrative text.
public final class NarrativeTextProcessor {

	private static final Set<String> SUPPORTED_SIMPLE_PLACEHOLDERS = Set
			.of("leaderName", "groupMember2", "groupMember3", "groupMember4");

	private static final String RECRUIT_NAME_PREFIX = "recruitName:";

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private NarrativeTextProcessor() {
	}

	public static String decodeTemplate(String value, String fieldName) {
		String decodedText = decodeLineBreaks(value, fieldName);

		return validateTemplate(decodedText, fieldName);
	}

	public static String decodeLiteral(String value, String fieldName) {
		String decodedText = decodeLineBreaks(value, fieldName);

		rejectPlaceholderCharacters(decodedText, fieldName);

		return decodedText;
	}

	public static String requireLiteral(String value, String fieldName) {
		String normalizedValue = requireText(value, fieldName);

		if (normalizedValue.contains("\\")) {
			throw new IllegalArgumentException(fieldName + " cannot contain backslashes.");
		}

		if (normalizedValue.contains("\n") || normalizedValue.contains("\r")) {
			throw new IllegalArgumentException(fieldName + " cannot contain line breaks.");
		}

		rejectPlaceholderCharacters(normalizedValue, fieldName);

		return normalizedValue;
	}

	public static String validateTemplate(String value, String fieldName) {
		String normalizedValue = requireText(value, fieldName);

		if (normalizedValue.contains("\\")) {
			throw new IllegalArgumentException(fieldName + " cannot contain unresolved backslashes.");
		}

		validatePlaceholders(normalizedValue, fieldName);

		return normalizedValue;
	}

	public static String resolve(String template, Party party) {
		if (party == null) {
			throw new IllegalArgumentException("Party cannot be null.");
		}

		String validatedTemplate = validateTemplate(template, "Narrative text");

		List<PlayableCharacter> members = party.getMembers();

		StringBuilder resolvedText = new StringBuilder();

		for (int index = 0; index < validatedTemplate.length(); index++) {
			char character = validatedTemplate.charAt(index);

			if (character != '{') {
				resolvedText.append(character);
				continue;
			}

			int closingBrace = validatedTemplate.indexOf('}', index + 1);

			String placeholder = validatedTemplate.substring(index + 1, closingBrace);

			resolvedText.append(resolvePlaceholder(placeholder, members));

			index = closingBrace;
		}

		return resolvedText.toString();
	}

	public static String resolveRecruitNames(String template, RecruitmentCandidateStateCatalog candidateStateCatalog) {
		if (candidateStateCatalog == null) {
			throw new IllegalArgumentException("Recruitment candidate state catalog cannot be null.");
		}

		String validatedTemplate = validateTemplate(template, "Narrative text");

		StringBuilder resolvedText = new StringBuilder();

		for (int index = 0; index < validatedTemplate.length(); index++) {
			char character = validatedTemplate.charAt(index);

			if (character != '{') {
				resolvedText.append(character);
				continue;
			}

			int closingBrace = validatedTemplate.indexOf('}', index + 1);

			String placeholder = validatedTemplate.substring(index + 1, closingBrace);

			if (placeholder.startsWith(RECRUIT_NAME_PREFIX)) {
				String candidateId = placeholder.substring(RECRUIT_NAME_PREFIX.length());

				resolvedText.append(candidateStateCatalog.getByCandidateId(candidateId).getName());
			} else {
				resolvedText.append('{').append(placeholder).append('}');
			}

			index = closingBrace;
		}

		return resolvedText.toString();
	}

	public static Set<String> getRecruitmentCandidateIds(String template) {
		String validatedTemplate = validateTemplate(template, "Narrative text");

		Set<String> candidateIds = new LinkedHashSet<>();

		for (int index = 0; index < validatedTemplate.length(); index++) {
			if (validatedTemplate.charAt(index) != '{') {
				continue;
			}

			int closingBrace = validatedTemplate.indexOf('}', index + 1);

			String placeholder = validatedTemplate.substring(index + 1, closingBrace);

			if (placeholder.startsWith(RECRUIT_NAME_PREFIX)) {
				candidateIds.add(placeholder.substring(RECRUIT_NAME_PREFIX.length()));
			}

			index = closingBrace;
		}

		return Set.copyOf(candidateIds);
	}

	private static String decodeLineBreaks(String value, String fieldName) {
		String normalizedValue = requireText(value, fieldName);

		StringBuilder decodedText = new StringBuilder();

		for (int index = 0; index < normalizedValue.length(); index++) {
			char character = normalizedValue.charAt(index);

			if (character != '\\') {
				decodedText.append(character);
				continue;
			}

			if (index + 1 >= normalizedValue.length() || normalizedValue.charAt(index + 1) != 'n') {
				throw new IllegalArgumentException(
						fieldName + " can only use backslashes as the \\\\n " + "line-break sequence."
				);
			}

			decodedText.append('\n');
			index++;
		}

		return decodedText.toString();
	}

	private static void validatePlaceholders(String value, String fieldName) {
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);

			if (character == '}') {
				throw new IllegalArgumentException(fieldName + " contains an unmatched closing brace.");
			}

			if (character != '{') {
				continue;
			}

			int closingBrace = value.indexOf('}', index + 1);

			if (closingBrace < 0) {
				throw new IllegalArgumentException(fieldName + " contains an unclosed placeholder.");
			}

			String placeholder = value.substring(index + 1, closingBrace);

			if (placeholder.contains("{") || !isSupportedPlaceholder(placeholder)) {
				throw new IllegalArgumentException(
						fieldName + " contains an unsupported placeholder: {" + placeholder + "}"
				);
			}

			index = closingBrace;
		}
	}

	private static boolean isSupportedPlaceholder(String placeholder) {
		if (SUPPORTED_SIMPLE_PLACEHOLDERS.contains(placeholder)) {
			return true;
		}

		if (!placeholder.startsWith(RECRUIT_NAME_PREFIX)) {
			return false;
		}

		String candidateId = placeholder.substring(RECRUIT_NAME_PREFIX.length());

		return candidateId.matches(ID_PATTERN);
	}

	private static String resolvePlaceholder(String placeholder, List<PlayableCharacter> members) {
		int memberIndex = switch (placeholder) {
		case "leaderName" -> 0;
		case "groupMember2" -> 1;
		case "groupMember3" -> 2;
		case "groupMember4" -> 3;
		default -> throw new IllegalStateException("Unsupported validated placeholder: " + placeholder);
		};

		if (memberIndex >= members.size()) {
			throw new IllegalStateException("Narrative text requires unavailable party member: {" + placeholder + "}");
		}

		return members.get(memberIndex).getName();
	}

	private static void rejectPlaceholderCharacters(String value, String fieldName) {
		if (value.contains("{") || value.contains("}")) {
			throw new IllegalArgumentException(fieldName + " cannot contain placeholders.");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (fieldName == null || fieldName.isBlank()) {
			throw new IllegalArgumentException("Narrative text field name cannot be empty.");
		}

		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}

		return value.trim();
	}
}
