package edu.cnm.deepdive.gallery.service;

import edu.cnm.deepdive.gallery.configuration.UploadConfiguration;
import edu.cnm.deepdive.gallery.configuration.UploadConfiguration.FilenameProperties;
import edu.cnm.deepdive.gallery.configuration.UploadConfiguration.FilenameProperties.TimestampProperties;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFilesystemStorageService implements StorageService {

  private final Random rng;
  private final Path uploadDirectory;
  private final Set<String> contentTypes;
  private final DateFormat formatter;
  private final String unknownFilename;
  private final String filenameFormat;
  private final int randomizerLimit;

  @Autowired
  public LocalFilesystemStorageService(
      Random rng, UploadConfiguration uploadConfiguration, ApplicationHome applicationHome) {
    this.rng = rng;
    FilenameProperties filenameProperties = uploadConfiguration.getFilename();
    TimestampProperties timestampProperties = filenameProperties.getTimestamp();
    String uploadPath = uploadConfiguration.getPath();
    uploadDirectory = uploadConfiguration.isApplicationHome()
        ? applicationHome.getDir().toPath().resolve(uploadPath)
        : Path.of(uploadPath);
    contentTypes = new HashSet<>(uploadConfiguration.getContentTypes());
    unknownFilename = filenameProperties.getUnknown();
    filenameFormat = filenameProperties.getFormat();
    randomizerLimit = filenameProperties.getRandomizerLimit();
    formatter = new SimpleDateFormat(timestampProperties.getFormat());
    formatter.setTimeZone(TimeZone.getTimeZone(timestampProperties.getTimeZone()));
  }

  @PostConstruct
  private void initUploads() {
    //noinspection ResultOfMethodCallIgnored
    uploadDirectory.toFile().mkdirs();
  }

  @Override
  public FilenameTranslation store(MultipartFile file) throws IOException, ForbiddenMimeTypeException {
    if (!contentTypes.contains(file.getContentType())) {
      throw new ForbiddenMimeTypeException();
    }
    try {
      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null) {
        originalFilename = unknownFilename;
      }
      originalFilename = new File(originalFilename).getName();
      String newFilename = String.format(filenameFormat, formatter.format(new Date()),
          rng.nextInt(randomizerLimit), getExtension(originalFilename));
      Files.copy(file.getInputStream(), uploadDirectory.resolve(newFilename));
      return new FilenameTranslation(originalFilename, newFilename);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Resource retrieve(String filename) throws MalformedURLException {
    Path file = uploadDirectory.resolve(filename);
    return new UrlResource(file.toUri());
  }

  @NonNull
  private String getExtension(@NonNull String filename) {
    int position;
    return ((position = filename.lastIndexOf('.')) >= 0) ? filename.substring(position + 1) : "";
  }

}
