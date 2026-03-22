package com.capstone.pronunciation.domain.upload.dto;

public record PresignedUrlResponse(
		String key,
		String url,
		long expiresInSeconds
) {
}
