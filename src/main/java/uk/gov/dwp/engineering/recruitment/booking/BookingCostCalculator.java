package uk.gov.dwp.engineering.recruitment.booking;

import java.math.BigDecimal;
import java.util.Map;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

public interface BookingCostCalculator {

  BigDecimal calcTotalCost(Map<TicketType, Integer> ticketRequests);
}
