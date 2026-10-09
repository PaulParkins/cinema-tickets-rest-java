package uk.gov.dwp.engineering.recruitment.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class RestExceptionHandlerTest {

  @Test
  void givenInvalidBookingException_whenHandle_thenMessageIsIncludedButNoInternalDetails() {
    final var exception = new InvalidBookingException("Test exception");
    final var uut = new RestExceptionHandler();

    final ProblemDetail actual = uut.handleInvalidBookingException(exception);

    assertThat(actual.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
    assertThat(actual.getDetail(), equalTo(exception.getMessage()));
  }

  @Test
  void givenOtherException_whenHandle_thenInternalServerErrorWithNoInternalDetails() {
    final var exception = new Exception("Test exception");
    final var uut = new RestExceptionHandler();

    final ProblemDetail actual = uut.handleOtherExceptions(exception);

    assertThat(actual.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    assertThat(actual.getDetail(), nullValue());
  }
}
