package com.home911.myspringboot.reservations.vo;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Data
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.ANY, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ErrorWS implements Serializable {
	private static final long serialVersionUID = 5697642037568426506L;

	private final int code;
	private final String status;
	private final String details;
}
