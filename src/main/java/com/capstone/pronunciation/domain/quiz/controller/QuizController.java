package com.capstone.pronunciation.domain.quiz.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerRequest;
import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerResponse;
import com.capstone.pronunciation.domain.quiz.service.QuizService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/learning/sessions/{sessionId}/answers")
@RestController
@Tag(name = "Support - Answers", description = "프론트 직접 사용하지 않는 보조 API: 텍스트 답안 저장이나 로컬 테스트용으로만 사용합니다.")
public class QuizController {

	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping
	@Operation(
			summary = "[보조용] 텍스트 답안 제출",
			description = "연동 대상: Support only. 세션 내 특정 문제에 대한 transcript 답안을 제출하고 점수를 저장합니다. 실서비스 프론트는 오디오 업로드 후 /api/media/audio/{uploadId}/analyze 흐름을 사용하는 것을 권장합니다."
	)
	public SubmitAnswerResponse submit(
			Authentication authentication,
			@PathVariable Long sessionId,
			@RequestBody SubmitAnswerRequest request) {
		return quizService.submitTranscript(authentication.getName(), sessionId, request.questionId(), request.transcript());
	}
}
