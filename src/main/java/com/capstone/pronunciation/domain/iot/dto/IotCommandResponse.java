package com.capstone.pronunciation.domain.iot.dto;

public record IotCommandResponse(
		boolean triggered,
		String actionCode,
		String status,
		String message
) {

	public static IotCommandResponse skipped(String actionCode, String status, String message) {
		return new IotCommandResponse(false, actionCode, status, message);
	}

	public static IotCommandResponse sent(String actionCode, String message) {
		return new IotCommandResponse(true, actionCode, "SENT", message);
	}

	public static IotCommandResponse failed(String actionCode, String message) {
		return new IotCommandResponse(false, actionCode, "FAILED", message);
	}
}
