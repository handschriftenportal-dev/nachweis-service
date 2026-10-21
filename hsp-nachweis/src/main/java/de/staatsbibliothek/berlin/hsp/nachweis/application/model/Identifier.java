package de.staatsbibliothek.berlin.hsp.nachweis.application.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 13.08.20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Identifier {

  private String text;
  private String url;
  private String type;

}
