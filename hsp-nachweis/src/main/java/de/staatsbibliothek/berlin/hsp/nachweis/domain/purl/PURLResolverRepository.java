package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import java.util.Set;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */

public interface PURLResolverRepository {

  Set<PURL> createDBMFile(Set<PURL> purls) throws PURLException;

}
