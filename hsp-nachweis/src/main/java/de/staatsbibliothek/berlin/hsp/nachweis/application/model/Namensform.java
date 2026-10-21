package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.06.2020.
 * @version 1.0
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Namensform {

  private String name;

  private String languageCode;

}
