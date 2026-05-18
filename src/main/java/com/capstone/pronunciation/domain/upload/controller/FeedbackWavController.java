package com.capstone.pronunciation.domain.upload.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.upload.dto.FeedbackWavRequest;
import com.capstone.pronunciation.domain.upload.dto.FeedbackWavResponse;
import com.capstone.pronunciation.domain.upload.service.FeedbackWavService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/media")
@RestController
@Tag(name = "Frontend - Feedback WAV", description = "프론트 실사용 API: AI 서버 /feedback-wav를 백엔드에서 프록시합니다.")
public class FeedbackWavController {

	private final FeedbackWavService feedbackWavService;

	public FeedbackWavController(FeedbackWavService feedbackWavService) {
		this.feedbackWavService = feedbackWavService;
	}

	@PostMapping("/feedback-wav")
	@Operation(
			summary = "[프론트 사용] mild/spicy 햄스터 음성 피드백 생성",
			description = "프론트가 보낸 {analysis_text} JSON을 AI 서버 POST /feedback-wav로 전달하고, mild/spicy wav base64 응답을 그대로 반환합니다."
	)
	public ResponseEntity<FeedbackWavResponse> synthesize(
			@org.springframework.web.bind.annotation.RequestBody FeedbackWavRequest request) {
		FeedbackWavService.FeedbackWavResult result = feedbackWavService.synthesize(request);
		return ResponseEntity.status(result.statusCode()).body(result.body());
	}
}
