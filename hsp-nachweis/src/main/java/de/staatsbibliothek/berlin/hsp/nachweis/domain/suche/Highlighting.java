package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class represents the Apache Solr Highlighting Config Parameter
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 14.08.2020.
 * @version 1.0
 */

@Data
@AllArgsConstructor
public class Highlighting implements Serializable {

  @Serial
  private static final long serialVersionUID = -8785241195547357575L;

  private boolean active;

  private String method;

  private List<String> fields;

}
