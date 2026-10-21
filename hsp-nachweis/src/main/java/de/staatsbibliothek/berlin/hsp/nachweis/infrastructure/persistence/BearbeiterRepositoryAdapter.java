package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.09.21
 */
@ApplicationScoped
public class BearbeiterRepositoryAdapter implements BearbeiterRepository {

  @Serial
  private static final long serialVersionUID = 4220764757371621792L;

  public Bearbeiter findByBearbeiterName(String bearbeitername) {
    return find("bearbeitername", bearbeitername).firstResult();
  }

  public List<Bearbeiter> findByBearbeiterName(Collection<String> bearbeitername) {
    return list("bearbeitername in ?1", bearbeitername);
  }

}
