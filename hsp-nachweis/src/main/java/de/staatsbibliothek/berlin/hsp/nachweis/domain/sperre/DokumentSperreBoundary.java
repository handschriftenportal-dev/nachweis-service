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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import java.util.List;
import java.util.Set;

/**
 * Provide access to document lock system
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public interface DokumentSperreBoundary {

  Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException;

  default Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
      KulturObjektDokument... kulturObjektDokumente)
      throws DokumentSperreException, SperreAlreadyExistException {
    return acquireSperre(bearbeiter, sperreTyp, sperreGrund,
        createSperreEintraege(kulturObjektDokumente).toArray(new SperreEintrag[0]));
  }

  default Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
      Beschreibung... beschreibungen)
      throws DokumentSperreException, SperreAlreadyExistException {
    return acquireSperre(bearbeiter, sperreTyp, sperreGrund,
        createSperreEintraege(beschreibungen).toArray(new SperreEintrag[0]));
  }

  boolean releaseSperre(Sperre sperreToRelease) throws DokumentSperreException;

  List<Sperre> findConflictSperrenForTransaction(String transactionId,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException;

  List<Sperre> findConflictSperrenForBearbeiter(String bearbeitername,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException;

  List<Sperre> findBySperreEintraege(SperreEintrag... sperreEintraege)
      throws DokumentSperreException;

  default boolean isGesperrt(SperreEintrag... sperreEintraege) throws DokumentSperreException {
    return !findBySperreEintraege(sperreEintraege).isEmpty();
  }

  List<Sperre> findAll() throws DokumentSperreException;

  Set<SperreEintrag> createSperreEintraege(Beschreibung... beschreibungen);

  Set<SperreEintrag> createSperreEintraege(KulturObjektDokument... kulturObjektDokumente);

  Set<SperreEintrag> createSperreEintraege(Set<KulturObjektDokument> kulturObjektDokumente,
      Set<Beschreibung> beschreibungen);
}
