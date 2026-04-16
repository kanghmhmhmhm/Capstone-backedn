package com.capstone.pronunciation.domain.dashboard.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.dashboard.dto.DashboardSummaryResponse;
import com.capstone.pronunciation.domain.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/dashboard")
@RestController
@Tag(name = "Frontend - Dashboard", description = "프론트 실사용 API: 대시보드 요약 정보 조회를 담당합니다.")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/summary")
	@Operation(
			summary = "[프론트 사용] 대시보드 요약 조회",
			description = "현재 사용자의 학습 현황, 평균 점수, 단계별 완료율, 취약 발음 요약 정보를 조회합니다."
	)
	public DashboardSummaryResponse summary(Authentication authentication) {
		return dashboardService.summary(authentication.getName());
	}
}
