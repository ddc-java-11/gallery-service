package edu.cnm.deepdive.gallery.service;

import java.io.IOException;
import java.net.MalformedURLException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  FilenameTranslation store(MultipartFile file) throws IOException, ForbiddenMimeTypeException;

  Resource retrieve(String filename) throws MalformedURLException;

  class FilenameTranslation {

    private final String originalFilename;
    private final String newFilename;

    public FilenameTranslation(String originalFilename, String newFilename) {
      this.originalFilename = originalFilename;
      this.newFilename = newFilename;
    }

    public String getOriginalFilename() {
      return originalFilename;
    }

    public String getNewFilename() {
      return newFilename;
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
