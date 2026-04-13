package com.capstone.pronunciation.domain.session.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.session.entity.SessionQuestion;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {

	List<SessionQuestion> findBySession_IdOrderByQuestionOrderAsc(Long sessionId);
}
