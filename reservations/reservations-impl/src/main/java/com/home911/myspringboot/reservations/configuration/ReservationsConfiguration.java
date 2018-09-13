package com.home911.myspringboot.reservations.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.home911.myspringboot.reservations.model.Reservation;
import com.home911.myspringboot.reservations.vo.ReservationBean;
import ma.glasnost.orika.BoundMapperFacade;

@Configuration
public class ReservationsConfiguration {
	@Bean
	public ReservationLockBeanMapper reservationLockBeanMapper() {
		return new ReservationLockBeanMapper();
	}
	@Bean
	public ReservationBeanMapper reservationBeanMapper() {
		return new ReservationBeanMapper();
	}

	@Bean
	public BoundMapperFacade<ReservationBean, Reservation> beanToEntityMapper() {
		return reservationBeanMapper().dedicatedMapperFor(ReservationBean.class, Reservation.class);
	}
}
