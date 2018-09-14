package com.home911.myspringboot.reservations.configuration;

import com.home911.myspringboot.reservations.model.ReservationLock;
import com.home911.myspringboot.reservations.vo.ReservationLockBean;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

public class ReservationLockBeanMapper extends ConfigurableMapper {

	@Override
	protected void configure(MapperFactory factory) {
		// WS to Bean
		factory.classMap(ReservationLockBean.class, ReservationLock.class)
				.constructorA()
				.constructorB()
				.byDefault()
				.register();
	}
}
