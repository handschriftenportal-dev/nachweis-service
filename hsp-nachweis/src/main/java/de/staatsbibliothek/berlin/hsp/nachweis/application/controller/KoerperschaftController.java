package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KoerperschaftModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.experimental.Delegate;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.08.2020.
 * @version 1.0
 */

@ViewScoped
@Named
@LoginCheck
public class KoerperschaftController implements Serializable {

  private static final long serialVersionUID = -2707467478781797559L;

  @Delegate
  private KoerperschaftModel koerperschaftModel;

  @Inject
  KoerperschaftController(KoerperschaftModel koerperschaftModel) {
    this.koerperschaftModel = koerperschaftModel;
  }
}
