package com.capstone.pronunciation.domain.user.dto;

public record UserSettingsRequest(
		Boolean dailyReminderEnabled,
		Boolean soundEffectsEnabled,
		Boolean mouthGuideOverlayEnabled,
		Boolean autoPlayPronunciationEnabled,
		String preferredCoachTone
) {
}
