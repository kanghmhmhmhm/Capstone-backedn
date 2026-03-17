package com.capstone.pronunciation.domain.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.session.entity.AnswerSubmission;

public interface AnswerSubmissionRepository extends JpaRepository<AnswerSubmission, Long> {
}

