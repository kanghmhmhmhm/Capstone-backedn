package com.capstone.pronunciation.domain.upload.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.capstone.pronunciation.domain.upload.dto.FastApiDispatchResponse;
import com.capstone.pronunciation.domain.upload.dto.PresignedUrlResponse;
import com.capstone.pronunciation.domain.upload.dto.SendToFastApiRequest;
import com.capstone.pronunciation.domain.upload.dto.UploadResponse;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
import com.capstone.pronunciation.domain.upload.repository.UploadFileRepository;
import com.capstone.pronunciation.domain.upload.service.FastApiUploadService;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;
import com.capstone.pronunciation.global.config.S3Config;

@RequestMapping("/api/uploads")
@RestController
public class UploadController {

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private static final long PRESIGNED_URL_EXPIRES_IN_SECONDS = 600;

	private final AmazonS3 amazonS3;
	private final S3Config s3Config;
	private final UserRepository userRepository;
	private final UploadFileRepository uploadFileRepository;
	private final FastApiUploadService fastApiUploadService;

	public UploadController(
			AmazonS3 amazonS3,
			S3Config s3Config,
			UserRepository userRepository,
			UploadFileRepository uploadFileRepository,
			FastApiUploadService fastApiUploadService) {
		this.amazonS3 = amazonS3;
		this.s3Config = s3Config;
		this.userRepository = userRepository;
		this.uploadFileRepository = uploadFileRepository;
		this.fastApiUploadService = fastApiUploadService;
	}

	@PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadResponse uploadAudio(
			Authentication authentication,
			@RequestParam("file") MultipartFile file) throws IOException {
		String username = extractUsername(authentication);
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 필요합니다.");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("파일은 10MB 이하만 업로드할 수 있습니다.");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
			throw new IllegalArgumentException("mp3 파일만 업로드할 수 있습니다.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !isSupportedMp3ContentType(contentType)) {
			throw new IllegalArgumentException("mp3 파일만 업로드할 수 있습니다.");
		}

		String bucket = s3Config.getBucket();
		String key = buildObjectKey(username, originalFilename);

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(contentType);

		try (InputStream inputStream = file.getInputStream()) {
			PutObjectRequest request = new PutObjectRequest(bucket, key, inputStream, metadata);
			amazonS3.putObject(request);
		}

		String objectUrl = amazonS3.getUrl(bucket, key).toString();
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		UploadFile uploadFile = uploadFileRepository.save(new UploadFile(
				user,
				key,
				objectUrl,
				originalFilename,
				contentType,
				file.getSize(),
				Instant.now()
		));

		return new UploadResponse(uploadFile.getId(), key, objectUrl, contentType, file.getSize());
	}

	@GetMapping("/presigned-url")
	public PresignedUrlResponse getPresignedUrl(
			Authentication authentication,
			@RequestParam("key") String key) {
		String username = extractUsername(authentication);
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("key 값이 필요합니다.");
		}
		if (!key.startsWith("audio/%s/".formatted(username))) {
			throw new IllegalArgumentException("본인 파일만 조회할 수 있습니다.");
		}

		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
		URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), key, expiration);
		return new PresignedUrlResponse(key, presignedUrl.toString(), PRESIGNED_URL_EXPIRES_IN_SECONDS);
	}

	@PostMapping("/{uploadId}/send-to-fastapi")
	public FastApiDispatchResponse sendToFastApi(
			Authentication authentication,
			@org.springframework.web.bind.annotation.PathVariable Long uploadId,
			@org.springframework.web.bind.annotation.RequestBody SendToFastApiRequest request) {
		String username = extractUsername(authentication);
		UploadFile uploadFile = uploadFileRepository.findById(uploadId)
				.orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다."));

		if (!uploadFile.getUser().getEmail().equals(username)) {
			throw new IllegalArgumentException("본인 파일만 전송할 수 있습니다.");
		}

		return fastApiUploadService.sendUpload(
				uploadFile,
				request.sessionId(),
				request.questionId(),
				request.expectedText(),
				request.frames()
		);
	}

	private String buildObjectKey(String username, String originalFilename) {
		String safeFilename = originalFilename == null ? "audio" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
		LocalDate today = LocalDate.now();
		return "audio/%s/%d/%02d/%02d/%s-%s".formatted(
				username,
				today.getYear(),
				today.getMonthValue(),
				today.getDayOfMonth(),
				UUID.randomUUID(),
				safeFilename);
	}

	private boolean isSupportedMp3ContentType(String contentType) {
		return "audio/mpeg".equalsIgnoreCase(contentType)
				|| "audio/mp3".equalsIgnoreCase(contentType);
	}

	private String extractUsername(Authentication authentication) {
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			throw new IllegalArgumentException("인증 정보가 올바르지 않습니다.");
		}
		return authentication.getName();
	}
}
