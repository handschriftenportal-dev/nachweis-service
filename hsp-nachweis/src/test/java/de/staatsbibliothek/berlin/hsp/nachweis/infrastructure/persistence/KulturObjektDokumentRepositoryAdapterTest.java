package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 15.06.2022
 */

@QuarkusTest
public class KulturObjektDokumentRepositoryAdapterTest {

  @Inject
  KulturObjektDokumentRepositoryAdapter kodRepository;

  @Inject
  IdentifikationRepository identifikationRepository;

  @AfterEach
  @TestTransaction
  void tearDown() {
    kodRepository.deleteAll();
  }

  @Test
  @TestTransaction
  void testFindByIdentifikationIdent() {
    KulturObjektDokument kodOriginal = createKOD();
    kodRepository.save(kodOriginal);

    List<KulturObjektDokument> kods = kodRepository.findByIdentifikationIdent("Msc.Can.18");
    assertNotNull(kods);
    assertEquals(1, kods.size());

    checkKOD(kodOriginal, kods.iterator().next());
  }

  @Test
  @TestTransaction
  void testFindByPurl() {
    KulturObjektDokument kod = createKODBuilder()
        .addPURL(new PURL(
            URI.create("https://resolver.example/KOD-1"),
            URI.create("https://target.example/KOD-1"),
            PURLTyp.INTERNAL))
        .build();
    kodRepository.save(kod);

    Optional<KulturObjektDokument> result = kodRepository.findByPurl(
        "https://resolver.example/KOD-1");

    assertTrue(result.isPresent());
    assertEquals(kod.getId(), result.get().getId());
    assertTrue(kodRepository.findByPurl("https://resolver.example/unknown").isEmpty());
  }

  @Test
  @TestTransaction
  void testDeleteIfIdentifikationRemovesAutomatically() {
    // Create KOD without beschreibungenIDs - KODs with linked Beschreibungen cannot be deleted
    KulturObjektDokument kod = createKODWithoutBeschreibungen();

    assertTrue(identifikationRepository.findAll().list().isEmpty());
    kodRepository.save(kod);

    assertEquals(4, identifikationRepository.findAll().list().size());

    kodRepository.deleteByIdAndFlush(kod.getId());

    assertTrue(identifikationRepository.findAll().list().isEmpty(), "size of identifiktaions " +
        identifikationRepository.listAll(false).size());
  }

  @Test
  @TestTransaction
  void testFindByAlternativeIdentifikationIdent() {
    KulturObjektDokument kodOriginal = createKOD();
    kodRepository.save(kodOriginal);

    List<KulturObjektDokument> kods = kodRepository.findByAlternativeIdentifikationIdent(
        "Msc.Can.18#1");
    assertNotNull(kods);
    assertEquals(1, kods.size());
    checkKOD(kodOriginal, kods.iterator().next());

    kods = kodRepository.findByAlternativeIdentifikationIdent("Msc.Can.18#2");
    assertNotNull(kods);
    assertEquals(1, kods.size());
    checkKOD(kodOriginal, kods.iterator().next());

    kods = kodRepository.findByAlternativeIdentifikationIdent("Msc.Can.18#3");
    assertNotNull(kods);
    assertEquals(1, kods.size());
    checkKOD(kodOriginal, kods.iterator().next());
  }

