package com.capstone.pronunciation.domain.history.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.history.dto.StudyHistoryItemResponse;
import com.capstone.pronunciation.domain.history.service.HistoryService;
import com.capstone.pronunciation.domain.session.dto.SessionDetailResponse;
import com.capstone.pronunciation.domain.session.dto.SessionSummaryResponse;
import com.capstone.pronunciation.domain.session.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/history")
@RestController
@Tag(name = "Frontend - History", description = "프론트 실사용 API: 학습 기록 목록과 최근 결과 조회를 담당합니다.")
public class HistoryController {

	private final SessionService sessionService;
	private final HistoryService historyService;

	public HistoryController(SessionService sessionService, HistoryService historyService) {
		this.sessionService = sessionService;
		this.historyService = historyService;
	}

	@GetMapping("/sessions")
	@Operation(
			summary = "[프론트 사용] 학습 세션 목록 조회",
			description = "현재 사용자의 학습 세션 목록을 최신순으로 조회합니다. 내 학습 기록 화면의 기본 목록 API입니다."
	)
	public List<SessionSummaryResponse> sessions(Authentication authentication) {
		return sessionService.sessions(authentication.getName());
	}

	@GetMapping("/sessions/{sessionId}")
	@Operation(
			summary = "[프론트 사용] 학습 기록 세션 상세 조회",
			description = "내 학습 기록 화면에서 선택한 세션의 문제별 결과와 점수 정보를 조회합니다."
	)
	public SessionDetailResponse sessionDetail(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionDetail(authentication.getName(), sessionId);
	}

	@GetMapping("/results/recent")
	@Operation(
			summary = "[프론트 사용] 최근 학습 결과 조회",
			description = "최근 학습 결과를 최신순으로 조회합니다. 대시보드 최근 기록, 오답노트 요약 등에 사용할 수 있습니다."
	)
	public List<StudyHistoryItemResponse> recentResults(
			Authentication authentication,
			@RequestParam(defaultValue = "50") int limit) {
		return historyService.recentResults(authentication.getName(), limit);
	}
}
