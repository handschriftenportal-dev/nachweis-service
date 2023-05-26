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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 *
 * @see SuchDokument
 */
class SuchDokumentTest {

  @Test
  void testBuilder() {
    SuchDokument suchDokument = SuchDokument.builder()
        .withId("HSP-123")
        .withSignatur("Sig. 123")
        .withSuchDokumentTyp(SuchDokumentTyp.KOD)
        .withTitel("titel")
        .withSichtbarkeit("sichtbarkeit")
        .withVerwaltungsTyp("verwaltungstyp")
        .withBearbeiter(List.of("b1", "b2"))
        .addBearbeiter("b3")
        .withBestandhaltendeInstitutionName("iName")
        .withBestandhaltendeInstitutionOrt("iOrt")
        .withJahrDerPublikation(2022)
        .withLastUpdate(LocalDateTime.of(2022, 12, 29, 0, 0))
        .withContainsDigitalisat(Boolean.TRUE)
        .withContainsBuchschmuck(Boolean.FALSE)
        .withSearchableValues("sv")
        .withAutoren(List.of("a1", "a2"))
        .addAutor("a3")
        .withContainsBeschreibung(Boolean.TRUE)
        .withPubliziert(Boolean.FALSE)
        .build();

    assertNotNull(suchDokument);
    assertEquals("HSP-123", suchDokument.getId());
    assertEquals("Sig. 123", suchDokument.getSignatur());
    assertEquals("KOD", suchDokument.getSuchDokumentTyp());
    assertEquals("titel", suchDokument.getTitel());
    assertEquals("sichtbarkeit", suchDokument.getSichtbarkeit());
    assertEquals("verwaltungstyp", suchDokument.getVerwaltungsTyp());
    assertNotNull(suchDokument.getBearbeiter());
    assertEquals(3, suchDokument.getBearbeiter().size());
    assertEquals("b1/b2/b3", String.join("/", suchDokument.getBearbeiter()));
    assertEquals("iName", suchDokument.getBestandhaltendeInstitutionName());
    assertEquals("iName", suchDokument.getBestandhaltendeInstitutionNameFacette());
    assertEquals("iOrt", suchDokument.getBestandhaltendeInstitutionOrt());
    assertEquals(2022, suchDokument.getJahrDerPublikation());
    assertEquals("2022-12-29T00:00:00Z", suchDokument.getLastUpdate());
    assertEquals(Boolean.TRUE, suchDokument.getContainsDigitalisat());
    assertEquals(Boolean.FALSE, suchDokument.getContainsBuchschmuck());
    assertEquals("sv", suchDokument.getSearchableValues());
    assertNotNull(suchDokument.getAutoren());
    assertEquals(3, suchDokument.getAutoren().size());
    assertEquals("a1/a2/a3", String.join("/", suchDokument.getAutoren()));
    assertEquals(Boolean.TRUE, suchDokument.getContainsBeschreibung());
    assertEquals(Boolean.FALSE, suchDokument.getPubliziert());

    assertEquals("SuchDokument{id='HSP-123', suchDokumentTyp=KOD, signatur='Sig. 123', title='titel', "
            + "sichtbarkeit='sichtbarkeit', verwaltungsTyp='verwaltungstyp', bearbeiter='b1;b2;b3', "
            + "bestandhaltendeInstitutionName='iName', bestandhaltendeInstitutionOrt='iOrt', "
            + "bestandhaltendeInstitutionNameFacette='iName', jahrDerPublikation=2022, lastUpdate=2022-12-29T00:00:00Z, "
            + "containsDigitalisat=true, containsBuchschmuck=false, containsBeschreibung=true, publiziert=false, "
            + "autoren=a1;a2;a3}",
        suchDokument.toString());
  }

  @Test
  void testBuilder_empty() {
    SuchDokument suchDokument = SuchDokument.builder().build();

    assertNotNull(suchDokument);
    assertNull(suchDokument.getId());
    assertNull(suchDokument.getSignatur());
    assertNull(suchDokument.getSuchDokumentTyp());
    assertNull(suchDokument.getTitel());
    assertNull(suchDokument.getSichtbarkeit());
    assertNull(suchDokument.getVerwaltungsTyp());
    assertNotNull(suchDokument.getBearbeiter());
    assertEquals(0, suchDokument.getBearbeiter().size());
    assertNull(suchDokument.getBestandhaltendeInstitutionName());
    assertNull(suchDokument.getBestandhaltendeInstitutionNameFacette());
    assertNull(suchDokument.getBestandhaltendeInstitutionOrt());
    assertNull(suchDokument.getJahrDerPublikation());
    assertNull(suchDokument.getLastUpdate());
    assertNull(suchDokument.getContainsDigitalisat());
    assertNull(suchDokument.getContainsBuchschmuck());
    assertNull(suchDokument.getSearchableValues());
    assertNotNull(suchDokument.getAutoren());
    assertEquals(0, suchDokument.getAutoren().size());
    assertNull(suchDokument.getContainsBeschreibung());
    assertNull(suchDokument.getPubliziert());

    assertEquals("SuchDokument{id='null', suchDokumentTyp=null, signatur='null', title='null', "
            + "sichtbarkeit='null', verwaltungsTyp='null', bearbeiter='', bestandhaltendeInstitutionName='null', "
            + "bestandhaltendeInstitutionOrt='null', bestandhaltendeInstitutionNameFacette='null', "
            + "jahrDerPublikation=null, lastUpdate=null, containsDigitalisat=null, containsBuchschmuck=null, "
            + "containsBeschreibung=null, publiziert=null, autoren=}",
        suchDokument.toString());
  }


}
