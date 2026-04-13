package com.capstone.pronunciation.domain.curriculum.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.capstone.pronunciation.domain.curriculum.entity.UserProgress;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
	Optional<UserProgress> findByUser_IdAndStage_Id(Long userId, Long stageId);

	@Modifying
	void deleteByUser_Id(Long userId);
}
