package com.home911.myspringboot.availabilities.vo;

import java.io.Serializable;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Data
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.ANY, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ErrorWS implements Serializable {
	private static final long serialVersionUID = 5693679100978426506L;

	private final int code;
	private final String status;
	private final String details;
}
