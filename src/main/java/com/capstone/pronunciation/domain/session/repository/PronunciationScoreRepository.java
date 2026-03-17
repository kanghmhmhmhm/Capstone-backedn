package com.capstone.pronunciation.domain.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.session.entity.PronunciationScore;

public interface PronunciationScoreRepository extends JpaRepository<PronunciationScore, Long> {
}

