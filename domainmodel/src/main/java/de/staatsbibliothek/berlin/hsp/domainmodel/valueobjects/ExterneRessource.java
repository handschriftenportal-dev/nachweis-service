package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A link to a manuscript record in an external portal.
 *
 * @param url absolute http(s) URL including a host; stripped by the constructor, so two instances
 *            differing only in padding are equal
 */
public record ExterneRessource(ExterneRessourceTyp typ, String url) implements Serializable {

  @Serial
  private static final long serialVersionUID = 4118391174846128953L;

  public ExterneRessource {
    Objects.requireNonNull(typ, "typ is required");
    Objects.requireNonNull(url, "url is required");

    url = url.strip();

    if (url.isBlank()) {
      throw new IllegalArgumentException("url must not be blank");
    }

    if (!isValidUrl(url)) {
      throw new IllegalArgumentException(
          "url must be an absolute HTTP or HTTPS URL with a host: " + url);
    }
  }

  /**
   * Same rule the constructor enforces, exposed so callers can check an url without using an
   * exception as control flow.
   */
  public static boolean isValidUrl(String url) {
    return HttpUrlValidator.isValid(url);
  }
}
