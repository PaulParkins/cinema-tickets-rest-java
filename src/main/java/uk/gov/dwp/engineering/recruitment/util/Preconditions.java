package uk.gov.dwp.engineering.recruitment.util;

// TODO: replace with Guava once approval confirmed
public final class Preconditions {

  private Preconditions() {
  }

  public static void checkState(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  public static void checkArgument(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }
}
