package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 26.02.2020.
 */
@Data
@AllArgsConstructor
public class Page implements Serializable {

  @Serial
  private static final long serialVersionUID = -2364598440258189353L;

  private int numberOfRows;
  private int offset;

}
