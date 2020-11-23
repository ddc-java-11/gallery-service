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
import edu.cnm.deepdive.gallery.service.ImageService.ImageNotFoundException;
import edu.cnm.deepdive.gallery.service.UserService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("unused")
@RestController
@RequestMapping(ImageController.RELATIVE_PATH)
@ExposesResourceFor(Image.class)
public class ImageController {

  public static final String RELATIVE_PATH = "/images";

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

  public ImageController(UserService userService, ImageService imageService) {
    this.userService = userService;
    this.imageService = imageService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE,
      params = {CONTRIBUTOR_PARAM_NAME, FRAGMENT_PARAM_NAME})
  public Iterable<Image> search(@RequestParam(value = CONTRIBUTOR_PARAM_NAME) UUID contributorId,
      @RequestParam(value = FRAGMENT_PARAM_NAME) String fragment, Authentication auth) {
    return userService.get(contributorId)
        .map((contributor) -> imageService.search(contributor, fragment))
        .orElse(List.of());
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = CONTRIBUTOR_PARAM_NAME)
  public Iterable<Image> search(
      @RequestParam(value = CONTRIBUTOR_PARAM_NAME) UUID contributorId, Authentication auth) {
    return userService.get(contributorId)
        .map(imageService::search)
        .orElse(List.of());
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = FRAGMENT_PARAM_NAME)
  public Iterable<Image> search(
      @RequestParam(value = FRAGMENT_PARAM_NAME) String fragment, Authentication auth) {
    return imageService.search(fragment);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> list(Authentication auth) {
    return imageService.list();
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Image> post(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String description,
      @RequestParam MultipartFile file, Authentication auth) {
    try {
      Image image = imageService.store(file, title, description, (User) auth.getPrincipal());
      return ResponseEntity.created(image.getHref()).body(image);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, NOT_STORED_MESSAGE, e);
    } catch (HttpMediaTypeNotAcceptableException e) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, NOT_WHITELISTED_MESSAGE, e);
    }
  }

  @GetMapping(value = BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
  public Image get(@PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .orElseThrow(ImageNotFoundException::new);
  }

  @DeleteMapping(value = BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, Authentication auth) {
    imageService.get(id, (User) auth.getPrincipal())
        .ifPresentOrElse(
            image -> {
              try {
                imageService.delete(image);
              } catch (IOException e) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, FILE_STORE_FAILURE_MESSAGE, e);
              }
            },
            () -> {
              throw new ImageNotFoundException();
            }
        );
  }

  @GetMapping(value = TITLE_PROPERTY_PATTERN,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String getTitle(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map(Image::getTitle)
        .orElseThrow(ImageNotFoundException::new);
  }

  @PutMapping(value = TITLE_PROPERTY_PATTERN,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String putTitle(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id,
      @RequestBody String title, Authentication auth) {
    return imageService.get(id, (User) auth.getPrincipal())
        .map((image) -> {
          image.setTitle(title);
          return imageService.save(image).getTitle();
        })
        .orElseThrow(ImageNotFoundException::new);
  }

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

  @GetMapping(value = DESCRIPTION_PROPERTY_PATTERN,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String getDescription(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map(Image::getDescription)
        .orElseThrow(ImageNotFoundException::new);
  }

  @PutMapping(value = DESCRIPTION_PROPERTY_PATTERN,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String putDescription(@SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id,
      @RequestBody String description, Authentication auth) {
    return imageService.get(id, (User) auth.getPrincipal())
        .map((image) -> {
          image.setDescription(description);
          return imageService.save(image).getDescription();
        })
        .orElseThrow(ImageNotFoundException::new);
  }

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

  @GetMapping(value = CONTENT_PROPERTY_PATTERN)
  public ResponseEntity<Resource> getContent(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return imageService.get(id)
        .map((image) -> {
          try {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionHeader(image.getName()))
                .header(HttpHeaders.CONTENT_TYPE, image.getContentType())
                .body(imageService.retrieve(image));
          } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, NOT_RETRIEVED_MESSAGE, e);
          }
        })
        .orElseThrow(ImageNotFoundException::new);
  }

  private String dispositionHeader(String filename) {
    return String.format(ATTACHMENT_DISPOSITION_FORMAT, filename);
  }

}
