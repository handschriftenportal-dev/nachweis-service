package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test basic functionality of the NormdatenReferenzRepository
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.12.20
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class NormdatenReferenzRepositoryTest {

  private static final ZoneId ZONE_ID = ZoneId.systemDefault();
  final String xml4469;
  final String xmlacd5;
  final String xml19ad;

  @Inject
  EntityManager em;
  @Inject
  NormdatenReferenzRepository normdatenReferenzRepository;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  public NormdatenReferenzRepositoryTest() throws URISyntaxException, IOException {
    URL url4469 =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469.xml");
    xml4469 = Files.readString(Paths.get(Objects.requireNonNull(url4469).toURI()));
    URL urlacd5 =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5.xml");
    xmlacd5 = Files.readString(Paths.get(Objects.requireNonNull(urlacd5).toURI()));
    URL url19ad =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad.xml");
    xml19ad = Files.readString(Paths.get(Objects.requireNonNull(url19ad).toURI()));
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void cleanUp() {
    em.createQuery("DELETE FROM KulturObjektDokument").executeUpdate();
    em.createQuery("DELETE FROM Identifikation").executeUpdate();
    em.createQuery("DELETE FROM NormdatenReferenz").executeUpdate();
  }

  @Test
  @TestTransaction
  void testFindByNameOptional() {
    NormdatenReferenz referenz =
        new NormdatenReferenz(UUID.randomUUID().toString(), "Piotr Czarnecki", "DE-007");
    normdatenReferenzRepository.save(referenz);
    assertTrue(normdatenReferenzRepository.findByNameOptional("Piotr Czarnecki").isPresent());
    assertFalse(normdatenReferenzRepository.findByNameOptional("Konrad").isPresent());
    normdatenReferenzRepository.deleteByIdAndFlush(referenz.getId());
  }

  @Test
  @TestTransaction
  void findByGndIDOptional() {
    NormdatenReferenz referenz =
        new NormdatenReferenz(UUID.randomUUID().toString(), "Piotr Czarnecki", "DE-007");
    normdatenReferenzRepository.save(referenz);
    assertTrue(normdatenReferenzRepository.findByGndIDOptional("DE-007").isPresent());
    assertFalse(normdatenReferenzRepository.findByGndIDOptional("PL-007").isPresent());
    normdatenReferenzRepository.deleteByIdAndFlush(referenz.getId());
  }

  @Test
  @TestTransaction
  void findByIdOrIdentifikatorOptional() {
    NormdatenReferenz original = createNormdatenReferenzGerman();
    normdatenReferenzRepository.save(original);

    Optional<NormdatenReferenz> found = normdatenReferenzRepository.findByIdOrIdentifikatorOptional(
        "ger");
    assertTrue(found.isPresent());

    assertEquals(original.getId(), found.get().getId());
    assertEquals(original.getName(), found.get().getName());
    assertEquals(original.getTypeName(), found.get().getTypeName());
    assertEquals(original.getGndID(), found.get().getGndID());
    assertNotNull(found.get().getVarianterName());
    assertEquals(original.getVarianterName().size(), found.get().getVarianterName().size());
    assertNotNull(found.get().getIdentifikator());
    assertEquals(original.getIdentifikator().size(), found.get().getIdentifikator().size());

    assertFalse(normdatenReferenzRepository.findByIdOrIdentifikatorOptional("DE-008").isPresent());
  }

  @Test
  @TestTransaction
  void findByCorporateBody() {
    createTestData();
    var fromDB = normdatenReferenzRepository.findReferencedCorporateBodies();
    assertEquals(2, fromDB.size());
  }

  private List<NormdatenReferenz> createKoerperschaften() {
    return List.of(
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-1")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .withName("Deutsche Nationalbibliothek")
            .build(),
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-2")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .withName("Staatsbibliothek zu Berlin")
            .build());
  }

  private NormdatenReferenz createNormdatenReferenzGerman() {
    return new NormdatenReferenz.NormdatenReferenzBuilder()
        .withId(UUID.nameUUIDFromBytes("de".getBytes()).toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addIdentifikator(
            new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("de", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .withGndID("4113292-0")
        .build();
  }

  @Transactional(TxType.REQUIRES_NEW)
  void createTestData() {
    var normdatumBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-1")
            .withName("Normdatum1")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .build();
    var identifikation =
        new Identifikation.IdentifikationBuilder()
            .withId("ID1")
            .withBesitzer(normdatumBesitzer)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    var kod =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withRegistrierungsDatum(
                LocalDateTime.ofInstant(Instant.parse("2020-07-01T00:00:00Z"), ZONE_ID))
            .withId("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .withTEIXml(xml4469)
            .withGueltigerIdentifikation(identifikation)
            .build();

    var identifikation2 =
        new Identifikation.IdentifikationBuilder()
            .withId("ID2")
            .withBesitzer(normdatumBesitzer)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    var kod2 =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withRegistrierungsDatum(
                LocalDateTime.ofInstant(Instant.parse("2020-05-01T00:00:00Z"), ZONE_ID))
            .withId("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5")
            .withTEIXml(xmlacd5)
            .withGueltigerIdentifikation(identifikation2)
            .build();

    var normdatumBesitzer3 =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-3")
            .withName("Normdatum3")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .build();
    var identifikation3 =
        new Identifikation.IdentifikationBuilder()
            .withId("ID3")
            .withBesitzer(normdatumBesitzer3)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    var kod3 =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withRegistrierungsDatum(
                LocalDateTime.ofInstant(Instant.parse("2020-03-01T00:00:00Z"), ZONE_ID))
            .withId("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad")
            .withTEIXml(xml19ad)
            .withGueltigerIdentifikation(identifikation3)
            .build();
    var kods = new ArrayList<KulturObjektDokument>();
    kods.add(kod);
    kods.add(kod2);
    kods.add(kod3);
    kods.forEach(kulturObjektDokumentRepository::persist);
    var normdatumBesitzer4 =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-4")
            .withName("Normdatum4")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .build();
    normdatenReferenzRepository.persist(normdatumBesitzer4);
  }
}
