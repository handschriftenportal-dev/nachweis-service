package de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.11.22
 */

public interface GeloeschtesDokumentRepository extends CRUDRepository<GeloeschtesDokument, String> {

}
