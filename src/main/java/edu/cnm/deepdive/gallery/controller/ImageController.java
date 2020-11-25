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
package edu.cnm.deepdive.gallery.controller;

import edu.cnm.deepdive.gallery.model.entity.Image;
import edu.cnm.deepdive.gallery.model.entity.User;
import edu.cnm.deepdive.gallery.service.ImageService;
import edu.cnm.deepdive.gallery.service.UserService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles REST requests for operations on individual instances and collections of the {@link Image}
 * type.
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping(ImageController.RELATIVE_PATH)
@ExposesResourceFor(Image.class)
@Validated
public class ImageController {

  static final String RELATIVE_PATH = "/images";

  private static final String TITLE_PROPERTY_PATTERN =
      BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/title";
  private static final String DESCRIPTION_PROPERTY_PATTERN =
      BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/description";
  private static final String CONTENT_PROPERTY_PATTERN =
      BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/content";
  private static final String CONTRIBUTOR_PARAM_NAME = "contributor";
  private static final String FRAGMENT_PARAM_NAME = "q";
  private static final String ATTACHMENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"";
  private static final String NOT_RETRIEVED_MESSAGE = "Unable to retrieve previously uploaded file";
  private static final String NOT_STORED_MESSAGE = "Unable to store uploaded content";
  private static final String NOT_WHITELISTED_MESSAGE = "Upload MIME type not in whitelist";
  private static final String FILE_STORE_FAILURE_MESSAGE = "File store error";

  private final UserService userService;
  private final ImageService imageService;

  /**
   * Initializes this instance with the {@link UserService} and {@link ImageService} instances used
   * to perform the requested operations.
   *
   * @param userService  Provides access to high-level query operations on {@link User} instances.
   * @param imageService Provides access to high-level query &amp; persistence operations on {@link
   *                     Image} instances.
   */
  @Autowired
  public ImageController(UserService userService, ImageService imageService) {
    this.userService = userService;
    this.imageService = imageService;
  }

