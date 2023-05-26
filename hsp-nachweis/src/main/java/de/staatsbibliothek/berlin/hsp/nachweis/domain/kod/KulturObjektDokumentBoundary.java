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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface KulturObjektDokumentBoundary {

  Optional<KulturObjektDokument> findById(String id);

  List<KulturObjektDokument> findAll();

  long count();

  void delete(Bearbeiter bearbeiter, String... kulturObjektDokumentIds) throws Exception;

  void reindexAllKulturObjektDokumente(Bearbeiter bearbeiter) throws Exception;

  Map<String, SignatureValue> getAllSignaturesWithKodIDs();

  Map<String, List<String>> getAllBeschreibungsIdsWithKodIDs();

  List<KulturObjektDokument> registrieren(Bearbeiter bearbeiter, NormdatenReferenz ort,
      NormdatenReferenz koerperschaft,
      byte[] signaturen)
      throws KulturObjektDokumentRegistrierenException, SperreAlreadyExistException;

  List<KulturObjektDokument> registrieren(Bearbeiter bearbeiter, List<KulturObjektDokument> kods)
      throws KulturObjektDokumentRegistrierenException;

  Optional<KulturObjektDokumentViewModel> buildKulturObjektDokumentViewModel(String id);

  Optional<KulturObjektDokument> digitalisatHinzufuegen(
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException, SperreAlreadyExistException;

  Optional<KulturObjektDokument> digitalisatLoeschen(
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException, SperreAlreadyExistException;

  void saveKulturObjektDokument(KulturObjektDokument kod, ActivityStreamAction action)
      throws TeiXmlTransformationException, TeiXmlValidationException, SolrUebernahmeException,
      ActivityStreamsException;

  Sperre kulturObjektDokumentSperren(Bearbeiter bearbeiter, String sperreGrund, String kulturObjektDokumentID)
      throws DokumentSperreException, SperreAlreadyExistException;

  Optional<Sperre> findSperreForKulturObjektDokument(String kulturObjektDokumentID) throws DokumentSperreException;

  void kulturObjektDokumentEntsperren(String kulturObjektDokumentID) throws DokumentSperreException;

  void addPURL(String kodId, PURL purl) throws KulturObjektDokumentException;

  List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy);

  KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id);

  KulturObjektDokument kulturObjektDokumentAktualisieren(KulturObjektDokumentViewModel kodViewModel)
      throws KulturObjektDokumentException;

  void migrateAttributsReferenzen() throws KulturObjektDokumentException;

}
