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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

@QuarkusTest
public class BestandsRepositoryAdapterTest {

  @Inject
  BestandsRepository bestandsRepository;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Test
  void testCreation() {

    assertNotNull(bestandsRepository);
    assertNotNull(((BestandsRepositoryAdapter)bestandsRepository).getEntityManager());
  }

  @Test
  @TestTransaction
  void testFindAllBestaende() {

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>").build();
    kulturObjektDokumentRepository.save(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withKodID(kod.getId())
        .withKatalog("kat-1")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN).build();
    beschreibungsRepository.save(beschreibung);

    BestandNachweisDTO bestandNachweisDTO = bestandsRepository.findAllBestaende();

    assertNotNull(bestandNachweisDTO);
    assertEquals(1, bestandNachweisDTO.getAnzahlKOD());
    assertEquals(1, bestandNachweisDTO.getAnzahlBeschreibungen());
    assertEquals(0, bestandNachweisDTO.getAnzahlDigitalisate());
  }
}
