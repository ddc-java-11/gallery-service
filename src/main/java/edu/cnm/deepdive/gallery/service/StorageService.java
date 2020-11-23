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

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Declares {@link #store(MultipartFile)}, {@link #retrieve(String)}, and {@link #delete(String)}
 * methods for a file store. The key expectations for implementing and consuming this interface are
 * these:
 * <ul>
 *   <li>Files will not be updated or versioned; updates must be saved as distinct files.</li>
 *   <li>Filenames will not be respected by the file store itself; instead, an opaque {@link String}
 *   reference, presumed unique, will be returned when a file is stored, and the same reference must
 *   be provided when retrieving or deleting a file. On the other hand, this means that a consumer
 *   of a service implementing this interface need not be concerned with filename collisions.</li>
 * </ul>
 */
public interface StorageService {

  /**
   * Stores the specified file, returning an opaque reference to the file as a {@link String}.
   *
   * @param file Uploaded file.
   * @return Opaque reference, to be used when retrieving or deleting the file from the store.
   * @throws IOException                         If the file cannot be written for any reason.
   * @throws HttpMediaTypeNotAcceptableException If the content-type of {@code file} is not
   *                                             allowed.
   */
  String store(MultipartFile file) throws IOException, HttpMediaTypeNotAcceptableException;

  /**
   * Retrieves a file from a reference (as returned by {@link #store(MultipartFile)}), returning a
   * {@link Resource} usable by the consumer. When an exception is thrown by this method, the
   * consumer should generally assume that subsequent invocations for the specified file will fail
   * as well.
   *
   * @param reference Opaque reference to the file, as returned by {@link #store(MultipartFile)}.
   * @return Consumer-usable {@link Resource}.
   * @throws IOException If the file cannot be read for any reason.
   */
  Resource retrieve(String reference) throws IOException;

  /**
   * Deletes the file referred to by the provided opaque {@code reference}.
   *
   * @param reference Opaque reference {@link String} returned from {@link #store(MultipartFile)}.
   * @return {@code true} if the file was successfully deleted from the store; false otherwise.
   * @throws IOException                   If the file cannot be accessed (for any reason) from the
   *                                       specified {@code reference}.
   * @throws UnsupportedOperationException If the file store is write-only.
   * @throws SecurityException             If the current process does not have permission to delete
   *                                       the specified file.
   */
  boolean delete(String reference)
      throws IOException, UnsupportedOperationException, SecurityException;

}
