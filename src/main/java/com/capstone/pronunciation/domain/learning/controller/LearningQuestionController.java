package com.capstone.pronunciation.domain.learning.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.curriculum.dto.LessonDetailResponse;
import com.capstone.pronunciation.domain.curriculum.service.CurriculumService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/learning/questions")
@RestController
@Tag(name = "Frontend - Learning Questions", description = "프론트 실사용 API: 학습 화면에서 단건 문제 정보를 다시 조회할 때 사용합니다.")
public class LearningQuestionController {

	private final CurriculumService curriculumService;

	public LearningQuestionController(CurriculumService curriculumService) {
		this.curriculumService = curriculumService;
	}

	@GetMapping("/{questionId}")
	@Operation(
			summary = "[프론트 사용] 학습 문제 단건 조회",
			description = "학습 화면에서 특정 문제를 다시 로드할 때 사용하는 단건 조회 API입니다. 커리큘럼 레슨 상세와 동일한 데이터를 반환합니다."
	)
	public LessonDetailResponse questionDetail(Authentication authentication, @PathVariable Long questionId) {
		return curriculumService.questionDetail(authentication.getName(), questionId);
	}
}
