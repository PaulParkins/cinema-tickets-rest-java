package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import uk.gov.dwp.engineering.recruitment.booking.BookingConfig;
import uk.gov.dwp.engineering.recruitment.domain.Booking;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;


/**
 * Test the controller and service, using real validation and calculator classes and config.  (Only
 * the external (payment and reservation services are mocked.) <p/>
 * <p>
 * Some key valid/invalid cases only.  See
 * {@link uk.gov.dwp.engineering.recruitment.booking.BookingValidatorImplTest} for the complete set
 * of valid/invalid test combinations.
 */
// TODO: add swagger-request-validator or similar to check the OpenAPI spec lines up too
@WebMvcTest(CinemaTicketsController.class)
@Import({CinemaTicketsServiceImpl.class, BookingConfig.class})
@AutoConfigureRestTestClient
public class CinemaTicketsControllerTest {

  private static final BigDecimal ADULT_TICKET_PRICE = BigDecimal.valueOf(25.99);
  private static final BigDecimal CHILD_TICKET_PRICE = BigDecimal.valueOf(17.50);
  private static final BigDecimal INFANT_TICKET_PRICE = BigDecimal.ZERO;

  @Autowired
  private RestTestClient client;

  @MockitoBean
  private PaymentService paymentService;

  @MockitoBean
  private SeatReservationService seatReservationService;

  @ParameterizedTest
  @MethodSource("validRequests")
  void givenValidRequest_whenMakeBooking_thenTicketsPurchased(
      BigDecimal expectedCost, long expectedSeatsReserved, Booking request) {
    final var expectedAccountId = request.accountId();

    client.post()
        .uri("/cinema/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.accountId").isEqualTo(expectedAccountId)
        .jsonPath("$.seatCount").isEqualTo(expectedSeatsReserved)
        .jsonPath("$.totalCost").isEqualTo(expectedCost)
        .jsonPath("$.detail").doesNotExist();

    verify(paymentService).debitAccount(expectedAccountId, expectedCost);
    verify(seatReservationService).reserveSeats(expectedAccountId, expectedSeatsReserved);
  }

  public static Stream<Arguments> validRequests() {
    return Stream.of(
        argumentSet("Single ADULT ticket", ADULT_TICKET_PRICE, 1L,
            new Booking(15L, new TicketRequest(ADULT, 1))),
        argumentSet("Combination of ADULT+CHILD+INFANT tickets",
            ADULT_TICKET_PRICE.multiply(BigDecimal.valueOf(4)).add(
                CHILD_TICKET_PRICE.multiply(BigDecimal.valueOf(3))
                    .add(INFANT_TICKET_PRICE.multiply(BigDecimal.valueOf(2)))), 7L,
            new Booking(
                57L,
                new TicketRequest(ADULT, 4),
                new TicketRequest(CHILD, 3),
                new TicketRequest(INFANT, 2))),
        argumentSet("Maximum ticket count", ADULT_TICKET_PRICE.multiply(BigDecimal.valueOf(25)),
            25L, new Booking(123L, new TicketRequest(ADULT, 25))));
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  void givenInvalidRequest_whenMakeBooking_thenFailureStatus(
      String expectedMessage, String request) {
    client.post()
        .uri("/cinema/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.detail").isEqualTo(expectedMessage)
        .jsonPath("$.accountId").doesNotExist()
        .jsonPath("$.seatCount").doesNotExist()
        .jsonPath("$.totalCost").doesNotExist();

    verifyNoInteractions(paymentService);
    verifyNoInteractions(seatReservationService);
  }

  public static Stream<Arguments> invalidRequests() {
    return Stream.of(
        // Invalid arguments
        argumentSet("Invalid accountId", "Invalid accountId", """
            {"accountId": 0, "ticketRequests": [{"type": "ADULT", "ticketCount": 1}]}
            """),
        argumentSet("Null accountId", "Missing accountId", """
            {"accountId": null, "ticketRequests": [{"type": "ADULT", "ticketCount": 1}]}
            """),
        argumentSet("Wrong name for accountId", "Missing accountId", """
            {"accountIdentifier": 0, "ticketRequests": [{"type": "ADULT", "ticketCount": 1}]}
            """),
        argumentSet("Wrong name for ticketRequests", "Missing ticketRequests", """
            {"accountId": 15, "ticketsRequested": [{"type": "ADULT", "ticketCount": 1}]}
            """),
        argumentSet("Unknown ticketType", "Failed to read request", """
            {"accountId": 15, "ticketRequests": [{"type": "DISCOUNT", "ticketCount": 1}]}
            """),
        argumentSet("Wrong name for ticketType", "Missing ticket type", """
            {"accountId": 15, "ticketRequests": [{"ticketType": "ADULT", "ticketCount": 1}]}
            """),
        argumentSet("Wrong name for ticketCount", "Failed to read request", """
            {"accountId": 15, "ticketRequests": [{"type": "ADULT", "ticketsRequired": 1}]}
            """),

        // Business rule violations - see also note in class javadoc above
        argumentSet("Negative ticketCount", "Invalid ticket count", """
            {"accountId": 1, "ticketRequests": [{"type": "CHILD", "ticketCount": -1}]}
            """),
        argumentSet("CHILD without ADULT", "At least one ADULT ticket required", """
            {"accountId": 2, "ticketRequests": [{"type": "CHILD", "ticketCount": 1}]}
            """),
        argumentSet("INFANT without ADULT", "At least one ADULT ticket required", """
            {"accountId": 2, "ticketRequests": [{"type": "INFANT", "ticketCount": 1}]}
            """),
        argumentSet("More INFANT tickets than ADULT", "More INFANT than ADULT tickets", """
            {"accountId": 2, "ticketRequests": [{"type": "ADULT", "ticketCount": 1}, {"type": "INFANT", "ticketCount": 2}]}
            """),
        argumentSet("Above ticket count limit", "Too many tickets", """
            {"accountId": 2, "ticketRequests": [{"type": "ADULT", "ticketCount": 26}]}
            """));
  }
}
