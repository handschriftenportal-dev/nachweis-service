package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 06.12.2021
 */

@Getter
@EqualsAndHashCode(of = {"value"})
@AllArgsConstructor
@ToString
public class Facette implements Serializable {

  private static final long serialVersionUID = 647569139902907911L;

  private final String value;

  private final int amount;

}
