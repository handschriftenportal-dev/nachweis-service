package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForIndexRemoveAll;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKatalog;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokument;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokumentOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.03.23.
 * @version 1.0
 */
class ActivityStreamMessageFactoryTest {

  private static final Identifikation IDENTIFIKATION = new Identifikation.IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @Test
  void testCreateMessageForUpload() throws ActivityStreamsException {
    byte[] content = "Test".getBytes(StandardCharsets.UTF_8);
    ImportUploadDatei datei = new ImportUploadDatei("test.txt", content, "text");

    ActivityStream message = ActivityStreamMessageFactory.createMessageForUpload(datei,
        ActivityStreamsDokumentTyp.BESCHREIBUNG, ActivityStreamAction.ADD, Arrays.asList(),
        "Konrad");

    assertNotNull(message);
    assertFalse(message.getId().isEmpty());
    assertEquals(ActivityStreamAction.ADD, message.getAction());
    assertNotNull(message.getPublished());
    assertEquals("Konrad", message.getActor().getName());
    assertEquals(1, message.getObjects().size());

    ActivityStreamObject object = message.getObjects().get(0);
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.BESCHREIBUNG, object.getType());
    assertNotNull(object.getTag());
    assertEquals(content.length, object.getContent().length);
  }

  @Test
  void testCreateMessageForImportVorgang()
      throws JsonProcessingException, ActivityStreamsException {
    String jsonJob = "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\"ms_1.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[{\"id\":\"HSP-c137570e-95b8-4af8-a92e-eae03d0363b2\",\"label\":\"Ms 1\",\"url\":null}]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"SUCCESS\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}";
    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    ActivityStream message = ActivityStreamMessageFactory.createMessageForImportJob(importVorgang);

    assertNotNull(message);
    assertEquals(importVorgang.getId(), message.getId());
    assertEquals(ActivityStreamAction.UPDATE, message.getAction());
    assertNotNull(message.getPublished());
    assertEquals(1, message.getObjects().size());

    ActivityStreamObject object = message.getObjects().getFirst();
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.IMPORT, object.getType());
    assertNotNull(object.getContent());
  }

  @Test
  void testCreateMessageForKOD() throws ActivityStreamsException {
    BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder().withId("KOD1")
        .withTEIXml("<xml></xml>")
        .build();
    Publikation publikation = new Publikation.PublikationBuilder()
        .withPublikationsDatumHSP(LocalDateTime.now())
        .addPURL(new PURL(
            URI.create("https://purl.test/1"), URI.create("https://ziel.test/1"),
            PURLTyp.EXTERNAL))
        .build();
    publikation.onApproved();
    Beschreibung external = new BeschreibungsBuilder("BESCHREIBUNG1", kulturObjektDokument.getId(),
        IDENTIFIKATION)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(publikation)
        .withTEIXml("<xml></xml>")
        .build();
    Beschreibung internal = new BeschreibungsBuilder("BESCHREIBUNG2", kulturObjektDokument.getId(),
        IDENTIFIKATION)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withTEIXml("<xml></xml>")
        .build();
    kulturObjektDokument.addBeschreibungsdokument(external.getId());
    kulturObjektDokument.addBeschreibungsdokument(internal.getId());
    when(beschreibungsRepository.findAllById(any()))
        .thenReturn(List.of(external, internal));

    ActivityStream message = createMessageForKulturobjektDokument(kulturObjektDokument,
        ActivityStreamAction.ADD, true,
        "Konrad", beschreibungsRepository);

    assertEquals(ActivityStreamAction.ADD, message.getAction());
    assertTrue(message.getObjects().getFirst().isCompressed());
    assertEquals(2, message.getObjects().size());
    assertEquals("Konrad", message.getActor().getName());
  }

  @Test
  void testCreateMessageForKODOnly() throws ActivityStreamsException {

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder().withId("KOD1")
        .withTEIXml("<xml></xml>")
        .build();
    Beschreibung external = new BeschreibungsBuilder("BESCHREIBUNG1", kulturObjektDokument.getId(),
        IDENTIFIKATION).withVerwaltungsTyp(
        VerwaltungsTyp.EXTERN).withTEIXml("<xml></xml>").build();
    Beschreibung internal = new BeschreibungsBuilder("BESCHREIBUNG2", kulturObjektDokument.getId(),
        IDENTIFIKATION).withVerwaltungsTyp(
        VerwaltungsTyp.INTERN).withTEIXml("<xml></xml>").build();
    kulturObjektDokument.addBeschreibungsdokument(external.getId());
    kulturObjektDokument.addBeschreibungsdokument(internal.getId());

    ActivityStream message = createMessageForKulturobjektDokumentOnly(kulturObjektDokument,
        ActivityStreamAction.ADD, true,
        "Konrad");

    assertEquals(ActivityStreamAction.ADD, message.getAction());
    assertTrue(message.getObjects().get(0).isCompressed());
    assertEquals(1, message.getObjects().size());
    assertEquals("Konrad", message.getActor().getName());
  }

  @Test
  void testCreateMessageForKatalog() throws ActivityStreamsException {

    Katalog katalog = new KatalogBuilder().withId("K1").withTEIXML("<xml></xml>").build();

    ActivityStream message = createMessageForKatalog(katalog, ActivityStreamAction.ADD, "Konrad",
        true);

    assertEquals(ActivityStreamAction.ADD, message.getAction());
    assertTrue(message.getObjects().get(0).isCompressed());
    assertEquals(1, message.getObjects().size());
    assertEquals("Konrad", message.getActor().getName());
    assertEquals("K1", message.getId());

    ActivityStreamObject object = message.getObjects().get(0);
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.KATALOG, object.getType());
    assertNotNull(object.getContent());

  }

  @Test
  void testCreateMessageForIndexRemoveALl() throws ActivityStreamsException {

    ActivityStream message = createMessageForIndexRemoveAll("Konrad");

    assertEquals(ActivityStreamAction.REMOVE_ALL, message.getAction());
    assertFalse(message.getObjects().get(0).isCompressed());
    assertEquals(1, message.getObjects().size());
    assertEquals("Konrad", message.getActor().getName());

    ActivityStreamObject object = message.getObjects().get(0);
    assertNotNull(object);
    assertEquals(ActivityStreamsDokumentTyp.ANY, object.getType());

  }
}
