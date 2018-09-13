package com.home911.myspringboot.reservations.configuration;

import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationWS;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

public class ReservationWSMapper extends ConfigurableMapper {

	@Override
	protected void configure(MapperFactory factory) {
		// WS to Bean
		factory.classMap(ReservationWS.class, ReservationBean.class)
				.constructorA()
				.constructorB()
				.byDefault()
				.register();
	}
}
