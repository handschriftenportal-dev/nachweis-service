package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 26.10.2021
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LizenzViewModel implements Serializable {

  private static final long serialVersionUID = -1542640872037835500L;

  private Set<String> uris;

  private String beschreibungsText;
}
