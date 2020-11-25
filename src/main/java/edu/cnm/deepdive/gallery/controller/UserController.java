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
import edu.cnm.deepdive.gallery.service.UserService;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles REST requests for operations on individual instances and collections of the {@link User}
 * type.
 */
@RestController
@RequestMapping(UserController.RELATIVE_PATH)
@ExposesResourceFor(User.class)
@Validated
public class UserController {

  static final String RELATIVE_PATH = "/users";

  private static final String NAME_PROPERTY_PATTERN =
      BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/name";
  private static final String IMAGES_PROPERTY_PATTERN =
      BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/images";
  private static final String CURRENT_USER = "/me";

  private final UserService userService;

  /**
   * Initializes this instance with the {@link UserService} instance used to perform the requested
   * operations.
   *
   * @param userService Provides access to high-level query &amp; persistence operations on {@link
   *                    User} instances.
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Returns all users.
   *
   * @param auth Authentication token with {@link User} principal.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<User> get(Authentication auth) {
    return userService.getAll();
  }

  /**
   * Selects and returns a single {@link User}, as specified by {@code id}.
   *
   * @param id   Unique identifier of {@link User} resource.
   * @param auth Authentication token with {@link User} principal.
   * @return Instance of {@link User} identified by {@code id}.
   */
  @GetMapping(value = BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public User get(@PathVariable UUID id, Authentication auth) {
    return userService.get(id)
        .orElseThrow(UserNotFoundException::new);
  }

  /**
   * Returns the current authenticated {@link User} profile.
   *
   * @param auth Authentication token with {@link User} principal.
   * @return Current {@link User}.
   */
  @GetMapping(value = CURRENT_USER, produces = MediaType.APPLICATION_JSON_VALUE)
  public User me(Authentication auth) {
    return (User) auth.getPrincipal();
  }

  /**
   * Selects and returns the display name of the specified {@link User}.
   *
   * @param id   Unique identifier of {@link User} resource.
   * @param auth Authentication token with {@link User} principal.
   * @return Display name.
   */
  @GetMapping(value = NAME_PROPERTY_PATTERN,
      produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public String getName(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return get(id, auth).getDisplayName();
  }

  /**
   * Replaces the display name of the specified {@link User}.
   *
   * @param id   Unique identifier of {@link User} resource.
   * @param name New display name.
   * @param auth Authentication token with {@link User} principal.
   * @return Updated display name.
   */
  @PutMapping(value = NAME_PROPERTY_PATTERN,
      consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public String putName(@SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id,
      @RequestBody @Length(min = 3) String name, Authentication auth) {
    return userService.get(id, (User) auth.getPrincipal())
        .map((user) -> {
          user.setDisplayName(name);
          return userService.save(user).getDisplayName();
        })
        .orElseThrow(UserNotFoundException::new);
  }

  /**
   * Selects and returns all images uploaded by the {@link User} identified by {@code id}.
   *
   * @param id   Unique identifier of uploading {@link User}.
   * @param auth Authentication token with {@link User} principal.
   * @return Selected images.
   */
  @GetMapping(value = IMAGES_PROPERTY_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> getImages(
      @SuppressWarnings("MVCPathVariableInspection") @PathVariable UUID id, Authentication auth) {
    return get(id, auth).getImages();
  }

}
