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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.05.2020.
 * @version 1.0
 */
public interface ImportVorgangBoundary {

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
}
