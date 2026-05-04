package com.capstone.pronunciation.domain.ranking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.ranking.dto.RankingResponse;
import com.capstone.pronunciation.domain.ranking.service.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/ranking")
@RestController
@Tag(name = "Frontend - Ranking", description = "프론트 실사용 API: 랭킹 화면의 순위 목록과 내 순위를 제공합니다.")
public class RankingController {

	private final RankingService rankingService;

	public RankingController(RankingService rankingService) {
		this.rankingService = rankingService;
	}

	@GetMapping
	@Operation(
			summary = "[프론트 사용] 랭킹 조회",
			description = "누적 점수 기준 랭킹 목록과 현재 사용자의 순위를 함께 조회합니다."
	)
	public RankingResponse rankings(Authentication authentication, @RequestParam(defaultValue = "20") int limit) {
		return rankingService.rankings(authentication.getName(), limit);
	}
}
