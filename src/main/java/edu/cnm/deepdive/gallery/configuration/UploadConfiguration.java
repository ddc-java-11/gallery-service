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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Declares a hierarchical set of properties, mapping to a corresponding hierarchy of application
 * properties, all with the {@code "upload."} prefix. These properties customize the root path,
 * filename generation, and subdirectory organization used by the {@link
 * edu.cnm.deepdive.gallery.service.LocalFilesystemStorageService}.
 */
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfiguration {

  /**
   * Flag indicating whether the application's home directory should be used as the parent directory
   * of the file store.
   */
  private boolean applicationHome = true;
  /**
   * Base directory of the file store, relative to the application home directory (if {@code
   * applicationHome} is {@code true}) or to the current working directory.
   */
  private String directory = "uploads";
  /**
   * Regular expression pattern that (in general) includes one or more capture groups, used for
   * constructing a subdirectory path for any given generated filename.
   */
  private Pattern subdirectoryPattern = Pattern.compile("^(.{4})(.{2})(.{2}).*$");
  /**
   * Set of MIME types permitted for upload into the file store.
   */
  private Set<String> whitelist = new LinkedHashSet<>();
  /**
   * Properties used to customize generation of filenames in the file store.
   */
  private FilenameProperties filename;

  /**
   * Returns a flag indicating whether the application's home directory should be used as the parent
   * directory of the file store.
   */
  public boolean isApplicationHome() {
    return applicationHome;
  }

  /**
   * Sets a flag indicating whether the application's home directory should be used as the parent
   * directory of the file store.
   */
  public void setApplicationHome(boolean applicationHome) {
    this.applicationHome = applicationHome;
  }

  /**
   * Returns the base directory of the file store, relative to the application home directory (if
   * {@link #isApplicationHome()} returns {@code true}) or to the current working directory.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Sets the base directory of the file store. If {@code directory} is an absolute path, then it
   * will be used as-is; otherwise, it will be interpreted relative to the application home
   * directory (if {@link #isApplicationHome()} returns {@code true}) or to the current working
   * directory.
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Returns a regular expression pattern that (in general) includes one or more capture groups,
   * used for constructing a subdirectory path for any given generated filename.
   */
  public Pattern getSubdirectoryPattern() {
    return subdirectoryPattern;
  }

  /**
   * Sets the regular expression pattern used to capture the subdirectory path components from a
   * generated filename.
   */
  public void setSubdirectoryPattern(Pattern subdirectoryPattern) {
    this.subdirectoryPattern = subdirectoryPattern;
  }

  /**
   * Returns the set of MIME types permitted for upload into the file store.
   */
  public Set<String> getWhitelist() {
    return whitelist;
  }

  /**
   * Sets the set of MIME types permitted for upload into the file store.
   */
  public void setWhitelist(Set<String> whitelist) {
    this.whitelist = whitelist;
  }

  /**
   * Returns a {@link FilenameProperties} instance, used to customize generation of filenames in the
   * file store.
   */
  public FilenameProperties getFilename() {
    return filename;
  }

  /**
   * Sets the {@link FilenameProperties} instance used to customize generation of filenames in the
   * file store.
   */
  public void setFilename(
      FilenameProperties filename) {
    this.filename = filename;
  }

  /**
   * Encapsulates properties specifying the composition of filenames generated for files uploaded
   * and stored in the file store.
   */
  public static class FilenameProperties {

    /**
     * Format string (see the {@link java.util.Formatter} class for specifications) used to compose
     * a filename from a timestamp (format specifier 1), a random value (format specifier 2), and
     * the file extension (format specifier 3).
     */
    private String format = "%1$s-%2$d.%3$s";
    /**
     * Exclusive upper bound on pseudorandom values included in generated filenames.
     */
    private int randomizerLimit = 1_000_000;
    /**
     * Properties used to customize the format and timezone context of timestamps incorporated into
     * generated filenames.
     */
    private TimestampProperties timestamp;

    /**
     * Returns the format string (see the {@link java.util.Formatter} class for the specifications
     * of such strings) used to compose a filename from a timestamp and a random value.
     */
    public String getFormat() {
      return format;
    }

    /**
     * Sets the format string (see the {@link java.util.Formatter} class for the specifications of
     * such strings) used to compose a filename from a timestamp and a random value.
     */
    public void setFormat(String format) {
      this.format = format;
    }

    /**
     * Returns the exclusive upper bound on pseudorandom values included as filename elements.
     */
    public int getRandomizerLimit() {
      return randomizerLimit;
    }

    /**
     * Sets the exclusive upper bound on pseudorandom values included as filename elements.
     */
    public void setRandomizerLimit(int randomizerLimit) {
      this.randomizerLimit = randomizerLimit;
    }

    /**
     * Returns a {@link TimestampProperties} instance, used to customize the format and timezone
     * context of timestamps incorporated into file store filenames.
     */
    public TimestampProperties getTimestamp() {
      return timestamp;
    }

    /**
     * Sets the {@link TimestampProperties} instance used to customize the format and timezone
     * context of timestamps incorporated into file store filenames.
     */
    public void setTimestamp(
        TimestampProperties timestamp) {
      this.timestamp = timestamp;
    }

    /**
     * Encapsulates properties specifying the format and timezone context of timestamps incorporated
     * into filenames generated for files uploaded and stored in the file store.
     */
    public static class TimestampProperties {

      /**
       * Format string (passed to {@link java.text.SimpleDateFormat#SimpleDateFormat(String)} used
       * to format timestamps in generated filenames.
       */
      private String format = "yyyyMMddHHmmssSSS";
      private TimeZone timeZone = TimeZone.getTimeZone("UTC");

      /**
       * Returns the format string (passed to {@link java.text.SimpleDateFormat#SimpleDateFormat(String)}
       * used to format timestamps in generated filenames.
       */
      public String getFormat() {
        return format;
      }

      /**
       * Sets the format string (passed to {@link java.text.SimpleDateFormat#SimpleDateFormat(String)}
       * used to format timestamps in generated filenames.
       */
      public void setFormat(String format) {
        this.format = format;
      }

      /**
       * Returns the time zone used when formatting the current time as a timestamp in a generated
       * filename.
       */
      public TimeZone getTimeZone() {
        return timeZone;
      }

      /**
       * Sets the time zone used when formatting the current time as a timestamp in a generated
       * filename.
       */
      public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
      }

    }

  }

}
