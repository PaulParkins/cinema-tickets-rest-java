package uk.gov.dwp.engineering.recruitment.booking;

import static java.util.Objects.requireNonNull;
import static uk.gov.dwp.engineering.recruitment.util.Preconditions.checkArgument;

import java.util.Map;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

public class SeatReservationCalculatorImpl implements SeatReservationCalculator {

  private final Map<TicketType, Long> seatsRequired;

  public SeatReservationCalculatorImpl(final Map<TicketType, Long> seatsRequired) {
    this.seatsRequired = requireNonNull(seatsRequired);
    checkArgument(seatsRequired.size() == TicketType.values().length,
        "Not all ticket types have seatsRequired");
  }

  @Override
  public Long calcSeatsRequired(final Map<TicketType, Integer> ticketRequests) {
    return ticketRequests.entrySet().stream().mapToLong(tr -> {
      var ticketType = tr.getKey();
      var count = tr.getValue();
      return seatsRequired.get(ticketType) * count;
    }).sum();
  }
}
