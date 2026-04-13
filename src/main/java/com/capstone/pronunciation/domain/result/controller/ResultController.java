package com.capstone.pronunciation.domain.result.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.capstone.pronunciation.domain.feedback.dto.FeedbackItemResponse;
import com.capstone.pronunciation.domain.feedback.service.FeedbackService;
import com.capstone.pronunciation.domain.history.dto.StudyHistoryItemResponse;
import com.capstone.pronunciation.domain.history.service.HistoryService;
import com.capstone.pronunciation.domain.session.dto.SessionDetailResponse;
import com.capstone.pronunciation.domain.session.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/results")
@RestController
@Tag(name = "Results", description = "학습 결과, 최근 기록, 세션 피드백 조회 API")
public class ResultController {

	private final SessionService sessionService;
	private final FeedbackService feedbackService;
	private final HistoryService historyService;

	public ResultController(SessionService sessionService, FeedbackService feedbackService, HistoryService historyService) {
		this.sessionService = sessionService;
		this.feedbackService = feedbackService;
		this.historyService = historyService;
	}

	@GetMapping("/recent")
	@Operation(summary = "최근 학습 결과 조회", description = "최근 학습 결과를 최신순으로 조회합니다.")
	public List<StudyHistoryItemResponse> recentResults(
			Authentication authentication,
			@RequestParam(defaultValue = "50") int limit) {
		return historyService.recentResults(authentication.getName(), limit);
	}

	@GetMapping("/sessions/{sessionId}")
	@Operation(summary = "세션 결과 상세 조회", description = "특정 학습 세션의 결과와 문제별 점수 정보를 조회합니다.")
	public SessionDetailResponse sessionResult(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionDetail(authentication.getName(), sessionId);
	}

	@GetMapping("/sessions/{sessionId}/feedback")
	@Operation(summary = "세션 피드백 조회", description = "특정 학습 세션에 대해 저장된 피드백 목록을 조회합니다.")
	public List<FeedbackItemResponse> sessionFeedback(Authentication authentication, @PathVariable Long sessionId) {
		return feedbackService.bySession(authentication.getName(), sessionId);
	}
}
