package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.InitiumModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import lombok.experimental.Delegate;

@ViewScoped
@Named
@LoginCheck
public class InitiumController implements Serializable {

  @Serial
  private static final long serialVersionUID = 6234871692057089134L;

  @Delegate
  private final InitiumModel initiumModel;

  @Inject
  public InitiumController(InitiumModel initiumModel) {
    this.initiumModel = initiumModel;
  }
}
