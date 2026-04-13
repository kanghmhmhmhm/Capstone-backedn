package com.capstone.pronunciation.domain.quiz.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerRequest;
import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerResponse;
import com.capstone.pronunciation.domain.quiz.dto.SubmitGradedRequest;
import com.capstone.pronunciation.domain.quiz.service.QuizService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/learning/sessions/{sessionId}/answers")
@RestController
@Tag(name = "Learning Answers", description = "학습 세션 내 답안 제출 및 채점 API")
public class QuizController {

	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping
	@Operation(summary = "텍스트 답안 제출", description = "세션 내 특정 문제에 대한 transcript 답안을 제출하고 점수를 저장합니다.")
	public SubmitAnswerResponse submit(
			Authentication authentication,
			@PathVariable Long sessionId,
			@RequestBody SubmitAnswerRequest request) {
		return quizService.submitTranscript(authentication.getName(), sessionId, request.questionId(), request.transcript());
	}

	@PostMapping("/graded")
	@Operation(summary = "채점된 답안 제출", description = "외부 분석 결과를 포함한 점수, transcript를 세션 답안으로 저장합니다.")
	public SubmitAnswerResponse submitGraded(
			Authentication authentication,
			@PathVariable Long sessionId,
			@RequestBody SubmitGradedRequest request) {
		return quizService.submitGraded(authentication.getName(), sessionId, request);
	}

	@PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "오디오 답안 제출", description = "오디오 파일과 transcript, 점수 정보를 함께 제출하여 세션 답안으로 저장합니다.")
	public SubmitAnswerResponse submitAudio(
			Authentication authentication,
			@PathVariable Long sessionId,
			@RequestParam Long questionId,
			@RequestParam(required = false) String transcript,
			@RequestParam(required = false) Integer score,
			@RequestParam(required = false) Integer voiceScore,
			@RequestParam(required = false) Integer visionScore,
			@RequestParam(required = false) MultipartFile audio) throws java.io.IOException {
		byte[] audioData = null;
		String audioFileName = null;
		String audioContentType = null;
		Long audioSizeBytes = null;

		if (audio != null && !audio.isEmpty()) {
			if (audio.getSize() > 5 * 1024 * 1024) {
				throw new IllegalArgumentException("audio 파일은 5MB 이하만 지원합니다.");
			}
			audioData = audio.getBytes();
			audioFileName = audio.getOriginalFilename();
			audioContentType = audio.getContentType();
			audioSizeBytes = audio.getSize();
		}

		return quizService.submitGradedAudio(
				authentication.getName(),
				sessionId,
				questionId,
				transcript,
				score,
				voiceScore,
				visionScore,
				audioFileName,
				audioContentType,
				audioSizeBytes,
				audioData
		);
	}
}
