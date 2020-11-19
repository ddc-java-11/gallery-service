package edu.cnm.deepdive.gallery.exception;

import java.util.NoSuchElementException;

public class NotFoundException extends NoSuchElementException {

  public static final String DEFAULT_MESSAGE = "Resource not found";

  public NotFoundException() {
    this(DEFAULT_MESSAGE);
  }

  public NotFoundException(String message) {
    super(message);
  }

}
