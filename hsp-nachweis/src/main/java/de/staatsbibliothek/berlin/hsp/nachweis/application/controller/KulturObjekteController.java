package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjekteModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 27.11.20
 */
@ViewScoped
@Named
@Slf4j
@LoginCheck
public class KulturObjekteController implements Serializable {

  private static final long serialVersionUID = -8264178794627229067L;

  private KulturObjekteModel model;

  KulturObjekteController() {
  }

  @Inject
  public KulturObjekteController(KulturObjekteModel model) {
    this.model = model;
  }

  public KulturObjekteModel getKulturObjekteModel() {
    return model;
  }

}
