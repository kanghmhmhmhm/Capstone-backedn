package com.capstone.pronunciation.domain.upload.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public record FastApiUploadRequest(
		Long sessionId, //세션아이디
		Long questionId,//문제아이디
		Long uploadId,//업로드아이디	
		String s3Key,//S3키
		String audioUrl,//오디오URL
		String expectedText,//정답 테스트
		List<JsonNode> frames,//입술모양 분석 json
		Instant submittedAt//제출시간
) {
}
