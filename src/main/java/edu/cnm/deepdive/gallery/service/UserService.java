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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implements high-level operations on {@link User} instances, including automatic creation on
 * OpenID token verification, inclusion of an instance as the value returned by {@link
 * Authentication#getPrincipal()}, and delegation to methods declared in {@link UserRepository}.
 */
@Service
public class UserService implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

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
   * Converts the provided JSON web token (JWT) to a {@link UsernamePasswordAuthenticationToken},
   * containing the {@link User} instance retrieved from {@link #getOrCreate(String, String)}, the
   * original bearer token presented, and the granted role of {@code USER}.
   *
   * @param jwt Decrypted bearer token.
   * @return Instance of {@link UsernamePasswordAuthenticationToken} which will be available in the
   * context of the request.
   */
  @Override
  public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
    Collection<SimpleGrantedAuthority> grants =
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    return new UsernamePasswordAuthenticationToken(
        getOrCreate(jwt.getSubject(), jwt.getClaim("name")), jwt.getTokenValue(), grants);
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

  /**
   * Convenience class extending {@link ResponseStatusException}, for the purpose of including a
   * default HTTP response status &amp; message when the no-parameter constructor is used.
   */
  public static class UserNotFoundException extends ResponseStatusException {

    private static final String USER_NOT_FOUND_REASON = "User not found";

    /**
     * Initializes this instance with a relevant message &amp; response status.
     */
    public UserNotFoundException() {
      super(HttpStatus.NOT_FOUND, USER_NOT_FOUND_REASON);
    }

  }

}
