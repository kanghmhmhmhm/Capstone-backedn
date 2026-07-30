package com.capstone.pronunciation.domain.iot.model;

import java.util.Locale;

public enum IotActionCode {
	LIGHT_ON,
	LIGHT_OFF,
	LIGHT_RED,
	LIGHT_GREEN,
	LIGHT_BLUE;

	public static IotActionCode from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("actionCode는 필수입니다.");
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("지원하지 않는 IoT 명령입니다: " + value);
		}
	}
}
