package com.home911.myspringboot.reservations.service;

import java.time.LocalDate;
import java.util.List;

import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationLockBean;

public interface ReservationsService {
	ReservationBean readReservation(String reservationUuid);
	List<ReservationBean> readReservations(LocalDate from, LocalDate to);
	String createReservation(ReservationBean reservationBean);
	void updateReservation(ReservationBean reservationBean);
	void cancelReservation(String reservationUuid);

	List<ReservationLockBean> readLocks(LocalDate from, LocalDate to);
}
