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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
public interface BeschreibungsBoundary {

  Optional<Beschreibung> findById(String id);

  List<Beschreibung> findAll();

  List<BeschreibungListDTO> findLatestModified();

  long count();

  SignatureValue getSignatureForBeschreibungId(String beschreibungId);

  Map<String, SignatureValue> getAllSignaturesWithBeschreibungIDs();

  Optional<BeschreibungsViewModel> buildBeschreibungsViewModel(String id);

  void delete(Bearbeiter bearbeiter, Beschreibung... beschreibung)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException;

  void deleteBeschreibung(Bearbeiter bearbeiter, String beschreibungID)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException;

  void updateBeschreibungMitXML(String id, String xml, Locale locale) throws BeschreibungsException;

  ValidationResult validateXML(String xmlTEI, String schema, String locale);

  Beschreibung beschreibungErstellen(Bearbeiter bearbeiter, String kodid, NormdatenReferenz sprache,
      Set<NormdatenReferenz> autoren, Lizenz lizenz, String template,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary) throws BeschreibungsException;

  Sperre beschreibungSperren(Bearbeiter bearbeiter, String beschreibungsID)
      throws DokumentSperreException;

  Optional<Sperre> findSperreForBeschreibung(String beschreibungsID) throws DokumentSperreException;

  void beschreibungEntsperren(String beschreibungsID) throws DokumentSperreException;

  void reindexAllBeschreibungen(Bearbeiter bearbeiter) throws Exception;

  void addPURL(String beschreibungId, PURL purl) throws BeschreibungsException;

  List<BeschreibungListDTO> findFilteredBeschreibungListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredBeschreibungListDTOs(Map<String, String> filterBy);

  BeschreibungListDTO findBeschreibungListDTOId(String id);

}
