package com.home911.myspringboot.reservations.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationBean {
	private long id;
	private LocalDateTime createdOn;
	private LocalDateTime lastModified;
	private String reservationUuid;
	private String email;
	private String name;
	private LocalDate fromDate;
	private LocalDate toDate;
}
