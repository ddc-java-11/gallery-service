package edu.cnm.deepdive.gallery.configuration;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorConfiguration {

  @ExceptionHandler({NoSuchElementException.class})
  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not found")
  public void notFound() {}

  @ExceptionHandler({IllegalArgumentException.class})
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
  public void badRequest() {}

}
