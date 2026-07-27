package com.capstone.pronunciation.domain.iot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.iot.dto.IotCommandRequest;
import com.capstone.pronunciation.domain.iot.dto.IotCommandResponse;
import com.capstone.pronunciation.domain.iot.service.IotCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/iot/commands")
@Tag(name = "IoT Commands", description = "Pronimo AIoT 기기 명령 전송 API")
public class IotCommandController {

	private final IotCommandService iotCommandService;

	public IotCommandController(IotCommandService iotCommandService) {
		this.iotCommandService = iotCommandService;
	}

	@PostMapping
	@Operation(
			summary = "IoT 명령 수동 전송",
			description = "Wokwi 또는 실제 ESP32 연결을 확인하기 위한 인증 사용자용 테스트 API입니다."
	)
	public IotCommandResponse sendCommand(@RequestBody IotCommandRequest request) {
		return iotCommandService.sendManualCommand(request.actionCode());
	}
}
