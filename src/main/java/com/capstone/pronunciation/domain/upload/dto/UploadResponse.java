package com.capstone.pronunciation.domain.upload.dto;

public record UploadResponse(
		Long uploadId,
		String key,
		String url,
		String contentType,
		long size
) {
}
