package com.home911.myspringboot.reservations.vo;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationLockBean {
	private Long id;
	private LocalDate reservationDate;
}
