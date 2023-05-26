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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2KatalogMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tei_c.ns._1.TEI;

@ApplicationScoped
@Slf4j
public class KatalogImportService {

  private final KatalogBoundary katalogBoundary;
  private final BearbeiterBoundary bearbeiterBoundary;
  private final ImportVorgangBoundary importVorgangBoundary;

  private final String nachweisserverbaseurl;

  @Inject
  public KatalogImportService(
      KatalogBoundary katalogBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      ImportVorgangBoundary importVorgangBoundary,
      @ConfigProperty(name = "nachweisserverbaseurl") String nachweisserverbaseurl
  ) {
    this.katalogBoundary = katalogBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.importVorgangBoundary = importVorgangBoundary;
    this.nachweisserverbaseurl = nachweisserverbaseurl;
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 7200)
  public void onImportMessage(@Observes @neueImporte KatalogImport importe) {
    log.info("Neuer Katalog Import erhalten {} ", importe.getMessageDTO());

    try {
      ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(importe.getMessageDTO());

      log.info("Starting ImportVorgang {} for Katalog Import", importVorgang.getId());

      Optional<Katalog> optKatalog = convertTEI2Katalog(importe.getMessageDTO());

      if (optKatalog.isEmpty()) {
        throw new Exception("ImportVorgang " + importVorgang.getId() + " contains no Katalog!");
      }

      Bearbeiter bearbeiter = bearbeiterBoundary.findBearbeiterWithName(importe.getMessageDTO().getActor().getName());
      Katalog katalog = katalogBoundary.importieren(bearbeiter, optKatalog.get());

      if (Objects.isNull(katalog)) {
        throw new Exception("ImportVorgang " + importVorgang.getId() + " returned no Katalog!");
      }
      sendSuccessMessageToImport(importe.getMessageDTO(), katalog, importVorgang);

      log.info("Finished ImportVorgang {} for Katalog Import", importVorgang.getId());

    } catch (Exception error) {
      log.error("Error during Katalog importieren!", error);
      sendFailureMessageToImport(importe.getMessageDTO(), error.getMessage());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void sendSuccessMessageToImport(ActivityStreamMessageDTO message,
      Katalog katalog, ImportVorgang importVorgang) throws Exception {

    ImportFile importFile = message.getObjects().stream()
        .filter(o -> ActivityStreamsDokumentTyp.KATALOG.equals(o.getType()))
        .findFirst()
        .map(activityStreamObject -> findImportFileForActivityStreamObject(importVorgang, activityStreamObject))
        .orElseThrow(() -> new ActivityStreamsException("Found no ImportFile for message " + message.getId()));

    importFile.getDataEntityList()
        .add(new DataEntity(katalog.getId(),
            katalog.getHskID(),
            nachweisserverbaseurl + "katalog-detail-view.xhtml?id="
                + katalog.getId()));

    for (KatalogBeschreibungReferenz referenz : katalog.getBeschreibungen()) {
      String id = referenz.getBeschreibungsID();
      String label = Optional.ofNullable(referenz.getManifestRangeURL())
          .map(uri -> uri.toASCIIString())
          .orElse("");
      String url = Optional.ofNullable(referenz.getBeschreibungsID())
          .filter(beschreibungId -> beschreibungId.startsWith("HSP"))
          .map(beschreibungId -> nachweisserverbaseurl + "beschreibung/beschreibung.xhtml?id=" + beschreibungId)
          .orElse("");
      importFile.getDataEntityList().add(new DataEntity(id, label, url));
    }

    log.info("Sending Success to Kafka for importVorgang {}.", importVorgang.getId());

    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
    importVorgangBoundary.sendImportJobToKafka(importVorgang);
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
  public void sendFailureMessageToImport(ActivityStreamMessageDTO message, String errorMessage) {
    ImportVorgang importVorgang;
    try {

      importVorgang = importVorgangBoundary.getImportVorgang(message);
      importVorgang.setFehler(errorMessage);
      importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);

    } catch (Exception e) {
      log.error("Error during Send Import Message!", e);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  Optional<Katalog> convertTEI2Katalog(ActivityStreamMessageDTO activityStreamMessage)
      throws Exception {
    TEI2KatalogMapper katalogMapper = new TEI2KatalogMapper();

    if (activityStreamMessage != null && activityStreamMessage.getObjects() != null) {
      for (ActivityStreamObjectDTO streamObject : activityStreamMessage.getObjects()) {
        if (ActivityStreamsDokumentTyp.KATALOG.equals(streamObject.getType())) {
          Optional<TEI> katalogTEI = TEIObjectFactory.unmarshalOne(new ByteArrayInputStream(streamObject.getContent()));
          if (katalogTEI.isPresent()) {
            return Optional.ofNullable(katalogMapper.map(katalogTEI.get()));
          }
        }
      }
    }

    return Optional.empty();
  }

  ImportFile findImportFileForActivityStreamObject(ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject) {

    ImportFile importFile;
    Objects.requireNonNull(activityStreamObject.getName(), "ActivityStreamObject without name");
    importFile = importVorgang.getImportFiles().stream()
        .filter(searchForImportFile -> activityStreamObject.getName()
            .equals(searchForImportFile.getDateiName()))
        .findFirst()
        .orElseThrow(() -> new NullPointerException(
            "No importFile found for " + activityStreamObject.getName()));
    return importFile;
  }

}
