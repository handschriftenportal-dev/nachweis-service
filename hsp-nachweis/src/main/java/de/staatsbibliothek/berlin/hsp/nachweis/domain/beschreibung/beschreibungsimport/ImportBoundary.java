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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import java.util.List;
import java.util.Optional;
import javax.enterprise.event.Observes;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public interface ImportBoundary {

  List<BeschreibungImport> alleImporteAnzeigen();

  Optional<BeschreibungImport> findById(String id);

  void importeUebernehmen(String beschreibungImportId, String verwaltungsTyp) throws BeschreibungImportException;

  void importeAblehnen(List<String> importe) throws BeschreibungImportException;

  void selectForImport(List<String> id);

  Optional<KulturObjektDokument> ermittleKOD(Beschreibung beschreibung) throws KulturObjektDokumentNotFoundException;

  Optional<Beschreibung> checkIfBeschreibungAlreadyExists(Beschreibung beschreibung);

  List<Beschreibung> convertTEI2Beschreibung(ActivityStreamObjectDTO streamObject) throws Exception;

  void onImportMessage(@Observes @neueImporte BeschreibungImport beschreibungImportJob)
      throws BeschreibungImportException;

  boolean isAutomatischenUebernahmeAktiv();

  void setAutomatischenUebernahmeAktiv(boolean automatischenUebernahmeAktiv);
}
