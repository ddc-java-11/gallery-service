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
import edu.cnm.deepdive.gallery.model.dao.ImageRepository;
import edu.cnm.deepdive.gallery.model.entity.Image;
import edu.cnm.deepdive.gallery.model.entity.User;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageService {

  private final ImageRepository imageRepository;
  private final Random rng;
  private final Path uploadDirectory;
  private final Set<String> contentTypes;
  private final DateFormat formatter;
  private final String unknownFilename;
  private final String filenameFormat;
  private final int randomizerLimit;

  @Autowired
  public ImageService(ImageRepository imageRepository, UploadConfiguration uploadConfiguration,
      ApplicationHome applicationHome, Random rng) {
    this.imageRepository = imageRepository;
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

  public Optional<Image> get(UUID id) {
    return imageRepository.findById(id);
  }

  public Optional<Image> get(UUID id, User contributor) {
    return imageRepository.findFirstByIdAndContributor(id, contributor);
  }

  public void delete(Image image) {
    imageRepository.delete(image);
  }

  public Streamable<Image> search(User contributor, String fragment) {
    Streamable<Image> images;
    if (contributor != null) {
      if (fragment != null) {
        images = Streamable.of(
            imageRepository
                .findAllByContributorAndDescriptionContainsOrderByNameAsc(contributor, fragment)
                .and(imageRepository
                    .findAllByContributorAndNameContainsOrderByNameAsc(contributor, fragment))
                .toSet()
        );
      } else {
        images = imageRepository.findAllByContributorOrderByNameAsc(contributor);
      }
    } else if (fragment != null) {
      images = Streamable.of(
          imageRepository
              .findAllByNameContainsOrderByNameAsc(fragment)
              .and(imageRepository.findAllByDescriptionContainsOrderByNameAsc(fragment))
              .toSet()
      );
    } else {
      images = imageRepository.getAllByOrderByNameAsc();
    }
    return images;
  }

  public Image save(Image image) {
    return imageRepository.save(image);
  }

  public Image save(MultipartFile file, User contributor) {
    if (!contentTypes.contains(file.getContentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Unsupported MIME type in uploaded content.");
    }
    try {
      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null) {
        originalFilename = unknownFilename;
      }
      String newFilename = String.format(filenameFormat, formatter.format(new Date()),
          rng.nextInt(randomizerLimit), getExtension(originalFilename));
      Files.copy(file.getInputStream(), uploadDirectory.resolve(newFilename));
      Image image = new Image();
      image.setName(new File(originalFilename).getName());
      image.setPath(newFilename);
      image.setContributor(contributor);
      image.setContentType(file.getContentType());
      return imageRepository.save(image);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<Resource> getContent(Image image) {
    try {
      Path file = uploadDirectory.resolve(image.getPath());
      return Optional.of(new UrlResource(file.toUri()));
    } catch (MalformedURLException e) {
      return Optional.empty();
    }
  }

  @NonNull
  private String getExtension(@NonNull String filename) {
    int position;
    return ((position = filename.lastIndexOf('.')) >= 0) ? filename.substring(position + 1) : "";
  }

}
