package com.capstone.pronunciation.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.client.RestClientResponseException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
		log.warn("Bad request: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
		log.warn("Conflict: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("CONFLICT", e.getMessage()));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
		log.warn("Unauthorized: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("UNAUTHORIZED", e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		log.warn("Validation failed: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BAD_REQUEST", "요청 값이 올바르지 않습니다."));
	}

	@ExceptionHandler(AmazonServiceException.class)
	public ResponseEntity<ErrorResponse> handleAwsService(AmazonServiceException e) {
		log.error("AWS service error: status={}, code={}, message={}", e.getStatusCode(), e.getErrorCode(), e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
				new ErrorResponse("AWS_S3_ERROR", "S3 업로드 중 오류가 발생했습니다. 버킷 이름, IAM 권한, AWS 자격 증명을 확인하세요."));
	}

	@ExceptionHandler(SdkClientException.class)
	public ResponseEntity<ErrorResponse> handleAwsClient(SdkClientException e) {
		log.error("AWS client error: {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
				new ErrorResponse("AWS_CLIENT_ERROR", "AWS 연결에 실패했습니다. Access Key, Secret Key, 리전 설정을 확인하세요."));
	}

	@ExceptionHandler(RestClientResponseException.class)
	public ResponseEntity<ErrorResponse> handleRestClientResponse(RestClientResponseException e) {
		log.error("Upstream API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
		String responseBody = e.getResponseBodyAsString();
		String message = "AI 서버 응답 오류: status=%s".formatted(e.getStatusCode());
		if (responseBody != null && !responseBody.isBlank()) {
			message += ", body=" + responseBody;
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(new ErrorResponse("UPSTREAM_API_ERROR", message));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
		log.warn("Static resource not found: {}", e.getResourcePath());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
		log.error("Unhandled server error", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
	}
}
