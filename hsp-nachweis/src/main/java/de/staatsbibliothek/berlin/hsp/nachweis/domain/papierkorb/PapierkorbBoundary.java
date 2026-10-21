package de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import java.util.List;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.11.22
 */

public interface PapierkorbBoundary {

  String erzeugeGeloeschtesDokument(KulturObjektDokument kulturObjektDokument, Bearbeiter bearbeiter);

  String erzeugeGeloeschtesDokument(Beschreibung beschreibung, Bearbeiter bearbeiter);

  List<GeloeschtesDokument> findAllGeloeschteDokumente();
}
