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
