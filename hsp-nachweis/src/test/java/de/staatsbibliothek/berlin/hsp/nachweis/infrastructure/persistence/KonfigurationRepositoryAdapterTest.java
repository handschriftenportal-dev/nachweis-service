package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;
import static org.wildfly.common.Assert.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Konfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
@QuarkusTest
class KonfigurationRepositoryAdapterTest {

  @Inject
  KonfigurationRepository konfigurationRepository;

  @Test
  @TestTransaction
  void testGetWert() {
    Konfiguration konfiguration = new Konfiguration("schluessel", "wert");
    konfigurationRepository.save(konfiguration);

    Optional<String> wert_1 = konfigurationRepository.getWert("does.not.exist");
    assertNotNull(wert_1);
    assertFalse(wert_1.isPresent());

    Optional<String> wert_2 = konfigurationRepository.getWert("schluessel");
    assertNotNull(wert_2);
    assertTrue(wert_2.isPresent());
  }

  @Test
  @TestTransaction
  void testSetWert() {
    assertFalse(konfigurationRepository.findByIdOptional("schluessel").isPresent());

    konfigurationRepository.setWert("schluessel", "wert");

    Optional<Konfiguration> konfiguration = konfigurationRepository.findByIdOptional("schluessel");
    assertNotNull(konfiguration);
    assertTrue(konfiguration.isPresent());
    assertEquals("wert", konfiguration.map(Konfiguration::getWert).orElse(null));
  }

}
