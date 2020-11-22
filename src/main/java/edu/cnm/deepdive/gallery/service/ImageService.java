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

import edu.cnm.deepdive.gallery.model.dao.ImageRepository;
import edu.cnm.deepdive.gallery.model.entity.Image;
import edu.cnm.deepdive.gallery.model.entity.User;
import edu.cnm.deepdive.gallery.service.StorageService.StorageReference;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  private final ImageRepository imageRepository;
  private final StorageService storageService;

  @Autowired
  public ImageService(ImageRepository imageRepository, StorageService storageService) {
    this.imageRepository = imageRepository;
    this.storageService = storageService;
  }

  public Optional<Image> get(UUID id) {
    return imageRepository.findById(id);
  }

  public Optional<Image> get(UUID id, User contributor) {
    return imageRepository.findFirstByIdAndContributor(id, contributor);
  }

  public void delete(Image image) {
    storageService.delete(image.getPath());
    imageRepository.delete(image); // Delete unconditonally.
  }

  public Streamable<Image> search(User contributor, String fragment) {
    Streamable<Image> images;
    if (contributor != null) {
      if (fragment != null) {
        images = Streamable.of(
            imageRepository
                .findAllByContributorAndDescriptionContainsOrderByNameAsc(contributor, fragment)
                .and(imageRepository
                    .findAllByContributorAndNameContainsOrderByNameAsc(contributor, fragment))
                .toSet()
        );
      } else {
        images = imageRepository.findAllByContributorOrderByNameAsc(contributor);
      }
    } else if (fragment != null) {
      images = Streamable.of(
          imageRepository
              .findAllByNameContainsOrderByNameAsc(fragment)
              .and(imageRepository.findAllByDescriptionContainsOrderByNameAsc(fragment))
              .toSet()
      );
    } else {
      images = imageRepository.getAllByOrderByNameAsc();
    }
    return images;
  }

  public Image save(Image image) {
    return imageRepository.save(image);
  }

  public Image store(MultipartFile file, String description, User contributor) throws IOException {
    StorageReference translation = storageService.store(file);
    Image image = new Image();
    image.setName(translation.getFilename());
    image.setPath(translation.getReference());
    image.setDescription(description);
    image.setContributor(contributor);
    image.setContentType(file.getContentType());
    return imageRepository.save(image);
  }

  public Resource retrieve(Image image) throws MalformedURLException {
    return storageService.retrieve(image.getPath());
  }

}
