package com.capstone.pronunciation.domain.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.feedback.entity.FeedbackLog;

public interface FeedbackLogRepository extends JpaRepository<FeedbackLog, Long> {
}

