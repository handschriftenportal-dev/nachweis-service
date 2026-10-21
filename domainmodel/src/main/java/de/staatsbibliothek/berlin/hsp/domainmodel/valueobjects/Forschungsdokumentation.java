package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.io.Serial;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * A textual reference to research documentation for a KulturObjektDokument.
 *
 * @param ressource required plain text; whitespace is normalized by the constructor
 * @param link optional absolute HTTP(S) URL including a host; surrounding whitespace is removed
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.09.2026
 */
public record Forschungsdokumentation(String ressource, String link) implements Serializable {

  @Serial
  private static final long serialVersionUID = 3795717606714027612L;

  public Forschungsdokumentation {
    ressource = StringUtils.normalizeSpace(ressource);
    link = StringUtils.trimToNull(link);

    if (StringUtils.isBlank(ressource)) {
      throw new InvalidRessourceException("ressource must not be blank");
    }

    if (!isValidLinkValue(link)) {
      throw new InvalidLinkException(
          "link must be an absolute HTTP or HTTPS URL with a host: " + link);
    }
  }

  public boolean hasLink() {
    return link != null;
  }

  /**
   * Validates an optional link according to the rule enforced by the constructor. A blank value is
   * valid because the link is optional.
   */
  public static boolean isValidLinkValue(String link) {
    return StringUtils.isBlank(link) || HttpUrlValidator.isValid(link);
  }

  public static final class InvalidRessourceException extends IllegalArgumentException {

    public InvalidRessourceException(String message) {
      super(message);
    }
  }

  public static final class InvalidLinkException extends IllegalArgumentException {

    public InvalidLinkException(String message) {
      super(message);
    }
  }
}
