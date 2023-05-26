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

package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */
public class KatalogDirector {

  private KatalogBuilder builder;

  public KatalogDirector(
      KatalogBuilder builder) {
    this.builder = builder;
  }

  public Katalog createSimpleHSKKatalog(String title, String teiXML, String publikationsJahr,
      String hskID, NormdatenReferenz verlag) {

    String id = UUID.randomUUID().toString();

    if (hskID != null && !hskID.isEmpty()) {
      id = UUID.nameUUIDFromBytes(hskID.getBytes()).toString();
    }

    return this.builder.withId(id)
        .withHSKID(hskID)
        .withTitle(title).withTEIXML(teiXML)
        .withErstelldatum(LocalDateTime.now())
        .withAenderungsdatum(LocalDateTime.now())
        .withPublikationsJahr(publikationsJahr)
        .withVerlag(verlag)
        .build();
  }
}
