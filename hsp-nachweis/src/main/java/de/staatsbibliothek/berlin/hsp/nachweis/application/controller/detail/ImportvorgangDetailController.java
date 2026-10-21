package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.09.20
 */
@ViewScoped
@Named
public class ImportvorgangDetailController implements Serializable {

  private static final long serialVersionUID = -3490539787210253062L;

  private ImportVorgangBoundary importVorgangBoundary;

  private ImportVorgang selectedVorgang;


  ImportvorgangDetailController() {
  }

  @Inject
  public ImportvorgangDetailController(ImportVorgangBoundary importVorgangBoundary) {
    this.importVorgangBoundary = importVorgangBoundary;
  }

  @PostConstruct
  public void setup() {
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    if (Objects.nonNull(currentInstance)) {
      ExternalContext externalContext = currentInstance.getExternalContext();
      if (Objects.nonNull(externalContext)) {
        String id = externalContext.getRequestParameterMap().get("id");
        fillImportVorgang(id);
      }
    }
  }

  void fillImportVorgang(String id) {
    Optional<ImportVorgang> byId = importVorgangBoundary.findById(id);
    byId.ifPresent(importVorgang -> selectedVorgang = importVorgang);
  }

  public ImportVorgang getSelectedVorgang() {
    return selectedVorgang;
  }

  public void setSelectedVorgang(ImportVorgang selectedVorgang) {
    this.selectedVorgang = selectedVorgang;
  }

  public boolean isError() {
    if (selectedVorgang == null) {
      return false;
    }
    return selectedVorgang.isError();
  }
}
