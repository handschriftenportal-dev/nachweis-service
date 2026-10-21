package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.06.23.
 * @version 1.0
 */

@Named
@SessionScoped
@Slf4j
public class NavigationModel implements Serializable {

  @Serial
  private static final long serialVersionUID = -950457628764779711L;

  private boolean dataTypeSubmenuOpen;

  private boolean importSubmenuOpen;

  private boolean redactionSubmenuOpen;

  private boolean otherSubmenuOpen;

  private final Set<String> dataTypeSubmenuItems = Set.of("/kulturobjekte.xhtml",
      "/beschreibung/beschreibungen.xhtml", "/orte.xhtml", "/koerperschaften.xhtml",
      "/person/personen.xhtml", "/sprachen.xhtml", "/kataloge.xhtml");

  private final Set<String> importSubmenuItems = Set.of("/dateiupload.xhtml",
      "/importvorgaenge.xhtml");

  private final Set<String> redactionSubmenuItems = Set.of("/datenuebernahme.xhtml",
      "/kod-verwalten.xhtml", "/purls.xhtml", "/werkzeuge.xhtml",
      "/schemapflege.xhtml", "/papierkorb.xhtml");

  private final Set<String> otherSubmenuItems = Set.of("/sperren.xhtml", "/bestand.xhtml",
      "/datensicherung.xhtml", "/barrierefreiheit.xhtml");

  public boolean isDataTypeSubmenuOpen() {
    return dataTypeSubmenuOpen;
  }

  public boolean isImportSubmenuOpen() {
    return importSubmenuOpen;
  }

  public boolean isRedactionSubmenuOpen() {
    return redactionSubmenuOpen;
  }

  public boolean isOtherSubmenuOpen() {
    return otherSubmenuOpen;
  }

  public void onClickHandler(String viewId) {

    log.info("Getting onClick Event {}", viewId);

    this.dataTypeSubmenuOpen = dataTypeSubmenuItems.contains(viewId);

    this.importSubmenuOpen = importSubmenuItems.contains(viewId);

    this.redactionSubmenuOpen = redactionSubmenuItems.contains(viewId);

    this.otherSubmenuOpen = otherSubmenuItems.contains(viewId);
  }
}
