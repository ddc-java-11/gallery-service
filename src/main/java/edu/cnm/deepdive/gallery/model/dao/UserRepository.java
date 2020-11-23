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
package edu.cnm.deepdive.gallery.model.dao;

import edu.cnm.deepdive.gallery.model.entity.Image;
import edu.cnm.deepdive.gallery.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Declares custom queries (beyond those declared in {@link JpaRepository}) on {@link User} entity
 * instances.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

  /**
   * Returns an {@link Optional Optional&lt;User&gt;} containing a {@link User} with the specified
   * {@code oauthKey}, if one exists; if not, an empty {@link Optional} is returned.
   *
   * @param oauthKey OpenID unique identifier.
   * @return {@link Optional} containing the selected user, if any; if not, an empty {@link
   * Optional} is returned.
   */
  Optional<User> findFirstByOauthKey(String oauthKey);

  /**
   * Returns all {@link User} instances, in ascending order of datetime created (i.e. datetime of
   * first connection to the service).
   */
  Iterable<User> getAllByOrderByCreatedAsc();

  /**
   * Returns all {@link User} instances, in ascending order of display name.
   */
  Iterable<User> getAllByOrderByDisplayNameAsc();

}
