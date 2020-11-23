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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
 * Encapsulates a persistent user object with basic OpenID properties.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    name = "user_profile",
    indexes = {
        @Index(columnList = "created"),
        @Index(columnList = "updated")
    }
)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(
    value = {"id", "created", "updated", "href"},
    allowGetters = true, ignoreUnknown = true
)
@JsonPropertyOrder({"id", "displayName", "href", "created", "updated"})
@Component
public class User implements Comparable<User> {

  private static final Comparator<User> NATURAL_COMPARATOR =
      Comparator.comparing((u) -> u.displayName);

  private static EntityLinks entityLinks;

  @NonNull
  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(name = "user_id", nullable = false, updatable = false,
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
  @Column(nullable = false)
  private Date updated;

  @NonNull
  @Column(nullable = false, updatable = false, unique = true)
  @JsonIgnore
  private String oauthKey;

  @NonNull
  @Column(nullable = false, unique = true)
  @JsonProperty("name")
  private String displayName;

  @NonNull
  @OneToMany(mappedBy = "contributor")
  @OrderBy("created DESC")
  @JsonIgnore
  private final List<Image> images = new LinkedList<>();

  /**
   * Returns the unique identifier of this user.
   */
  @NonNull
  public UUID getId() {
    return id;
  }

  /**
   * Returns the datetime this user was first persisted to the database.
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
   * Returns the unique identifier provided (and recognized) by the OpenID/OAuth2.0 provider.
   */
  @NonNull
  public String getOauthKey() {
    return oauthKey;
  }

  /**
   * Sets the unique OpenID/OAuth2.0 identifier of this user to the specified {@code oauthKey}.
   */
  public void setOauthKey(@NonNull String oauthKey) {
    this.oauthKey = oauthKey;
  }

  /**
   * Returns the unique display name of this user.
   */
  @NonNull
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Sets the display name of this user to the specified {@code displayName}.
   */
  public void setDisplayName(@NonNull String displayName) {
    this.displayName = displayName;
  }

  /**
   * Returns the {@link List List&lt;Image&gt;} contributed by this user, in descending date order.
   */
  @NonNull
  public List<Image> getImages() {
    return images;
  }

  /**
   * Returns the {@link String#hashCode()} of the {@code oauthKey}. Since this key will not change
   * on or after persistence, this guarantees that the hash for a {@code User} instance does not
   * change.
   */
  @Override
  public int hashCode() {
    //noinspection ConstantConditions
    return (oauthKey != null) ? oauthKey.hashCode() : 0;
  }

  /**
   * Compares this user with {@code obj}, to test for equality. In general, distinct instances that
   * are not yet persisted will not be considered equal, regardless of content; persisted instances
   * will only be considered equal if the primary key values are equal.
   *
   * @param obj object to be tested for equality with this user.
   * @return {@code true} if {@code this} and {@code obj} may be considered equal; false
   * otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    boolean equal;
    if (this == obj) {
      equal = true;
    } else if (obj instanceof User) {
      User other = (User) obj;
      //noinspection ConstantConditions
      equal = id != null || other.id != null && id.equals(other.id);
    } else {
      equal = false;
    }
    return equal;
  }

  /**
   * Compares this user to {@code other} by {@code displayName}, for the purpose of
   * &ldquo;natural&rdquo; ordering.
   *
   * @param other Instance compared to {@code this}.
   * @return Negative if {@code this < other}, positive if {@code this > other}, zero otherwise.
   */
  @Override
  public int compareTo(User other) {
    return NATURAL_COMPARATOR.compare(this, other);
  }

  /**
   * Returns the location of REST resource representation of this image.
   */
  public URI getHref() {
    //noinspection ConstantConditions
    return (id != null) ? entityLinks.linkForItemResource(User.class, id).toUri() : null;
  }

  @PostConstruct
  private void initHateoas() {
    //noinspection ResultOfMethodCallIgnored
    entityLinks.toString();
  }

  /**
   * Injects the {@link EntityLinks} required for constructing the REST resource location of a
   * user.
   */
  @Autowired
  public void setEntityLinks(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EntityLinks entityLinks) {
    User.entityLinks = entityLinks;
  }

}
