package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport.DigitalisatRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */

@QuarkusTest
public class KatalogRepositoryAdapterTest {

  public static final String ID = "1";
  public static final String KATALOG_TITLE = "Katalog Title";
  public static final String PUBLIKATIONS_JAHR = "1965";
  public static final String LIZENZ_URI = "https://rightsstatements.org/page/InC/1.0/";
  public static final String HSK_ID = "440";
  public static final LocalDateTime ERSTELL_DATUM = LocalDateTime.of(2022, 2, 21, 8, 2, 30);
  public static final LocalDateTime AENDERUNGS_DATUM = LocalDateTime.of(2022, 2, 21, 8, 51, 30);
  public static final KatalogBeschreibungReferenz KATALOG_BESCHREIBUNG_REFERENZ = new KatalogBeschreibungReferenz(
      "BeschreibungsID", "1", "20", null, null);
  public static final String TEI_XML = "<TEI></TEI>";
  public static final NormdatenReferenz AUTOR = new NormdatenReferenz(UUID.randomUUID().toString(),
      "Stahl, Irene",
      "GND14546456-X");
  public static final NormdatenReferenz VERLAG = new NormdatenReferenz(UUID.randomUUID().toString(),
      "Harrassowitz ",
      "1068107626");
  @Inject
  KatalogRepositoryAdapter katalogRepository;

  @Inject
  DigitalisatRepository digitalisatRepository;

  @BeforeEach
  @TestTransaction
  void setup() {
    katalogRepository.deleteAll();
  }

  @AfterEach
  @TestTransaction
  void tearDown() {
    katalogRepository.deleteAll();
  }

  @Test
  @TestTransaction
  void testSaveKatalog() {

    Katalog katalog = createKatalog();

    katalogRepository.save(katalog);

    assertEquals(katalog, katalogRepository.findById("1"));
  }

  @Test
  @TestTransaction
  void testFindKatalogByHSKId() {

    katalogRepository.save(createKatalog());

    Optional<Katalog> katalog = katalogRepository.findByHskId("044");

    assertTrue(katalog.isPresent());

    assertFalse(katalogRepository.findByHskId("043").isPresent());
  }

  @Test
  @TestTransaction
  void testfindKatalogBeschreibungReferenzByBeschreibungsID() {

    katalogRepository.save(createKatalog());

    Optional<KatalogBeschreibungReferenz> katalogBeschreibungReferenz = katalogRepository.findKatalogBeschreibungReferenzByBeschreibungsID(
        "BeschreibungsID");

    assertTrue(katalogBeschreibungReferenz.isPresent());
  }

  @Test
  @TestTransaction
  void testSelectAllTeiXml() {
    katalogRepository.deleteAll();
    Katalog katalog = katalogRepository.save(createKatalog());
    katalogRepository.selectAllTeiXml().findFirst().ifPresentOrElse(
        d -> {
          assertEquals(katalog.getId(), d.getId());
          assertEquals(katalog.getTeiXML(), d.getTeiXml());
        },
        () -> fail("No Katalog found"));
  }

  @Test
  @TestTransaction
  void testSelectAllTeiXmlNoDocumentsInnDB() {
    katalogRepository.deleteAll();
    katalogRepository.selectAllTeiXml().findFirst().ifPresentOrElse(
        d -> {
          fail("No Katalog should be found");
        },
        () -> {
        });
  }

  @Test
  @TestTransaction
  void testSaveAndFlushIfDigitalisateWillBeRemoved() {
    Katalog katalog = createKatalog();

    katalogRepository.saveAndFlush(katalog);

    var updatedKatalog = katalogRepository.findById(katalog.getId());

    assertNotNull(updatedKatalog);

    assertEquals(1, digitalisatRepository.listAll(false).size());

    Digitalisat neuesDigitalisat = Digitalisat.DigitalisatBuilder()
        .withID(UUID.randomUUID().toString()).withManifest("https://neuesmanifestz.url").build();

    updatedKatalog.setDigitalisat(neuesDigitalisat);

    katalogRepository.saveAndFlush(updatedKatalog);

    assertEquals(1, digitalisatRepository.listAll(false).size());

    katalogRepository.deleteByIdAndFlush(updatedKatalog.getId());

    assertEquals(0, digitalisatRepository.listAll(false).size());
  }

  Katalog createKatalog() {
    return new KatalogBuilder().withId(ID).withTitle(KATALOG_TITLE)
        .withVerlag(VERLAG)
        .addAutor(AUTOR).withDigitalisat(
            Digitalisat.DigitalisatBuilder().withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
                .withManifest("").build()).withPublikationsJahr(PUBLIKATIONS_JAHR)
        .withLizenzURI(LIZENZ_URI)
        .withHSKID(HSK_ID)
        .withErstelldatum(ERSTELL_DATUM)
        .withAenderungsdatum(AENDERUNGS_DATUM).addKatalogBeschreibungReferenz(
            KATALOG_BESCHREIBUNG_REFERENZ)
        .withTEIXML(TEI_XML)
        .withHSKID("044")
        .build();
  }
}
