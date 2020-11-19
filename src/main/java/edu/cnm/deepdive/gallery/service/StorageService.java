package edu.cnm.deepdive.gallery.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  StorageReference store(MultipartFile file) throws IOException, ForbiddenMimeTypeException;

  Resource retrieve(String reference) throws InvalidPathException, MalformedURLException;

  boolean delete(String reference)
      throws InvalidPathException, UnsupportedOperationException, SecurityException;

  class StorageReference {

    private final String filename;
    private final String reference;

    public StorageReference(String filename, String reference) {
      this.filename = filename;
      this.reference = reference;
    }

    public String getFilename() {
      return filename;
    }

    public String getReference() {
      return reference;
    }

  }

  class ForbiddenMimeTypeException extends RuntimeException {

    public ForbiddenMimeTypeException() {
    }

    public ForbiddenMimeTypeException(String message) {
      super(message);
    }

    public ForbiddenMimeTypeException(String message, Throwable cause) {
      super(message, cause);
    }

    public ForbiddenMimeTypeException(Throwable cause) {
      super(cause);
    }

  }

}
