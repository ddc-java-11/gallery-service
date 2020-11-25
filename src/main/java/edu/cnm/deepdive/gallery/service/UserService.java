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

import edu.cnm.deepdive.gallery.model.dao.UserRepository;
import edu.cnm.deepdive.gallery.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implements high-level operations on {@link User} instances, including automatic creation on
 * OpenID token verification, inclusion of an instance as the value returned by {@link
 * Authentication#getPrincipal()}, and delegation to methods declared in {@link UserRepository}.
 */
@Service
public class UserService {

  private final UserRepository userRepository;

  /**
   * Initializes this service with an injected {@link UserRepository}.
   *
   * @param userRepository Spring Data repository used for CRUD operations.
   */
  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Retrieves an instance of {@link User} with the specified {@code oauthKey}; if none exists,
   * creates and persists a new instance.
   *
   * @param oauthKey    OpenID unique identifier.
   * @param displayName Name used for display (not identification) purposes.
   * @return Created or retrieved instance of {@link User}.
   */
  public User getOrCreate(@NonNull String oauthKey, @NonNull String displayName) {
    return userRepository.findFirstByOauthKey(oauthKey)
        .orElseGet(() -> {
          User user = new User();
          user.setOauthKey(oauthKey);
          user.setDisplayName(displayName);
          return userRepository.save(user);
        });
  }

  /**
   * Selects and returns a {@link User} with the specified {@code id}, as the content of an {@link
   * Optional Optional&lt;User&gt;}. If no such instance exists, the {@link Optional} is empty.
   *
   * @param id Unique identifier of the {@link User}.
   * @return {@link Optional Optional&lt;User&gt;} containing the selected user.
   */
  public Optional<User> get(@NonNull UUID id) {
    return userRepository.findById(id);
  }

  /**
   * Selects and returns a {@link User} with the specified {@code id}, as the content of an {@link
   * Optional Optional&lt;User&gt;}&mdash;but only if that user is the same as that passed in {@code
   * user}. This can be used to deny access without information leakage.
   *
   * @param id Unique identifier of the {@link User}.
   * @param user Test user.
   * @return {@link Optional Optional&lt;User&gt;} containing the selected user.
   */
  public Optional<User> get(@NonNull UUID id, @NonNull User user) {
    return Optional.ofNullable(user.getId().equals(id) ? user : null);
  }

  /**
   * Selects and returns all instances of {@link User}, ordered by {@code displayName}.
   */
  public Iterable<User> getAll() {
    return userRepository.getAllByOrderByDisplayNameAsc();
  }

  /**
   * Persists (creates or updates) the specified {@link User} to the database, updating the instance
   * accordingly. (The instance is updated in-place, but the reference to it is also returned.)
   *
   * @param user Instance to be persisted.
   * @return Updated instance.
   */
  public User save(@NonNull User user) {
    return userRepository.save(user);
  }

}
