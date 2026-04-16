package com.capstone.pronunciation.domain.session.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capstone.pronunciation.domain.session.entity.LearningSession;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
	Optional<LearningSession> findByIdAndUser_Id(Long sessionId, Long userId);

	List<LearningSession> findByUser_IdOrderByStartTimeDesc(Long userId);

	Optional<LearningSession> findTopByUser_IdAndSelectedLevelAndEndTimeIsNullOrderByStartTimeDesc(Long userId, Integer selectedLevel);

	List<LearningSession> findByUser_IdAndSelectedLevelAndEndTimeIsNullOrderByStartTimeDesc(Long userId, Integer selectedLevel);

	long countByUser_Id(Long userId);

	long countByUser_IdAndEndTimeIsNotNull(Long userId);

	@Modifying
	@Query("delete from LearningSession s where s.user.id = :userId")
	void deleteByUserId(@Param("userId") Long userId);
}
