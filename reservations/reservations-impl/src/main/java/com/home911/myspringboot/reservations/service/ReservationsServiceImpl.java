package com.home911.myspringboot.reservations.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.home911.myspringboot.reservations.configuration.ReservationBeanMapper;
import com.home911.myspringboot.reservations.configuration.ReservationLockBeanMapper;
import com.home911.myspringboot.reservations.dao.ReservationRepository;
import com.home911.myspringboot.reservations.exception.ReservationConflictException;
import com.home911.myspringboot.reservations.exception.ReservationNotFoundException;
import com.home911.myspringboot.reservations.model.Reservation;
import com.home911.myspringboot.reservations.model.ReservationLock;
import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationLockBean;
import ma.glasnost.orika.BoundMapperFacade;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ReservationsServiceImpl implements ReservationsService {
	private ReservationRepository reservationRepository;
	private ReservationBeanMapper reservationBeanMapper;
	private ReservationLockBeanMapper reservationLockBeanMapper;
	private BoundMapperFacade<ReservationBean, Reservation> beanToEntityMapper;
	private ReservationLockService reservationLockService;

	@Override
	public List<ReservationLockBean> readLocks(LocalDate from, LocalDate to) {
		List<ReservationLock> reservationLocks = reservationLockService.readLock(from, to);
		List<ReservationLockBean> reservationLockBeans = new ArrayList<>(reservationLocks.size());
		reservationLocks.stream().forEachOrdered(reservationLock -> reservationLockBeans.add(reservationLockBeanMapper.map(reservationLock, ReservationLockBean.class)));
		return reservationLockBeans;
	}

	@Override
	public ReservationBean readReservation(String reservationUuid) {
		log.info("Reading reservation with reservationUuid={}", reservationUuid);
		try {
			Reservation reservation = reservationRepository.findByReservationUuid(reservationUuid);
			if (reservation == null) {
				throw new ReservationNotFoundException("Reservation with reservationUuid=" + reservationUuid + ", is not found.");
			}
			return reservationBeanMapper.map(reservation, ReservationBean.class);
		} catch (EntityNotFoundException e) {
			throw new ReservationNotFoundException("Reservation with reservationUuid=" + reservationUuid + ", is not found.");
		}
	}

	@Override
	public List<ReservationBean> readReservations(LocalDate from, LocalDate to) {
		List<Reservation> reservations = reservationRepository.findByIntervalsOrderByFromDate(from, to);
		List<ReservationBean> reservationBeans = new ArrayList<>(reservations.size());
		reservations.stream().forEachOrdered(reservation -> reservationBeans.add(reservationBeanMapper.map(reservation, ReservationBean.class)));
		return reservationBeans;
	}

	@Override
	@Transactional
	public String createReservation(ReservationBean reservationBean) {
		log.info("Creating reservation with reservationBean={}", reservationBean);
		String reservationUuid = null;
		try {
			List<ReservationLock> reservationLocks = reservationLockService.gatherLock(reservationBean.getFromDate(), reservationBean.getToDate());
			if (!isAvailable(reservationBean.getFromDate(), reservationBean.getToDate())) {
				reservationLockService.releaseLock(reservationLocks);
				throw new ReservationConflictException("The requested reservation dates are already taken. (fromDate=" + reservationBean.getFromDate() + ", toDate=" + reservationBean.getToDate() + ")");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			LocalDateTime now = LocalDateTime.now();
			Reservation reservation = beanToEntityMapper.map(reservationBean);
			reservation.setCreatedOn(now);
			reservation.setLastModified(now);
			reservation.setReservationUuid(UUID.randomUUID().toString());
			reservation = reservationRepository.save(reservation);
			reservationUuid = reservation.getReservationUuid();
			reservationLockService.releaseLock(reservationLocks);

			return reservationUuid;
		} catch (DataIntegrityViolationException e) {
			throw new ReservationConflictException("The requested reservation dates are already taken. (fromDate=" + reservationBean.getFromDate() + ", toDate=" + reservationBean.getToDate() + ")");
		}
	}

	@Override
	@Transactional
	public void updateReservation(ReservationBean reservationBean) {
		log.info("Updating reservation with reservationBean={}", reservationBean);
		try {
			List<ReservationLock> reservationLocks = reservationLockService.gatherLock(reservationBean.getFromDate(), reservationBean.getToDate());
			Reservation reservation = fetchReservation(reservationBean.getReservationUuid());
			if (!isAvailableForUpdate(reservationBean.getFromDate(), reservationBean.getToDate(), reservation.getReservationUuid())) {
				reservationLockService.releaseLock(reservationLocks);
				throw new ReservationConflictException("The requested reservation dates are already taken. (fromDate=" + reservationBean.getFromDate() + ", toDate=" + reservationBean.getToDate() + ")");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			reservation.setLastModified(LocalDateTime.now());
			reservation.setName(reservationBean.getName());
			reservation.setEmail(reservationBean.getEmail());
			reservation.setFromDate(reservationBean.getFromDate());
			reservation.setToDate(reservationBean.getToDate());
			reservationRepository.save(reservation);
			reservationLockService.releaseLock(reservationLocks);
		} catch (DataIntegrityViolationException e) {
			throw new ReservationConflictException("The requested reservation dates are already taken. (fromDate=" + reservationBean.getFromDate() + ", toDate=" + reservationBean.getToDate() + ")");
		}
	}

	/*
	@Override
	public String createReservation(ReservationBean reservationBean) {
		log.info("Creating reservation with reservationBean={}", reservationBean);
		String reservationUuid = null;
		synchronized (LOCK) {
			if (!isAvailable(reservationBean.getFromDate(), reservationBean.getToDate())) {
				throw new ReservationConflictException("The requested reservation dates are already taken. (fromDate=" + reservationBean.getFromDate() + ", toDate=" + reservationBean.getToDate() + ")");
			}
			LocalDateTime now = LocalDateTime.now();
			Reservation reservation = beanToEntityMapper.map(reservationBean);
			reservation.setCreatedOn(now);
			reservation.setLastModified(now);
			reservation.setReservationUuid(UUID.randomUUID().toString());
			reservation = reservationRepository.save(reservation);
			reservationUuid = reservation.getReservationUuid();
		}

		return reservationUuid;
	}

	@Override
	@Transactional
	public void updateReservation(ReservationBean reservationBean) {
		log.info("Updating reservation with reservationBean={}", reservationBean);
		Reservation reservation = fetchReservation(reservationBean.getReservationUuid());
		if (!isAvailable(reservationBean.getFromDate(), reservationBean.getToDate(), null)) {
			throw new ReservationConflictException("The requested reservation dates are already taken. (fromDate=" + reservationBean.getFromDate() + ", toDate=" + reservationBean.getToDate() + ")");
		}
		reservation.setLastModified(LocalDateTime.now());
		reservation.setName(reservationBean.getName());
		reservation.setEmail(reservationBean.getEmail());
		reservation.setFromDate(reservationBean.getFromDate());
		reservation.setToDate(reservationBean.getToDate());
		reservationRepository.save(reservation);
	}
	 */

	@Override
	public void cancelReservation(String reservationUuid) {
		log.info("Cancelling reservation with reservationUuid={}", reservationUuid);
		Reservation reservation = fetchReservation(reservationUuid);
		reservationRepository.delete(reservation);
	}

	private boolean isAvailable(LocalDate from, LocalDate to) {
		return reservationRepository.count(from, to) == 0;
	}

	private boolean isAvailableForUpdate(LocalDate from, LocalDate to, String reservationUuid) {
		List<Reservation> reservations = reservationRepository.findByIntervalsOrderByFromDate(from, to);
		for (Reservation reservation : reservations) {
			if (!StringUtils.equals(reservationUuid, reservation.getReservationUuid())) {
				return false;
			}
		}
		return true;
	}

	private Reservation fetchReservation(String reservationUuid) {
		try {
			Reservation reservation = reservationRepository.findByReservationUuid(reservationUuid);
			if (reservation == null) {
				throw new ReservationNotFoundException("Reservation with reservationUuid=" + reservationUuid + ", is not found.");
			}
			return reservation;
		} catch (EntityNotFoundException e) {
			throw new ReservationNotFoundException("Reservation with reservationUuid=" + reservationUuid + ", is not found.");
		}
	}
}
