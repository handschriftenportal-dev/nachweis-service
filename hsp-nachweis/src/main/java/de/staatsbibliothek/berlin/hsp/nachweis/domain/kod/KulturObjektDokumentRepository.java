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
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.11.2019.
 */
public interface KulturObjektDokumentRepository extends
    CRUDRepository<KulturObjektDokument, String> {

  KulturObjektDokument findByBeschreibung(String beschreibungId);

  default Optional<KulturObjektDokument> findByBeschreibungOptional(String beschreibungId) {
    return Optional.ofNullable(findByBeschreibung(beschreibungId));
  }

  List<KulturObjektDokument> findByIdentifikationIdent(String ident);

  List<KulturObjektDokument> findByAlternativeIdentifikationIdent(String ident);

  Optional<KulturObjektDokument> findByIdentifikationBeteiligeKoerperschaftName(String name);

  List<KulturObjektDokument> findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(String ident,
      String besitzerID, String aufbewahrungsOrtID);

  Optional<KulturObjektDokument> findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(String ident,
      String besitzerName, String aufbewahrungsOrtName);

  Optional<KulturObjektDokument> findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
      String ident, String besitzerName, String aufbewahrungsOrtName);

  Map<String, List<String>> getAllBeschreibungsIdsWithKodIDs();

  List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy);

  KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id);

  default Optional<KulturObjektDokumentListDTO> findKulturObjektDokumentListDTOByIdOptional(String id) {
    return Optional.ofNullable(findKulturObjektDokumentListDTOById(id));
  }
}
