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
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(UserController.RELATIVE_PATH)
@ExposesResourceFor(User.class)
public class UserController {

  public static final String RELATIVE_PATH = "/users";

  private static final String NAME_PROPERTY_PATTERN =
      ParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/name";
  private static final String IMAGES_PROPERTY_PATTERN =
      ParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/images";
  private static final String CURRENT_USER = "/me";
  private static final String NOT_FOUND_REASON = "User not found";

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<User> get(Authentication auth) {
    return userService.getAll().toList();
  }

  @GetMapping(value = ParameterPatterns.UUID_PATH_PARAMETER_PATTERN,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public User get(@PathVariable UUID id, Authentication auth) {
    return userService.get(id)
        .orElseThrow(this::notFound);
  }

  @GetMapping(value = CURRENT_USER, produces = MediaType.APPLICATION_JSON_VALUE)
  public User me(Authentication auth) {
    return (User) auth.getPrincipal();
  }

  @GetMapping(value = NAME_PROPERTY_PATTERN,
      produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public String getName(@PathVariable UUID id, Authentication auth) {
    return get(id, auth).getDisplayName();
  }

  @PutMapping(value = NAME_PROPERTY_PATTERN,
      consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public String putName(@PathVariable UUID id, @RequestBody String name, Authentication auth) {
    return userService.get(id)
        .map((user) -> {
          user.setDisplayName(name);
          return userService.save(user).getDisplayName();
        })
        .orElseThrow(this::notFound);
  }

  @GetMapping(value = IMAGES_PROPERTY_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> getImages(@PathVariable UUID id, Authentication auth) {
    return get(id, auth).getImages();
  }

  private ResponseStatusException notFound() {
   return new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_REASON);
  }

}
