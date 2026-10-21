package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.09.21
 */
public interface BearbeiterBoundary {

  String SYSTEM_USERNAME = "System";

  Bearbeiter getLoggedBearbeiter();

  Set<Bearbeiter> getAllBearbeiter();

  Bearbeiter getUnbekannterBearbeiter();

  Bearbeiter findBearbeiterWithName(String name);

  List<Bearbeiter> findBearbeiterByNames(Collection<String> names);

  List<Bearbeiter> findBearbeiterByFirstnameOrLastname(String name);

  Bearbeiter updateLoggedBearbeiter(NormdatenReferenz person, NormdatenReferenz institution)
      throws BearbeiterException;

  boolean isBearbeiterNew();

  Authentication getAuthentication();

  String getRole();

  void logout();

  void startLogin();

  String getConfig(String username);

  void setConfig(String config, String username);
}
