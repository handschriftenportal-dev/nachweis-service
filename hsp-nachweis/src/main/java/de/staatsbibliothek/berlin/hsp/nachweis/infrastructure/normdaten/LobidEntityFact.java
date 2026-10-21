package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/**
 * DTO for the lobid.org GND REST API response.
 *
 * <p>Lobid returns JSON-LD with a {@code "type"} array (e.g. {@code ["AuthorityResource",
 * "Person", "DifferentiatedPerson"]}), while the normdatenservice GraphQL API uses a
 * {@code "typeName"} string. These are separate concerns and therefore separate classes.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LobidEntityFact {

  private static final Map<String, String> TYPE_MAP = Map.of(
      "Person", GNDEntityFact.PERSON_TYPE_NAME,
      "DifferentiatedPerson", GNDEntityFact.PERSON_TYPE_NAME,
      "UndifferentiatedPerson", GNDEntityFact.PERSON_TYPE_NAME,
      "PlaceOrGeographicName", GNDEntityFact.PLACE_TYPE_NAME,
      "AdministrativeUnit", GNDEntityFact.PLACE_TYPE_NAME,
      "CorporateBody", GNDEntityFact.CORPORATE_BODY_TYPE_NAME,
      "ConferenceOrEvent", GNDEntityFact.CORPORATE_BODY_TYPE_NAME,
      "Language", GNDEntityFact.LANGUAGE_TYPE_NAME);

  @JsonProperty("gndIdentifier")
  private String gndIdentifier;

  @JsonProperty("preferredName")
  private String preferredName;

  @JsonProperty("type")
  private List<String> types;

  @JsonProperty("variantName")
  private Set<VariantName> variantName;

  @JsonProperty("identifier")
  private Set<Identifier> identifier;

  /**
   * Resolves the simplified type name ("Person", "Place", "CorporateBody", "Language") from the
   * lobid {@code type} array, or {@code null} if the type cannot be determined.
   */
  public String resolveTypeName() {
    if (types == null) {
      return null;
    }
    return types.stream()
        .map(TYPE_MAP::get)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * Converts this lobid response to a {@link GNDEntityFact} suitable for display or for creating
   * a node in the normdatenservice graph.
   */
  public GNDEntityFact toGNDEntityFact(String typeName) {
    return new GNDEntityFact(
        null,
        gndIdentifier,
        preferredName,
        typeName,
        identifier != null ? identifier : new HashSet<>(),
        variantName != null ? variantName : new HashSet<>());
  }
}
