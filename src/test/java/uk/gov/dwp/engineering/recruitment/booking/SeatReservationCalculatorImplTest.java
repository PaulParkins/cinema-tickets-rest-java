package uk.gov.dwp.engineering.recruitment.booking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

class SeatReservationCalculatorImplTest {

  private static final Map<TicketType, Long> seatsRequired =
      Map.of(ADULT, 3L, CHILD, 2L, INFANT, 1L);

  @ParameterizedTest
  @MethodSource("ticketRequests")
  void givenTicketRequests_whenCalcSeatsRequired_thenReturnExpectedSeatCount(
      Long expected, Map<TicketType, Integer> ticketRequests) {
    var uut = new SeatReservationCalculatorImpl(seatsRequired);

    var actual = uut.calcSeatsRequired(ticketRequests);

    assertThat(actual, equalTo(expected));
  }

  public static Stream<Arguments> ticketRequests() {
    return Stream.of(
        argumentSet("Single ticket", 3L, Map.of(ADULT, 1)),
        argumentSet("Some of each type", 21L, Map.of(ADULT, 3, CHILD, 5, INFANT, 2)));
  }
}
