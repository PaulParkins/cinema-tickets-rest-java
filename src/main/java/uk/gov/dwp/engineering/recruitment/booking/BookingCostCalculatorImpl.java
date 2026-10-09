package uk.gov.dwp.engineering.recruitment.booking;

import static java.util.Objects.requireNonNull;
import static uk.gov.dwp.engineering.recruitment.util.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.Map;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

public class BookingCostCalculatorImpl implements BookingCostCalculator {

  private final Map<TicketType, BigDecimal> ticketPrices;

  public BookingCostCalculatorImpl(final Map<TicketType, BigDecimal> ticketPrices) {
    this.ticketPrices = requireNonNull(ticketPrices);
    checkArgument(ticketPrices.size() == TicketType.values().length,
        "Not all ticket types have ticketPrices");
  }

  @Override
  public BigDecimal calcTotalCost(final Map<TicketType, Integer> ticketRequests) {
    return ticketRequests.entrySet().stream().map(tr -> {
      var ticketType = tr.getKey();
      var count = tr.getValue();
      var costPerTicket = ticketPrices.get(ticketType);
      return costPerTicket.multiply(BigDecimal.valueOf(count));
    }).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
