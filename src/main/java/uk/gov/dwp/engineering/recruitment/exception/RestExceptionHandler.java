package uk.gov.dwp.engineering.recruitment.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(InvalidBookingException.class)
  protected ProblemDetail handleInvalidBookingException(final InvalidBookingException exception) {
    // Log but do not leak internal details in the problem detail response
    logger.debug("InvalidBookingException caught", exception);
    final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problemDetail.setDetail(exception.getMessage());
    return problemDetail;
  }

  /**
   * Catch-all Exception handler, to avoid any leaks, just in case.
   */
  @ExceptionHandler(Exception.class)
  protected ProblemDetail handleOtherExceptions(final Exception exception) {
    logger.error("Unexpected Exception caught", exception);
    return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
