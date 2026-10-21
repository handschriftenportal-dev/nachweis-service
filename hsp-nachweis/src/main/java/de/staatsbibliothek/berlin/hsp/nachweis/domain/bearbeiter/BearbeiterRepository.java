package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.09.21
 */
public interface BearbeiterRepository extends CRUDRepository<Bearbeiter, String>, Serializable {

  Bearbeiter findByBearbeiterName(String bearbeitername);

  List<Bearbeiter> findByBearbeiterName(Collection<String> bearbeitername);

}
