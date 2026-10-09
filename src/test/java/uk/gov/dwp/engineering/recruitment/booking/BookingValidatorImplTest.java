package uk.gov.dwp.engineering.recruitment.booking;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

/**
 * MUST test all valid/invalid request combinations.
 */
public class BookingValidatorImplTest {

  private BookingValidatorImpl uut;

  @BeforeEach
  void setUp() {
    uut = new BookingValidatorImpl(25);
  }

  @ParameterizedTest
  @MethodSource("validRequests")
  void givenValidRequest_whenValidate_thenMapReturned(
      Map<TicketType, Integer> expected, Long accountId, List<TicketRequest> ticketRequestsList) {
    var ticketRequests = ticketRequestsList.toArray(new TicketRequest[0]);

    var actual = uut.validate(accountId, ticketRequests);

    assertThat(actual, equalTo(expected));
  }

  public static Stream<Arguments> validRequests() {
    return Stream.of(
        argumentSet("One ADULT ticket", Map.of(ADULT, 1), 1L,
            List.of(new TicketRequest(ADULT, 1))),
        argumentSet("One of each TicketType", Map.of(ADULT, 1, CHILD, 1, INFANT, 1), 37L,
            List.of(new TicketRequest(ADULT, 1), new TicketRequest(CHILD, 1),
                new TicketRequest(INFANT, 1))),
        argumentSet("Repeated TicketType are combined", Map.of(ADULT, 2), 7L,
            List.of(new TicketRequest(ADULT, 1), new TicketRequest(ADULT, 1))),
        argumentSet("Zero ticketCount is ignored", Map.of(ADULT, 1), 1L,
            List.of(new TicketRequest(ADULT, 1), new TicketRequest(CHILD, 0))),
        argumentSet("Max tickets allowed",
            Map.of(ADULT, 10, CHILD, 10, INFANT, 5), 53L,
            List.of(new TicketRequest(ADULT, 10), new TicketRequest(CHILD, 10),
                new TicketRequest(INFANT, 5))));
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  void givenInvalidRequest_whenValidate_thenInvalidBookingExceptionThrown(
      String expectedExceptionMessage, Long accountId, List<TicketRequest> ticketRequestsList) {

    var ticketRequests =
        (ticketRequestsList == null) ? null : ticketRequestsList.toArray(new TicketRequest[0]);
    var exception = assertThrows(InvalidBookingException.class,
        () -> uut.validate(accountId, ticketRequests));

    assertThat(exception.getMessage(), equalTo(expectedExceptionMessage));
  }

  public static Stream<Arguments> invalidRequests() {
    var listContainingNull = new ArrayList<TicketRequest>();
    listContainingNull.add(null);
    return Stream.of(
        // Invalid arguments
        argumentSet("Account ID is null", "Missing accountId", null, emptyList()),
        argumentSet("Account ID is 0", "Invalid accountId", 0L, emptyList()),
        argumentSet("Account ID is negative", "Invalid accountId", -1L, emptyList()),
        argumentSet("TicketRequests is null", "Missing ticketRequests", 1L, null),
        argumentSet("Empty list of TicketRequests", "No tickets", 1L, emptyList()),
        argumentSet("TicketRequest is null", "Missing ticket request", 1L,
            listContainingNull),
        argumentSet("ticketType is null", "Missing ticket type", 1L,
            List.of(new TicketRequest(null, 0))),
        argumentSet("Negative tickets requested", "Invalid ticket count", 1L,
            List.of(new TicketRequest(ADULT, -1))),
        argumentSet("Too many tickets requested per type", "Too many tickets", 1L,
            List.of(new TicketRequest(ADULT, 26))),

        // Business rule violations
        argumentSet("Zero tickets requested overall", "No tickets", 1L,
            List.of(new TicketRequest(ADULT, 0), new TicketRequest(CHILD, 0))),
        argumentSet("CHILD ticket without ADULT", "At least one ADULT ticket required", 1L,
            List.of(new TicketRequest(CHILD, 1))),
        argumentSet("INFANT ticket without ADULT", "At least one ADULT ticket required", 1L,
            List.of(new TicketRequest(INFANT, 1))),
        argumentSet("More INFANT tickets than ADULTs", "More INFANT than ADULT tickets", 1L,
            List.of(new TicketRequest(ADULT, 4), new TicketRequest(INFANT, 5))),
        argumentSet("Too many tickets when combined", "Too many tickets", 1L,
            List.of(new TicketRequest(ADULT, 10), new TicketRequest(CHILD, 10),
                new TicketRequest(INFANT, 5), new TicketRequest(INFANT, 1))));
  }
}
