package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import lombok.Builder;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 18.08.2026
 */
@Builder
public record ExterneRessourcenImportEntry(
    String aufbewahrungsort,
    String besitzendeEinrichtung,
    String fuehrendeSignatur,
    String kodUuid,
    String resolverAdresse,
    String ressource,
    String link,
    int lineNumber) {

}
