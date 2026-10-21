package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.datatype;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PersonModel;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: Christoph Marten on 10.03.2021 at 13:02
 */
@ViewScoped
@Slf4j
@Named
public class PersonDataTypeViewController implements Serializable {

  @Serial
  private static final long serialVersionUID = 8837144449085033224L;

  @Delegate
  PersonModel personModel;

  @Inject
  PersonDataTypeViewController(PersonModel personModel) {
    this.personModel = personModel;
  }
}
