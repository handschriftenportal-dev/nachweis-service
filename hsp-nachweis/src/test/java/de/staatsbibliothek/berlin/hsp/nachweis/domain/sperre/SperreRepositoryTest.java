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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test for SperreRepository
 *
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 02.11.2021
 */
@QuarkusTest
@TestTransaction
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class SperreRepositoryTest {

  private final String TRANSACTION_SPERRE_ID = "Sperre_Transaction";
  private final String MANUAL_SPERRE_ID = "Sperre_MANUAL";

  private final String TX_ID = "TX_ID_1";

  private final Set<SperreEintrag> GESPERRT_TRANSACTION = Set.of(
      new SperreEintrag("KOD_1", KOD),
      new SperreEintrag("BES_1_1", BESCHREIBUNG));

  private final Set<SperreEintrag> GESPERRT_MANUAL = Set.of(
      new SperreEintrag("KOD_2", KOD),
      new SperreEintrag("BES_2_1", BESCHREIBUNG));

  private final Set<SperreEintrag> GESPERRT_TRANSACTION_ONLY_KOD = Set.of(
      new SperreEintrag("KOD_1", KOD),
      new SperreEintrag("BES_1_2", BESCHREIBUNG));

  private final Set<SperreEintrag> GESPERRT_MANUAL_ONLY_KOD = Set.of(
      new SperreEintrag("KOD_2", KOD),
      new SperreEintrag("BES_2_2", BESCHREIBUNG));

  private final Set<SperreEintrag> NICHT_GESPERRT = Set.of(
      new SperreEintrag("KOD_3", KOD),
      new SperreEintrag("BES_3_1", BESCHREIBUNG));

  private final Bearbeiter unbekannt = new Bearbeiter("1", "Unbekannter Benutzer");

  @Inject
  SperreRepository sperreRepository;
  @Inject
  BearbeiterRepository bearbeiterRepository;

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void setup() {
    bearbeiterRepository.saveAndFlush(unbekannt);

    Sperre sperreTransaction = Sperre.newBuilder()
        .withId(TRANSACTION_SPERRE_ID)
        .withBearbeiter(unbekannt)
        .withTxId(TX_ID)
        .withSperreGrund("Unit-Test")
        .withSperreTyp(SperreTyp.IN_TRANSACTION)
        .withStartDatum(LocalDateTime.now())
        .withSperreEintraege(GESPERRT_TRANSACTION)
        .build();

    sperreRepository.saveAndFlush(sperreTransaction);

    Sperre sperreManual = Sperre.newBuilder()
        .withId(MANUAL_SPERRE_ID)
        .withBearbeiter(unbekannt)
        .withSperreGrund("Unit-Test")
        .withSperreTyp(SperreTyp.MANUAL)
        .withStartDatum(LocalDateTime.now())
        .withSperreEintraege(GESPERRT_MANUAL)
        .build();

    sperreRepository.saveAndFlush(sperreManual);
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void cleanup() {
    sperreRepository.deleteByIdAndFlush(TRANSACTION_SPERRE_ID);
    sperreRepository.deleteByIdAndFlush(MANUAL_SPERRE_ID);
    bearbeiterRepository.deleteByIdAndFlush(unbekannt.getId());
  }

  @Test
  @Transactional
  void testFindByBearbeiter() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findByBearbeiter(unbekannt.getBearbeitername());
    assertNotNull(sperren_1);
    assertEquals(1, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findByBearbeiter("none");
    assertNotNull(sperren_2);
    assertEquals(0, sperren_2.size());
  }

  @Test
  @Transactional
  void testFindByTransactionId() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findByTransactionId(TX_ID);
    assertNotNull(sperren_1);
    assertEquals(1, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findByTransactionId("any");
    assertNotNull(sperren_2);
    assertEquals(0, sperren_2.size());
  }

  @Test
  @Transactional
  void testFindBySperreEintraege() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findBySperreEintraege(
        GESPERRT_MANUAL.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_1);
    assertEquals(1, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findBySperreEintraege(
        GESPERRT_TRANSACTION.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_2);
    assertEquals(1, sperren_2.size());

    Collection<Sperre> sperren_3 = sperreRepository.findBySperreEintraege(
        GESPERRT_MANUAL_ONLY_KOD.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_3);
    assertEquals(1, sperren_3.size());

    Collection<Sperre> sperren_4 = sperreRepository.findBySperreEintraege(
        GESPERRT_TRANSACTION_ONLY_KOD.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_4);
    assertEquals(1, sperren_4.size());

    Collection<Sperre> sperren_5 = sperreRepository.findBySperreEintraege(
        NICHT_GESPERRT.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_5);
    assertEquals(0, sperren_5.size());
  }

  @Test
  @Transactional
  void testFindConflictSperrenForBearbeiter_self() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findConflictSperrenForBearbeiter(
        unbekannt.getBearbeitername(), GESPERRT_MANUAL.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_1);
    assertEquals(0, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findConflictSperrenForBearbeiter(
        unbekannt.getBearbeitername(),
        GESPERRT_MANUAL_ONLY_KOD.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_2);
    assertEquals(0, sperren_2.size());

    Collection<Sperre> sperren_3 = sperreRepository.findConflictSperrenForBearbeiter(
        unbekannt.getBearbeitername(), NICHT_GESPERRT.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_3);
    assertEquals(0, sperren_3.size());

    Collection<Sperre> sperren_4 = sperreRepository.findConflictSperrenForBearbeiter(
        unbekannt.getBearbeitername(),
        GESPERRT_TRANSACTION.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_4);
    assertEquals(1, sperren_4.size());
  }

  @Test
  @Transactional
  void testFindConflictSperrenForBearbeiter_other() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findConflictSperrenForBearbeiter(
        "other", GESPERRT_MANUAL.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_1);
    assertEquals(1, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findConflictSperrenForBearbeiter(
        "other", GESPERRT_MANUAL_ONLY_KOD.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_2);
    assertEquals(1, sperren_2.size());

    Collection<Sperre> sperren_3 = sperreRepository.findConflictSperrenForBearbeiter(
        "other", NICHT_GESPERRT.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_3);
    assertEquals(0, sperren_3.size());

    Collection<Sperre> sperren_4 = sperreRepository.findConflictSperrenForBearbeiter(
        "other",
        GESPERRT_TRANSACTION.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_4);
    assertEquals(1, sperren_4.size());
  }

  @Test
  @Transactional
  void testFindConflictSperrenForTransaction_self() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findConflictSperrenForTransaction(
        TX_ID,
        GESPERRT_TRANSACTION.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_1);
    assertEquals(0, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findConflictSperrenForTransaction(
        TX_ID,
        GESPERRT_TRANSACTION_ONLY_KOD.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_2);
    assertEquals(0, sperren_2.size());

    Collection<Sperre> sperren_3 = sperreRepository.findConflictSperrenForTransaction(
        TX_ID,
        NICHT_GESPERRT.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_3);
    assertEquals(0, sperren_3.size());

    Collection<Sperre> sperren_4 = sperreRepository.findConflictSperrenForTransaction(
        TX_ID,
        GESPERRT_MANUAL.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_4);
    assertEquals(1, sperren_4.size());
  }

  @Test
  @Transactional
  void testFindConflictSperrenForTransaction_other() throws Exception {
    Collection<Sperre> sperren_1 = sperreRepository.findConflictSperrenForTransaction(
        "other",
        GESPERRT_TRANSACTION.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_1);
    assertEquals(1, sperren_1.size());

    Collection<Sperre> sperren_2 = sperreRepository.findConflictSperrenForTransaction(
        "other",
        GESPERRT_TRANSACTION_ONLY_KOD.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_2);
    assertEquals(1, sperren_2.size());

    Collection<Sperre> sperren_3 = sperreRepository.findConflictSperrenForTransaction(
        "other",
        NICHT_GESPERRT.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_3);
    assertEquals(0, sperren_3.size());

    Collection<Sperre> sperren_4 = sperreRepository.findConflictSperrenForTransaction(
        "other",
        GESPERRT_MANUAL.toArray(new SperreEintrag[0]));
    assertNotNull(sperren_4);
    assertEquals(1, sperren_4.size());
  }

}
