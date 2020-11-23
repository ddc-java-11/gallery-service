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

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Implements methods to satisfy dependencies on classes implemented outside of Spring and this
 * application. Using the {@code @Bean} annotation, the instances returned by these methods are made
 * available for injection into other classes.
 */
@Configuration
public class Beans {

  /**
   * Constructs and returns an instance of {@link Random} (or a suitable subclass),
   */
  @Bean
  public Random random() {
    return new SecureRandom();
  }

  /**
   * Constructs and returns an {@link ApplicationHome}, reflecting this application's runtime
   * location context.
   */
  @Bean
  public ApplicationHome applicationHome() {
    return new ApplicationHome(this.getClass());
  }

}
