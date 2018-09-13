package com.home911.myspringboot.availabilities.controller;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.home911.myspringboot.availabilities.service.AvailabilitiesService;

@Slf4j
@AvailabilitiesController
@RequestMapping(value = "/availabilities", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AvailabilitiesControllerImpl {
	private AvailabilitiesService availabilitiesService;

	@RequestMapping(method = RequestMethod.GET, params = "from")
	@ResponseStatus(value = HttpStatus.OK)
	public List<LocalDate> readAvailabilities(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
	                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		log.info("Reading availabilities with from={} and to={}", from, to);
		if (to == null) {
			to = from.plusMonths(1);
			log.info("to request param was null defaulting to a month, to={}", to);
		}
		return availabilitiesService.readAvailabilities(from, to);
		//return availabilityMapper.mapAsList(availabilityBeans, AvailabilityWS.class);
	}
}
