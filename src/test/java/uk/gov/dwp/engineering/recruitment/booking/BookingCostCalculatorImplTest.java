package uk.gov.dwp.engineering.recruitment.booking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

class BookingCostCalculatorImplTest {

  private static final BigDecimal ADULT_TICKET_PRICE = BigDecimal.valueOf(19.99);
  private static final BigDecimal CHILD_TICKET_PRICE = BigDecimal.valueOf(14.47);
  private static final BigDecimal INFANT_TICKET_PRICE = BigDecimal.valueOf(9.22);
  private static final Map<TicketType, BigDecimal> ticketPrices =
      Map.of(ADULT, ADULT_TICKET_PRICE, CHILD, CHILD_TICKET_PRICE, INFANT, INFANT_TICKET_PRICE);

  @ParameterizedTest
  @MethodSource("ticketRequests")
  void givenTicketRequests_whenCalcTotalCost_thenReturnExpectedTotalCost(
      BigDecimal expected, Map<TicketType, Integer> ticketRequests) {
    var uut = new BookingCostCalculatorImpl(ticketPrices);

    var actual = uut.calcTotalCost(ticketRequests);

    assertThat(actual, equalTo(expected));
  }

  public static Stream<Arguments> ticketRequests() {
    var totalCostForSomeOfEachType = ADULT_TICKET_PRICE.multiply(BigDecimal.valueOf(3))
        .add(CHILD_TICKET_PRICE.multiply(BigDecimal.valueOf(5)))
        .add(INFANT_TICKET_PRICE.multiply(BigDecimal.valueOf(2)));
    return Stream.of(
        argumentSet("Single ticket", ADULT_TICKET_PRICE, Map.of(ADULT, 1)),
        argumentSet("Some of each type", totalCostForSomeOfEachType,
            Map.of(ADULT, 3, CHILD, 5, INFANT, 2)));
  }
}
