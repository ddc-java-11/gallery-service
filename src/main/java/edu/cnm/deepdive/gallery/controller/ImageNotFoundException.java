package edu.cnm.deepdive.gallery.controller;

import edu.cnm.deepdive.gallery.model.entity.Image;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Convenience class extending {@link ResponseStatusException}, for use when a request references a
 * non-existing {@link Image} entity instance.
 */
public class ImageNotFoundException extends ResponseStatusException {

  private static final String NOT_FOUND_REASON = "Image not found";

  /**
   * Initializes this instance with a relevant message &amp; response status.
   */
  public ImageNotFoundException() {
    super(HttpStatus.NOT_FOUND, NOT_FOUND_REASON);
  }

}
