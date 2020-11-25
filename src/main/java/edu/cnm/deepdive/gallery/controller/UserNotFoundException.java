package edu.cnm.deepdive.gallery.controller;

import edu.cnm.deepdive.gallery.model.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Convenience class extending {@link ResponseStatusException}, for use when a request references a
 * non-existing {@link User} entity instance.
 */
public class UserNotFoundException extends ResponseStatusException {

  private static final String NOT_FOUND_REASON = "User not found";

  /**
   * Initializes this instance with a relevant message &amp; response status.
   */
  public UserNotFoundException() {
    super(HttpStatus.NOT_FOUND, NOT_FOUND_REASON);
  }

}
