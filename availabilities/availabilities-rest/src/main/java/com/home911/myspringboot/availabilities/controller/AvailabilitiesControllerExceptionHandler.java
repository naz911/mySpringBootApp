package com.home911.myspringboot.availabilities.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.home911.myspringboot.availabilities.vo.ErrorWS;

@Slf4j
@ControllerAdvice(annotations = AvailabilitiesController.class)
public class AvailabilitiesControllerExceptionHandler {
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorWS handleConflictRegistrationServiceException(Exception e) {
		return handleError(HttpStatus.CONFLICT, e);
	}

	private ErrorWS handleError(HttpStatus httpStatus, Exception e) {
		log.error("Exception occurred status={}.", httpStatus, e);
		return new ErrorWS(httpStatus.value(), httpStatus.getReasonPhrase(), e.getMessage());
	}
}
