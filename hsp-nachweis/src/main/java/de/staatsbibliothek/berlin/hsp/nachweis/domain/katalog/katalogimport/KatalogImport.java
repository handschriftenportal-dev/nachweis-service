package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.util.Objects;

public class KatalogImport {

  private final ActivityStreamMessageDTO messageDTO;

  private final Bearbeiter bearbeiter;

  public KatalogImport(ActivityStream message, Bearbeiter bearbeiter)
      throws ActivityStreamsException {
    this.messageDTO = new ActivityStreamMessageDTO(message);
    this.bearbeiter = bearbeiter;
  }

  public KatalogImport(ActivityStreamMessageDTO message, Bearbeiter bearbeiter)
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
    KatalogImport that = (KatalogImport) o;
    return Objects.equals(messageDTO, that.messageDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageDTO);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("KatalogImport{");
    sb.append(", message=").append(messageDTO);
    sb.append('}');
    return sb.toString();
  }
}
