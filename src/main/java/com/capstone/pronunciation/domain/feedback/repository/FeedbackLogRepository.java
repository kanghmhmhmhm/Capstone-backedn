package com.capstone.pronunciation.domain.feedback.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capstone.pronunciation.domain.feedback.entity.FeedbackLog;

public interface FeedbackLogRepository extends JpaRepository<FeedbackLog, Long> {
	@Query("""
			select f
			from FeedbackLog f
			join fetch f.result r
			join fetch r.session s
			join fetch r.question q
			where s.user.id = :userId
			order by f.id desc
			""")
	List<FeedbackLog> findByUserId(@Param("userId") Long userId);

	@Query("""
			select f
			from FeedbackLog f
			join fetch f.result r
			join fetch r.session s
			join fetch r.question q
			where s.user.id = :userId
			  and s.id = :sessionId
			order by f.id asc
			""")
	List<FeedbackLog> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

	@Modifying
	@Query("""
			delete from FeedbackLog f
			where f.result.id in (
				select r.id from SessionResult r
				join r.session s
				where s.user.id = :userId
			)
			""")
	void deleteByUserId(@Param("userId") Long userId);
}
