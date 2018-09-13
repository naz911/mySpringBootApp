package com.home911.myspringboot.reservations.service;

import java.time.LocalDate;
import java.util.List;

import com.home911.myspringboot.reservations.model.ReservationLock;

public interface ReservationLockService {
	List<ReservationLock> gatherLock(LocalDate from, LocalDate to);
	void releaseLock(List<ReservationLock> reservationLocks);
	List<ReservationLock> readLock(LocalDate from, LocalDate to);
}
