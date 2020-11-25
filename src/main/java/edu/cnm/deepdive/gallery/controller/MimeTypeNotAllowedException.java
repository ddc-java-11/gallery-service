package edu.cnm.deepdive.gallery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Convenience class extending {@link ResponseStatusException}, for use when uploaded content is not
 * on the whitelist.
 */
public class MimeTypeNotAllowedException extends ResponseStatusException {

  private static final String NOT_ALLOWED_REASON = "File type not allowed";

  /**
   * Initializes this instance with a relevant message &amp; response status.
   */
  public MimeTypeNotAllowedException() {
    super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, NOT_ALLOWED_REASON);
  }

}
