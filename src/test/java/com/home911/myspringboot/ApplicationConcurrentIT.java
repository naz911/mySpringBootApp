package com.home911.myspringboot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home911.myspringboot.reservations.dao.ReservationLockRepository;
import com.home911.myspringboot.reservations.dao.ReservationRepository;
import com.home911.myspringboot.vo.ErrorWS;
import com.home911.myspringboot.reservations.vo.ReservationWS;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationConcurrentIT {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final LocalDate NOW = LocalDate.now();

	@LocalServerPort
	private int port;

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private ReservationLockRepository reservationLockRepository;


	private TestRestTemplate restTemplate = new TestRestTemplate();
	private List<MediaType> acceptableMediaTypes = new ArrayList<>();

	@Before
	public void setup() {
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
	}

	@After
	public void cleanup() {
		reservationRepository.deleteAll();
		reservationLockRepository.deleteAll();
	}

	@Test
	public void testConcurrentCreateReservation() throws Exception {
		List<MediaType> acceptableMediaTypes = new ArrayList<>();
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Connor McDavid")
				.email("connor.mcdavid@hnl.com")
				.fromDate(NOW.plusDays(12))
				.toDate(NOW.plusDays(12))
				.build();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ReservationWS> entity = new HttpEntity<>(reservationWS, headers);
		CreateReservationRunnable runnable1 = new CreateReservationRunnable(createURLWithPort("/reservations"), entity, restTemplate);
		CreateReservationRunnable runnable2 = new CreateReservationRunnable(createURLWithPort("/reservations"), entity, restTemplate);
		Thread thread1 = new Thread(runnable1);
		Thread thread2 = new Thread(runnable2);
		thread1.start();
		thread2.start();

		Thread.sleep(2000);

		List<ReservationWS> reservationWSs = readReservations();
		assertThat(reservationWSs).isNotNull();
		assertThat(reservationWSs).hasSize(1);
		assertThat(reservationWSs.get(0).getName()).isEqualTo("Connor McDavid");
		assertThat(reservationWSs.get(0).getEmail()).isEqualTo("connor.mcdavid@hnl.com");
		assertThat(reservationWSs.get(0).getFromDate()).isEqualTo(NOW.plusDays(12));
		assertThat(reservationWSs.get(0).getToDate()).isEqualTo(NOW.plusDays(12));
	}

	private List<ReservationWS> readReservations() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/reservations?from=" + NOW.toString()),
				HttpMethod.GET, entity, String.class);

		return MAPPER.readValue(response.getBody(), MAPPER.getTypeFactory().
				constructCollectionType(List.class, ReservationWS.class));
	}

	private static class CreateReservationRunnable implements Runnable {
		private TestRestTemplate restTemplate;
		private HttpEntity<ReservationWS> entity;
		private String url;
		CreateReservationRunnable(String url, HttpEntity<ReservationWS> entity, TestRestTemplate restTemplate) {
			this.url = url;
			this.entity = entity;
			this.restTemplate = restTemplate;
		}

		@Override
		public void run() {
			ResponseEntity<String> response = restTemplate.exchange(
					url, HttpMethod.POST, entity, String.class);
			String responsePayload = response.getBody();
			try {
				if (StringUtils.contains(responsePayload, "{")) {
					ErrorWS errorWS = MAPPER.readValue(responsePayload, ErrorWS.class);
					log.error("Conflict error:{}", errorWS);
				} else {
					log.info("Success reservationUuid={}", responsePayload);
				}
			} catch (Exception e) {
				log.error("Unexpected exception occurred.", e);
			}
		}
	}

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
