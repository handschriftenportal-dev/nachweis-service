package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenProducer;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.02.24.
 * @version 1.0
 */

@QuarkusTest
@TestTransaction
public class WerkzeugBatchAllKODAsManuscriptTest {

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Test
  void testExecute() throws ActivityStreamsException, IOException {

    KulturObjektDokument kod = createKulturObjektDokument();
    kulturObjektDokumentRepository.save(kod);

    KafkaNormdatenProducer kafkaNormdatenProducer = Mockito.mock(
        KafkaNormdatenProducer.class);

    BearbeiterBoundary bearbeiterBoundary = Mockito.mock(BearbeiterBoundary.class);
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b-ke101", "b-ke101"));

    WerkzeugBatchAllKODAsManuscript werkzeugBatchAllKODAsManuscript = new WerkzeugBatchAllKODAsManuscript(
        kulturObjektDokumentRepository, kafkaNormdatenProducer, bearbeiterBoundary);

    assertNotNull(werkzeugBatchAllKODAsManuscript);

    werkzeugBatchAllKODAsManuscript.execute();

    Mockito.verify(kafkaNormdatenProducer, times(1))
        .sendKulturobjektDokumentAsActivityStreamMessage(kod, ActivityStreamAction.ADD, true,
            "b-ke101");

  }

  private KulturObjektDokument createKulturObjektDokument() throws IOException {

    return new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP123")
        .withTEIXml(
            Files.readString(Paths.get("src", "test", "resources", "kulturobjekteimport.xml")))
        .build();
  }
}
