package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */

public interface PURLRepository extends CRUDRepository<PURL, String> {

  List<PURLViewModel> findAllAsViewModels();

  Optional<PURL> findByPurlOptional(URI purl);

  Set<PURL> findByDokumentIdAndPURLTyp(String dokumentId, PURLTyp purlTyp);
}
