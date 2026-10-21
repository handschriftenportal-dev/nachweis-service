package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 * <p>
 * Central Boundary Interface for Catalog Use Cases
 */
public interface KatalogBoundary {

  Collection<Katalog> findAll();

  Katalog importieren(Bearbeiter bearbeiter, Katalog katalog) throws KatalogException;

  Optional<KatalogViewModel> buildKatalogViewModel(String katalogId);

  Set<KatalogValidationReportItem> findAllInvalid();

}
