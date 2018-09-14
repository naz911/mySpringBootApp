package com.home911.myspringboot;

import static org.assertj.core.api.Assertions.assertThat;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home911.myspringboot.availabilities.vo.AvailabilityWS;
import com.home911.myspringboot.reservations.dao.ReservationLockRepository;
import com.home911.myspringboot.reservations.dao.ReservationRepository;
import com.home911.myspringboot.reservations.service.ReservationsService;
import com.home911.myspringboot.vo.ErrorWS;
import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationWS;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@LocalServerPort
	private int port;

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private ReservationsService reservationsService;
	@Autowired
	private ReservationLockRepository reservationLockRepository;

	private TestRestTemplate restTemplate = new TestRestTemplate();
	private LocalDate now = LocalDate.now();
	private List<MediaType> acceptableMediaTypes = new ArrayList<>();

	@Before
	public void setup() {
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

		reservationsService.createReservation(ReservationBean.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(now.plusDays(1))
				.toDate(now.plusDays(2))
				.build());
		reservationsService.createReservation(ReservationBean.builder()
				.id(2L)
				.reservationUuid(UUID.randomUUID().toString())
				.name("Sydney Crosby")
				.email("sydney.crosby@nhl.com")
				.fromDate(now.plusDays(7))
				.toDate(now.plusDays(9))
				.build());
	}

	@After
	public void cleanup() {
		reservationRepository.deleteAll();
		reservationLockRepository.deleteAll();
	}

	@Test
	public void testReadReservations() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/reservations?from=" + now.toString()),
				HttpMethod.GET, entity, String.class);

		List<ReservationWS> reservationWSs = MAPPER.readValue(response.getBody(), MAPPER.getTypeFactory().
				constructCollectionType(List.class, ReservationWS.class));

		assertThat(reservationWSs).isNotNull();
		assertThat(reservationWSs).hasSize(2);
		assertThat(reservationWSs.get(0).getName()).isEqualTo("Logan Couture");
		assertThat(reservationWSs.get(1).getName()).isEqualTo("Sydney Crosby");
	}

	@Test
	public void testReadAvailabilities() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		LocalDate firstDayOfMonth = now.withDayOfMonth(1);
		LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/availabilities?from=" + firstDayOfMonth.toString()),
				HttpMethod.GET, entity, String.class);

		List<AvailabilityWS> availabilities = MAPPER.readValue(response.getBody(), MAPPER.getTypeFactory().
				constructCollectionType(List.class, AvailabilityWS.class));

		List<ReservationBean> reservationBeans = reservationsService.readReservations(firstDayOfMonth, lastDayOfMonth);
		int nbReservedDays = countReservationDays(reservationBeans, firstDayOfMonth, lastDayOfMonth);
		assertThat(availabilities).isNotNull();
		assertThat(availabilities).hasSize(lastDayOfMonth.getDayOfMonth() - nbReservedDays);
	}

	@Test
	public void testReservation_create_update_delete() throws Exception {
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Connor McDavid")
				.email("connor.mcdavid@hnl.com")
				.fromDate(now.plusDays(12))
				.toDate(now.plusDays(12))
				.build();
		String reservationUuid = testCreateReservation(reservationWS);
		assertThat(reservationUuid).isNotBlank();

		ReservationWS savedReservation = testReadReservation(reservationUuid);
		assertThat(savedReservation).isNotNull();
		assertThat(savedReservation.getReservationUuid()).isEqualTo(reservationUuid);
		assertThat(savedReservation.getName()).isEqualTo(savedReservation.getName());

		LocalDate newFrom = savedReservation.getFromDate().plusDays(1);
		LocalDate newTo = savedReservation.getToDate().plusDays(1);
		testUpdateReservation(reservationUuid, newFrom, newTo);
		savedReservation = testReadReservation(reservationUuid);
		assertThat(savedReservation).isNotNull();
		assertThat(savedReservation.getReservationUuid()).isEqualTo(reservationUuid);
		assertThat(savedReservation.getReservationUuid()).isEqualTo(reservationUuid);
		assertThat(savedReservation.getFromDate()).isEqualTo(newFrom);
		assertThat(savedReservation.getToDate()).isEqualTo(newTo);

		testDeleteReservation(reservationUuid);
		ErrorWS errorWS = testReadReservationWithError(reservationUuid);
		assertThat(errorWS).isNotNull();
		assertThat(errorWS.getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	private String testCreateReservation(ReservationWS reservationWS) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ReservationWS> entity = new HttpEntity<>(reservationWS, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/reservations"),
				HttpMethod.POST, entity, String.class);

		return response.getBody();
	}

	private ReservationWS testReadReservation(String reservationUuid) throws Exception {
		ResponseEntity<String> response = getResponseFromReadReservation(reservationUuid);
		return MAPPER.readValue(response.getBody(), ReservationWS.class);
	}

	private ErrorWS testReadReservationWithError(String reservationUuid) throws Exception {
		ResponseEntity<String> response = getResponseFromReadReservation(reservationUuid);
		return MAPPER.readValue(response.getBody(), ErrorWS.class);
	}

	private ResponseEntity<String> getResponseFromReadReservation(String reservationUuid) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		return restTemplate.exchange(
				createURLWithPort("/reservations/" + reservationUuid),
				HttpMethod.GET, entity, String.class);
	}

	private void testUpdateReservation(String reservationUuid, LocalDate newFrom, LocalDate newTo) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		headers.setContentType(MediaType.APPLICATION_JSON);
		ReservationWS reservationWS = ReservationWS.builder()
				.fromDate(newFrom)
				.toDate(newTo)
				.build();
		HttpEntity<ReservationWS> entity = new HttpEntity<>(reservationWS, headers);

		restTemplate.exchange(
				createURLWithPort("/reservations/" + reservationUuid),
				HttpMethod.PUT, entity, String.class);
	}

	private void testDeleteReservation(String reservationUuid) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		restTemplate.exchange(
				createURLWithPort("/reservations/" + reservationUuid),
				HttpMethod.DELETE, entity, String.class);
	}

	private int countReservationDays(List<ReservationBean> reservationBeans, LocalDate firstDayOfMonth, LocalDate lastDayOfMonth) {
		int nbDays = 1;
		for (ReservationBean reservationBean : reservationBeans) {
			LocalDate current = reservationBean.getFromDate();
			while(current.isBefore(firstDayOfMonth)) {
				current = current.plusDays(1);
			}
			for (int i=0; i<=DAYS.between(current, reservationBean.getToDate()) + 1; i++) {
				if (current.isAfter(lastDayOfMonth)) {
					break;
				}
				nbDays++;
				current = current.plusDays(1);
			}
		}
		return nbDays;
	}

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
