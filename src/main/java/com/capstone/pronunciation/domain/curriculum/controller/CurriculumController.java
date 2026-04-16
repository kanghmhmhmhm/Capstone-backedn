package com.capstone.pronunciation.domain.curriculum.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.curriculum.dto.LessonDetailResponse;
import com.capstone.pronunciation.domain.curriculum.dto.LessonSummaryResponse;
import com.capstone.pronunciation.domain.curriculum.dto.StageProgressResponse;
import com.capstone.pronunciation.domain.curriculum.service.CurriculumService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/curriculum")
@RestController
@Tag(name = "Frontend - Curriculum", description = "프론트 실사용 API: 커리큘럼, 레슨 목록, 완료 처리를 담당합니다.")
public class CurriculumController {
	private final CurriculumService curriculumService;

	public CurriculumController(CurriculumService curriculumService) {
		this.curriculumService = curriculumService;
	}

	@GetMapping("/stages")
	@Deprecated
	@Operation(
			summary = "[호환용] stage 목록 조회",
			description = "기존 호환성을 위한 API입니다. 신규 프론트는 사용하지 않고 /api/curriculum/levels 를 사용합니다.",
			deprecated = true
	)
	public List<StageProgressResponse> stages(Authentication authentication) {
		return curriculumService.stages(authentication.getName());
	}

	@GetMapping("/levels")
	@Operation(
			summary = "[프론트 사용] 레벨 목록 조회",
			description = "레벨별 잠금 상태, 완료 여부, 진행도를 조회합니다."
	)
	public List<StageProgressResponse> levels(Authentication authentication) {
		return curriculumService.stages(authentication.getName());
	}

	@GetMapping("/levels/{level}/lessons")
	@Operation(
			summary = "[프론트 사용] 레벨별 레슨 조회",
			description = "선택한 레벨에 속한 레슨(문제) 목록과 완료 여부를 조회합니다."
	)
	public List<LessonSummaryResponse> lessonsByLevel(
			Authentication authentication,
			@PathVariable Integer level) {
		return curriculumService.lessonsByStageName(authentication.getName(), toStageName(level));
	}

	@GetMapping("/lessons/{lessonId}")
	@Operation(
			summary = "[프론트 사용] 레슨 상세 조회",
			description = "특정 레슨의 상세 문제 정보와 완료 여부를 조회합니다."
	)
	public LessonDetailResponse lessonDetail(
			Authentication authentication,
			@PathVariable Long lessonId) {
		return curriculumService.questionDetail(authentication.getName(), lessonId);
	}

	@PostMapping("/lessons/{lessonId}/complete")
	@Operation(
			summary = "[프론트 사용] 레슨 완료 처리",
			description = "특정 레슨을 완료 처리하고 진행도를 반영합니다."
	)
	public void completeLesson(
			Authentication authentication,
			@PathVariable Long lessonId) {
		curriculumService.completeQuestion(authentication.getName(), lessonId, 100);
	}

	private static String toStageName(Integer level) {
		if (level == null) {
			throw new IllegalArgumentException("level은 필수입니다.");
		}
		return switch (level) {
			case 1 -> "BASIC_PRONUNCIATION";
			case 2 -> "WORD";
			default -> {
				if (level < 3 || level > 15) {
					throw new IllegalArgumentException("지원하지 않는 level입니다: " + level);
				}
				yield "Sentence Lv" + level;
			}
		};
	}
}
