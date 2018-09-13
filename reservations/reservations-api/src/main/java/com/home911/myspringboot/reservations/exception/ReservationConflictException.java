package com.home911.myspringboot.reservations.exception;

public class ReservationConflictException extends RuntimeException {
	public ReservationConflictException(String message) {
		super(message);
	}
}
