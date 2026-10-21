package de.staatsbibliothek.berlin.hsp.nachweis.domain.person;

import lombok.Builder;
import org.eclipse.microprofile.graphql.Input;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.03.2026
 */

@Builder
@Input("PersonFilter")
public record PersonFilter(String id, String name, String preferredName, String variantName, String gndIdentifier) {

}
