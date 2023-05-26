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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 06.08.21
 */
@QuarkusTest
class IdentifikationRepositoryAdapterTest {

  public static final String ENTITY_ID = "entityId";
  public static final String GUELTIGE_SIGNATURE = "a-007-777";
  public static final String ALTERNATIVE_SIGNATURE_1 = "alt-777-007";
  public static final String ALTERNATIVE_SIGNATURE_2 = "alt-777-008";

  @Inject
  IdentifikationRepository identifikationRepository;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Test
  @TestTransaction
  void testFindSignatureByBeschreibungID() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("123")
        .withIndentifikationen(Set.of(identifikation))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(ENTITY_ID)
        .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
        .build();

    assertNull(identifikationRepository.getSignatureByBeschreibungID(ENTITY_ID));

    beschreibungsRepository.save(beschreibung);

    SignatureValue actual = identifikationRepository.getSignatureByBeschreibungID(ENTITY_ID);
    assertNotNull(actual);

    assertEquals(GUELTIGE_SIGNATURE, actual.getSignature());

  }
  @Test
  @TestTransaction
  void testGetAllSignaturesWithKodIDs() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(ENTITY_ID)
        .withGueltigerIdentifikation(identifikation)
        .build();

    int initialSize = identifikationRepository.getAllSignaturesWithKodIDs().size();

    kulturObjektDokumentRepository.save(kulturObjektDokument);

    Map<String, SignatureValue> actual = identifikationRepository.getAllSignaturesWithKodIDs();
    assertNotNull(actual);

    assertEquals(initialSize + 1, actual.size());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());
  }

  @Test
  @TestTransaction
  void testGetGueltigeSignatureByKodID() {
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(ENTITY_ID)
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build())
        .build();

    kulturObjektDokumentRepository.save(kulturObjektDokument);

    Optional<SignatureValue> gueltigeSignatur =
        identifikationRepository.getGueltigeSignatureByKodID(ENTITY_ID);

    assertNotNull(gueltigeSignatur);
    assertTrue(gueltigeSignatur.isPresent());
    assertEquals(ENTITY_ID, gueltigeSignatur.get().getDokumentId());
    assertEquals(GUELTIGE_SIGNATURE, gueltigeSignatur.get().getSignature());
  }

  @Test
  @TestTransaction
  void testGetAlternativeSignaturesByKodID() {
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(ENTITY_ID)
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("008")
            .withIdent(ALTERNATIVE_SIGNATURE_1)
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("009")
            .withIdent(ALTERNATIVE_SIGNATURE_2)
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .build())
        .build();

    kulturObjektDokumentRepository.save(kulturObjektDokument);

    Set<SignatureValue> alternativeSignaturen = identifikationRepository.getAlternativeSignaturesByKodID(ENTITY_ID);
    assertNotNull(alternativeSignaturen);
    assertEquals(2, alternativeSignaturen.size());

    assertTrue(alternativeSignaturen.stream()
        .anyMatch(sv -> ENTITY_ID.equals(sv.getDokumentId()) && ALTERNATIVE_SIGNATURE_1.equals(sv.getSignature())));

    assertTrue(alternativeSignaturen.stream()
        .anyMatch(sv -> ENTITY_ID.equals(sv.getDokumentId()) && ALTERNATIVE_SIGNATURE_2.equals(sv.getSignature())));
  }

  @Test
  @TestTransaction
  void testGetAllSignaturesWithBeschreibungIDs() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("123")
        .withIndentifikationen(Set.of(identifikation))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(ENTITY_ID)
        .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
        .build();

    assertTrue(identifikationRepository.getAllSignaturesWithBeschreibungIDs().isEmpty());

    beschreibungsRepository.save(beschreibung);

    Map<String, SignatureValue> actual = identifikationRepository.getAllSignaturesWithBeschreibungIDs();
    assertNotNull(actual);

    assertFalse(actual.isEmpty());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());

  }
}
