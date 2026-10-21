package de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.05.2020.
 * @version 1.0
 */
public interface ImportVorgangBoundary {

  Logger logger = LoggerFactory.getLogger(ImportVorgangBoundary.class);

  void sendDateiUploadMessageToImportService(ImportUploadDatei datei,
      ActivityStreamsDokumentTyp dokumentTyp,
      Bearbeiter bearbeiter)
      throws ImportVorgangException;

  Optional<ImportVorgang> findById(String id);

  Set<ImportVorgang> findAll();

  void sendDateiUploadMessageToImportService(ImportUploadDatei datei,
      ActivityStreamsDokumentTyp dokumentTyp,
      Bearbeiter bearbeiter,
      List<ActivityStreamObjectTag> tag)
      throws ImportVorgangException;

  ImportVorgang getImportVorgang(ActivityStreamMessageDTO activityStream)
      throws ActivityStreamsException, JsonProcessingException;

  void sendImportJobToKafka(ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException;

  void deleteVorgaenge(List<ImportVorgang> importVorgang) throws ImportVorgangException;

  default void sendFailureMessageToImport(ActivityStreamMessageDTO message, String errorMessage) {
    ImportVorgang importVorgang;
    try {
      importVorgang = getImportVorgang(message);
      importVorgang.setFehler(errorMessage);
      importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
      sendImportJobToKafka(importVorgang);
    } catch (Exception e) {
      logger.error("Error during Send Import Message!", e);
    }
  }
}
