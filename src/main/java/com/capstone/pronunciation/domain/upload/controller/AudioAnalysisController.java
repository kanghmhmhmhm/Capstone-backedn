package com.capstone.pronunciation.domain.upload.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.upload.dto.FastApiDispatchResponse;
import com.capstone.pronunciation.domain.upload.dto.SendToFastApiRequest;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
import com.capstone.pronunciation.domain.upload.repository.UploadFileRepository;
import com.capstone.pronunciation.domain.upload.service.FastApiUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/media/audio/{uploadId}")
@RestController
@Tag(name = "Frontend - Audio Analysis", description = "프론트 실사용 API이면서 AI 서버 연동 API입니다. 프론트엔드가 호출하면 Spring이 FastAPI 분석 서버로 요청을 전달합니다.")
public class AudioAnalysisController {

	private final UploadFileRepository uploadFileRepository;
	private final FastApiUploadService fastApiUploadService;

	public AudioAnalysisController(
			UploadFileRepository uploadFileRepository,
			FastApiUploadService fastApiUploadService) {
		this.uploadFileRepository = uploadFileRepository;
		this.fastApiUploadService = fastApiUploadService;
	}

	@PostMapping("/analyze")
	@Operation(
			summary = "[프론트 사용] [AI 서버 연동] 오디오 분석 요청",
			description = "연동 대상: Frontend + FastAPI AI Server. 프론트엔드가 이 API를 호출하면 Spring 서버가 업로드된 오디오와 MediaPipe 프레임 데이터를 FastAPI 분석 서버로 전달하고, 분석 결과·점수·피드백을 저장합니다. 실제 발음 제출 완료 API입니다."
	)
	public FastApiDispatchResponse analyze(
			Authentication authentication,
			@PathVariable Long uploadId,
			@RequestBody(description = "MediaPipe 프레임 데이터가 포함된 분석 요청 바디", required = true)
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
				request.selectedChoice(),
				request.frames()
		);
	}

	private String extractUsername(Authentication authentication) {
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			throw new IllegalArgumentException("인증 정보가 올바르지 않습니다.");
		}
		return authentication.getName();
	}
}
