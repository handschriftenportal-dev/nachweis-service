package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import java.util.List;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */
public interface BestandsBoundary {

  BestandsView createBestandsView();

  void loadBestaendeOfAllEntities(BestandsView bestandsView);

  List<BestandInstitutionElement> loadBestandsInstitutionDataForKulturobjektdokument();

  List<BestandInstitutionElement> loadBestandsInstitutionDataForBeschreibungen();

  List<BestandInstitutionElement> loadBestandsInstitutionDataForDigitalisate();
}
