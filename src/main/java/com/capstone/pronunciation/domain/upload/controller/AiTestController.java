package com.capstone.pronunciation.domain.upload.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.upload.dto.FastApiTestAnalyzeRequest;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
import com.capstone.pronunciation.domain.upload.repository.UploadFileRepository;
import com.capstone.pronunciation.domain.upload.service.FastApiUploadService;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/test/ai")
@RestController
@Tag(name = "Support - AI Test", description = "프론트 직접 사용하지 않는 테스트용 API입니다. 업로드한 오디오를 이용해 AI 서버 연동만 검증하고, DB에는 결과를 저장하지 않습니다.")
public class AiTestController {

	private final UploadFileRepository uploadFileRepository;
	private final FastApiUploadService fastApiUploadService;

	public AiTestController(
			UploadFileRepository uploadFileRepository,
			FastApiUploadService fastApiUploadService) {
		this.uploadFileRepository = uploadFileRepository;
		this.fastApiUploadService = fastApiUploadService;
	}

	@PostMapping("/audio/{uploadId}/analyze")
	@Operation(
			summary = "[테스트용] 업로드 오디오 AI 분석 호출",
			description = "연동 대상: Support only. 이미 업로드된 audio uploadId를 사용해 AI 서버로만 요청을 보내고, 응답 원문을 그대로 반환합니다. 세션 결과/피드백/점수는 DB에 저장하지 않습니다."
	)
	public JsonNode analyzeUploadedAudio(
			Authentication authentication,
			@PathVariable Long uploadId,
			@RequestBody(description = "AI 서버 테스트 요청 바디", required = true)
			@org.springframework.web.bind.annotation.RequestBody FastApiTestAnalyzeRequest request) {
		String username = extractUsername(authentication);
		UploadFile uploadFile = uploadFileRepository.findById(uploadId)
				.orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다."));

		if (!uploadFile.getUser().getEmail().equals(username)) {
			throw new IllegalArgumentException("본인 파일만 테스트할 수 있습니다.");
		}

		return fastApiUploadService.testAnalyze(uploadFile, request.word(), request.frames());
	}

	private String extractUsername(Authentication authentication) {
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			throw new IllegalArgumentException("인증 정보가 올바르지 않습니다.");
		}
		return authentication.getName();
	}
}
