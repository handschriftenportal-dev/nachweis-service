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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
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

  @Inject
  NormdatenReferenzRepository repository;

  @Test
  @TestTransaction
  void testFindByNameOptional() {
    NormdatenReferenz referenz = new NormdatenReferenz(UUID.randomUUID().toString(),
        "Piotr Czarnecki", "DE-007");
    repository.save(referenz);
    assertTrue(repository.findByNameOptional("Piotr Czarnecki").isPresent());
    assertFalse(repository.findByNameOptional("Konrad").isPresent());
    repository.deleteByIdAndFlush(referenz.getId());
  }

  @Test
  @TestTransaction
  void findByGndIDOptional() {
    NormdatenReferenz referenz = new NormdatenReferenz(UUID.randomUUID().toString(),
        "Piotr Czarnecki", "DE-007");
    repository.save(referenz);
    assertTrue(repository.findByGndIDOptional("DE-007").isPresent());
    assertFalse(repository.findByGndIDOptional("PL-007").isPresent());
    repository.deleteByIdAndFlush(referenz.getId());
  }

  @Test
  @TestTransaction
  void findByIdOrIdentifikatorOptional() {
    NormdatenReferenz original = createNormdatenReferenzGerman();
    repository.save(original);

    Optional<NormdatenReferenz> found = repository.findByIdOrIdentifikatorOptional("ger");
    assertTrue(found.isPresent());

    assertEquals(original.getId(), found.get().getId());
    assertEquals(original.getName(), found.get().getName());
    assertEquals(original.getTypeName(), found.get().getTypeName());
    assertEquals(original.getGndID(), found.get().getGndID());
    assertNotNull(found.get().getVarianterName());
    assertEquals(original.getVarianterName().size(), found.get().getVarianterName().size());
    assertNotNull(found.get().getIdentifikator());
    assertEquals(original.getIdentifikator().size(), found.get().getIdentifikator().size());

    assertFalse(repository.findByIdOrIdentifikatorOptional("DE-008").isPresent());
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

}
