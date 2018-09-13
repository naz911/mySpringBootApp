package com.home911.myspringboot.reservations.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationWS;
import ma.glasnost.orika.BoundMapperFacade;

@Configuration
public class RestReservationsConfiguration {
	@Bean
	public ReservationWSMapper reservationWSMapper() {
		return new ReservationWSMapper();
	}

	@Bean
	public BoundMapperFacade<ReservationWS, ReservationBean> wsToBeanMapper() {
		return reservationWSMapper().dedicatedMapperFor(ReservationWS.class, ReservationBean.class);
	}
}
