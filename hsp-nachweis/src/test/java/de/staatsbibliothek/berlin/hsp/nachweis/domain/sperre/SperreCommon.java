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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp.BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp.KOD;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre.SperreBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import java.time.LocalDateTime;
import java.util.Random;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.09.21
 */
public class SperreCommon {

  public static final String USER_NAME = "b-pc101";

  @Transactional(TxType.REQUIRES_NEW)
  Sperre getTestSperre(BearbeiterRepository bearbeiterRepository, SperreTyp sperreTyp) {
    int start = new Random().nextInt();
    String uniqueUserName = USER_NAME;
    Bearbeiter bearbeiter = bearbeiterRepository.findById(uniqueUserName);
    if (bearbeiter == null) {
      bearbeiter = new Bearbeiter(uniqueUserName, uniqueUserName);
      bearbeiter = bearbeiterRepository.save(bearbeiter);
    }

    SperreBuilder sperreBuilder = Sperre.newBuilder()
        .withBearbeiter(bearbeiter)
        .withSperreGrund("Import")
        .withSperreTyp(sperreTyp)
        .withStartDatum(LocalDateTime.now());

    for (int i = start, k = start + 10; i < k; i++) {
      sperreBuilder.addSperreEintrag(new SperreEintrag("ckm-" + i, (i % 2 == 0) ? BESCHREIBUNG : KOD));
    }
    return sperreBuilder.build();
  }

}
