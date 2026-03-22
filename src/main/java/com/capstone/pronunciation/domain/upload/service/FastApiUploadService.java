package com.capstone.pronunciation.domain.upload.service;

import java.net.URL;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.amazonaws.services.s3.AmazonS3;
import com.capstone.pronunciation.domain.upload.dto.FastApiDispatchResponse;
import com.capstone.pronunciation.domain.upload.dto.FastApiUploadRequest;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
import com.capstone.pronunciation.global.config.S3Config;

@Service
public class FastApiUploadService {

	private static final long PRESIGNED_URL_EXPIRES_IN_SECONDS = 600;

	private final RestClient restClient;
	private final AmazonS3 amazonS3;
	private final S3Config s3Config;
	private final String analyzePath;

	public FastApiUploadService(
			@Value("${app.fastapi.base-url:http://localhost:8000}") String baseUrl,
			@Value("${app.fastapi.analyze-path:/analyze}") String analyzePath,
			AmazonS3 amazonS3,
			S3Config s3Config) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
		this.amazonS3 = amazonS3;
		this.s3Config = s3Config;
		this.analyzePath = analyzePath;
	}

	public FastApiDispatchResponse sendUpload(
			UploadFile uploadFile,
			Long sessionId,
			Long questionId,
			String expectedText) {
		if (sessionId == null) {
			throw new IllegalArgumentException("sessionId는 필수입니다.");
		}
		if (questionId == null) {
			throw new IllegalArgumentException("questionId는 필수입니다.");
		}
		if (expectedText == null || expectedText.isBlank()) {
			throw new IllegalArgumentException("expectedText는 필수입니다.");
		}

		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
		URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), uploadFile.getS3Key(), expiration);

		FastApiUploadRequest request = new FastApiUploadRequest(
				sessionId,
				questionId,
				uploadFile.getId(),
				uploadFile.getS3Key(),
				presignedUrl.toString(),
				expectedText,
				Instant.now()
		);

		HttpStatusCode statusCode = restClient.post()
				.uri(analyzePath)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.toBodilessEntity()
				.getStatusCode();

		return new FastApiDispatchResponse(uploadFile.getId(), analyzePath, statusCode.value());
	}
}
