package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DatenuebernahmeModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.experimental.Delegate;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */

@ViewScoped
@Named
@LoginCheck
public class DatenuebernahmeController implements Serializable {

  private static final long serialVersionUID = 875395979591841095L;

  @Delegate
  private DatenuebernahmeModel datenuebernahmeModel;

  DatenuebernahmeController() {
  }

  @Inject
  public DatenuebernahmeController(DatenuebernahmeModel datenuebernahmeModel) {
    this.datenuebernahmeModel = datenuebernahmeModel;
  }


}
