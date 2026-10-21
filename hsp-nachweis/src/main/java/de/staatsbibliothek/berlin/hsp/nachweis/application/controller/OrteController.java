package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.OrteModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import lombok.experimental.Delegate;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */

@ViewScoped
@Named
@LoginCheck
public class OrteController implements Serializable {

  @Serial
  private static final long serialVersionUID = 875395979591841095L;

  @Delegate
  OrteModel orteModel;

  @Inject
  OrteController(OrteModel orteModel) {
    this.orteModel = orteModel;
  }
}
