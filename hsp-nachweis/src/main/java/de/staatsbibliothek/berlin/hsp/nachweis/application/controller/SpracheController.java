package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SpracheModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.experimental.Delegate;

/**
 * @author konstantin.goerlitz@sbb.spk-berlin.de
 */

@ViewScoped
@Named
@LoginCheck
public class SpracheController implements Serializable {

  private static final long serialVersionUID = -2621820654237299711L;

  @Delegate
  private SpracheModel spracheModel;

  @Inject
  SpracheController(SpracheModel spracheModel) {
    this.spracheModel = spracheModel;
  }
}
