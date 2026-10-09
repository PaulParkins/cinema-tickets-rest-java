package uk.gov.dwp.engineering.recruitment;

import static java.util.Objects.requireNonNull;
import static uk.gov.dwp.engineering.recruitment.util.Preconditions.checkState;

import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.booking.BookingCostCalculator;
import uk.gov.dwp.engineering.recruitment.booking.BookingValidator;
import uk.gov.dwp.engineering.recruitment.booking.SeatReservationCalculator;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

  private final BookingValidator bookingValidator;
  private final BookingCostCalculator bookingCostCalculator;
  private final SeatReservationCalculator seatReservationCalculator;
  private final PaymentService paymentService;
  private final SeatReservationService seatReservationService;

  public CinemaTicketsServiceImpl(BookingValidator bookingValidator,
      BookingCostCalculator bookingCostCalculator,
      SeatReservationCalculator seatReservationCalculator, PaymentService paymentService,
      SeatReservationService seatReservationService) {
    this.bookingValidator = requireNonNull(bookingValidator);
    this.bookingCostCalculator = requireNonNull(bookingCostCalculator);
    this.seatReservationCalculator = requireNonNull(seatReservationCalculator);
    this.paymentService = requireNonNull(paymentService);
    this.seatReservationService = requireNonNull(seatReservationService);
  }

  @Override
  public BookingConfirmation purchaseTickets(
      final Long accountId, final TicketRequest... ticketRequests)
      throws InvalidBookingException {

    final var bookingRequest = bookingValidator.validate(accountId, ticketRequests);

    final var totalCost = bookingCostCalculator.calcTotalCost(bookingRequest);
    final var seatCount = seatReservationCalculator.calcSeatsRequired(bookingRequest);
    checkState(totalCost.doubleValue() > 0, "totalCost <= 0");
    checkState(seatCount > 0, "seatCount <= 0");

    paymentService.debitAccount(accountId, totalCost);
    seatReservationService.reserveSeats(accountId, seatCount);

    return new BookingConfirmation(accountId, seatCount, totalCost);
  }
}
