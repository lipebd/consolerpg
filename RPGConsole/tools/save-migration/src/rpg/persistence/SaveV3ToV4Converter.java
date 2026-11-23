package rpg.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Properties;

// Converts private development saves from internal format V3 to V4.
public final class SaveV3ToV4Converter {

	private static final String FORMAT_VERSION_PROPERTY = "save.formatVersion";
	private static final String BESTIARY_PREFIX = "bestiary.";
	private static final String BESTIARY_COUNT_PROPERTY = "bestiary.discovered.count";
	private static final String SOURCE_VERSION = "3";
	private static final String TARGET_VERSION = "4";

	private SaveV3ToV4Converter() {
	}

	public static void main(String[] args) {
		if (args.length > 2) {
			System.err.println("Uso: converter-save-v3-to-v4.cmd [entrada] [saida]");
			System.exit(1);
		}

		Path inputPath = args.length >= 1 ? Path.of(args[0]) : Path.of("save.txt");
		Path outputPath = args.length >= 2 ? Path.of(args[1]) : inputPath.resolveSibling("save-v4.txt");

		try {
			convert(inputPath, outputPath);

			System.out.println("Conversão concluída com sucesso.");
			System.out.println("Save V3 preservado: " + inputPath.toAbsolutePath().normalize());
			System.out.println("Save V4 criado: " + outputPath.toAbsolutePath().normalize());
			System.out.println("O Bestiário convertido começa vazio.");
		} catch (IllegalArgumentException | IllegalStateException | IOException exception) {
			System.err.println("A conversão falhou: " + exception.getMessage());
			System.exit(1);
		}
	}

	static void convert(Path inputPath, Path outputPath) throws IOException {
		Path normalizedInput = inputPath.toAbsolutePath().normalize();
		Path normalizedOutput = outputPath.toAbsolutePath().normalize();

		if (normalizedInput.equals(normalizedOutput)) {
			throw new IllegalArgumentException("O arquivo de saída deve ser diferente do save V3 original.");
		}

		if (!Files.isRegularFile(normalizedInput)) {
			throw new IllegalArgumentException("O save V3 de entrada não foi encontrado: " + normalizedInput);
		}

		if (Files.exists(normalizedOutput)) {
			throw new IllegalArgumentException("O arquivo de saída já existe: " + normalizedOutput);
		}

		byte[] encodedInput = Files.readAllBytes(normalizedInput);
		byte[] sourcePlaintext = null;
		byte[] convertedPlaintext = null;
		byte[] encodedOutput = null;
		byte[] verificationPlaintext = null;

		try {
			SaveEncryptionService encryptionService = createApplicationEncryptionService();
			EncryptedSavePayload sourcePayload = SaveContainerCodec.decode(encodedInput);

			sourcePlaintext = encryptionService.decrypt(sourcePayload);

			Properties convertedProperties = PropertyPayloadDecoder.decode(sourcePlaintext);

			validateSourceProperties(convertedProperties);

			convertedProperties.setProperty(FORMAT_VERSION_PROPERTY, TARGET_VERSION);
			convertedProperties.setProperty(BESTIARY_COUNT_PROPERTY, "0");

			convertedPlaintext = PropertyPayloadEncoder.encode(convertedProperties);
			EncryptedSavePayload convertedPayload = encryptionService.encrypt(convertedPlaintext);
			encodedOutput = SaveContainerCodec.encode(convertedPayload);

			EncryptedSavePayload verificationPayload = SaveContainerCodec.decode(encodedOutput);
			verificationPlaintext = encryptionService.decrypt(verificationPayload);

			Properties verificationProperties = PropertyPayloadDecoder.decode(verificationPlaintext);

			if (!convertedProperties.equals(verificationProperties)) {
				throw new IllegalStateException("O payload recriptografado não preservou as propriedades convertidas.");
			}

			CampaignPropertiesDeserializer.deserialize(verificationProperties);

			Files.write(normalizedOutput, encodedOutput, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		} finally {
			Arrays.fill(encodedInput, (byte) 0);
			fillIfPresent(sourcePlaintext);
			fillIfPresent(convertedPlaintext);
			fillIfPresent(encodedOutput);
			fillIfPresent(verificationPlaintext);
		}
	}

	private static void validateSourceProperties(Properties properties) {
		String sourceVersion = properties.getProperty(FORMAT_VERSION_PROPERTY);

		if (!SOURCE_VERSION.equals(sourceVersion)) {
			throw new IllegalArgumentException(
					"A ferramenta aceita somente saves internos V3; versão encontrada: " + sourceVersion
			);
		}

		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith(BESTIARY_PREFIX)) {
				throw new IllegalArgumentException("O save V3 contém uma propriedade inesperada de Bestiário.");
			}
		}
	}

	private static SaveEncryptionService createApplicationEncryptionService() {
		byte[] encryptionKey = ApplicationSaveEncryptionKey.getBytes();

		try {
			return new SaveEncryptionService(encryptionKey);
		} finally {
			Arrays.fill(encryptionKey, (byte) 0);
		}
	}

	private static void fillIfPresent(byte[] bytes) {
		if (bytes != null) {
			Arrays.fill(bytes, (byte) 0);
		}
	}
}
