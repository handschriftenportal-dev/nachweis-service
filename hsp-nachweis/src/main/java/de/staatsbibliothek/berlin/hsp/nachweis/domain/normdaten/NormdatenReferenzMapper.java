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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Identifier;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.VariantName;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 10.11.2021
 */
public final class NormdatenReferenzMapper {

  private NormdatenReferenzMapper() {
  }

  public static NormdatenReferenz map(GNDEntityFact gndEntityFact) {
    if (Objects.isNull(gndEntityFact)) {
      return null;
    }

    return new NormdatenReferenzBuilder()
        .withId(gndEntityFact.getId())
        .withName(gndEntityFact.getPreferredName())
        .withGndID(gndEntityFact.getGndIdentifier())
        .withTypeName(gndEntityFact.getTypeName())
        .withIdentifikator(mapIdentifiers(gndEntityFact.getIdentifier()))
        .withVarianterName(mapVariantNames(gndEntityFact.getVariantName()))
        .build();
  }

  static Set<Identifikator> mapIdentifiers(Set<Identifier> identifiers) {
    if (Objects.nonNull(identifiers)) {
      return identifiers.stream()
          .map(i -> new Identifikator(i.getText(), i.getUrl(), i.getType()))
          .collect(Collectors.toSet());
    } else {
      return Set.of();
    }
  }

  static Set<VarianterName> mapVariantNames(Set<VariantName> variantNames) {
    if (Objects.nonNull(variantNames)) {
      return variantNames
          .stream()
          .map(vn -> new VarianterName(vn.getName(), vn.getLanguageCode()))
          .collect(Collectors.toSet());
    } else {
      return Set.of();
    }
  }
}
