package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Name;

/**
 * Input type for the normdaten service {@code create} mutation.
 * Corresponds to server-side type {@code GNDEntityFactGraphQLInput}.
 */
@Name("GNDEntityFactGraphQLInput")
public class GNDEntityFactGraphQLInput {

  private final String gndIdentifier;
  private final String preferredName;
  private final String typeName;
  private final Set<Identifier> identifier;
  private final Set<VariantName> variantName;

  public GNDEntityFactGraphQLInput(String gndIdentifier, String preferredName, String typeName,
      Set<Identifier> identifier, Set<VariantName> variantName) {
    this.gndIdentifier = gndIdentifier;
    this.preferredName = preferredName;
    this.typeName = typeName;
    this.identifier = identifier != null ? identifier : new HashSet<>();
    this.variantName = variantName != null ? variantName : new HashSet<>();
  }

  public static GNDEntityFactGraphQLInput from(GNDEntityFact fact) {
    return new GNDEntityFactGraphQLInput(
        fact.getGndIdentifier(),
        fact.getPreferredName(),
        fact.getTypeName(),
        fact.getIdentifier(),
        fact.getVariantName());
  }

  public String getGndIdentifier() {
    return gndIdentifier;
  }

  public String getPreferredName() {
    return preferredName;
  }

  public String getTypeName() {
    return typeName;
  }

  public Set<Identifier> getIdentifier() {
    return identifier;
  }

  public Set<VariantName> getVariantName() {
    return variantName;
  }
}
