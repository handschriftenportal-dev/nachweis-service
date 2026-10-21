package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

  @Inject
  EntityManager entityManager;

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
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder(ENTITY_ID, "kodId", identifikation)
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

    Set<SignatureValue> alternativeSignaturen = identifikationRepository.getAlternativeSignaturesByKodID(
        ENTITY_ID);
    assertNotNull(alternativeSignaturen);
    assertEquals(2, alternativeSignaturen.size());

    assertTrue(alternativeSignaturen.stream()
        .anyMatch(sv -> ENTITY_ID.equals(sv.getDokumentId()) && ALTERNATIVE_SIGNATURE_1.equals(
            sv.getSignature())));

    assertTrue(alternativeSignaturen.stream()
        .anyMatch(sv -> ENTITY_ID.equals(sv.getDokumentId()) && ALTERNATIVE_SIGNATURE_2.equals(
            sv.getSignature())));
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
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder(ENTITY_ID, "kodId", identifikation)
        .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
        .build();

    assertTrue(identifikationRepository.getAllSignaturesWithBeschreibungIDs().isEmpty());

    beschreibungsRepository.save(beschreibung);

    Map<String, SignatureValue> actual = identifikationRepository.getAllSignaturesWithBeschreibungIDs();
    assertNotNull(actual);

    assertFalse(actual.isEmpty());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());

  }

  @Test
  @TestTransaction
  void testAltIdentifikationIsDeletable() {
    String query = """      
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID1', 'Signatur1', 'GUELTIGE_SIGNATUR', NULL);
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID2', 'Signatur1', 'GUELTIGE_SIGNATUR', NULL);
        INSERT INTO kulturobjektdokument (dtype, ID, gueltigeidentifikation_id) VALUES ('KulturObjektDokument', 'kod1', 'ID1');
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID3', 'Signatur2', 'ALTSIGNATUR', 'kod1');
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID4', 'Signatur3', 'ALTSIGNATUR', 'kod1');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd1', 'ID1');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd2', 'ID2');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd3', 'ID3');
        """;
    entityManager.createNativeQuery(query).executeUpdate();

    Optional.of(identifikationRepository.findById("ID2")).ifPresentOrElse(identifikation ->
            assertTrue(
                identifikationRepository.isAltIdentifikationDeletable(identifikation.getId())),
        () -> fail("Identifikation not found")
    );

  }

  @Test
  @TestTransaction
  void testAltIdentifikationIsNotDeletable() {
    String query = """      
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID1', 'Signatur1', 'GUELTIGE_SIGNATUR', NULL);
        INSERT INTO kulturobjektdokument (dtype, ID, gueltigeidentifikation_id) VALUES ('KulturObjektDokument', 'kod1', 'ID1');
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID2', 'Signatur2', 'ALTSIGNATUR', 'kod1');
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID3', 'Signatur3', 'ALTSIGNATUR', 'kod1');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd1', 'ID1');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd2', 'ID2');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd3', 'ID3');
        """;
    entityManager.createNativeQuery(query).executeUpdate();

    Optional.of(identifikationRepository.findById("ID2")).ifPresentOrElse(identifikation ->
            assertFalse(
                identifikationRepository.isAltIdentifikationDeletable(identifikation.getId())),
        () -> fail("Identifikation not found")
    );

  }

  @Test
  @TestTransaction
  void testAltIdentifikationNotDeletable() {
    String query = """      
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID1', 'Signatur1', 'GUELTIGE_SIGNATUR', NULL);
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID2', 'Signatur2', 'GUELTIGE_SIGNATUR', NULL);
        INSERT INTO kulturobjektdokument (dtype, ID, gueltigeidentifikation_id) VALUES ('KulturObjektDokument', 'kod1', 'ID1');
        INSERT INTO identifikation (ID, ident, identtyp, alternativeidentifikationen_id) VALUES ('ID3', 'Signatur2', 'ALTSIGNATUR', 'kod1');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd1', 'ID1');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd2', 'ID2');
        INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd3', 'ID3');
        """;
    entityManager.createNativeQuery(query).executeUpdate();

    Optional.of(identifikationRepository.findById("ID3")).ifPresentOrElse(identifikation ->
            assertFalse(
                identifikationRepository.isAltIdentifikationDeletable(identifikation.getId())),
        () -> fail("Identifikation not found")
    );
  }

  @Test
  @TestTransaction
  void testIdentifikationJpaRoundTrip_withSortKey() {

    Identifikation ident =
        new IdentifikationBuilder()
            .withIdent("MS 999")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withSortKey("ms_00999")
            .build();

    identifikationRepository.persist(ident);
    entityManager.flush();
    entityManager.clear();

    Identifikation reloaded =
        identifikationRepository.findById(ident.getId());

    assertNotNull(reloaded);
    assertEquals("ms_00999", reloaded.getSortKey());
  }

  @Test
  @TestTransaction
  void testIdentifikationJpaRoundTrip_blankSortKey_shouldBeNull() {

    Identifikation ident =
        new IdentifikationBuilder()
            .withIdent("MS 998")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withSortKey("   ")
            .build();

    identifikationRepository.persist(ident);
    entityManager.flush();
    entityManager.clear();

    Identifikation reloaded =
        identifikationRepository.findById(ident.getId());

    assertNotNull(reloaded);
    assertNull(reloaded.getSortKey());
  }
}
