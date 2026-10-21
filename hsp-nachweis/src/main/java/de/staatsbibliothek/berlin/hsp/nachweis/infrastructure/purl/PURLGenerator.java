package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import java.net.URI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.22
 */
public interface PURLGenerator {

  PURL createNewPURL(URI target) throws PURLGeneratorException;

}
