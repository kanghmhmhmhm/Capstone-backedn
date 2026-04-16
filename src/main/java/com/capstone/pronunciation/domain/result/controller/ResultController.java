package com.capstone.pronunciation.domain.result.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.feedback.dto.FeedbackItemResponse;
import com.capstone.pronunciation.domain.feedback.service.FeedbackService;
import com.capstone.pronunciation.domain.session.dto.SessionDetailResponse;
import com.capstone.pronunciation.domain.session.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/results")
@RestController
@Tag(name = "Frontend - Results", description = "프론트 실사용 API: 결과 화면과 세션별 피드백 조회를 담당합니다.")
public class ResultController {

	private final SessionService sessionService;
	private final FeedbackService feedbackService;

	public ResultController(SessionService sessionService, FeedbackService feedbackService) {
		this.sessionService = sessionService;
		this.feedbackService = feedbackService;
	}

	@GetMapping("/sessions/{sessionId}")
	@Operation(
			summary = "[프론트 사용] 세션 결과 상세 조회",
			description = "특정 학습 세션의 결과와 문제별 점수 정보를 조회합니다. 결과 화면에서 사용합니다."
	)
	public SessionDetailResponse sessionResult(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionDetail(authentication.getName(), sessionId);
	}

	@GetMapping("/sessions/{sessionId}/feedback")
	@Operation(
			summary = "[프론트 사용] 세션 피드백 조회",
			description = "특정 학습 세션에 대해 저장된 피드백 목록을 조회합니다. 결과 상세 화면의 피드백 영역에서 사용합니다."
	)
	public List<FeedbackItemResponse> sessionFeedback(Authentication authentication, @PathVariable Long sessionId) {
		return feedbackService.bySession(authentication.getName(), sessionId);
	}
}
