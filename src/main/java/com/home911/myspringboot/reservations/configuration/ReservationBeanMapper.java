package com.home911.myspringboot.reservations.configuration;

import com.home911.myspringboot.reservations.model.Reservation;
import com.home911.myspringboot.reservations.vo.ReservationBean;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

public class ReservationBeanMapper extends ConfigurableMapper {

	@Override
	protected void configure(MapperFactory factory) {
		// WS to Bean
		factory.classMap(ReservationBean.class, Reservation.class)
				.constructorA()
				.constructorB()
				.byDefault()
				.register();
	}
}
