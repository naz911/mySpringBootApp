package com.home911.myspringboot.reservations.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home911.myspringboot.reservations.model.ReservationLock;

public interface ReservationLockRepository extends JpaRepository<ReservationLock, Long> {
	List<ReservationLock> findByReservationDateInOrderByReservationDateAsc(LocalDate reservationDate);

	@Query("select reservationLock from ReservationLock reservationLock where reservationDate >= ?1 and reservationDate <= ?2 order by reservationDate asc")
	List<ReservationLock> findByDatesOrderByReservationDate(LocalDate from, LocalDate to);
}
