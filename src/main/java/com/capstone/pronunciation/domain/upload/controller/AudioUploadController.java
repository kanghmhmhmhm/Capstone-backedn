package com.capstone.pronunciation.domain.upload.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.capstone.pronunciation.domain.upload.dto.PresignedUrlResponse;
import com.capstone.pronunciation.domain.upload.dto.UploadResponse;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
import com.capstone.pronunciation.domain.upload.repository.UploadFileRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;
import com.capstone.pronunciation.global.config.S3Config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/media/audio")
@RestController
@Tag(name = "Frontend - Audio Upload", description = "프론트 실사용 API: 프론트엔드가 오디오를 업로드하고 presigned URL을 조회하는 단계입니다. AI 서버 호출 전 단계입니다.")
public class AudioUploadController {

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private static final long PRESIGNED_URL_EXPIRES_IN_SECONDS = 600;

	private final AmazonS3 amazonS3;
	private final S3Config s3Config;
	private final UserRepository userRepository;
	private final UploadFileRepository uploadFileRepository;

	public AudioUploadController(
			AmazonS3 amazonS3,
			S3Config s3Config,
			UserRepository userRepository,
			UploadFileRepository uploadFileRepository) {
		this.amazonS3 = amazonS3;
		this.s3Config = s3Config;
		this.userRepository = userRepository;
		this.uploadFileRepository = uploadFileRepository;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
			summary = "[프론트 사용] 오디오 업로드",
			description = "연동 대상: Frontend. WAV 오디오 파일을 업로드하고 uploadId, 파일 URL, 메타데이터를 반환합니다. 이후 /api/media/audio/{uploadId}/analyze 호출의 입력값으로 사용됩니다."
	)
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
		if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".wav")) {
			throw new IllegalArgumentException("WAV 파일만 업로드할 수 있습니다.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !isSupportedWavContentType(contentType)) {
			throw new IllegalArgumentException("WAV 파일만 업로드할 수 있습니다.");
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

	@GetMapping("/{uploadId}/presigned-url")
	@Operation(
			summary = "[프론트 사용] 오디오 presigned URL 조회",
			description = "연동 대상: Frontend. 업로드된 오디오 파일에 접근할 수 있는 presigned URL을 발급합니다. 재생이 필요한 결과 화면에서 사용할 수 있습니다."
	)
	public PresignedUrlResponse getPresignedUrl(
			Authentication authentication,
			@PathVariable Long uploadId) {
		String username = extractUsername(authentication);
		UploadFile uploadFile = uploadFileRepository.findWithUserById(uploadId)
				.orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다."));
		if (!uploadFile.getUser().getEmail().equals(username)) {
			throw new IllegalArgumentException("본인 업로드 파일을 찾을 수 없습니다.");
		}

		String key = uploadFile.getS3Key();
		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
		URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), key, expiration);
		return new PresignedUrlResponse(key, presignedUrl.toString(), PRESIGNED_URL_EXPIRES_IN_SECONDS);
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

	private boolean isSupportedWavContentType(String contentType) {
		return "audio/wav".equalsIgnoreCase(contentType)
				|| "audio/wave".equalsIgnoreCase(contentType)
				|| "audio/x-wav".equalsIgnoreCase(contentType)
				|| "audio/vnd.wave".equalsIgnoreCase(contentType)
				|| MediaType.APPLICATION_OCTET_STREAM_VALUE.equalsIgnoreCase(contentType);
	}

	private String extractUsername(Authentication authentication) {
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			throw new IllegalArgumentException("인증 정보가 올바르지 않습니다.");
		}
		return authentication.getName();
	}
}
