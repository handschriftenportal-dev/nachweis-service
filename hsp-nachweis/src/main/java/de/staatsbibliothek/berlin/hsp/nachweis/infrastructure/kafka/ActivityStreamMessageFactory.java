package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.03.23.
 * @version 1.0
 */

public class ActivityStreamMessageFactory {

  private ActivityStreamMessageFactory() {
  }

  public static ActivityStream createMessageForKulturobjektDokument(KulturObjektDokument kod,
      ActivityStreamAction action, boolean compressed, String actor,
      BeschreibungsRepository beschreibungsRepository)
      throws ActivityStreamsException {

    final ActivityStream message = createMessageForKulturobjektDokumentOnly(
        kod, action, compressed, actor);

    addExternalBeschreibungenToMessage(kod, message, compressed, beschreibungsRepository);

    return message;
  }

  public static ActivityStream createMessageForKulturobjektDokumentOnly(KulturObjektDokument kod,
      ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {
    checkKODAndActionTypeParameter(kod, action);

    ActivityStream message = createActivityStreamMessage(
        kod.getId(), action, actor);

    ActivityStreamObject activityStreamObjectKOD = ActivityStreamObject.builder()
        .withId(kod.getId())
        .withContent(kod.getTeiXML())
        .withCompressed(compressed)
        .withType(ActivityStreamsDokumentTyp.KOD)
        .build();

    message.addObject(activityStreamObjectKOD);
    return message;
  }

  public static ActivityStream createMessageForKatalog(Katalog katalog, ActivityStreamAction action,
      String actor, boolean compressed)
      throws ActivityStreamsException {
    ActivityStream message = createActivityStreamMessage(
        katalog.getId(), action, actor);

    ActivityStreamObject activityStreamObjectKatalog = ActivityStreamObject.builder()
        .withId(katalog.getId())
        .withContent(katalog.getTeiXML())
        .withCompressed(compressed)
        .withType(ActivityStreamsDokumentTyp.KATALOG)
        .build();

    message.addObject(activityStreamObjectKatalog);

    return message;
  }

  public static ActivityStream createMessageForUpload(
      ImportUploadDatei datei, ActivityStreamsDokumentTyp dokumentTyp,
      ActivityStreamAction action, List<ActivityStreamObjectTag> tag, String actor)
      throws ActivityStreamsException {

    ActivityStreamObject uploadObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(dokumentTyp)
        .withContent(datei.getContent())
        .withName(datei.getName())
        .withMediaType(datei.getContentType())
        .withTag(tag)
        .build();

    return ActivityStream
        .builder()
        .withId(UUID.randomUUID().toString())
        .withType(action)
        .withPublished(LocalDateTime.now())
        .withActorName(actor)
        .withTargetName(datei.getName())
        .addObject(uploadObject)
        .build();
  }

  public static ActivityStream createMessageForImportJob(ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException {

    ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

    ActivityStreamObject importJobObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName(importVorgang.getId())
        .withContent(mapper.writeValueAsString(importVorgang))
        .withMediaType("application/json")
        .build();

    return ActivityStream
        .builder()
        .withId(importVorgang.getId())
        .withType(ActivityStreamAction.UPDATE)
        .withPublished(LocalDateTime.now())
        .addObject(importJobObject)
        .build();
  }

  public static ActivityStream createMessageForIndexRemoveAll(String actor)
      throws ActivityStreamsException {

    ActivityStreamObject anyObject = ActivityStreamObject.builder()
        .withCompressed(false)
        .withType(ActivityStreamsDokumentTyp.ANY).build();

    return ActivityStream
        .builder()
        .withId(UUID.randomUUID().toString())
        .withType(ActivityStreamAction.REMOVE_ALL)
        .withActorName(actor)
        .withPublished(LocalDateTime.now())
        .addObject(anyObject)
        .build();
  }

  protected static ActivityStream createActivityStreamMessage(String id,
      ActivityStreamAction action, String actor) throws ActivityStreamsException {
    return ActivityStream
        .builder()
        .withId(id)
        .withType(action)
        .withActorName(actor)
        .withPublished(LocalDateTime.now())
        .build();
  }

  protected static void addExternalBeschreibungenToMessage(KulturObjektDokument kod,
      ActivityStream message, boolean compressed, BeschreibungsRepository beschreibungsRepository)
      throws ActivityStreamsException {

    List<Beschreibung> beschreibungen = beschreibungsRepository.findAllById(
        kod.getBeschreibungenIDs());

    long invalidCount = beschreibungen.stream()
        .filter(Objects::nonNull)
        .filter(b -> b.getId() == null || !b.hasTeiXml())
        .count();

    if (invalidCount > 0) {
      throw new IllegalArgumentException(
          String.format("Found %d invalid Beschreibungen with null ID or null/blank TEI XML",
              invalidCount));
    }

    for (Beschreibung beschreibung : beschreibungen) {
      if (beschreibung != null && beschreibung.isPublished()) {
        ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
            .withId(beschreibung.getId())
            .withContent(beschreibung.getTeiXML())
            .withCompressed(compressed)
            .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
            .build();
        message.addObject(activityStreamObject);
      }
    }
  }

  protected static void checkKODAndActionTypeParameter(KulturObjektDokument kulturObjektDokument,
      ActivityStreamAction action) {
    if (Objects.isNull(kulturObjektDokument) || Objects.isNull(kulturObjektDokument.getId())
        || Objects.isNull(action) || !kulturObjektDokument.hasTeiXml()) {
      throw new IllegalArgumentException("Can't send message because of needed parameter of KOD.");
    }
  }

}
