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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 10.11.2021
 */
public final class NormdatenReferenzMapper {

  private static final Logger log = LoggerFactory.getLogger(NormdatenReferenzMapper.class);

  private NormdatenReferenzMapper() {
  }

  public static NormdatenReferenz map(GNDEntityFact gndEntityFact) {
    if (Objects.isNull(gndEntityFact)) {
      return null;
    }

    NormdatenReferenzBuilder normdatenReferenzBuilder = new NormdatenReferenzBuilder()
        .withId(gndEntityFact.getId())
        .withName(gndEntityFact.getPreferredName())
        .withGndID(gndEntityFact.getGndIdentifier())
        .withTypeName(gndEntityFact.getTypeName())
        .withVarianterName(mapVariantNames(gndEntityFact.getVariantName()))
        .withIdentifikator(mapIdentifiers(gndEntityFact.getIdentifier()))
        .withUri(mapURIs(gndEntityFact));
    return normdatenReferenzBuilder.build();
  }

  static String mapURIs(GNDEntityFact gndEntityFact) {
    Set<Identifier> identifiers = gndEntityFact.getIdentifier();
    if (Objects.isNull(identifiers)) {
      log.warn("No identifier found for GND entity fact {}", gndEntityFact);
      return null;
    }
    return identifiers.stream()
        .filter(i -> Identifier.URI_TYPE.equals(i.getType()))
        .findFirst().orElse(new Identifier()).getUrl();
  }

  static Set<Identifikator> mapIdentifiers(Set<Identifier> identifiers) {
    if (Objects.isNull(identifiers)) {
      return Set.of();
    }
    return identifiers.stream()
        .map(i -> new Identifikator(i.getText(), i.getUrl(), i.getType()))
        .collect(Collectors.toSet());
  }

  static Set<VarianterName> mapVariantNames(Set<VariantName> variantNames) {
    if (Objects.isNull(variantNames)) {
      return Set.of();
    }
    return variantNames
        .stream()
        .map(vn -> new VarianterName(vn.getName(), vn.getLanguageCode()))
        .collect(Collectors.toSet());
  }
}
