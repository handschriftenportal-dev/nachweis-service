package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.datatype;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.BeschreibungModel;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Christoph.Marten
 * @since 12.01.21
 */
@ViewScoped
@Named
@Slf4j
public class BeschreibungsDataTypeViewController implements Serializable {

  private static final long serialVersionUID = -8264178794627229067L;

  private BeschreibungModel model;

  BeschreibungsDataTypeViewController() {
  }

  @Inject
  public BeschreibungsDataTypeViewController(BeschreibungModel model) {
    this.model = model;
  }

  public BeschreibungModel getModel() {
    return model;
  }
}
