package com.home911.myspringboot.reservations.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.home911.myspringboot.reservations.dao.ReservationLockRepository;
import com.home911.myspringboot.reservations.model.ReservationLock;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ReservationLockServiceImpl implements ReservationLockService {
	private ReservationLockRepository reservationLockRepository;

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public List<ReservationLock> gatherLock(LocalDate from, LocalDate to) {
		List<ReservationLock> reservationLocks = new ArrayList<>();
		LocalDate reservationDate = from;
		while (reservationDate.isBefore(to) || reservationDate.isEqual(to)) {
			ReservationLock reservationLock = new ReservationLock();
			reservationLock.setReservationDate(reservationDate);
			reservationLocks.add(reservationLock);
			reservationDate = reservationDate.plusDays(1);
		}
		return reservationLockRepository.saveAll(reservationLocks);
	}

	@Override
	public void releaseLock(List<ReservationLock> reservationLocks) {
		reservationLockRepository.deleteAll(reservationLocks);
	}

	@Override
	public List<ReservationLock> readLock(LocalDate from, LocalDate to) {
		return reservationLockRepository.findByDatesOrderByReservationDate(from, to);
	}
}
