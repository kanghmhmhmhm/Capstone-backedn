package com.capstone.pronunciation.domain.user.dto;

import java.time.Instant;

public record UserSettingsResponse(
		Boolean dailyReminderEnabled,
		Boolean soundEffectsEnabled,
		Boolean mouthGuideOverlayEnabled,
		Boolean autoPlayPronunciationEnabled,
		String preferredCoachTone,
		Instant updatedAt
) {
}
