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
package edu.cnm.deepdive.gallery.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Encapsulates a persistent image object with: title, description, file metadata (original filename
 * and MIME type), reference to the contributing user, and reference to the actual content.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    indexes = {
        @Index(columnList = "created, updated"),
        @Index(columnList = "title")
    }
)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(
    value = {"id", "created", "updated", "href", "contributor"},
    allowGetters = true, ignoreUnknown = true
)
@JsonPropertyOrder(
    {"id", "title", "description", "name", "href", "created", "updated", "contributor"})
@Component
public class Image implements Comparable<Image> {

  private static final Comparator<Image> NATURAL_COMPARATOR =
      Comparator.comparing((img) -> (img.title != null) ? img.title : img.name);

  private static EntityLinks entityLinks;

  @NonNull
  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(name = "image_id", nullable = false, updatable = false,
      columnDefinition = "CHAR(16) FOR BIT DATA")
  private UUID id;

  @NonNull
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  private Date created;

  @NonNull
  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  private Date updated;

  @Column(length = 100)
  private String title;

  @Column(length = 1024)
  private String description;

  @NonNull
  @Column(nullable = false, updatable = false)
  private String name;

  @NonNull
  @Column(nullable = false, updatable = false)
  @JsonIgnore
  private String path;

  @NonNull
  @Column(nullable = false, updatable = false)
  private String contentType;

  @NonNull
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "contributor_id", nullable = false, updatable = false)
  private User contributor;

  /**
   * Returns the unique identifier of this image.
   */
  @NonNull
  public UUID getId() {
    return id;
  }

  /**
   * Returns the datetime this image was first persisted to the database.
   */
  @NonNull
  public Date getCreated() {
    return created;
  }

  /**
   * Returns the datetime this image was most recently updated in the database.
   */
  @NonNull
  public Date getUpdated() {
    return updated;
  }

  /**
   * Returns the title of this image.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of this image to the specified {@code title}.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the description of this image.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this image to the specified {@code description}.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the original filename of this image.
   */
  @NonNull
  public String getName() {
    return name;
  }

  /**
   * Sets the original filename of this image to the specified {@code name}.
   */
  public void setName(@NonNull String name) {
    this.name = name;
  }

  /**
   * Returns a reference (a {@link String} representation of a {@link java.nio.file.Path}, {@link
   * URI}, etc.) to the location of this image. This should be treated as an &ldquo;opaque&rdquo;
   * value, meaningful only to the storage service.
   */
  @NonNull
  public String getPath() {
    return path;
  }

  /**
   * Sets the location reference of this image to the specified {@code path}. This should be treated
   * as an &ldquo;opaque&rdquo; value, meaningful only to the storage service.
   */
  public void setPath(@NonNull String path) {
    this.path = path;
  }

  /**
   * Returns the MIME type of this image.
   */
  @NonNull
  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the MIME type of this image to the specified {@code contentType}.
   */
  public void setContentType(@NonNull String contentType) {
    this.contentType = contentType;
  }

  /**
   * Returns the {@link User} that contributed this image.
   */
  @NonNull
  public User getContributor() {
    return contributor;
  }

  /**
   * Sets this image's contributor to the specified {@link User}.
   */
  public void setContributor(@NonNull User contributor) {
    this.contributor = contributor;
  }

  /**
   * Returns the {@link String#hashCode()} of the original filename. Since this filename will not
   * change on or after persistence, this guarantees that the hash for an {@code Image} instance
   * does not change.
   */
  @Override
  public int hashCode() {
    //noinspection ConstantConditions
    return (name != null) ? name.hashCode() : 0;
  }

  /**
   * Compares this image with {@code obj}, to test for equality. In general, distinct instances that
   * are not yet persisted will not be considered equal, regardless of content; persisted instances
   * will only be considered equal if the primary key values are equal.
   *
   * @param obj object to be tested for equality with this image.
   * @return {@code true} if {@code this} and {@code obj} may be considered equal; false
   * otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    boolean equal;
    if (this == obj) {
      equal = true;
    } else if (obj instanceof Image) {
      Image other = (Image) obj;
      //noinspection ConstantConditions
      equal = id != null || other.id != null && id.equals(other.id);
    } else {
      equal = false;
    }
    return equal;
  }

  /**
   * Compares this image to {@code other} by {@code title}, then {@code name} if {@code title} is
   * {@code null}, for the purpose of &ldquo;natural&rdquo; ordering.
   *
   * @param other Instance compared to {@code this}.
   * @return Negative if {@code this < other}, positive if {@code this > other}, zero otherwise.
   */
  @Override
  public int compareTo(Image other) {
    return NATURAL_COMPARATOR.compare(this, other);
  }

  /**
   * Returns the location of REST resource representation of this image.
   */
  public URI getHref() {
    //noinspection ConstantConditions
    return (id != null) ? entityLinks.linkForItemResource(Image.class, id).toUri() : null;
  }

  @PostConstruct
  private void initHateoas() {
    //noinspection ResultOfMethodCallIgnored
    entityLinks.toString();
  }

  /**
   * Injects the {@link EntityLinks} required for constructing the REST resource location of an
   * image.
   */
  @Autowired
  public void setEntityLinks(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EntityLinks entityLinks) {
    Image.entityLinks = entityLinks;
  }

}
