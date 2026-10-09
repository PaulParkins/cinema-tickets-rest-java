package uk.gov.dwp.engineering.recruitment.booking;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;
import static uk.gov.dwp.engineering.recruitment.util.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.Map;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

public class BookingValidatorImpl implements BookingValidator {

  private final int maxTicketCount;

  public BookingValidatorImpl(final int maxTicketCount) {
    checkArgument(maxTicketCount > 0, "Invalid maxTicketCount");
    this.maxTicketCount = maxTicketCount;
  }

  @Override
  public Map<TicketType, Integer> validate(
      final Long accountId, final TicketRequest[] ticketRequests) {
    validateArguments(accountId, ticketRequests);

    final var bookingRequest = Arrays.stream(ticketRequests)
        .filter(tr -> tr.ticketCount() > 0)
        .collect(toUnmodifiableMap(TicketRequest::type, TicketRequest::ticketCount, Integer::sum));

    validateTicketCounts(bookingRequest);

    return bookingRequest;
  }

  private void validateArguments(Long accountId, TicketRequest[] ticketRequests) {
    checkBooking(accountId != null, "Missing accountId");
    checkBooking(ticketRequests != null, "Missing ticketRequests");
    checkBooking(accountId > 0, "Invalid accountId");
    for (final var ticketRequest : ticketRequests) {
      checkBooking(ticketRequest != null, "Missing ticket request");
      checkBooking(ticketRequest.type() != null, "Missing ticket type");
      checkBooking(ticketRequest.ticketCount() >= 0, "Invalid ticket count");
      checkBooking(ticketRequest.ticketCount() <= maxTicketCount, "Too many tickets");
    }
  }

  private void validateTicketCounts(Map<TicketType, Integer> bookingRequest) {
    final var totalTicketCount = bookingRequest.values().stream()
        .mapToLong(c -> c)
        .sum();
    checkBooking(totalTicketCount > 0, "No tickets");
    checkBooking(totalTicketCount <= maxTicketCount, "Too many tickets");

    final int adultTicketCount = bookingRequest.getOrDefault(ADULT, 0);
    final int infantTicketCount = bookingRequest.getOrDefault(INFANT, 0);
    checkBooking(adultTicketCount > 0, "At least one ADULT ticket required");
    checkBooking(adultTicketCount >= infantTicketCount, "More INFANT than ADULT tickets");
  }

  private void checkBooking(final boolean predicate, final String errorMessage) {
    if (!predicate) {
      throw new InvalidBookingException(errorMessage);
    }
  }
}
