package com.home911.myspringboot.reservations.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class Reservation {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private LocalDateTime createdOn;
	private LocalDateTime lastModified;
	private String reservationUuid;
	private String email;
	private String name;
	private LocalDate fromDate;
	private LocalDate toDate;
}
