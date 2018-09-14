package com.home911.myspringboot.reservations.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home911.myspringboot.reservations.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	String RESERVATION_BY_INTERVALS_WHERE_CLAUSE = "(fromDate <= ?1 and toDate >= ?1) or (fromDate >= ?1 and fromDate <= ?2)";

	Reservation findByReservationUuid(String reservationUuid);

	@Query("select reservation from Reservation reservation where " + RESERVATION_BY_INTERVALS_WHERE_CLAUSE + " order by fromDate asc")
	List<Reservation> findByIntervalsOrderByFromDate(LocalDate from, LocalDate to);

	@Query("select count(reservation) from Reservation reservation where " + RESERVATION_BY_INTERVALS_WHERE_CLAUSE)
	long count(LocalDate from, LocalDate to);
}
