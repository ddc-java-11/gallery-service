package edu.cnm.deepdive.gallery.service;

import edu.cnm.deepdive.gallery.model.dao.ImageRepository;
import edu.cnm.deepdive.gallery.model.entity.Image;
import edu.cnm.deepdive.gallery.model.entity.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  private final ImageRepository imageRepository;
  private final ApplicationHome applicationHome;
  private final Random rng;

  @Value("${upload.use-application-home}")
  private boolean useApplicationHome;
  @Value("${upload.path}")
  private String path;
  @Value("${upload.timestamp.format}")
  private String timestampFormat;
  @Value("${upload.timestamp.time-zone}")
  private String timeZone;
  @Value("${upload.filename.format}")
  private String filenameFormat;
  @Value("${upload.filename.randomizer-limit}")
  private int randomizerLimit;
  @Value("${upload.filename.unknown}")
  private String unknownFilename;

  private Path uploadDirectory;
  private DateFormat formatter;

  @Autowired
  public ImageService(
      ImageRepository imageRepository, ApplicationHome applicationHome, Random rng) {
    this.imageRepository = imageRepository;
    this.applicationHome = applicationHome;
    this.rng = rng;
  }

  @PostConstruct
  private void initUploads() {
    if (useApplicationHome) {
      uploadDirectory = applicationHome.getDir().toPath().resolve(path);
    } else {
      uploadDirectory = Path.of(path);
    }
    //noinspection ResultOfMethodCallIgnored
    uploadDirectory.toFile().mkdirs();
    formatter = new SimpleDateFormat(timestampFormat);
    formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
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

  public Iterable<Image> search(User contributor, String fragment) {
    Iterable<Image> images;
    if (contributor != null) {
      if (fragment != null) {
        images = imageRepository
            .findAllByContributorAndDescriptionContainsOrderByNameAsc(contributor, fragment)
            .and(imageRepository
                .findAllByContributorAndNameContainsOrderByNameAsc(contributor, fragment))
            .stream()
            .distinct()
            .collect(Collectors.toList());
      } else {
        images = imageRepository.findAllByContributorOrderByNameAsc(contributor).toList();
      }
    } else if (fragment != null) {
      images = imageRepository.findAllByNameContainsOrderByNameAsc(fragment)
          .and(imageRepository.findAllByDescriptionContainsOrderByNameAsc(fragment))
          .stream()
          .distinct()
          .collect(Collectors.toList());
    } else {
      images = imageRepository.getAllByOrderByNameAsc();
    }
    return images;
  }

  public Image save(Image image) {
    return imageRepository.save(image);
  }

  public Image save(MultipartFile file, User contributor) {
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

  @NonNull
  private String getExtension(@NonNull String filename) {
    int position;
    return ((position = filename.lastIndexOf('.')) >= 0) ? filename.substring(position + 1) : "";
  }

}
