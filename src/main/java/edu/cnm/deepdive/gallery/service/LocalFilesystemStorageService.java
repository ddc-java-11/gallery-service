/*
 *  Copyright 2020 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.gallery.service;

import edu.cnm.deepdive.gallery.configuration.UploadConfiguration;
import edu.cnm.deepdive.gallery.configuration.UploadConfiguration.FilenameProperties;
import edu.cnm.deepdive.gallery.configuration.UploadConfiguration.FilenameProperties.TimestampProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Simple implementation of {@link StorageService}, storing files in a directory specified in the
 * application properties, further organized into subdirectories by specified portions of the
 * generated filenames.
 */
@Service
public class LocalFilesystemStorageService implements StorageService {

  private static final String REFERENCE_PATH_DELIMITER = "/";
  private static final String REFERENCE_PATH_FORMAT = "%s" + REFERENCE_PATH_DELIMITER + "%s";

  private final Random rng;
  private final Path uploadDirectory;
  private final Pattern subdirectoryPattern;
  private final Set<String> whitelist;
  private final DateFormat formatter;
  private final String filenameFormat;
  private final int randomizerLimit;
  private final List<MediaType> contentTypes;

  /**
   * Initializes this instance with a source of randomness, a service configuration object
   * (presumably read from {@code application.properties} or {@code application.yml}), and an
   * instance of {@link ApplicationHome}.
   *
   * @param rng                 Source of randomness.
   * @param uploadConfiguration Configuration object read from application properties.
   * @param applicationHome     Application location context.
   */
  @Autowired
  public LocalFilesystemStorageService(
      Random rng, UploadConfiguration uploadConfiguration, ApplicationHome applicationHome) {
    this.rng = rng;
    FilenameProperties filenameProperties = uploadConfiguration.getFilename();
    TimestampProperties timestampProperties = filenameProperties.getTimestamp();
    String uploadPath = uploadConfiguration.getDirectory();
    uploadDirectory = uploadConfiguration.isApplicationHome()
        ? applicationHome.getDir().toPath().resolve(uploadPath)
        : Path.of(uploadPath);
    //noinspection ResultOfMethodCallIgnored
    uploadDirectory.toFile().mkdirs();
    subdirectoryPattern = uploadConfiguration.getSubdirectoryPattern();
    whitelist = uploadConfiguration.getWhitelist();
    contentTypes = whitelist.stream()
        .map(MediaType::valueOf)
        .collect(Collectors.toList());
    filenameFormat = filenameProperties.getFormat();
    randomizerLimit = filenameProperties.getRandomizerLimit();
    formatter = new SimpleDateFormat(timestampProperties.getFormat());
    formatter.setTimeZone(timestampProperties.getTimeZone());
  }

  @Override
  public String store(MultipartFile file) throws IOException, HttpMediaTypeNotAcceptableException {
    if (!whitelist.contains(file.getContentType())) {
      throw new HttpMediaTypeNotAcceptableException(contentTypes);
    }
    String originalFilename = file.getOriginalFilename();
    String newFilename = String.format(filenameFormat,
        formatter.format(new Date()), rng.nextInt(randomizerLimit),
        getExtension((originalFilename != null) ? originalFilename : ""));
    String subdirectory = getSubdirectory(newFilename);
    Path resolvedPath = uploadDirectory.resolve(subdirectory);
    //noinspection ResultOfMethodCallIgnored
    resolvedPath.toFile().mkdirs();
    Files.copy(file.getInputStream(), resolvedPath.resolve(newFilename));
    return String.format(REFERENCE_PATH_FORMAT, subdirectory, newFilename);
  }

  @Override
  public Resource retrieve(String reference) throws IOException {
    Path file = uploadDirectory.resolve(reference);
    return new UrlResource(file.toUri());
  }

  @Override
  public boolean delete(String reference)
      throws IOException, UnsupportedOperationException, SecurityException {
    try {
      File file = uploadDirectory.resolve(reference).toFile();
      return file.delete();
    } catch (InvalidPathException e) {
      throw new IOException(e);
    }
  }

  @NonNull
  private String getExtension(@NonNull String filename) {
    int position;
    return ((position = filename.lastIndexOf('.')) >= 0) ? filename.substring(position + 1) : "";
  }

  private String getSubdirectory(@NonNull String filename) {
    String path;
    Matcher matcher = subdirectoryPattern.matcher(filename);
    if (matcher.matches()) {
      path = IntStream.rangeClosed(1, matcher.groupCount())
          .mapToObj(matcher::group)
          .collect(Collectors.joining(REFERENCE_PATH_DELIMITER));
    } else {
      path = "";
    }
    return path;
  }
}
