package com.home911.myspringboot.availabilities.service;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilitiesService {
	List<LocalDate> readAvailabilities(LocalDate from, LocalDate to);
}