  @Test
  @TestTransaction
  void testfindByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID() {
    KulturObjektDokument kodalreadyExist = createKOD();
    kodRepository.deleteAll();
    kodRepository.save(kodalreadyExist);

    assertEquals(1, kodRepository.findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
        kodalreadyExist.getGueltigeIdentifikation().getIdent(),
        kodalreadyExist.getGueltigeIdentifikation().getBesitzer().getId(),
        kodalreadyExist.getGueltigeIdentifikation().getAufbewahrungsOrt().getId()).size());
  }

  @Test
  @TestTransaction
  void testfindByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName() {

    KulturObjektDokument kodalreadyExist = createKOD();
    kodRepository.deleteAll();
    kodRepository.save(kodalreadyExist);

    Identifikation gueltigeSignatur = kodalreadyExist.getGueltigeIdentifikation();

    assertTrue(kodRepository.findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
        gueltigeSignatur.getIdent(), gueltigeSignatur.getBesitzer().getName(),
        gueltigeSignatur.getAufbewahrungsOrt().getName()).isPresent());

    assertTrue(kodRepository.findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
        gueltigeSignatur.getIdent(),
        gueltigeSignatur.getBesitzer().getVarianterName().stream().findFirst().get().getName(),
        gueltigeSignatur.getAufbewahrungsOrt().getName()).isPresent());
  }


  @Test
  @TestTransaction
  void testfindByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName() {

    KulturObjektDokument kodalreadyExist = createKOD();
    kodRepository.deleteAll();
    kodRepository.save(kodalreadyExist);

    Identifikation alt1Signatur = kodalreadyExist.getAlternativeIdentifikationen()
        .stream().filter(i -> i.getId().equals("181")).findFirst().get();

    assertTrue(
        kodRepository.findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
            alt1Signatur.getIdent(), alt1Signatur.getBesitzer().getName(),
            alt1Signatur.getAufbewahrungsOrt().getName()).isPresent());

    assertTrue(
        kodRepository.findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
            alt1Signatur.getIdent(),
            alt1Signatur.getBesitzer().getVarianterName().stream().findFirst().get().getName(),
            alt1Signatur.getAufbewahrungsOrt().getName()).isPresent());
  }

  @Test
  @TestTransaction
  void testFindByBeschreibungsID() {
    KulturObjektDokument kulturObjektDokument = createKOD();
    kodRepository.saveAndFlush(kulturObjektDokument);
    kodRepository.getEntityManager().clear();

    KulturObjektDokument kodReloadFromDB = kodRepository.findByBeschreibung("HSP-1B");

    assertNotNull(kodReloadFromDB);
    assertEquals(2, kodReloadFromDB.getBeschreibungenIDs().size());
  }

  @Test
  @TestTransaction
  void testfindByIdentifikationBeteiligeKoerperschaftName() {
    kodRepository.deleteAll();
    KulturObjektDokument kulturObjektDokument = createKOD();
    kodRepository.saveAndFlush(kulturObjektDokument);
    kodRepository.getEntityManager().clear();

    Optional<KulturObjektDokument> kodFromDatabase = kodRepository.findByIdentifikationBeteiligeKoerperschaftName(
        "Staatsbibliothek Berlin");

    assertTrue(kodFromDatabase.isPresent());
    assertEquals(3, kodFromDatabase.get().getAlternativeIdentifikationen().size());
  }

  void checkKOD(KulturObjektDokument kodOriginal, KulturObjektDokument kod) {
    assertEquals(kodOriginal, kod);
    assertEquals(kodOriginal.getGueltigeIdentifikation(), kod.getGueltigeIdentifikation());
    assertNotNull(kod.getAlternativeIdentifikationen());
    assertEquals(3, kod.getAlternativeIdentifikationen().size());
  }

  KulturObjektDokument createKOD() {
    return createKODBuilder()
        .addBeschreibungsdokumentID("HSP-1B")
        .addBeschreibungsdokumentID("HSP-2B")
        .build();
  }

  KulturObjektDokument createKODWithoutBeschreibungen() {
    return createKODBuilder().build();
  }

  private KulturObjektDokumentBuilder createKODBuilder() {
    NormdatenReferenz bamberg = new NormdatenReferenz("123", "Bamberg", "", "Place");
    bamberg.getVarianterName().add(new VarianterName("Bamsberg", "de"));
    NormdatenReferenz stabiBamberg = new NormdatenReferenz("456", "Staatsbibliothek Bamberg",
        "http://d-nb.info/gnd/2022477-1", "CorporateBody");
    stabiBamberg.getVarianterName().add(new VarianterName("StabiBamberg", "de"));

    NormdatenReferenz berlin = new NormdatenReferenz("1234", "Berlin", "", "Place");
    berlin.getVarianterName().add(new VarianterName("Berlin", "de"));
    NormdatenReferenz stabiBerlin = new NormdatenReferenz("4567", "Staatsbibliothek Berlin",
        "http://d-nb.info/gnd/2022477-1", "CorporateBody");
    stabiBerlin.getVarianterName().add(new VarianterName("StabbiBerlin", "de"));

    return new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("18")
            .withIdent("Msc.Can.18")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(stabiBamberg)
            .withAufbewahrungsOrt(bamberg).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("181")
            .withIdent("Msc.Can.18#1")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(stabiBamberg)
            .withAufbewahrungsOrt(bamberg).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("182")
            .withIdent("Msc.Can.18#2")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(stabiBamberg)
            .withAufbewahrungsOrt(bamberg).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("183")
            .withIdent("Msc.Can.18#3")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(stabiBerlin)
            .withAufbewahrungsOrt(berlin).build())
        .withAttributsReferenzenTyp(AttributsReferenzStatusTyp.MANUELL);
  }

  @Test
  @TestTransaction
  void testGetAllBeschreibungsIdsWithKodIDs() {
    KulturObjektDokument kulturObjektDokument = createKOD();
    KulturObjektDokument kulturObjektDokument1 = kodRepository.saveAndFlush(kulturObjektDokument);

    Map<String, List<String>> allBeschreibungsIdsWithKodIDs = kodRepository.getAllBeschreibungsIdsWithKodIDs();

    assertEquals(2, allBeschreibungsIdsWithKodIDs.get(kulturObjektDokument1.getId()).size());
  }

  @Test
  @TestTransaction
  void testFindFilteredKulturObjektDokumentListDTOs() {
    KulturObjektDokument kod = createKOD();
    kodRepository.saveAndFlush(kod);

    Map<String, Boolean> sortByAsc = new HashMap<>();
    sortByAsc.put("hspId", Boolean.TRUE);

    Map<String, String> filterBy_1 = new HashMap<>();
    filterBy_1.put("beschreibungenIds", "HSP-3B");
    List<KulturObjektDokumentListDTO> result_1 = kodRepository.findFilteredKulturObjektDokumentListDTOs(
        0,
        10,
        filterBy_1, sortByAsc);
    assertNotNull(result_1);
    assertEquals(0, result_1.size());

    Map<String, String> filterBy_2 = new HashMap<>();
    filterBy_2.put("hspId", kod.getId());
    filterBy_2.put("gndId", kod.getGndIdentifier());
    filterBy_2.put("gueltigeSignatur", kod.getGueltigeSignatur());
    filterBy_2.put("aufbewahrungsort",
        kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getName());
    filterBy_2.put("besitzer", kod.getGueltigeIdentifikation().getBesitzer().getName());
    filterBy_2.put("registrierungsdatumString", kod.getRegistrierungsDatum()
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
    filterBy_2.put("beschreibungenIds", "HSP-1B");

    List<KulturObjektDokumentListDTO> result_2 = kodRepository.findFilteredKulturObjektDokumentListDTOs(
        0,
        10, filterBy_2, sortByAsc);
    assertNotNull(result_2);
    assertEquals(1, result_2.size());
  }

  @Test
  @TestTransaction
  void testCountFilteredKulturObjektDokumentListDTOs() {
    KulturObjektDokument kod = createKOD();
    kodRepository.saveAndFlush(kod);

    Map<String, String> filterBy_1 = new HashMap<>();
    filterBy_1.put("beschreibungenIds", "HSP-3B");

    assertEquals(0, kodRepository.countFilteredKulturObjektDokumentListDTOs(filterBy_1));

    Map<String, String> filterBy_2 = new HashMap<>();
    filterBy_2.put("hspId", kod.getId());
    filterBy_2.put("gndId", kod.getGndIdentifier());
    filterBy_2.put("gueltigeSignatur", kod.getGueltigeSignatur());
    filterBy_2.put("aufbewahrungsort",
        kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getName());
    filterBy_2.put("besitzer", kod.getGueltigeIdentifikation().getBesitzer().getName());
    filterBy_2.put("registrierungsdatumString", kod.getRegistrierungsDatum()
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
    filterBy_2.put("beschreibungenIds", "HSP-1B");

    assertEquals(1, kodRepository.countFilteredKulturObjektDokumentListDTOs(filterBy_2));
  }

  @Test
  @TestTransaction
  void testFindKulturObjektDokumentListDTOById() {
    KulturObjektDokument kod = createKOD();
    kodRepository.saveAndFlush(kod);

    assertNull(kodRepository.findKulturObjektDokumentListDTOById("does.not.exist"));

    KulturObjektDokumentListDTO result =
        kodRepository.findKulturObjektDokumentListDTOById(kod.getId());

    assertNotNull(result);
    assertNotNull(result.getBeschreibungenIdsAsList());
    assertEquals(kod.getBeschreibungenIDs(), new HashSet<>(result.getBeschreibungenIdsAsList()));
  }

  @Test
  @TestTransaction
  void testSelectAllTeiXml() {
    KulturObjektDokument kod = createKOD();
    kodRepository.saveAndFlush(kod);

    kodRepository.selectAllTeiXml().findFirst().ifPresentOrElse(
        k -> {
          assertEquals(kod.getId(), k.getId());
          assertEquals(kod.getTeiXML(), k.getTeiXml());
        },
        () -> fail("No KulturObjektDokument found"));
  }

  @Test
  @TestTransaction
  void testSelectAllTeiXmlFails() {
    kodRepository.selectAllTeiXml().findFirst().ifPresentOrElse(
        d -> fail("No KOD should be found"),
        () -> {
        });
  }

  @Test
  @TestTransaction
  void testTwoKODsUsingSameDigitalisat() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withID("1").withManifest("http://localhost/manifest.json").build();

    KulturObjektDokument kod1 = new KulturObjektDokumentBuilder().withId("KOD1")
        .addDigitalisat(digitalisat).build();

    KulturObjektDokument kod2 = new KulturObjektDokumentBuilder().withId("KOD2")
        .addDigitalisat(digitalisat).build();

    kodRepository.saveAll(Arrays.asList(kod2, kod1));

    assertFalse(kodRepository.findById("KOD1").getDigitalisate().isEmpty());
    assertFalse(kodRepository.findById("KOD2").getDigitalisate().isEmpty());
  }
}
