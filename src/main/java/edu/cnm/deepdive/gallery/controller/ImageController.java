package edu.cnm.deepdive.gallery.controller;

import edu.cnm.deepdive.gallery.exception.NotFoundException;
import edu.cnm.deepdive.gallery.model.entity.Image;
import edu.cnm.deepdive.gallery.model.entity.User;
import edu.cnm.deepdive.gallery.service.ImageService;
import edu.cnm.deepdive.gallery.service.UserService;
import java.util.UUID;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ImageController.RELATIVE_PATH)
@ExposesResourceFor(Image.class)
public class ImageController {

  public static final String RELATIVE_PATH = "/images";

  private static final String UUID_PARAMETER_PATTERN = "/{id:[0-9a-fA-F\\-]{32,36}}";
  private static final String DESCRIPTION_PROPERTY_PATTERN = UUID_PARAMETER_PATTERN + "/description";

  private final UserService userService;
  private final ImageService imageService;

  public ImageController(UserService userService, ImageService imageService) {
    this.userService = userService;
    this.imageService = imageService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> search(
      @RequestParam(value = "contributor", required = false) UUID contributorId,
      @RequestParam(value = "q", required = false) String fragment, Authentication auth) {
    return imageService.search(userService.get(contributorId).orElse(null), fragment);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Image> post(@RequestParam MultipartFile file, Authentication auth) {
    Image image = imageService.save(file, (User) auth.getPrincipal());
    return ResponseEntity.created(image.getHref()).body(image);
  }

  @GetMapping(value = UUID_PARAMETER_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
  public Image get(@PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .orElseThrow(NotFoundException::new);
  }

  @DeleteMapping(value = UUID_PARAMETER_PATTERN)
  public void delete(@PathVariable UUID id, Authentication auth) {
    imageService.get(id, (User) auth.getPrincipal())
        .ifPresent(imageService::delete);
  }

  @GetMapping(value = DESCRIPTION_PROPERTY_PATTERN,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String getDescription(@PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map(Image::getDescription)
        .orElseThrow(NotFoundException::new);
  }

  @PutMapping(value = DESCRIPTION_PROPERTY_PATTERN,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String putDescription(
      @PathVariable UUID id, @RequestBody String description, Authentication auth) {
    return imageService.get(id, (User) auth.getPrincipal())
        .map((image) -> {
          image.setDescription(description);
          return imageService.save(image).getDescription();
        })
        .orElseThrow(NotFoundException::new);
  }

}
