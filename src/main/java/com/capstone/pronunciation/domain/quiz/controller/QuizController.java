package com.capstone.pronunciation.domain.quiz.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capstone.pronunciation.domain.quiz.dto.StartSessionResponse;
import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerRequest;
import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerResponse;
import com.capstone.pronunciation.domain.quiz.dto.SubmitGradedRequest;
import com.capstone.pronunciation.domain.quiz.service.QuizService;
import com.capstone.pronunciation.domain.session.dto.SessionStartRequest;

@RequestMapping("/api/quiz")
@RestController
public class QuizController {
	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping("/sessions")
	public StartSessionResponse startSession(
			Authentication authentication,
			@RequestBody SessionStartRequest request) {
		return quizService.startSession(authentication.getName(), request.selectedLevel());
	}

	@PostMapping("/sessions/{sessionId}/submit")
	public SubmitAnswerResponse submit(
			Authentication authentication,
			@PathVariable Long sessionId,
			@RequestBody SubmitAnswerRequest request) {
		return quizService.submitTranscript(authentication.getName(), sessionId, request.questionId(), request.transcript());
	}

	@PostMapping("/sessions/{sessionId}/submit-graded")
	public SubmitAnswerResponse submitGraded(
			Authentication authentication,
			@PathVariable Long sessionId,
			@RequestBody SubmitGradedRequest request) {
		return quizService.submitGraded(authentication.getName(), sessionId, request);
	}

	@PostMapping(value = "/sessions/{sessionId}/submit-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
			// 데모용 저장: DB에 LONGBLOB으로 저장합니다(운영에서는 S3 같은 외부 스토리지 권장).
			if (audio.getSize() > 5 * 1024 * 1024) {
				throw new IllegalArgumentException("audio 파일은 5MB 이하만 지원합니다.");
			}
			audioData = audio.getBytes();
			audioFileName = audio.getOriginalFilename();
			audioContentType = audio.getContentType();
			audioSizeBytes = audio.getSize();
		}

		// NOTE: STT(음성→텍스트)는 아직 미구현. transcript는 클라이언트에서 전달받아 저장/채점합니다.
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
