package edu.cnm.deepdive.gallery.controller;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Convenience class extending {@link ResponseStatusException}, for use when an attempt to read,
 * write, or delete a file fails with an IOException.
 */
public class StorageException extends ResponseStatusException {

  private static final String NOT_FOUND_REASON = "User not found";

  /**
   * Initializes this instance with the {@link IOException} cause, along with a suitable message
   * &amp; response status.
   *
   * @param ex Cause of this exception.
   */
  public StorageException(IOException ex) {
    super(HttpStatus.NOT_FOUND, NOT_FOUND_REASON, ex);
  }

}
