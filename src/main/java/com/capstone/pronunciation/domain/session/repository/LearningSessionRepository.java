package com.capstone.pronunciation.domain.session.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.session.entity.LearningSession;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
	Optional<LearningSession> findByIdAndUser_Id(Long sessionId, Long userId);

	List<LearningSession> findByUser_IdOrderByStartTimeDesc(Long userId);
}
