package com.capstone.pronunciation.domain.curriculum.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.curriculum.entity.UserProgress;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
	Optional<UserProgress> findByUser_IdAndStage_Id(Long userId, Long stageId);
}

