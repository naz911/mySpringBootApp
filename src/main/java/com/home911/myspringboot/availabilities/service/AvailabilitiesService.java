package com.home911.myspringboot.availabilities.service;

import java.time.LocalDate;
import java.util.List;

import com.home911.myspringboot.availabilities.vo.AvailibilityBean;

public interface AvailabilitiesService {
	List<AvailibilityBean> readAvailabilities(LocalDate from, LocalDate to);
}
