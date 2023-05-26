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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 16.08.21
 */

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public class NormdatenReferenzRepositoryAdapterTest {

  @Inject
  NormdatenReferenzRepositoryAdapter repository;

  @Test
  @TestTransaction
  void testFindByName() {
    NormdatenReferenz german = createNormdatenReferenzGerman();
    Assertions.assertEquals(0, repository.findAll().list().size());
    repository.save(german);
    NormdatenReferenz saved = repository.findByName(german.getName());
    Assertions.assertNotNull(saved);

    assertEquals(german.getId(), saved.getId());
    assertEquals(german.getName(), saved.getName());
    assertEquals(german.getTypeName(), saved.getTypeName());
    assertEquals(german.getGndID(), saved.getGndID());
    assertNotNull(saved.getVarianterName());
    assertEquals(german.getVarianterName().size(), saved.getVarianterName().size());
    assertNotNull(saved.getIdentifikator());
    assertEquals(german.getIdentifikator().size(), saved.getIdentifikator().size());
  }

  @Test
  @TestTransaction
  void testFindByGndID() {
    Assertions.assertEquals(0, repository.findAll().list().size());
    NormdatenReferenz german = createNormdatenReferenzGerman();
    repository.save(german);

    NormdatenReferenz saved = repository.findByGndID(german.getGndID());
    Assertions.assertNotNull(saved);

    assertEquals(german.getId(), saved.getId());
    assertEquals(german.getName(), saved.getName());
    assertEquals(german.getTypeName(), saved.getTypeName());
    assertEquals(german.getGndID(), saved.getGndID());
    assertNotNull(saved.getVarianterName());
    assertEquals(german.getVarianterName().size(), saved.getVarianterName().size());
    assertNotNull(saved.getIdentifikator());
    assertEquals(german.getIdentifikator().size(), saved.getIdentifikator().size());
  }

  @Test
  @TestTransaction
  void testFindByIdOrIdentifikator() {
    Assertions.assertEquals(0, repository.findAll().list().size());
    NormdatenReferenz german = createNormdatenReferenzGerman();
    repository.save(german);

    NormdatenReferenz saved = repository.findByIdOrIdentifikator("de");
    Assertions.assertNotNull(saved);

    assertEquals(german.getId(), saved.getId());
    assertEquals(german.getName(), saved.getName());
    assertEquals(german.getTypeName(), saved.getTypeName());
    assertEquals(german.getGndID(), saved.getGndID());
    assertNotNull(saved.getVarianterName());
    assertEquals(german.getVarianterName().size(), saved.getVarianterName().size());
    assertNotNull(saved.getIdentifikator());
    assertEquals(german.getIdentifikator().size(), saved.getIdentifikator().size());
  }

  private NormdatenReferenz createNormdatenReferenzGerman() {
    return new NormdatenReferenz.NormdatenReferenzBuilder()
        .withId(UUID.nameUUIDFromBytes("de".getBytes(StandardCharsets.UTF_8)).toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addIdentifikator(new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(new Identifikator("de", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .withGndID("4113292-0")
        .build();
  }

  @Test
  @TestTransaction
  void testFindOrCreateByIdOrName() throws Exception {
    NormdatenReferenz berlin = createBerlin();

    repository.saveAndFlush(berlin);

    assertEquals(1l, repository.findAll().count());

    NormdatenReferenz existing = repository.findByIdOrName("4005728-8",
        GNDEntityFact.PLACE_TYPE_NAME);
    assertNotNull(existing);
    assertEquals(berlin, existing);

    existing = repository.findByIdOrName("ee1611b6-1f56-38e7-8c12-b40684dbb395", GNDEntityFact.PLACE_TYPE_NAME);
    assertNotNull(existing);
    assertEquals(berlin, existing);

    existing = repository.findByIdOrName("2950159", GNDEntityFact.PLACE_TYPE_NAME);
    assertNotNull(existing);
    assertEquals(berlin, existing);

    existing = repository.findByIdOrName("Berlin", GNDEntityFact.PLACE_TYPE_NAME);
    assertNotNull(existing);
    assertEquals(berlin, existing);

    existing = repository.findByIdOrName("Berlino", GNDEntityFact.PLACE_TYPE_NAME);
    assertNotNull(existing);
    assertEquals(berlin, existing);

    NormdatenReferenz nonExisting = repository.findByIdOrName("NOT_EXISTS",
        GNDEntityFact.PLACE_TYPE_NAME);
    assertNull(nonExisting);
  }

  private NormdatenReferenz createBerlin() {
    return new NormdatenReferenzBuilder()
        .withId("ee1611b6-1f56-38e7-8c12-b40684dbb395")
        .withName("Berlin")
        .withTypeName(GNDEntityFact.PLACE_TYPE_NAME)
        .withGndID("4005728-8")
        .addVarianterName(new VarianterName("Berlino", "it"))
        .addIdentifikator(
            new Identifikator("2950159", "https://www.geonames.org/2950159", "GEONAMES"))
        .build();
  }
}
