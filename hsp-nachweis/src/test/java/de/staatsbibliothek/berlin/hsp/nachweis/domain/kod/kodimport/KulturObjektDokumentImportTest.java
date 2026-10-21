package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.07.21
 */
public class KulturObjektDokumentImportTest {

  @Test
  void testCreation() throws ActivityStreamsException {

    ActivityStreamObject activityStreamObjectDigitalisat = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.KOD)
        .build();

    ActivityStreamMessageDTO importKODMessage = new ActivityStreamMessageDTO(ActivityStream.builder()
        .withId("tei_kulturobjektdokument_test_01.xml")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withActorName("Konrad Eichstädt")
        .addObject(activityStreamObjectDigitalisat)
        .build());

    KulturObjektDokumentImport kulturObjektDokumentImport = new KulturObjektDokumentImport(importKODMessage);

    assertNotNull(kulturObjektDokumentImport);

    assertEquals(importKODMessage, kulturObjektDokumentImport.getMessageDTO());
  }
}
