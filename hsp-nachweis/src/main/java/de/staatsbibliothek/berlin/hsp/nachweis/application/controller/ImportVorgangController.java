package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.ImportVorgangModel;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.experimental.Delegate;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 10.06.2020.
 * @version 1.0
 */

@Named
@ViewScoped
@LoginCheck
public class ImportVorgangController implements Serializable {

  private static final long serialVersionUID = -3631421884917523426L;

  @Delegate
  ImportVorgangModel importVorgangModel;

  ImportVorgangController() {
  }

  @Inject
  public ImportVorgangController(ImportVorgangModel importVorgangModel) {
    this.importVorgangModel = importVorgangModel;
  }

  public String convertStatusToColor(IMPORTJOB_RESULT_VALUES status) {
    if (status == null) {
      return "none";
    }
    switch (status) {
      case SUCCESS:
        return "green";
      case FAILED:
        return "red";
      default:
        return "none";
    }
  }
}
