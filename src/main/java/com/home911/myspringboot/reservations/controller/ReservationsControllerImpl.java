package com.home911.myspringboot.reservations.controller;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.home911.myspringboot.reservations.configuration.ReservationWSMapper;
import com.home911.myspringboot.reservations.exception.ReservationInputException;
import com.home911.myspringboot.reservations.service.ReservationsService;
import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationLockBean;
import com.home911.myspringboot.reservations.vo.ReservationWS;
import ma.glasnost.orika.BoundMapperFacade;

@Slf4j
@ReservationsController
@RequestMapping(value = "/reservations")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ReservationsControllerImpl {
	private static final int MAX_DURATION = 3;
	private static final int MIN_ELIGIBILITY = 1;

	private ReservationsService reservationsService;
	private ReservationWSMapper reservationWSMapper;
	private BoundMapperFacade<ReservationWS, ReservationBean> wsToBeanMapper;

	/**
	 * For testing purposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/locks", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public List<ReservationLockBean> readReservationLocks(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
	                                            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		log.info("Reading reservation locks with from={} and to={}", from, to);
		if (to == null) {
			to = from.plusMonths(1);
			log.info("to request param was null defaulting to a month, to={}", to);
		}
		return reservationsService.readLocks(from, to);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public List<ReservationWS> readReservations(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
	                                            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		log.info("Reading reservations with from={} and to={}", from, to);
		if (to == null) {
			to = from.plusMonths(1);
			log.info("to request param was null defaulting to a month, to={}", to);
		}
		List<ReservationBean> reservationBeans = reservationsService.readReservations(from, to);
		List<ReservationWS> reservations = new ArrayList<>(reservationBeans.size());
		reservationBeans.stream().forEachOrdered(reservationBean -> reservations.add(reservationWSMapper.map(reservationBean, ReservationWS.class)));
		return reservations;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{reservationUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public ReservationWS readReservation(@PathVariable String reservationUuid) {
		log.info("Reading reservation with reservationUuid={}", reservationUuid);
		ReservationBean reservationBean = reservationsService.readReservation(reservationUuid);
		return reservationWSMapper.map(reservationBean, ReservationWS.class);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createReservation(@RequestBody ReservationWS reservationWS) {
		log.info("Creating reservation with reservationWS={}", reservationWS);
		validateInput(reservationWS);
		ReservationBean reservationBean = wsToBeanMapper.map(reservationWS);
		String reservationUuid = reservationsService.createReservation(reservationBean);
		return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.LOCATION, "/reservations/" + reservationUuid).body(reservationUuid);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{reservationUuid}")
	@ResponseStatus(value = HttpStatus.OK)
	public void cancelReservation(@PathVariable String reservationUuid) {
		log.info("Cancelling reservation with reservationUuid={}", reservationUuid);
		reservationsService.cancelReservation(reservationUuid);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{reservationUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public void updateReservation(@PathVariable String reservationUuid, @RequestBody ReservationWS reservationWS) {
		log.info("Updating reservation with reservationUuid={} and reservationWS={}", reservationUuid, reservationWS);
		checkIfEmpty(reservationWS);
		ReservationBean savedReservation = reservationsService.readReservation(reservationUuid);
		reservationWS.setReservationUuid(reservationUuid);
		if (reservationWS.getFromDate() == null) {
			reservationWS.setFromDate(savedReservation.getFromDate());
		}
		if (reservationWS.getToDate() == null) {
			reservationWS.setToDate(savedReservation.getToDate());
		}
		if (reservationWS.getName() == null) {
			reservationWS.setName(savedReservation.getName());
		}
		if (reservationWS.getEmail() == null) {
			reservationWS.setEmail(savedReservation.getEmail());
		}
		validateInput(reservationWS);
		ReservationBean reservationBean = reservationWSMapper.map(reservationWS, ReservationBean.class);
		reservationsService.updateReservation(reservationBean);
	}

	private void checkIfEmpty(ReservationWS reservationWS) {
		if (StringUtils.isBlank(reservationWS.getName()) && StringUtils.isBlank(reservationWS.getEmail())
				&& reservationWS.getFromDate() == null && reservationWS.getToDate() == null) {
			throw new ReservationInputException("Invalid update, must update at least one field? ('name', 'email', 'fromDate', 'toDate')");
		}
	}

	private void validateInput(ReservationWS reservationWS) {
		checkBasicFields(reservationWS);
		checkReservationDuration(reservationWS);
		checkReservationEligibility(reservationWS);
	}

	private void checkReservationDuration(ReservationWS reservationWS) {
		if (DAYS.between(reservationWS.getFromDate(), reservationWS.getToDate()) + 1 > MAX_DURATION) {
			throw new ReservationInputException("The reservation can only be for a maximum of 3 days! (fromDate=" + reservationWS.getFromDate() + ", toDate=" + reservationWS.getToDate() + ")");
		}
	}

	private void checkReservationEligibility(ReservationWS reservationWS) {
		LocalDate now = LocalDate.now();
		LocalDate monthLater = now.plusMonths(1);
		log.info("Data now={}, fromDate={}, monthLater={}", now, reservationWS.getFromDate(), monthLater);
		long daysBetweenNowAndFromDate = DAYS.between(now, reservationWS.getFromDate());
		long daysBetweenNowAndMonthLater = DAYS.between(now, monthLater);
		log.info("daysBetweenNowAndFromDate={},  daysBetweenNowAndMonthLater={}", daysBetweenNowAndFromDate, daysBetweenNowAndMonthLater);
		if (daysBetweenNowAndFromDate < MIN_ELIGIBILITY
				|| reservationWS.getFromDate().isAfter(monthLater)) {
			throw new ReservationInputException("Reservation fromDate must be done at least 1 day in advance and up to 1 month in advance! (now=" + now + ", fromDate=" + reservationWS.getFromDate() + ")");
		}
	}

	private void checkBasicFields(ReservationWS reservationWS) {
		if (reservationWS.getName() == null) {
			throw new ReservationInputException("Field 'name' must be specified!");
		}
		if (reservationWS.getEmail() == null) {
			throw new ReservationInputException("Field 'email' must be specified!");
		}
		if (reservationWS.getFromDate() == null) {
			throw new ReservationInputException("Field 'fromDate' must be specified!");
		}
		if (reservationWS.getToDate() == null) {
			throw new ReservationInputException("Field 'toDate' must be specified!");
		}
		if (reservationWS.getFromDate().isAfter(reservationWS.getToDate())) {
			throw new ReservationInputException("Reservation dates are not valid, 'fromDate' must be before toDate;");
		}
	}
}
