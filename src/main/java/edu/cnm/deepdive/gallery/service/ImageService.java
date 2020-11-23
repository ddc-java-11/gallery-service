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
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implements high-level operations on {@link Image} instances, including file store operations and
 * delegation to methods declared in {@link ImageRepository}.
 */
@Service
public class ImageService {

  private static final String UNTITLED_FILENAME = "untitled";

  private final ImageRepository imageRepository;
  private final StorageService storageService;

  /**
   * Initializes this instance with the provided instances of {@link ImageRepository} and {@link
   * StorageService}.
   *
   * @param imageRepository Spring Data repository providing CRUD operations on {@link Image}
   *                        instances.
   * @param storageService  File store.
   */
  @Autowired
  public ImageService(ImageRepository imageRepository, StorageService storageService) {
    this.imageRepository = imageRepository;
    this.storageService = storageService;
  }

  /**
   * Selects and returns a {@link Image} with the specified {@code id}, as the content of an {@link
   * Optional Optional&lt;Image&gt;}. If no such instance exists, the {@link Optional} is empty.
   *
   * @param id Unique identifier of the {@link Image}.
   * @return {@link Optional Optional&lt;Image&gt;} containing the selected image.
   */
  public Optional<Image> get(@NonNull UUID id) {
    return imageRepository.findById(id);
  }

  /**
   * Selects and returns a {@link Image} with the specified {@code id}, as long as the contributor
   * is the specified {@link User}. If no such instance exists, or if {@code contributor} is not, in
   * fact, the contributor of the image referenced by {@code id}, the {@link Optional} is empty.
   *
   * @param id          Unique identifier of the {@link Image}.
   * @param contributor Presumed contributor of the image.
   * @return {@link Optional Optional&lt;Image&gt;} containing the selected image.
   */
  public Optional<Image> get(@NonNull UUID id, @NonNull User contributor) {
    return imageRepository.findFirstByIdAndContributor(id, contributor);
  }

  /**
   * Deletes the specified {@link Image} instance from the database and the file store. It's assumed
   * that any access control conditions have already been checked.
   *
   * @param image Previously persisted {@link Image} instance to be deleted.
   * @throws IOException If the file cannot be accessed (for any reason) from the specified {@code
   *                     reference}.
   */
  public void delete(@NonNull Image image) throws IOException {
    storageService.delete(image.getPath());
    imageRepository.delete(image); // Delete unconditonally.
  }

  /**
   * Selects and returns all images uploaded by the specified {@link User}.
   *
   * @param contributor {@link User} that uploaded the images.
   * @return All images from {@code contributor}.
   */
  public Iterable<Image> search(@NonNull User contributor) {
    return imageRepository.findAllByContributorOrderByCreatedDesc(contributor);
  }

  /**
   * Selects and returns all images containing the search fragment in the metadata (specifically,
   * the title or description).
   *
   * @param fragment Search text.
   * @return All images containing {@code fragment} in the metadata.
   */
  public Iterable<Image> search(@NonNull String fragment) {
    return imageRepository.findAllByFragment(fragment);
  }

  /**
   * Selects and returns all images uploaded by the specified {@link User}, that also contain the
   * search fragment in the metadata (specifically, the title or description).
   *
   * @param contributor {@link User} that uploaded the images.
   * @param fragment    Search text.
   * @return All images from {@code contributor} with {@code fragment} in the metadata.
   */
  public Iterable<Image> search(@NonNull User contributor, @NonNull String fragment) {
    return imageRepository.findAllByContributorAndFragment(contributor, fragment);
  }

  /**
   * Selects and returns all images.
   */
  public Iterable<Image> list() {
    return imageRepository.getAllByOrderByTitleAscCreatedDesc();
  }

  /**
   * Persists (creates or updates) the specified {@link Image} instance to the database, updating
   * and returning the instance accordingly. (The instance is updated in-place, but the reference to
   * it is also returned.)
   *
   * @param image Instance to be persisted.
   * @return Updated instance.
   */
  public Image save(@NonNull Image image) {
    return imageRepository.save(image);
  }

  /**
   * Stores the image data to the file store, then constructs and returns the corresponding instance
   * of {@link Image}. The latter includes the specified {@code title} and {@code description}
   * metadata, along with a reference to {@code contributor}.
   *
   * @param file        Uploaded file content.
   * @param title       Optional (null is allowed) title of the image.
   * @param description Optional (null is allowed) description of the image.
   * @param contributor Uploading {@link User}.
   * @return {@link Image} instance referencing and describing the uploaded content.
   * @throws IOException                         If the file content cannot&mdash;for any
   *                                             reason&mdash;be written to the file store.
   * @throws HttpMediaTypeNotAcceptableException If the MIME type of the uploaded file is not on the
   *                                             whitelist.
   */
  public Image store(
      @NonNull MultipartFile file, String title, String description, @NonNull User contributor)
      throws IOException, HttpMediaTypeNotAcceptableException {
    String originalFilename = file.getOriginalFilename();
    String contentType = file.getContentType();
    String reference = storageService.store(file);
    Image image = new Image();
    image.setTitle(title);
    image.setDescription(description);
    image.setContributor(contributor);
    image.setName((originalFilename != null) ? originalFilename : UNTITLED_FILENAME);
    image.setContentType(
        (contentType != null) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
    image.setPath(reference);
    return save(image);
  }

  /**
   * Uses the opaque reference contained in {@code image} to return a consumer-usable {@link
   * Resource} to previously uploaded content.
   *
   * @param image {@link Image} entity instance referencing the uploaded content.
   * @return {@link Resource} usable in a response body (e.g. for downloading).
   * @throws IOException If the file content cannot&mdash;for any reason&mdash;be read from the file
   *                     store.
   */
  public Resource retrieve(@NonNull Image image) throws IOException {
    return storageService.retrieve(image.getPath());
  }

  /**
   * Convenience class extending {@link ResponseStatusException}, for the purpose of including a
   * default HTTP response status &amp; message when the no-parameter constructor is used.
   */
  public static class ImageNotFoundException extends ResponseStatusException {

    private static final String IMAGE_NOT_FOUND_REASON = "Image not found";

    /**
     * Initializes this instance with a relevant message &amp; response status.
     */
    public ImageNotFoundException() {
      super(HttpStatus.NOT_FOUND, IMAGE_NOT_FOUND_REASON);
    }

  }

}
