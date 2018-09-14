package com.home911.myspringboot.availabilities.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.home911.myspringboot.availabilities.vo.AvailibilityBean;
import com.home911.myspringboot.reservations.service.ReservationsService;
import com.home911.myspringboot.reservations.vo.ReservationBean;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AvailabilitiesServiceImpl implements AvailabilitiesService {
	private ReservationsService reservationsService;

	@Override
	public List<AvailibilityBean> readAvailabilities(LocalDate from, LocalDate to) {
		List<ReservationBean> reservations = reservationsService.readReservations(from, to);
		List<AvailibilityBean> availabilities = new ArrayList<>();
		LocalDate currentDate = from;
		for (ReservationBean reservation : reservations) {
			checkAvailabilities(availabilities, currentDate, reservation);
			currentDate = reservation.getToDate().plusDays(1);
			if (currentDate.isAfter(to)) {
				break;
			}
		}
		while (currentDate.isBefore(to)) {
			availabilities.add(AvailibilityBean.builder().date(currentDate).build());
			currentDate = currentDate.plusDays(1);
		}
		return availabilities;
	}

	private void checkAvailabilities(List<AvailibilityBean> availabilities, LocalDate currentDate, ReservationBean reservation) {
		LocalDate start = currentDate;
		while (start.isBefore(reservation.getFromDate())) {
			availabilities.add(AvailibilityBean.builder().date(start).build());
			start = start.plusDays(1);
		}
	}
}
