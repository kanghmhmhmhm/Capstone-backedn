package com.capstone.pronunciation.domain.user.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_settings")
public class UserSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "daily_reminder_enabled", nullable = false)
	private boolean dailyReminderEnabled;

	@Column(name = "sound_effects_enabled", nullable = false)
	private boolean soundEffectsEnabled;

	@Column(name = "mouth_guide_overlay_enabled", nullable = false)
	private boolean mouthGuideOverlayEnabled;

	@Column(name = "auto_play_pronunciation_enabled", nullable = false)
	private boolean autoPlayPronunciationEnabled;

	@Column(name = "preferred_coach_tone", nullable = false, length = 30)
	private String preferredCoachTone;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected UserSettings() {
	}

	public UserSettings(User user) {
		this.user = user;
		this.dailyReminderEnabled = true;
		this.soundEffectsEnabled = true;
		this.mouthGuideOverlayEnabled = true;
		this.autoPlayPronunciationEnabled = false;
		this.preferredCoachTone = "balanced";
		this.updatedAt = Instant.now();
	}

	@PrePersist
	@PreUpdate
	void touchUpdatedAt() {
		if (preferredCoachTone == null || preferredCoachTone.isBlank()) {
			preferredCoachTone = "balanced";
		}
		updatedAt = Instant.now();
	}

	public User getUser() {
		return user;
	}

	public boolean isDailyReminderEnabled() {
		return dailyReminderEnabled;
	}

	public void setDailyReminderEnabled(boolean dailyReminderEnabled) {
		this.dailyReminderEnabled = dailyReminderEnabled;
	}

	public boolean isSoundEffectsEnabled() {
		return soundEffectsEnabled;
	}

	public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
		this.soundEffectsEnabled = soundEffectsEnabled;
	}

	public boolean isMouthGuideOverlayEnabled() {
		return mouthGuideOverlayEnabled;
	}

	public void setMouthGuideOverlayEnabled(boolean mouthGuideOverlayEnabled) {
		this.mouthGuideOverlayEnabled = mouthGuideOverlayEnabled;
	}

	public boolean isAutoPlayPronunciationEnabled() {
		return autoPlayPronunciationEnabled;
	}

	public void setAutoPlayPronunciationEnabled(boolean autoPlayPronunciationEnabled) {
		this.autoPlayPronunciationEnabled = autoPlayPronunciationEnabled;
	}

	public String getPreferredCoachTone() {
		return preferredCoachTone;
	}

	public void setPreferredCoachTone(String preferredCoachTone) {
		this.preferredCoachTone = preferredCoachTone;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
