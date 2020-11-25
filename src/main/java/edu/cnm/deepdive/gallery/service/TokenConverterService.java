package edu.cnm.deepdive.gallery.service;

import edu.cnm.deepdive.gallery.model.entity.User;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Converter to create a {@link UsernamePasswordAuthenticationToken} from a JSON web token (JWT)
 * bearer token.
 */
@Service
public class TokenConverterService implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

  private final UserService userService;

  @Autowired
  public TokenConverterService(UserService userService) {
    this.userService = userService;
  }

  /**
   * Converts the provided JSON web token (JWT) to a {@link UsernamePasswordAuthenticationToken},
   * containing the {@link User} instance retrieved from {@link UserService#getOrCreate(String,
   * String)}, the original bearer token presented, and the granted role of {@code USER}.
   *
   * @param jwt Decrypted bearer token.
   * @return Instance of {@link UsernamePasswordAuthenticationToken} which will be available in the
   * context of the request.
   */
  @Override
  public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
    return new UsernamePasswordAuthenticationToken(
        userService.getOrCreate(jwt.getSubject(), jwt.getClaim("name")),
        jwt.getTokenValue(),
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }

}
