package com.capstone.pronunciation.domain.upload.dto;

public record FastApiDispatchResponse(
		Long uploadId,
		String fastApiUrl,
		int statusCode
) {
}
