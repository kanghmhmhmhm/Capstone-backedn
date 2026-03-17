package com.capstone.pronunciation.domain.session.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.capstone.pronunciation.domain.session.entity.SessionResult;

public interface SessionResultRepository extends JpaRepository<SessionResult, Long> {

	@Query("""
			select count(distinct r.question.id)
			from SessionResult r
			join r.session s
			where s.user.id = :userId
			  and r.question.stage.id = :stageId
			""")
	long countDistinctQuestionsByUserAndStage(@Param("userId") Long userId, @Param("stageId") Long stageId);

	@Query("""
			select (count(r) > 0)
			from SessionResult r
			join r.session s
			where s.user.id = :userId
			  and r.question.id = :questionId
			""")
	boolean existsByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

	@Query("""
			select r.question.id
			from SessionResult r
			where r.session.id = :sessionId
			""")
	List<Long> findQuestionIdsBySession(@Param("sessionId") Long sessionId);

	@Query("""
			select r
			from SessionResult r
			join fetch r.session s
			join fetch r.question q
			join fetch q.stage st
			left join fetch r.pronunciationScore ps
			left join fetch r.submission sub
			where s.user.id = :userId
			order by r.createdAt desc, r.id desc
			""")
	List<SessionResult> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

	default List<SessionResult> findRecentByUserId(Long userId, int limit) {
		return findRecentByUserId(userId, Pageable.ofSize(limit));
	}
}
