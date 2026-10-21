package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport.DigitalisatRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.05.24.
 * @version 1.0
 */

@QuarkusTest
@TestTransaction
public class DigitalisatRepositoryAdapterTest {

  @Inject
  DigitalisatRepository digitalisatRepository;

  @Test
  void testSaveDigitalisat() {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder().withID("D1")
        .withManifest("http://manifest.url").build();

    digitalisatRepository.save(digitalisat);

    assertNotNull(digitalisatRepository.findById(digitalisat.getId()));
  }
}
