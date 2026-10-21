package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
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
 * @since 01.03.22
 */
public class KatalogImportTest {

  @Test
  void testCreation() throws ActivityStreamsException {

    ActivityStreamObject activityStreamObjectDigitalisat = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.KATALOG)
        .build();

    ActivityStreamMessageDTO importKatalogMessage = new ActivityStreamMessageDTO(
        ActivityStream.builder()
            .withId("tei_katalog_test_4_190405.fmt.xml")
            .withType(ActivityStreamAction.ADD)
            .withPublished(LocalDateTime.now())
            .withActorName("Konrad Eichstädt")
            .addObject(activityStreamObjectDigitalisat)
            .build());

    Bearbeiter bearbeiter = new Bearbeiter("1", "Unbekannter Tester");

    KatalogImport katalogImport = new KatalogImport(importKatalogMessage, bearbeiter);

    assertNotNull(katalogImport);

    assertEquals(importKatalogMessage, katalogImport.getMessageDTO());
    assertEquals(bearbeiter, katalogImport.getBearbeiter());
  }

}
