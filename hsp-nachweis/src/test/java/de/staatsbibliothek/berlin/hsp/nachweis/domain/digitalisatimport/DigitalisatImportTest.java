package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

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
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.11.2020.
 * @version 1.0
 */
public class DigitalisatImportTest {

  @Test
  void testCreation() throws ActivityStreamsException {

    ActivityStreamObject activityStreamObjectDigitalisat = ActivityStreamObject
        .builder()
        .withType(ActivityStreamsDokumentTyp.DIGITALISAT)
        .build();

    ActivityStreamMessageDTO importDigitalisatMessage = new ActivityStreamMessageDTO(
        ActivityStream.builder()
            .withId("tei_digitalisat_test_4_190405.fmt.xml")
            .withType(ActivityStreamAction.ADD)
            .withPublished(LocalDateTime.now())
            .withActorName("Konrad Eichstädt")
            .addObject(activityStreamObjectDigitalisat)
            .build());

    DigitalisatImport digitalisatImport = new DigitalisatImport(importDigitalisatMessage,
        new Bearbeiter("1", "Unbekannter Tester"));

    assertNotNull(digitalisatImport);

    assertEquals(importDigitalisatMessage, digitalisatImport.getMessageDTO());
  }
}
