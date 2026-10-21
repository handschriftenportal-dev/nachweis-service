package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import jakarta.enterprise.event.Observes;
import java.util.List;
import java.util.Optional;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public interface ImportBoundary {

  List<BeschreibungImport> alleImporteAnzeigen();

  Optional<BeschreibungImport> findById(String id);

  void importeUebernehmen(String beschreibungImportId, String verwaltungsTyp)
      throws BeschreibungsImportException;

  void importeAblehnen(List<String> importe) throws BeschreibungsImportException;

  Optional<Beschreibung> findExistingBeschreibung(Beschreibung beschreibung);

  List<Beschreibung> convertTEI2Beschreibung(ActivityStreamObjectDTO streamObject) throws Exception;

  void onImportMessage(@Observes @neueImporte BeschreibungImport beschreibungImportJob)
      throws BeschreibungsImportException;

  boolean isAutomatischenUebernahmeAktiv();

  void setAutomatischenUebernahmeAktiv(boolean automatischenUebernahmeAktiv);
}
