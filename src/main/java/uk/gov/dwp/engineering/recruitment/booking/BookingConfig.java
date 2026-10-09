package uk.gov.dwp.engineering.recruitment.booking;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

/**
 * Provides validator and calc beans using values from config.
 */
@Configuration
@EnableConfigurationProperties(BookingConfig.Properties.class)
public class BookingConfig {

  @ConfigurationProperties(prefix = "ticket-service")
  public record Properties(
      int maxTicketCount,
      Map<TicketType, BigDecimal> ticketPrices,
      Map<TicketType, Long> seatsRequired) {

  }

  @Bean
  public BookingValidator bookingValidator(final Properties properties) {
    return new BookingValidatorImpl(properties.maxTicketCount());
  }

  @Bean
  public BookingCostCalculator bookingCostCalculator(final Properties properties) {
    return new BookingCostCalculatorImpl(properties.ticketPrices());
  }

  @Bean
  public SeatReservationCalculator seatReservationCalculator(final Properties properties) {
    return new SeatReservationCalculatorImpl(properties.seatsRequired());
  }
}
