package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument_;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation_;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.query.sqm.ComparisonOperator;
import org.hibernate.query.sqm.tree.predicate.SqmComparisonPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class OaiPmhRepositoryAdapterTest {

  private static final ZoneId ZONE_ID = ZoneId.systemDefault();
  final String xml4469;
  final String xmlacd5;
  final String xml19ad;
  @Inject
  OaiPmhRepositoryAdapter repository;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  EntityManager em;

  public OaiPmhRepositoryAdapterTest() throws URISyntaxException, IOException {
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

  @Test
  void getPredicatesArrayEmpty() {
    var predicates =
        repository.getPredicatesArray(
            Optional.empty(), Optional.empty(), Optional.empty(), null, null, null);
    assertEquals(0, predicates.length);
  }

  @Test
  void getPredicatesArray() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    Root<KulturObjektDokument> kodRoot =
        cb.createQuery(KulturObjektDokument.class).from(KulturObjektDokument.class);
    Join<KulturObjektDokument, Identifikation> identifikationJoin =
        kodRoot.join(KulturObjektDokument_.gueltigeIdentifikation);
    Join<Identifikation, NormdatenReferenz> normdatenReferenzJoin =
        identifikationJoin.join(Identifikation_.BESITZER);

    Optional<Instant> from = Optional.of(Instant.parse("2000-01-01T00:00:00Z"));
    Optional<Instant> until = Optional.of(Instant.parse("2000-01-01T00:00:00Z"));
    Optional<String> set = Optional.of("HSP-SET");

    var predicates =
        repository.getPredicatesArray(
            from, until, Optional.empty(), cb, kodRoot, normdatenReferenzJoin);

    assertEquals(2, predicates.length);
    assertInstanceOf(SqmComparisonPredicate.class, predicates[0]);
    assertInstanceOf(SqmComparisonPredicate.class, predicates[1]);
    assertEquals(
        ComparisonOperator.GREATER_THAN_OR_EQUAL,
        ((SqmComparisonPredicate) predicates[0]).getSqmOperator());
    assertEquals(
        ComparisonOperator.LESS_THAN_OR_EQUAL,
        ((SqmComparisonPredicate) predicates[1]).getSqmOperator());

    var predicatesWithSet =
        repository.getPredicatesArray(from, until, set, cb, kodRoot, normdatenReferenzJoin);
    assertEquals(3, predicatesWithSet.length);
    assertInstanceOf(SqmComparisonPredicate.class, predicatesWithSet[2]);
    assertEquals(
        ComparisonOperator.EQUAL,
        ((SqmComparisonPredicate) predicatesWithSet[2]).getSqmOperator());
  }

  @Test
  @TestTransaction
  void findFilteredKulturObjektDokumenteNoSets() {

    createTestData();

    var kods =
        repository.findFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-01-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-05-05T00:00:00Z")),
            Optional.empty(),
            2,
            0);
    assertEquals(2, kods.size());
    assertEquals("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5", kods.get(0).getId());
    assertEquals("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad", kods.get(1).getId());

    var kods2 =
        repository.findFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-01-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-08-05T00:00:00Z")),
            Optional.empty(),
            2,
            0);

    assertEquals(2, kods2.size());
    assertEquals("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469", kods2.get(0).getId());
    assertEquals("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5", kods2.get(1).getId());

    var kods3 =
        repository.findFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-01-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-08-05T00:00:00Z")),
            Optional.empty(),
            2,
            1);

    assertEquals("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5", kods3.get(0).getId());
    assertEquals("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad", kods3.get(1).getId());
  }

  @Test
  @TestTransaction
  void countFilteredKulturObjektDokumenteBetween() {

    createTestData();

    var count =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-01-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-07-01T00:00:00Z")),
            Optional.empty());

    assertEquals(3, count);

    var count2 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-07-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-08-05T00:00:00Z")),
            Optional.empty());

    assertEquals(1, count2);

    var count3 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-07-01T00:00:01Z")),
            Optional.of(Instant.parse("2020-08-05T00:00:00Z")),
            Optional.empty());

    assertEquals(0, count3);

    var count4 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-01-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-05-01T00:00:00Z")),
            Optional.empty());

    assertEquals(2, count4);

    var count5 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-01-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-04-30T23:59:59Z")),
            Optional.empty());

    assertEquals(1, count5);
  }

  @Test
  @TestTransaction
  void countFilteredKulturObjektDokumenteFromOnly() {
    createTestData();

    var count =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-07-01T00:00:00Z")), Optional.empty(), Optional.empty());

    assertEquals(1, count);

    var count2 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-03-01T00:00:00Z")), Optional.empty(), Optional.empty());

    assertEquals(3, count2);

    var count3 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-03-01T00:00:00Z").plusSeconds(1)),
            Optional.empty(),
            Optional.empty());

    assertEquals(2, count3);

    var count4 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-07-01T00:00:00Z")), Optional.empty(), Optional.empty());

    assertEquals(1, count4);

    var count5 =
        repository.countFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-07-01T00:00:00Z").plusSeconds(1)),
            Optional.empty(),
            Optional.empty());

    assertEquals(0, count5);
  }

  @Test
  @TestTransaction
  void countFilteredKulturObjektDokumenteUntilOnly() {
    createTestData();

    var count =
        repository.countFilteredKulturObjektDokumente(
            Optional.empty(), Optional.of(Instant.parse("2020-07-01T00:00:00Z")), Optional.empty());

    assertEquals(3, count);

    var count2 =
        repository.countFilteredKulturObjektDokumente(
            Optional.empty(),
            Optional.of(Instant.parse("2020-07-01T00:00:00Z").minusSeconds(1)),
            Optional.empty());

    assertEquals(2, count2);

    var count3 =
        repository.countFilteredKulturObjektDokumente(
            Optional.empty(), Optional.of(Instant.parse("2020-05-01T00:00:00Z")), Optional.empty());

    assertEquals(2, count3);

    var count4 =
        repository.countFilteredKulturObjektDokumente(
            Optional.empty(),
            Optional.of(Instant.parse("2020-05-01T00:00:00Z").minusSeconds(1)),
            Optional.empty());

    assertEquals(1, count4);

    var count5 =
        repository.countFilteredKulturObjektDokumente(
            Optional.empty(), Optional.of(Instant.parse("2020-03-01T00:00:00Z")), Optional.empty());

    assertEquals(1, count5);

    var count6 =
        repository.countFilteredKulturObjektDokumente(
            Optional.empty(),
            Optional.of(Instant.parse("2020-03-01T00:00:00Z").minusSeconds(1)),
            Optional.empty());

    assertEquals(0, count6);
  }

  @Test
  @TestTransaction
  void findFilteredKulturObjektDokumenteSet() {
    createTestData();
    var kods =
        repository.findFilteredKulturObjektDokumente(
            Optional.empty(), Optional.empty(), Optional.of("NORM-1"), 2, 0);
    assertEquals(2, kods.size());
    assertEquals("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469", kods.get(0).getId());
    assertEquals("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5", kods.get(1).getId());

    var kods2 =
        repository.findFilteredKulturObjektDokumente(
            Optional.empty(), Optional.empty(), Optional.of("NORM-3"), 2, 0);
    assertEquals(1, kods2.size());
    assertEquals("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad", kods2.get(0).getId());
  }

  @Test
  @TestTransaction
  void findFilteredKulturObjektDokumenteSetWithDates() {
    createTestData();
    var kods =
        repository.findFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-05-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-07-01T00:00:00Z")),
            Optional.of("NORM-1"),
            2,
            0);
    assertEquals(2, kods.size());
    assertEquals("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469", kods.get(0).getId());
    assertEquals("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5", kods.get(1).getId());

    var kods2 =
        repository.findFilteredKulturObjektDokumente(
            Optional.of(Instant.parse("2020-05-01T00:00:00Z")),
            Optional.of(Instant.parse("2020-06-01T00:00:00Z")),
            Optional.of("NORM-1"),
            2,
            0);
    assertEquals(1, kods2.size());
    assertEquals("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5", kods2.get(0).getId());

    var kods3 =
        repository.findFilteredKulturObjektDokumente(
            Optional.empty(), Optional.empty(), Optional.of("NORM-3"), 2, 0);
    assertEquals(1, kods3.size());
    assertEquals("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad", kods3.get(0).getId());
  }

  private void createTestData() {
    var normdatumBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder().withId("NORM-1").build();
    var identifikation =
        new Identifikation.IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
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
            .withId(UUID.randomUUID().toString())
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
        new NormdatenReferenz.NormdatenReferenzBuilder().withId("NORM-3").build();
    var identifikation3 =
        new Identifikation.IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
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
    kods.forEach(kulturObjektDokumentRepository::saveAndFlush);
  }
}
