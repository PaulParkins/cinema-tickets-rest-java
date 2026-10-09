package uk.gov.dwp.engineering.recruitment.booking;

import java.util.Map;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

public interface BookingValidator {

  Map<TicketType, Integer> validate(Long accountId, TicketRequest[] ticketRequests);
}