  /**
   * Returns all images uploaded by the specified {@code contributor}, containing the text {@code
   * fragment} in their titles or descriptions.
   *
   * @param contributorId Unique identifier of uploading {@link User}.
   * @param fragment      Text to search for in image title and description.
   * @param auth          Authentication token with {@link User} principal.
   * @return Selected images.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE,
      params = {CONTRIBUTOR_PARAM_NAME, FRAGMENT_PARAM_NAME})
  public Iterable<Image> search(
      @RequestParam(value = CONTRIBUTOR_PARAM_NAME) UUID contributorId,
      @RequestParam(value = FRAGMENT_PARAM_NAME) @Length(min = 3) String fragment,
      Authentication auth
  ) {
    return userService.get(contributorId)
        .map((contributor) -> imageService.search(contributor, fragment))
        .orElse(List.of());
  }

  /**
   * Selects and returns all images uploaded by the {@link User} identified by {@code
   * contributorId}.
   *
   * @param contributorId Unique identifier of uploading {@link User}.
   * @param auth          Authentication token with {@link User} principal.
   * @return Selected images.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = CONTRIBUTOR_PARAM_NAME)
  public Iterable<Image> search(
      @RequestParam(value = CONTRIBUTOR_PARAM_NAME) UUID contributorId, Authentication auth) {
    return userService.get(contributorId)
        .map(imageService::search)
        .orElse(List.of());
  }

  /**
   * Selects and returns all images containing the text {@code fragment} in their titles or
   * descriptions.
   *
   * @param fragment Text to search for in image title and description.
   * @param auth     Authentication token with {@link User} principal.
   * @return Selected images.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = FRAGMENT_PARAM_NAME)
  public Iterable<Image> search(
      @RequestParam(value = FRAGMENT_PARAM_NAME) @Length(min = 3) String fragment,
      Authentication auth
  ) {
    return imageService.search(fragment);
  }

  /**
   * Selects and returns all images.
   *
   * @param auth Authentication token with {@link User} principal.
   * @return Selected images.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> list(Authentication auth) {
    return imageService.list();
  }

  /**
   * Stores uploaded file content along with a new {@link Image} instance referencing the content.
   *
   * @param title       Summary of uploaded content.
   * @param description Detailed description of uploaded content.
   * @param file        MIME content of single file upload.
   * @param auth        Authentication token with {@link User} principal.
   * @return Instance of {@link Image} created &amp; persisted for the uploaded content.
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Image> post(
      @RequestParam(required = false) @Length(min = 3) String title,
      @RequestParam(required = false) @Length(min = 3) String description,
      @RequestParam MultipartFile file, Authentication auth) {
    try {
      Image image = imageService.store(file, title, description, (User) auth.getPrincipal());
      return ResponseEntity.created(image.getHref()).body(image);
    } catch (IOException e) {
      throw new StorageException(e);
    } catch (HttpMediaTypeNotAcceptableException e) {
      throw new MimeTypeNotAllowedException();
    }
  }

  /**
   * Selects and returns a single {@link Image}, as specified by {@code id}. File content is not
   * returned in the response.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   * @return Instance of {@link Image} identified by {@code id}.
   */
  @GetMapping(value = BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
  public Image get(@PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .orElseThrow(ImageNotFoundException::new);
  }

  /**
   * Deletes the {@link Image} specified by {@code id}. Both file content and metadata are deleted
   * in response to this request.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   */
  @DeleteMapping(value = BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, Authentication auth) {
    imageService.get(id, (User) auth.getPrincipal())
        .ifPresentOrElse(
            image -> {
              try {
                imageService.delete(image);
              } catch (IOException e) {
                throw new StorageException(e);
              }
            },
            () -> {
              throw new ImageNotFoundException();
            }
        );
  }

  /**
   * Returns the summary title of an {@link Image} resource.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   * @return Title of specified {@link Image} resource.
   */
  @GetMapping(value = TITLE_PROPERTY_PATTERN,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String getTitle(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map(Image::getTitle)
        .orElseThrow(ImageNotFoundException::new);
  }

  /**
   * Replaces the summary title of an {@link Image} resource. This method should not be used to set
   * the title to a {@code null} value; instead, a {@code DELETE} request (handled by the {@link
   * #deleteTitle(UUID, Authentication)} should be sent to the same endpoint.
   *
   * @param id    Unique identifier of {@link Image} resource.
   * @param title Text of new title.
   * @param auth  Authentication token with {@link User} principal.
   * @return Updated title of specified {@link Image} resource.
   */
  @PutMapping(value = TITLE_PROPERTY_PATTERN,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String putTitle(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id,
      @RequestBody @Length(min = 3) String title, Authentication auth) {
    return imageService.get(id, (User) auth.getPrincipal())
        .map((image) -> {
          image.setTitle(title);
          return imageService.save(image).getTitle();
        })
        .orElseThrow(ImageNotFoundException::new);
  }

  /**
   * Deletes the summary title of an {@link Image} resource.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   */
  @DeleteMapping(value = TITLE_PROPERTY_PATTERN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTitle(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    imageService.get(id, (User) auth.getPrincipal())
        .ifPresentOrElse(
            (image) -> {
              image.setTitle(null);
              imageService.save(image);
            },
            () -> {
              throw new ImageNotFoundException();
            }
        );
  }

  /**
   * Returns the detailed description of an {@link Image} resource.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   * @return Description of specified {@link Image} resource.
   */
  @GetMapping(value = DESCRIPTION_PROPERTY_PATTERN,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String getDescription(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map(Image::getDescription)
        .orElseThrow(ImageNotFoundException::new);
  }

  /**
   * Replaces the description of an {@link Image} resource. This method should not be used to set
   * the description to a {@code null} value; instead, a {@code DELETE} request (handled by the
   * {@link #deleteTitle(UUID, Authentication)} should be sent to the same endpoint.
   *
   * @param id          Unique identifier of {@link Image} resource.
   * @param description Text of new title.
   * @param auth        Authentication token with {@link User} principal.
   * @return Updated description of specified {@link Image} resource.
   */
  @PutMapping(value = DESCRIPTION_PROPERTY_PATTERN,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String putDescription(@SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id,
      @RequestBody @Length(min = 3) String description, Authentication auth) {
    return imageService.get(id, (User) auth.getPrincipal())
        .map((image) -> {
          image.setDescription(description);
          return imageService.save(image).getDescription();
        })
        .orElseThrow(ImageNotFoundException::new);
  }

  /**
   * Deletes the detailed description of an {@link Image} resource.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   */
  @DeleteMapping(value = DESCRIPTION_PROPERTY_PATTERN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDescription(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    imageService.get(id, (User) auth.getPrincipal())
        .ifPresentOrElse(
            (image) -> {
              image.setDescription(null);
              imageService.save(image);
            },
            () -> {
              throw new ImageNotFoundException();
            }
        );
  }

  /**
   * Returns the file content of the specified {@link Image} resource. The original filename of the
   * image is included in the {@code filename} portion of the {@code content-disposition} response
   * header, while the MIME type is returned in the {@code content-type} header.
   *
   * @param id   Unique identifier of {@link Image} resource.
   * @param auth Authentication token with {@link User} principal.
   * @return Image content.
   */
  @GetMapping(value = CONTENT_PROPERTY_PATTERN)
  public ResponseEntity<Resource> getContent(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map((image) -> {
          try {
            Resource file = imageService.retrieve(image);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionHeader(image.getName()))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.contentLength()))
                .header(HttpHeaders.CONTENT_TYPE, image.getContentType())
                .body(file);
          } catch (IOException e) {
            throw new StorageException(e);
          }
        })
        .orElseThrow(ImageNotFoundException::new);
  }

  private String dispositionHeader(String filename) {
    return String.format(ATTACHMENT_DISPOSITION_FORMAT, filename);
  }

}
