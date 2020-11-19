package edu.cnm.deepdive.gallery.exception;

public class InvalidQueryException extends IllegalArgumentException {

  private static final String DEFAULT_MESSAGE = "Invalid query";

  public InvalidQueryException() {
    this(DEFAULT_MESSAGE);
  }

  public InvalidQueryException(String message) {
    super(message);
  }

  public InvalidQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidQueryException(Throwable cause) {
    super(cause);
  }

}
