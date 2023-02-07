package com.cloud.exception;

public class UserNotVerifiedExceptionHandler extends RuntimeException {
	public UserNotVerifiedExceptionHandler(String message)
	{
		super(message);
	}
}
