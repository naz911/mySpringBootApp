package com.home911.myspringboot;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.home911.myspringboot.availabilities.AvailabilitiesModule;
import com.home911.myspringboot.reservations.ReservationsModule;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {ApplicationModule.class,
		AvailabilitiesModule.class,
		ReservationsModule.class})
@Slf4j
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
