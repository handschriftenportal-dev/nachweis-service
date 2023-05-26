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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Identifier;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.VariantName;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 15.11.2021
 */
public class NormdatenReferenzMapperTest {

  public static final Identifier IDENTIFIER_1
      = new Identifier("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2");
  public static final Identifier IDENTIFIER_2
      = new Identifier("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2");
  public static final Identifier IDENTIFIER_3 = new Identifier("de", null, null);
  public static final VariantName VARIANT_NAME_1 = new VariantName("Neuhochdeutsch", "de");
  public static final VariantName VARIANT_NAME_2 = new VariantName("Deutsche Sprache", "de");
  public static final VariantName VARIANT_NAME_3 = new VariantName("Hochdeutsch", null);


  @Test
  void testMap() {

    GNDEntityFact gndEntityFact = new GNDEntityFact("id", "gndIdentifier", "preferredName", "typeName");
    NormdatenReferenz normdatenReferenz = NormdatenReferenzMapper.map(gndEntityFact);

    assertNotNull(normdatenReferenz);
    assertEquals(gndEntityFact.getId(), normdatenReferenz.getId());
    assertEquals(gndEntityFact.getGndIdentifier(), normdatenReferenz.getGndID());
    assertEquals(gndEntityFact.getPreferredName(), normdatenReferenz.getName());
    assertEquals(gndEntityFact.getTypeName(), normdatenReferenz.getTypeName());
    assertNotNull(normdatenReferenz.getIdentifikator());
    assertEquals(0, normdatenReferenz.getIdentifikator().size());
    assertNotNull(normdatenReferenz.getVarianterName());
    assertEquals(0, normdatenReferenz.getVarianterName().size());
  }

  @Test
  void testMap_withIdentifiersAndVarianterNamen() {

    GNDEntityFact gndEntityFact = new GNDEntityFact.GNDEntityFactBuilder()
        .withId("id")
        .withPreferredName("preferredName")
        .withGndIdentifier("gndIdentifier")
        .withTypeName("typeName")
        .withIdentifier(Set.of(IDENTIFIER_1, IDENTIFIER_2))
        .addIdentifier(IDENTIFIER_3)
        .withVariantName(Set.of(VARIANT_NAME_1, VARIANT_NAME_2))
        .addVariantName(VARIANT_NAME_3)
        .build();

    NormdatenReferenz normdatenReferenz = NormdatenReferenzMapper.map(gndEntityFact);

    assertNotNull(normdatenReferenz);
    assertEquals(gndEntityFact.getId(), normdatenReferenz.getId());
    assertEquals(gndEntityFact.getGndIdentifier(), normdatenReferenz.getGndID());
    assertEquals(gndEntityFact.getPreferredName(), normdatenReferenz.getName());
    assertEquals(gndEntityFact.getTypeName(), normdatenReferenz.getTypeName());
    assertNotNull(normdatenReferenz.getIdentifikator());
    assertEquals(3, normdatenReferenz.getIdentifikator().size());
    assertNotNull(normdatenReferenz.getVarianterName());
    assertEquals(3, normdatenReferenz.getVarianterName().size());
  }

  @Test
  void testMapIdentifiers() {
    Set<Identifier> identifiers = Set.of(IDENTIFIER_1, IDENTIFIER_2, IDENTIFIER_3);

    Set<Identifikator> identifikators = NormdatenReferenzMapper.mapIdentifiers(identifiers);

    assertNotNull(identifikators);
    assertEquals(3, identifikators.size());

    assertTrue(identifikators.stream().anyMatch(createPredicate(IDENTIFIER_1)));
    assertTrue(identifikators.stream().anyMatch(createPredicate(IDENTIFIER_2)));
    assertTrue(identifikators.stream().anyMatch(createPredicate(IDENTIFIER_3)));
  }

  @Test
  void testMapVariantNames() {
    Set<VariantName> variantNames = Set.of(VARIANT_NAME_1, VARIANT_NAME_2, VARIANT_NAME_3);

    Set<VarianterName> varianterNamen = NormdatenReferenzMapper.mapVariantNames(variantNames);

    assertNotNull(varianterNamen);
    assertEquals(3, varianterNamen.size());

    assertTrue(varianterNamen.stream().anyMatch(createPredicate(VARIANT_NAME_1)));
    assertTrue(varianterNamen.stream().anyMatch(createPredicate(VARIANT_NAME_2)));
    assertTrue(varianterNamen.stream().anyMatch(createPredicate(VARIANT_NAME_3)));
  }

  private Predicate<Identifikator> createPredicate(Identifier expected) {
    return mapped -> Objects.equals(expected.getType(), mapped.getType())
        && Objects.equals(expected.getText(), mapped.getText())
        && Objects.equals(expected.getUrl(), mapped.getUrl());
  }

  private Predicate<VarianterName> createPredicate(VariantName expected) {
    return mapped -> Objects.equals(expected.getName(), mapped.getName())
        && Objects.equals(expected.getLanguageCode(), mapped.getLanguageCode());
  }

}
