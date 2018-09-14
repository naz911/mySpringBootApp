package com.home911.myspringboot.reservations.exception;

public class ReservationNotFoundException extends RuntimeException {
	public ReservationNotFoundException(String message) {
		super(message);
	}
}
