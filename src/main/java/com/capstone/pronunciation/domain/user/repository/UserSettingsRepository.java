package com.capstone.pronunciation.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pronunciation.domain.user.entity.UserSettings;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
	Optional<UserSettings> findByUser_Id(Long userId);
}
