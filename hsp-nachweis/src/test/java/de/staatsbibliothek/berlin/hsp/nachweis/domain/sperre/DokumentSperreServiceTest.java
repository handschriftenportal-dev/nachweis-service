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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.09.21
 */
@QuarkusTest
@TestTransaction
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class DokumentSperreServiceTest extends SperreCommon {

  @Inject
  DokumentSperreBoundary boundary;

  @Inject
  BearbeiterRepository bearbeiterRepository;

  Bearbeiter michael = new Bearbeiter("2", "Michael");
  Bearbeiter konrad = new Bearbeiter("3", "Konrad");

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void setup() {
    Arrays.asList(michael, konrad).forEach(b -> bearbeiterRepository.saveAndFlush(b));
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void cleanup() {
    Arrays.asList(michael, konrad)
        .forEach(b -> bearbeiterRepository.deleteByIdAndFlush(b.getId()));
  }

  @Test
  void testAcuireManualSperreWithoutBearbeiter() {
    assertThrows(DokumentSperreException.class, () -> {
      boundary.acquireSperre(null, SperreTyp.MANUAL, "Test",
          new SperreEintrag[0]);
    });
  }

  @ParameterizedTest
  @Transactional()
  @ValueSource(strings = {"1", "2"})
  void testAcuireManuelSperreWithConflict(String bearbeiterID) throws DokumentSperreException {
    BearbeiterBoundary bearbeiterBoundary = mock((BearbeiterBoundary.class));
    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(bearbeiterRepository.findById(bearbeiterID));
    final SperreEintrag sperreEintrag = new SperreEintrag("1",
        SperreDokumentTyp.BESCHREIBUNG);

    Sperre alreadyExistingSperre = new Sperre.SperreBuilder()
        .withSperreTyp(SperreTyp.MANUAL)
        .withBearbeiter(konrad)
        .withSperreGrund("Test")
        .addSperreEintrag(sperreEintrag)
        .build();

    ((DokumentSperreService) boundary).saveSperreInNewTransaction(alreadyExistingSperre);

    if ("2".equals(bearbeiterID)) {
      assertThrows(SperreAlreadyExistException.class, () -> {
        boundary.acquireSperre(michael, SperreTyp.MANUAL, "Test",
            Arrays.asList(sperreEintrag).toArray(new SperreEintrag[1]));
      });
    } else {

      Sperre newSperre = boundary.acquireSperre(konrad,
          SperreTyp.MANUAL, "Test",
          Arrays.asList(sperreEintrag).toArray(new SperreEintrag[1]));
      assertNotNull(newSperre);

      assertEquals(alreadyExistingSperre, newSperre);
    }

    ((DokumentSperreService) boundary).deleteSperreInNewTransaction(alreadyExistingSperre.getId());
  }


  @Test
  @Transactional
  void testAcquireReleaseSperreManual()
      throws SperreAlreadyExistException, DokumentSperreException {
    Sperre sperre = getTestSperre(bearbeiterRepository, SperreTyp.MANUAL);

    ((DokumentSperreService) boundary).saveSperreInNewTransaction(sperre);

    boundary.releaseSperre(sperre);

    assertTrue(boundary.findAll().stream().filter(s -> s.getId().equals(sperre.getId())).findFirst()
        .isEmpty());

  }

  @RepeatedTest(10)
  @Transactional()
  void testAcquireReleaseSperreInTransaction()
      throws SperreAlreadyExistException, DokumentSperreException {
    Sperre sperre = getTestSperre(bearbeiterRepository, SperreTyp.IN_TRANSACTION);
    ArrayList<SperreEintrag> sperreEintraege = new ArrayList<>(sperre.getSperreEintraege());

    assertFalse(boundary.isGesperrt(sperreEintraege.get(0)));

    Sperre acquiredSperre = boundary.acquireSperre(michael,
        sperre.getSperreTyp(), "Unittest",
        sperreEintraege.toArray(new SperreEintrag[0]));

    assertNotNull(acquiredSperre);

    assertTrue(boundary.isGesperrt(sperreEintraege.get(0)));
  }
}
