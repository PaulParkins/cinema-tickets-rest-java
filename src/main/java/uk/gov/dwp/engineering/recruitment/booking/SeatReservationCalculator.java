package uk.gov.dwp.engineering.recruitment.booking;

import java.util.Map;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

public interface SeatReservationCalculator {

  Long calcSeatsRequired(Map<TicketType, Integer> ticketRequests);
}
