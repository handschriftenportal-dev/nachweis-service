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
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;
import static org.wildfly.common.Assert.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Konfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import javax.inject.Inject;
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
