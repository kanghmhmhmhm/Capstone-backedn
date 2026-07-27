package com.capstone.pronunciation.domain.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IotCommandRequest(
		@Schema(
				description = "ESP32로 전달할 허용된 IoT 명령",
				example = "LIGHT_ON",
				allowableValues = {
						"LIGHT_ON", "LIGHT_OFF", "LIGHT_RED", "LIGHT_GREEN", "LIGHT_BLUE",
						"FAN_ON", "FAN_OFF", "CURTAIN_OPEN", "CURTAIN_CLOSE"
				}
		)
		String actionCode
) {
}
