package com.capstone.pronunciation.domain.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capstone.pronunciation.domain.session.entity.AnswerSubmission;

public interface AnswerSubmissionRepository extends JpaRepository<AnswerSubmission, Long> {
	@Modifying
	@Query("""
			delete from AnswerSubmission a
			where a.result.id in (
				select r.id from SessionResult r
				join r.session s
				where s.user.id = :userId
			)
			""")
	void deleteByUserId(@Param("userId") Long userId);
}
