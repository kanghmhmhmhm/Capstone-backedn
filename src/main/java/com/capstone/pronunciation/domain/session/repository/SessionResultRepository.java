package com.capstone.pronunciation.domain.session.repository;

import java.util.List;
import java.util.Optional;

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
			join fetch r.question q
			join fetch q.stage st
			left join fetch r.pronunciationScore ps
			left join fetch r.submission sub
			where r.session.id = :sessionId
			order by r.createdAt asc, r.id asc
			""")
	List<SessionResult> findDetailedBySessionId(@Param("sessionId") Long sessionId);

	long countBySession_Id(Long sessionId);

	@Query("""
			select avg(r.score)
			from SessionResult r
			where r.session.id = :sessionId
			""")
	Optional<Double> findAverageScoreBySessionId(@Param("sessionId") Long sessionId);

	Optional<SessionResult> findTopBySession_IdOrderByCreatedAtDescIdDesc(Long sessionId);

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

	@Query("""
			select r
			from SessionResult r
			join fetch r.session s
			join fetch r.question q
			join fetch q.stage st
			where s.user.id = :userId
			  and st.stageName = :stageName
			order by r.createdAt desc, r.id desc
			""")
	List<SessionResult> findRecentByUserIdAndStageName(
			@Param("userId") Long userId,
			@Param("stageName") String stageName,
			Pageable pageable);

	@Query("""
			select r
			from SessionResult r
			join fetch r.session s
			join fetch r.question q
			join fetch q.stage st
			where s.user.id = :userId
			  and lower(st.stageName) like lower(concat(:stagePrefix, '%'))
			order by r.createdAt desc, r.id desc
			""")
	List<SessionResult> findRecentByUserIdAndStagePrefix(
			@Param("userId") Long userId,
			@Param("stagePrefix") String stagePrefix,
			Pageable pageable);

	default List<SessionResult> findRecentByUserId(Long userId, int limit) {
		return findRecentByUserId(userId, Pageable.ofSize(limit));
	}

	default List<SessionResult> findRecentByUserIdAndStageName(Long userId, String stageName, int limit) {
		return findRecentByUserIdAndStageName(userId, stageName, Pageable.ofSize(limit));
	}

	default List<SessionResult> findRecentByUserIdAndStagePrefix(Long userId, String stagePrefix, int limit) {
		return findRecentByUserIdAndStagePrefix(userId, stagePrefix, Pageable.ofSize(limit));
	}
}
