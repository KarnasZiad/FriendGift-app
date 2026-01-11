package com.friendgift;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

public class JwtTestKeysResource implements QuarkusTestResourceLifecycleManager {
	private Path tempDir;

	@Override
	public Map<String, String> start() {
		try {
			tempDir = Files.createTempDirectory("friendgift-jwt-");
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();

			Path privateKeyPath = tempDir.resolve("privateKey.pem");
			Path publicKeyPath = tempDir.resolve("publicKey.pem");

			Files.writeString(privateKeyPath, toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()), StandardCharsets.US_ASCII);
			Files.writeString(publicKeyPath, toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()), StandardCharsets.US_ASCII);

			return Map.of(
					"smallrye.jwt.sign.key.location", privateKeyPath.toAbsolutePath().toString(),
					"mp.jwt.verify.publickey.location", publicKeyPath.toAbsolutePath().toString()
			);
		} catch (Exception e) {
			throw new RuntimeException("Unable to generate JWT test keys", e);
		}
	}

	@Override
	public void stop() {
		if (tempDir == null) {
			return;
		}
		try {
			Files.walk(tempDir)
					.sorted((a, b) -> b.compareTo(a))
					.forEach(p -> {
						try {
							Files.deleteIfExists(p);
						} catch (Exception ignored) {
						}
					});
		} catch (Exception ignored) {
		}
	}

	private static String toPem(String type, byte[] der) {
		String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
		return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
	}
}
