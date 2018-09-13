package com.home911.myspringboot.reservations.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.home911.myspringboot.reservations.exception.ReservationConflictException;
import com.home911.myspringboot.reservations.exception.ReservationInputException;
import com.home911.myspringboot.reservations.exception.ReservationNotFoundException;
import com.home911.myspringboot.reservations.vo.ErrorWS;

@Slf4j
@ControllerAdvice(annotations = ReservationsController.class)
public class ReservationsControllerExceptionHandler {
	@ExceptionHandler(ReservationNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorWS handleReservationNotFoundException(ReservationNotFoundException e) {
		return handleError(HttpStatus.NOT_FOUND, e);
	}

	@ExceptionHandler(ReservationInputException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorWS handleReservationInputException(ReservationInputException e) {
		return handleError(HttpStatus.BAD_REQUEST, e);
	}

	@ExceptionHandler(ReservationConflictException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorWS handleReservationConflictException(ReservationConflictException e) {
		return handleError(HttpStatus.CONFLICT, e);
	}

	private ErrorWS handleError(HttpStatus httpStatus, Exception e) {
		log.error("Exception occurred message={}", e.getMessage());
		return new ErrorWS(httpStatus.value(), httpStatus.getReasonPhrase(), e.getMessage());
	}
}
