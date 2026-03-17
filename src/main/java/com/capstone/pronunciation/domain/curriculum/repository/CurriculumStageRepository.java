package com.capstone.pronunciation.domain.curriculum.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;

public interface CurriculumStageRepository extends JpaRepository<CurriculumStage, Long> {
	Optional<CurriculumStage> findByStageName(String stageName);

	List<CurriculumStage> findAllByOrderByOrderAsc();
}

