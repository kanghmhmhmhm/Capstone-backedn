package com.capstone.pronunciation.domain.quiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
	long countByStage_Id(Long stageId);

	List<QuizQuestion> findByStage_IdOrderByIdAsc(Long stageId);

	List<QuizQuestion> findByStage_IdAndDifficultyBetweenOrderByIdAsc(Long stageId, int minDifficulty, int maxDifficulty);

	Optional<QuizQuestion> findTopByStage_StageNameOrderByDifficultyDesc(String stageName);

	List<QuizQuestion> findByStage_StageNameStartingWithIgnoreCaseAndDifficultyBetweenOrderByIdAsc(
			String stagePrefix,
			int minDifficulty,
			int maxDifficulty);

	List<QuizQuestion> findByStage_StageNameStartingWithIgnoreCaseOrderByIdAsc(String stagePrefix);

	Optional<QuizQuestion> findTopByStage_StageNameStartingWithIgnoreCaseOrderByDifficultyDesc(String stagePrefix);
}
