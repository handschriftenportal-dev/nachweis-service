package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.util.Objects;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.07.21
 */
public class KulturObjektDokumentImport {

  private final ActivityStreamMessageDTO messageDTO;

  public KulturObjektDokumentImport(ActivityStream message) throws ActivityStreamsException {
    this.messageDTO = new ActivityStreamMessageDTO(message);
  }

  public KulturObjektDokumentImport(ActivityStreamMessageDTO message) {
    this.messageDTO = message;
  }

  public ActivityStreamMessageDTO getMessageDTO() {
    return messageDTO;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KulturObjektDokumentImport that = (KulturObjektDokumentImport) o;
    return Objects.equals(messageDTO, that.messageDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageDTO);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("KulturObjektDokumentImport{");
    sb.append(", message=").append(messageDTO);
    sb.append('}');
    return sb.toString();
  }
}
