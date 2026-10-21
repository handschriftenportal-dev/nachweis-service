package de.staatsbibliothek.berlin.hsp.nachweis;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.11.23.
 * @version 1.0
 */
class ArchitectureTest {

  @Test
  void testLayers() {

    assertDoesNotThrow(() -> layeredArchitecture()
        .consideringAllDependencies()
        .layer("Application").definedBy("..application..")
        .layer("Domain").definedBy("..domain..")
        .layer("Infrastructure").definedBy("..infrastructure..")

        .whereLayer("Application").mayNotBeAccessedByAnyLayer()
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application")
        .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Domain"));

  }
}
