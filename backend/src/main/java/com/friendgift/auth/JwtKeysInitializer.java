package com.friendgift.auth;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@ApplicationScoped
public class JwtKeysInitializer {
	void onStart(@Observes StartupEvent event) {
		try {
			Path keysDir = Path.of("keys");
			Path privateKeyPath = keysDir.resolve("privateKey.pem");
			Path publicKeyPath = keysDir.resolve("publicKey.pem");

			if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
				return;
			}

			Files.createDirectories(keysDir);

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();

			String privatePem = toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded());
			String publicPem = toPem("PUBLIC KEY", keyPair.getPublic().getEncoded());

			Files.writeString(privateKeyPath, privatePem, StandardCharsets.US_ASCII);
			Files.writeString(publicKeyPath, publicPem, StandardCharsets.US_ASCII);
		} catch (Exception e) {
			// Don't crash the app on dev convenience; JWT endpoints will fail with a clear error if keys are missing.
			System.err.println("Failed to create local JWT keys under ./keys: " + e.getMessage());
		}
	}

	private static String toPem(String type, byte[] der) {
		String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
		return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
	}
}
