package com.home911.myspringboot.availabilities.configuration;

import com.home911.myspringboot.availabilities.vo.AvailabilityWS;
import com.home911.myspringboot.availabilities.vo.AvailibilityBean;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

public class AvailabilityWSMapper extends ConfigurableMapper {

	@Override
	protected void configure(MapperFactory factory) {
		// WS to Bean
		factory.classMap(AvailabilityWS.class, AvailibilityBean.class)
				.constructorA()
				.constructorB()
				.byDefault()
				.register();
	}
}
