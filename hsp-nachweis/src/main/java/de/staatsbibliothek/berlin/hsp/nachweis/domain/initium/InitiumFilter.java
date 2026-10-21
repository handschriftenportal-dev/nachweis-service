package de.staatsbibliothek.berlin.hsp.nachweis.domain.initium;

import lombok.Builder;
import org.eclipse.microprofile.graphql.Input;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 05.01.2026
 */

@Builder
@Input("InitiumFilter")
public record InitiumFilter(String id, String text, String language) {
}
