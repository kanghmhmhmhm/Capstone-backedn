package com.capstone.pronunciation.global.exception;

// 409 Conflict
public class ConflictException extends RuntimeException {
	public ConflictException(String message) {
		super(message);
	}
}
