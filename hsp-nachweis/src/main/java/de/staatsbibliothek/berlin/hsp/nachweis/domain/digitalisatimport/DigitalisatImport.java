package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.11.2020.
 * @version 1.0
 * <p>
 * This class represents an value object as transport message for an arrived digitalisat import.
 * </p>
 */
public class DigitalisatImport {

  private final ActivityStreamMessageDTO messageDTO;

  private final Bearbeiter bearbeiter;

  public DigitalisatImport(ActivityStream message, Bearbeiter bearbeiter)
      throws ActivityStreamsException {
    this.messageDTO = new ActivityStreamMessageDTO(message);
    this.bearbeiter = bearbeiter;
  }

  public DigitalisatImport(ActivityStreamMessageDTO message, Bearbeiter bearbeiter)
      throws ActivityStreamsException {
    this.messageDTO = message;
    this.bearbeiter = bearbeiter;
  }

  public ActivityStreamMessageDTO getMessageDTO() {
    return messageDTO;
  }

  public Bearbeiter getBearbeiter() {
    return bearbeiter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DigitalisatImport that = (DigitalisatImport) o;
    return Objects.equals(messageDTO, that.messageDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageDTO);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("DigitalisatImport{");
    sb.append(", message=").append(messageDTO);
    sb.append('}');
    return sb.toString();
  }
}
