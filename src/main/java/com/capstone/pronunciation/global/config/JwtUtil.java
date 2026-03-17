package com.capstone.pronunciation.global.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 외부 라이브러리 없이 HMAC-SHA256 JWT 생성/검증.
 * - payload에는 sub(loginId), iat, exp만 넣음.
 */
public final class JwtUtil {

	private static final Base64.Encoder B64_URL_ENC = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder B64_URL_DEC = Base64.getUrlDecoder();

	// 데모용 기본값. 운영에서는 반드시 환경변수/시크릿으로 주입해서 교체.
	private static volatile String secret = "CHANGE_ME_DEV_SECRET_32BYTES_MINIMUM";
	private static volatile long accessTokenTtlSeconds = 60 * 60; // 1h

	private JwtUtil() {
	}

	public static void configure(String secretValue, long ttlSeconds) {
		if (secretValue != null && !secretValue.isBlank()) {
			secret = secretValue;
		}
		if (ttlSeconds > 0) {
			accessTokenTtlSeconds = ttlSeconds;
		}
	}

	public static String issueAccessToken(String loginId) {
		Instant now = Instant.now();
		long iat = now.getEpochSecond();
		long exp = now.plusSeconds(accessTokenTtlSeconds).getEpochSecond();

		String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payloadJson = toJson(Map.of(
				"sub", loginId,
				"iat", iat,
				"exp", exp
		));

		String header = B64_URL_ENC.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
		String payload = B64_URL_ENC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		String unsigned = header + "." + payload;
		String signature = sign(unsigned);
		return unsigned + "." + signature;
	}

	/**
	 * 유효하면 sub(loginId) 반환, 아니면 null.
	 */
	public static String validateAndGetSubject(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) return null;

			String unsigned = parts[0] + "." + parts[1];
			String expectedSig = sign(unsigned);
			if (!MessageDigest.isEqual(expectedSig.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
				return null;
			}

			String payloadJson = new String(B64_URL_DEC.decode(parts[1]), StandardCharsets.UTF_8);
			String sub = readJsonString(payloadJson, "sub");
			Long exp = readJsonLong(payloadJson, "exp");
			if (sub == null || exp == null) return null;
			if (Instant.now().getEpochSecond() >= exp) return null;
			return sub;
		} catch (Exception e) {
			return null;
		}
	}

	private static String sign(String data) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return B64_URL_ENC.encodeToString(sig);
		} catch (Exception e) {
			throw new IllegalStateException("JWT sign failed", e);
		}
	}

	private static String toJson(Map<String, Object> map) {
		// 단순 claim만 쓰는 최소 JSON 생성기(문자열/숫자만 지원).
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (var entry : map.entrySet()) {
			if (!first) sb.append(",");
			first = false;
			sb.append("\"").append(escape(entry.getKey())).append("\":");
			Object value = entry.getValue();
			if (value instanceof Number) {
				sb.append(value);
			} else {
				sb.append("\"").append(escape(String.valueOf(value))).append("\"");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	private static String escape(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static String readJsonString(String json, String key) {
		// 매우 단순한 파서: "key":"value" 형태만 읽음.
		String pattern = "\"" + key + "\":\"";
		int start = json.indexOf(pattern);
		if (start < 0) return null;
		int from = start + pattern.length();
		int end = json.indexOf("\"", from);
		if (end < 0) return null;
		return json.substring(from, end);
	}

	private static Long readJsonLong(String json, String key) {
		// 매우 단순한 파서: "key":123 형태만 읽음.
		String pattern = "\"" + key + "\":";
		int start = json.indexOf(pattern);
		if (start < 0) return null;
		int from = start + pattern.length();
		int end = from;
		while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
		if (end == from) return null;
		return Long.parseLong(json.substring(from, end));
	}
}
