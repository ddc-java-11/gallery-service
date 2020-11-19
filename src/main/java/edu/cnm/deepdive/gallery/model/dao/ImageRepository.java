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
import org.springframework.data.util.Streamable;

public interface ImageRepository extends JpaRepository<Image, UUID> {

  Optional<Image> findFirstByIdAndContributor(UUID id, User contributer);

  Streamable<Image> getAllByOrderByNameAsc();

  Streamable<Image> findAllByContributorOrderByNameAsc(User contributor);

  Streamable<Image> findAllByNameContainsOrderByNameAsc(String nameFragment);

  Streamable<Image> findAllByDescriptionContainsOrderByNameAsc(String descriptionFragment);

  Streamable<Image> findAllByContributorAndNameContainsOrderByNameAsc(
      User contributor, String nameFragment);

  Streamable<Image> findAllByContributorAndDescriptionContainsOrderByNameAsc(
      User contributor, String descriptionFragment);

}
