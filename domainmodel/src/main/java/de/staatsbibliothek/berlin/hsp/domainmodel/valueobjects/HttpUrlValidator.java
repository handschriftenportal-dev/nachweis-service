package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.net.URI;
import org.apache.commons.lang3.StringUtils;

/** Validates absolute HTTP(S) URLs without checking whether the target is reachable. */
public final class HttpUrlValidator {

  private HttpUrlValidator() {
  }

  public static boolean isValid(String value) {
    if (StringUtils.isBlank(value)) {
      return false;
    }

    URI uri;
    try {
      uri = URI.create(value.strip());
    } catch (IllegalArgumentException _) {
      return false;
    }

    return uri.getHost() != null
        && ("http".equalsIgnoreCase(uri.getScheme())
        || "https".equalsIgnoreCase(uri.getScheme()));
  }
}
