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
package edu.cnm.deepdive.gallery.configuration;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Provides configuration methods to customize token validation and to secure endpoints by
 * authentication status &amp; role.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final Converter<Jwt, ? extends AbstractAuthenticationToken> converter;

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String issuerUri;

  @Value("${spring.security.oauth2.resourceserver.jwt.client-id}")
  private String clientId;

  /**
   * Initializes this instance with the provided {@link Converter}, used to convert the Bearer token
   * into a token useful for later injection into controller methods.
   *
   * @param converter Token converter.
   */
  @Autowired
  public SecurityConfiguration(Converter<Jwt, ? extends AbstractAuthenticationToken> converter) {
    this.converter = converter;
  }

  /**
   * Declares access-control rules on REST endpoints, based on HTTP method, authentication status,
   * and roles/authorities granted.
   *
   * @param http Security builder.
   * @throws Exception If an error occurs.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests((auth) ->
            auth
                .antMatchers(HttpMethod.GET, "/images/**").permitAll()
                .anyRequest().authenticated()
        )
        .oauth2ResourceServer().jwt()
        .jwtAuthenticationConverter(converter);
  }

  /**
   * Constructs and returns an injectable {@link JwtDecoder} that will be used automatically for
   * extended validation of the JSON web token (JWT) included as a bearer token in the request.
   *
   * @return Token decoder.
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
    OAuth2TokenValidator<Jwt> audienceValidator =
        new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, (aud) -> aud.contains(clientId));
    OAuth2TokenValidator<Jwt> issuerAndTimestampValidator =
        JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> combinedValidator =
        new DelegatingOAuth2TokenValidator<Jwt>(issuerAndTimestampValidator, audienceValidator);
    decoder.setJwtValidator(combinedValidator);
    return decoder;
  }

}
