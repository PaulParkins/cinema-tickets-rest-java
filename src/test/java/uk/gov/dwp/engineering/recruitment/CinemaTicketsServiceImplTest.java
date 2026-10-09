package uk.gov.dwp.engineering.recruitment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.engineering.recruitment.booking.BookingCostCalculator;
import uk.gov.dwp.engineering.recruitment.booking.BookingValidator;
import uk.gov.dwp.engineering.recruitment.booking.SeatReservationCalculator;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@ExtendWith(MockitoExtension.class)
class CinemaTicketsServiceImplTest {

  @Mock
  private BookingValidator bookingValidator;

  @Mock
  private BookingCostCalculator bookingCostCalculator;

  @Mock
  private SeatReservationCalculator seatReservationCalculator;

  @Mock
  private PaymentService paymentService;

  @Mock
  private SeatReservationService seatReservationService;

  @InjectMocks
  private CinemaTicketsServiceImpl uut;

  @Test
  void givenValidRequest_whenPurchaseTickets_thenExpectedServiceCallsAndConfirmationReturned() {
    final var accountId = 82L;
    final var expectedCost = BigDecimal.valueOf(33.54);
    final var expectedReservedSeats = 3L;
    final var expectedConfirmation = new BookingConfirmation(accountId);
    final var ticketRequests = new TicketRequest[]{new TicketRequest(ADULT, 1)};
    final var bookingRequest = Map.of(ADULT, 1);
    when(bookingValidator.validate(eq(accountId), eq(ticketRequests))).thenReturn(bookingRequest);
    when(bookingCostCalculator.calcTotalCost(eq(bookingRequest))).thenReturn(expectedCost);
    when(seatReservationCalculator.calcSeatsRequired(eq(bookingRequest)))
        .thenReturn(expectedReservedSeats);

    var actual = uut.purchaseTickets(accountId, ticketRequests);

    assertThat(actual, equalTo(expectedConfirmation));
    verify(paymentService).debitAccount(accountId, expectedCost);
    verify(seatReservationService).reserveSeats(accountId, expectedReservedSeats);
  }

  /**
   * See {@link uk.gov.dwp.engineering.recruitment.booking.BookingValidatorImplTest} for all the
   * valid/invalid test case combinations.
   */
  @Test
  void givenInvalidRequest_whenPurchaseTickets_thenExceptionThrownAndNoSeatsBooked() {
    final var accountId = 82L;
    final var ticketRequests = new TicketRequest[]{new TicketRequest(ADULT, 1)};
    final var expectedException = new InvalidBookingException("Test exception");
    when(bookingValidator.validate(eq(accountId), eq(ticketRequests))).thenThrow(expectedException);

    var exception = assertThrows(InvalidBookingException.class,
        () -> uut.purchaseTickets(accountId, ticketRequests));

    assertThat(exception, equalTo(expectedException));
    verifyNoInteractions(paymentService);
    verifyNoInteractions(seatReservationService);
  }
}
