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
package edu.cnm.deepdive.gallery.controller;

/**
 * Defines common path elements used in multiple REST controller classes.
 */
public class BaseParameterPatterns {

  /** Regex pattern for a UUID in standard form (required by Spring MVC). */
  public static final String UUID_PATTERN =
      "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";
  /** Path component for an {@code {id}} placeholder constrained to the{@link #UUID_PATTERN}. */
  public static final String UUID_PATH_PARAMETER_PATTERN = "/{id:" + UUID_PATTERN +  "}";

}
