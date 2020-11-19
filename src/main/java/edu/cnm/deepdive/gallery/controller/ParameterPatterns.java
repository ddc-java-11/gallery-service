package edu.cnm.deepdive.gallery.controller;

public class ParameterPatterns {

  public static final String UUID_PATTERN = "[0-9a-fA-F\\-]{32,36}";
  public static final String UUID_PATH_PARAMETER_PATTERN = "/{id:" + UUID_PATTERN +  "}";

}
