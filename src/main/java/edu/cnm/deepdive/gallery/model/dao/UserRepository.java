package edu.cnm.deepdive.gallery.model.dao;

import edu.cnm.deepdive.gallery.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findFirstByOauthKey(String oauthKey);

  Streamable<User> getAllByOrderByCreated();

  Streamable<User> getAllByOrderByDisplayName();

}
