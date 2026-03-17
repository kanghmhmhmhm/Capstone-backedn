package com.capstone.pronunciation.domain.quiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
	long countByStage_Id(Long stageId);

	List<QuizQuestion> findByStage_IdOrderByIdAsc(Long stageId);
}

