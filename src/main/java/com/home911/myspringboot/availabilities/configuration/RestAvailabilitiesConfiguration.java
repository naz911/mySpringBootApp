package com.home911.myspringboot.availabilities.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestAvailabilitiesConfiguration {
	@Bean
	public AvailabilityWSMapper availabilityMapper() {
		return new AvailabilityWSMapper();
	}
}
