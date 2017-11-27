package io.github.azanx.shopping_list.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.azanx.shopping_list.exception.MessageableException;
import io.github.azanx.shopping_list.exception.MessageableExceptionResponse;
import io.github.azanx.shopping_list.service.exception.DuplicateUserException;
import io.github.azanx.shopping_list.service.exception.ListNotFoundException;
import io.github.azanx.shopping_list.service.exception.UserNotFoundException;

@RestControllerAdvice("io.github.azanx.shopping_list.rest")
public class RestApiControllerAdvice {

	
	
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<MessageableExceptionResponse> userNotFound(final UserNotFoundException e) {
		return response(e, HttpStatus.NOT_FOUND);
	}
		
	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<MessageableExceptionResponse> duplicateUser(final DuplicateUserException e) {
		return response(e, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ListNotFoundException.class)
	public ResponseEntity<MessageableExceptionResponse> listNotFoud(final ListNotFoundException e) {
		return response(e, HttpStatus.NOT_FOUND);
	}
	
	/**
	 * @param e - Exception from which to exctract parameters, must implement MessageableException interface
	 * @param returnStatus - 
	 * @return ResponseEntity which can be sent back to the client
	 */
	private ResponseEntity<MessageableExceptionResponse> response(MessageableException e, HttpStatus returnStatus) {
		HttpHeaders headers = new HttpHeaders();
		MessageableExceptionResponse response = new MessageableExceptionResponse(e.getParameters(), returnStatus.value());
		return new ResponseEntity<MessageableExceptionResponse>(response, returnStatus);
	}
}
