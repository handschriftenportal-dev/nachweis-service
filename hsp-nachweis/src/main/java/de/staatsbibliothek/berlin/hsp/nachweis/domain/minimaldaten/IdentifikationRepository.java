package de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 06.08.21
 */
public interface IdentifikationRepository extends CRUDRepository<Identifikation, String> {

  SignatureValue getSignatureByBeschreibungID(String beschreibungsID);

  Map<String, SignatureValue> getAllSignaturesWithBeschreibungIDs();

  Map<String, SignatureValue> getAllSignaturesWithKodIDs();

  Optional<SignatureValue> getGueltigeSignatureByKodID(String kodID);

  Set<SignatureValue> getAlternativeSignaturesByKodID(String kodID);

  boolean isAltIdentifikationDeletable(String altIdentifikationId);

}
