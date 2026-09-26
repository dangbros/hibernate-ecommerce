package com.code.HibernateProject.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {

	private PasswordHasher() {
	}

	public static String computeSha256Hex(String rawCredential) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] digestBytes = digest.digest(rawCredential.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digestBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm is not available", e);
		}
	}
}
