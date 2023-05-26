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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.11.2019.
 * @version 1.0
 */
public class KulturObjektDokumentRepositoryTest {


  @Test
  public void testSave() {
    KulturObjektDokumentRepository repository = Mockito.mock(KulturObjektDokumentRepository.class);
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1234")
        .build();

    repository.persistAndFlush(kod);
    Mockito.when(repository.findById("1234")).thenReturn(kod);
    Assertions.assertSame(kod, repository.findById("1234"));

  }

  @Test
  public void TestfindAll() {
    KulturObjektDokumentRepository repository = Mockito.mock(KulturObjektDokumentRepository.class);
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1234")
        .build();

    repository.persistAndFlush(kod);
    Mockito.when(repository.listAll()).thenReturn(List.of(kod));
    assertEquals(1, repository.listAll().size());
  }


  @Test
  public void testfindByIdentifikationIdent() {
    KulturObjektDokumentRepository repository = Mockito.mock(KulturObjektDokumentRepository.class);
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();

    repository.persistAndFlush(kod);
    Mockito.when(repository.findByIdentifikationIdent("Mscr.Dresd.A.111")).thenReturn(List.of(kod));

    assertEquals(1, repository.findByIdentifikationIdent("Mscr.Dresd.A.111").size());
  }

  @Test
  public void testfindByBeteiligteKoerperschaftName() {

    KulturObjektDokumentRepository repository = Mockito.mock(KulturObjektDokumentRepository.class);
    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("", "Staatsbibliothek zu Berlin", ""))
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();
    Mockito.when(repository.save(kod)).thenReturn(kod);
    Mockito.when(
        repository.findByIdentifikationBeteiligeKoerperschaftName("Staatsbibliothek zu Berlin"))
        .thenReturn(Optional.of(kod));

    repository.save(kod);

    Assertions.assertNotNull(
        repository
            .findByIdentifikationBeteiligeKoerperschaftName("Staatsbibliothek zu Berlin"));
  }
}
