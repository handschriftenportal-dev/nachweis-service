package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KatalogModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.experimental.Delegate;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 */

@ViewScoped
@Named
@LoginCheck
public class KatalogDatatypController {

  @Delegate
  KatalogModel katalogModel;

  @Inject
  public KatalogDatatypController(
      KatalogModel katalogModel) {
    this.katalogModel = katalogModel;
  }
}
