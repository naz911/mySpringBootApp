package com.home911.myspringboot.reservations.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.home911.myspringboot.reservations.configuration.ReservationWSMapper;
import com.home911.myspringboot.reservations.exception.ReservationInputException;
import com.home911.myspringboot.reservations.service.ReservationsService;
import com.home911.myspringboot.reservations.vo.ReservationBean;
import com.home911.myspringboot.reservations.vo.ReservationWS;
import ma.glasnost.orika.BoundMapperFacade;

public class ReservationsControllerImplTest {
	private static final LocalDate NOW = LocalDate.now();

	@Mock
	private ReservationsService reservationsService;
	@Mock
	private ReservationWSMapper reservationWSMapper;
	@Mock
	private BoundMapperFacade<ReservationWS, ReservationBean> wsToBeanMapper;

	@InjectMocks
	private ReservationsControllerImpl controller;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testReadReservations_withOnlyFrom() {
		LocalDate from = NOW;
		LocalDate to = from.plusMonths(1);
		List<ReservationBean> reservationBeans = buildReservationBeans(from);
		ReservationWS reservationWS1 = copyReservationBean(reservationBeans.get(0));
		ReservationWS reservationWS2 = copyReservationBean(reservationBeans.get(1));

		when(reservationsService.readReservations(eq(from), eq(to))).thenReturn(reservationBeans);
		when(reservationWSMapper.map(eq(reservationBeans.get(0)), eq(ReservationWS.class))).thenReturn(reservationWS1);
		when(reservationWSMapper.map(eq(reservationBeans.get(1)), eq(ReservationWS.class))).thenReturn(reservationWS2);

		List<ReservationWS> reservations = controller.readReservations(from, null);

		assertThat(reservations).isNotNull();
		assertThat(reservations).hasSize(2);
		assertThat(reservationBeans.get(0).getName()).isEqualTo(reservations.get(0).getName());
		assertThat(reservationBeans.get(1).getName()).isEqualTo(reservations.get(1).getName());
	}

	@Test
	public void testReadReservations() {
		LocalDate from = NOW;
		LocalDate to = from.plusDays(14);
		List<ReservationBean> reservationBeans = buildReservationBeans(from);
		ReservationWS reservationWS1 = copyReservationBean(reservationBeans.get(0));
		ReservationWS reservationWS2 = copyReservationBean(reservationBeans.get(1));

		when(reservationsService.readReservations(eq(from), eq(to))).thenReturn(reservationBeans);
		when(reservationWSMapper.map(eq(reservationBeans.get(0)), eq(ReservationWS.class))).thenReturn(reservationWS1);
		when(reservationWSMapper.map(eq(reservationBeans.get(1)), eq(ReservationWS.class))).thenReturn(reservationWS2);

		List<ReservationWS> reservations = controller.readReservations(from, to);

		assertThat(reservations).isNotNull();
		assertThat(reservations).hasSize(2);
		assertThat(reservationBeans.get(0).getName()).isEqualTo(reservations.get(0).getName());
		assertThat(reservationBeans.get(1).getName()).isEqualTo(reservations.get(1).getName());
	}

	@Test
	public void testReadReservation() {
		LocalDate from = NOW;
		String uuid = UUID.randomUUID().toString();
		ReservationBean reservationBean = ReservationBean.builder()
				.id(1L)
				.reservationUuid(uuid)
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();
		ReservationWS reservationWS = copyReservationBean(reservationBean);

		when(reservationsService.readReservation(eq(uuid))).thenReturn(reservationBean);
		when(reservationWSMapper.map(eq(reservationBean), eq(ReservationWS.class))).thenReturn(reservationWS);

		ReservationWS reservation = controller.readReservation(uuid);

		assertThat(reservation).isNotNull();
		assertThat(reservationBean.getName()).isEqualTo(reservationBean.getName());
	}

	@Test
	public void testCreateReservation() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();
		ReservationBean reservationBean = ReservationBean.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();

		when(wsToBeanMapper.map(eq(reservationWS))).thenReturn(reservationBean);
		when(reservationsService.createReservation(eq(reservationBean))).thenReturn("a_uuid");

		ResponseEntity<String> response = controller.createReservation(reservationWS);
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotBlank();
		assertThat(response.getBody()).isEqualTo("a_uuid");
	}

	@Test
	public void testCreateReservation_missingName() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_missingEmail() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_missingFromDate() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.toDate(from.plusDays(2))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_missingToDate() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_fromDateAfterToDate() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(3))
				.toDate(from.plusDays(2))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_invalidDuration() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(4))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_invalidEligibility_fromInThePast() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.minusDays(1))
				.toDate(from.plusDays(2))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCreateReservation_invalidEligibility_fromToFarInFuture() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.minusDays(32))
				.toDate(from.plusDays(33))
				.build();

		try {
			controller.createReservation(reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, wsToBeanMapper);
	}

	@Test
	public void testCancelReservation() {
		controller.cancelReservation("a_uuid");
		verify(reservationsService).cancelReservation(eq("a_uuid"));
	}

	@Test
	public void testUpdateReservation() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();
		ReservationBean reservationBean = ReservationBean.builder()
				.id(1L)
				.reservationUuid("a_uuid")
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();

		when(reservationsService.readReservation(eq("a_uuid"))).thenReturn(reservationBean);
		when(reservationWSMapper.map(eq(reservationWS), eq(ReservationBean.class))).thenReturn(reservationBean);

		controller.updateReservation("a_uuid", reservationWS);

		verify(reservationsService).updateReservation(eq(reservationBean));
	}

	@Test
	public void testUpdateReservation_empty() {
		LocalDate from = NOW;
		ReservationWS reservationWS = ReservationWS.builder()
				.build();
		try {
			controller.updateReservation("a_uuid", reservationWS);
			fail();
		} catch (ReservationInputException e) {
			// expected
		}

		verifyZeroInteractions(reservationsService, reservationWSMapper);
	}

	private List<ReservationBean> buildReservationBeans(LocalDate from) {
		List<ReservationBean> reservationBeans = new ArrayList<>(2);
		ReservationBean reservationBean1 = ReservationBean.builder()
				.id(1L)
				.reservationUuid(UUID.randomUUID().toString())
				.name("Logan Couture")
				.email("logan.couture@hnl.com")
				.fromDate(from.plusDays(1))
				.toDate(from.plusDays(2))
				.build();
		reservationBeans.add(reservationBean1);
		ReservationBean reservationBean2 = ReservationBean.builder()
				.id(2L)
				.reservationUuid(UUID.randomUUID().toString())
				.name("Sydney Crosby")
				.email("sydney.crosby@nhl.com")
				.fromDate(from.plusDays(7))
				.toDate(from.plusDays(9))
				.build();
		reservationBeans.add(reservationBean2);
		return reservationBeans;
	}

	private ReservationWS copyReservationBean(ReservationBean reservationBean) {
		return ReservationWS.builder()
				.reservationUuid(reservationBean.getReservationUuid())
				.name(reservationBean.getName())
				.email(reservationBean.getEmail())
				.fromDate(reservationBean.getFromDate())
				.toDate(reservationBean.getToDate())
				.build();
	}
}
