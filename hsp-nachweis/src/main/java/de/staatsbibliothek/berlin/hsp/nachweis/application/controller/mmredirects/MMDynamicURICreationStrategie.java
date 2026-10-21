package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import java.net.URI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.10.23.
 * @version 1.0
 */
public interface MMDynamicURICreationStrategie {

  URI create(String path);

}
