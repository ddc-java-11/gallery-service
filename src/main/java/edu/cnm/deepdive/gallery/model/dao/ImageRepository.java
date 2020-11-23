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
import org.springframework.lang.NonNull;

/**
 * Declares custom queries (beyond those declared in {@link JpaRepository}) on {@link Image} entity
 * instances.
 */
public interface ImageRepository extends JpaRepository<Image, UUID> {

  /**
   * Returns an {@link Optional Optional&lt;Image&gt;} containing an image with the specified {@code
   * id} and contributed by the specified {@link User}, if any exists.
   *
   * @param id          Unique identifier of image.
   * @param contributer {@link User} that uploaded the image.
   * @return {@link Optional} containing the selected image, if any; if not, an empty {@link
   * Optional} is returned.
   */
  Optional<Image> findFirstByIdAndContributor(UUID id, User contributer);

  /**
   * Returns all images in title (ascending) and created datetime (descending) order.
   */
  Iterable<Image> getAllByOrderByTitleAscCreatedDesc();

  /**
   * Selects and returns all images uploaded by {@code contributor} in descending order of datetime
   * created (uploaded).
   *
   * @param contributor {@link User} whose uploaded images are to be selected.
   * @return All images from {@code contributor}.
   */
  Iterable<Image> findAllByContributorOrderByCreatedDesc(User contributor);

  /**
   * Selects and returns all images that have the specified text in their titles or descriptions.
   * Note that this method is intended to be invoked by {@link #findAllByFragment(String)}, which
   * passes the same value for {@code titleFragment} and {@code descriptionFragment}. This method
   * could be specified more directly using a JPQL query, but escaping of the fragment text would be
   * required, to minimize the risk of SQL injection attacks.
   *
   * @param titleFragment       Text fragment to search for (should be the same as {@code
   *                            descriptionFragment}).
   * @param descriptionFragment Text fragment to search for (should be the same as {@code
   *                            titleFragment}).
   * @return All images with the specified fragment(s) in their titles or descriptions.
   */
  Iterable<Image> findAllByTitleContainsOrDescriptionContainsOrderByTitleAscCreatedDesc(
      String titleFragment, String descriptionFragment);

  /**
   * Selects and returns all images that have the specified text in their titles or descriptions.
   *
   * @param fragment Text fragment to search for.
   * @return All images with the specified fragment in their titles or descriptions.
   */
  default Iterable<Image> findAllByFragment(String fragment) {
    return findAllByTitleContainsOrDescriptionContainsOrderByTitleAscCreatedDesc(
        fragment, fragment);
  }

  /**
   * Selects and returns all images from the specified {@code contributor}, with the specified text
   * fragments in their titles or descriptions. Note that this method is intended to be invoked by
   * {@link #findAllByContributorAndFragment(User, String)}, which duplicates the arguments as
   * appropriate in the invocation. This method could be specified more directly using a JPQL query,
   * but escaping of the fragment text would be required, to minimize the risk of SQL injection
   * attacks.
   *
   * @param contributor         Uploading {@link User} (should be same value as {@code
   *                            contributorAgain}).
   * @param titleFragment       Text fragment to search for (should be the same as {@code
   *                            descriptionFragment}).
   * @param contributorAgain    Uploading {@link User} (should be same value as {@code
   *                            contributor}).
   * @param descriptionFragment Text fragment to search for (should be the same as {@code
   *                            titleFragment}).
   * @return All images satisfying the specified criteria.
   */
  Iterable<Image> findAllByContributorAndTitleContainsOrContributorAndDescriptionContainsOrderByTitleAscCreatedDesc(
      @NonNull User contributor, @NonNull String titleFragment,
      @NonNull User contributorAgain, @NonNull String descriptionFragment);

  /**
   * Selects and returns all images from the specified {@code contributor}, with the specified text
   * fragments in their titles or descriptions.
   *
   * @param contributor Uploading {@link User} (should be same value as {@code contributorAgain}).
   * @param fragment    Text fragment to search for.
   * @return All images satisfying the specified criteria.
   */
  default Iterable<Image> findAllByContributorAndFragment(
      @NonNull User contributor, @NonNull String fragment) {
    return findAllByContributorAndTitleContainsOrContributorAndDescriptionContainsOrderByTitleAscCreatedDesc(
        contributor, fragment, contributor, fragment);
  }

}
