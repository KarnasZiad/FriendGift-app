import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class GenerateJwtKeys {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: java GenerateJwtKeys <privateKeyPemPath> <publicKeyPemPath>");
			System.exit(2);
		}

		Path privateKeyPath = Path.of(args[0]);
		Path publicKeyPath = Path.of(args[1]);

		KeyPair keyPair = generateRsaKeyPair();

		String privatePem = toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded());
		String publicPem = toPem("PUBLIC KEY", keyPair.getPublic().getEncoded());

		writeString(privateKeyPath, privatePem);
		writeString(publicKeyPath, publicPem);

		System.out.println("Wrote keys:");
		System.out.println("- " + privateKeyPath.toAbsolutePath());
		System.out.println("- " + publicKeyPath.toAbsolutePath());
	}

	private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}

	private static String toPem(String type, byte[] der) {
		String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
		return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
	}

	private static void writeString(Path path, String content) throws IOException {
		Files.createDirectories(path.toAbsolutePath().getParent());
		Files.writeString(path, content, StandardCharsets.US_ASCII);
	}
}
