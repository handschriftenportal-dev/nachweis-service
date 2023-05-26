/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.03.23.
 * @version 1.0
 */

public class ActivityStreamMessageFactory {

  public static ActivityStream createMessageForKulturobjektDokument(KulturObjektDokument kod, ActivityStreamAction action, boolean compressed, String actor,
      BeschreibungsRepository beschreibungsRepository)
      throws ActivityStreamsException {

    checkKODAndActionTypeParameter(kod,action);

    ActivityStream message = createActivityStreamMessage(
        kod.getId(), action, actor);

    ActivityStreamObject activityStreamObjectKOD = ActivityStreamObject.builder()
        .withId(kod.getId())
        .withContent(kod.getTeiXML())
        .withCompressed(compressed)
        .withType(ActivityStreamsDokumentTyp.KOD)
        .build();

    message.addObject(activityStreamObjectKOD);

    addExternalBeschreibungenToMessage(kod, message,compressed, beschreibungsRepository);

    return message;
  }

  public static ActivityStream createMessageForKatalog(Katalog katalog,ActivityStreamAction action, String actor,boolean compressed)
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

    ActivityStream message = ActivityStream
        .builder()
        .withId(UUID.randomUUID().toString())
        .withType(action)
        .withPublished(LocalDateTime.now())
        .withActorName(actor)
        .withTargetName(datei.getName())
        .addObject(uploadObject)
        .build();

    return message;
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

  protected static  ActivityStream createActivityStreamMessage(String id, ActivityStreamAction action, String actor) throws ActivityStreamsException {
    return ActivityStream
        .builder()
        .withId(id)
        .withType(action)
        .withActorName(actor)
        .withPublished(LocalDateTime.now())
        .build();
  }

  protected static void addExternalBeschreibungenToMessage(KulturObjektDokument kod, ActivityStream message, boolean compressed, BeschreibungsRepository beschreibungsRepository)
      throws ActivityStreamsException {
    if (kod.getBeschreibungenIDs() != null) {

      for (String beschreibungsID : kod.getBeschreibungenIDs()) {
        Optional<Beschreibung> beschreibungOption = beschreibungsRepository
            .findByIdOptional(beschreibungsID);

        if (beschreibungOption.isPresent() && VerwaltungsTyp.EXTERN
            .equals(beschreibungOption.get().getVerwaltungsTyp())) {

          Beschreibung beschreibung = beschreibungOption.get();

          if(Objects.isNull(beschreibung.getId()) || Objects.isNull(beschreibung.getTeiXML()) || beschreibung.getTeiXML().isEmpty())
          {
            throw new IllegalArgumentException("Can't send message because of needed parameter Beschreibung.");
          }

          ActivityStreamObject activityStreamObjectBeschreibung = ActivityStreamObject.builder()
              .withId(beschreibung.getId())
              .withContent(beschreibung.getTeiXML())
              .withCompressed(compressed)
              .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
              .build();

          message.addObject(activityStreamObjectBeschreibung);
        }
      }
    }
  }

  protected static void checkKODAndActionTypeParameter(KulturObjektDokument kulturObjektDokument, ActivityStreamAction action) {
    if(Objects.isNull(kulturObjektDokument) || Objects.isNull(kulturObjektDokument.getId()) || Objects.isNull(action) || Objects.isNull(kulturObjektDokument.getTeiXML()) || kulturObjektDokument.getTeiXML().isEmpty())
    {
      throw new IllegalArgumentException("Can't send message because of needed parameter of KOD.");
    }
  }

}
