package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SperrenModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.08.21
 */

@ViewScoped
@Named
@LoginCheck
@Slf4j
public class SperrenController implements Serializable {

  private static final long serialVersionUID = 2375122691079944674L;

  @Delegate
  private SperrenModel sperrenModel;

  @Inject
  SperrenController(SperrenModel sperrenModel) {
    this.sperrenModel = sperrenModel;
  }

}
