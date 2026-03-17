package com.capstone.pronunciation.global.exception;

// 401 Unauthorized
public class UnauthorizedException extends RuntimeException {
	public UnauthorizedException(String message) {
		super(message);
	}
}
