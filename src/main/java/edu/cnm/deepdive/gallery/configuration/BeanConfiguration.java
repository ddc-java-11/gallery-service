package edu.cnm.deepdive.gallery.configuration;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

  @Bean
  public Random random() {
    return new SecureRandom();
  }

  @Bean
  public ApplicationHome applicationHome() {
    return new ApplicationHome(this.getClass());
  }

}
