package com.cloud.exception;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class CustomizedExceptionHandler extends ResponseEntityExceptionHandler
{
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String errorDetail = "";
		for(FieldError fieldError : ex.getBindingResult().getFieldErrors())
		{
			errorDetail = fieldError.getDefaultMessage();
			break;
		}
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Validation Error", errorDetail);
		return new ResponseEntity<Object>(exceptionResponse, status);
	}
	
	@ExceptionHandler(DuplicateUsernameException.class)
	public final ResponseEntity<ExceptionResponse> handleDuplicateUsername(DuplicateUsernameException ex, WebRequest request)
	{
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Username already exists", "");
		return new ResponseEntity<ExceptionResponse>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(UsernameUpdateException.class)
	public final ResponseEntity<ExceptionResponse> handleUsernameUpdate(UsernameUpdateException ex, WebRequest request)
	{
		String message = ex.getMessage() == null || "".equals(ex.getMessage().trim()) ? "You cannot update username" : ex.getMessage();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), message, "");
		return new ResponseEntity<ExceptionResponse>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(UserNotVerifiedExceptionHandler.class)
	public final ResponseEntity<ExceptionResponse> handleNotVerified(UserNotVerifiedExceptionHandler ex, WebRequest request)
	{
		String message = ex.getMessage() == null || "".equals(ex.getMessage().trim()) ? "User's email is not verified" : ex.getMessage();
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), message, "");
		return new ResponseEntity<ExceptionResponse>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}
}
