package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;

/**
 * Pairs a KulturObjektDokument with one Digitalisat parsed from the import. Replaces the former single-entry
 * {@code Map<KulturObjektDokument, DigitalisatViewModel>} carrier.
 */
public record KodDigitalisat(KulturObjektDokument kod, DigitalisatViewModel digitalisat) {

}
