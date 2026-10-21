package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
public interface BeschreibungsBoundary {

  Optional<Beschreibung> findById(String id);

  Optional<byte[]> findTeiSnapshotZip(String beschreibungId);

  Optional<Beschreibung> findByPublikation(Publikation publikation);

  List<Beschreibung> findAll();

  List<BeschreibungListDbView> findLatestModifiedTopTen();

  List<BeschreibungListDbView> findLatestModifiedTopTenByBearbeiter(
      String bearbeiter);

  long count();

  SignatureValue getSignatureForBeschreibungId(String beschreibungId);

  Map<String, SignatureValue> getAllSignaturesWithBeschreibungIDs();

  Optional<BeschreibungsViewModel> buildBeschreibungsViewModel(String id);

  void delete(Bearbeiter bearbeiter, Beschreibung... beschreibung)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException;

  void delete(Bearbeiter bearbeiter, Beschreibung beschreibung)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException;

  void deleteBeschreibung(Bearbeiter bearbeiter, String beschreibungID)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException;

  void updateBeschreibungMitXML(String id, String xml, String bearbeiter, boolean validateWithOdd,
      Locale locale)
      throws BeschreibungsException, DokumentSperreException;

  /**
   * Propagates all side-effects of a just-published Beschreibung: refreshes the Beschreibung's TEI,
   * recalculates the KOD's Attributsreferenzen, persists the KOD, pushes Beschreibung + KOD to
   * Solr, and sends the KOD (with its published Beschreibungen) to the Kafka indexing topic.
   *
   * <p>All steps run in a single transaction so DB, Solr, and Kafka commit together. Failures
   * propagate to the caller, allowing rollback.
   *
   * @throws PublikationException  if the associated KOD is not found or if the Solr/Kafka
   *                               propagation fails
   * @throws IllegalStateException if the KOD is not indexable after the publish step
   */
  void publizieren(Beschreibung beschreibung)
      throws de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;

  ValidationResult validateXML(String xmlTEI, String schema, String locale);

  Beschreibung beschreibungErstellen(Bearbeiter bearbeiter, String kodid,
      NormdatenReferenz sprache,
      Set<NormdatenReferenz> autoren,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary)
      throws BeschreibungsException;

  Sperre beschreibungSperren(Bearbeiter bearbeiter, String beschreibungsID,
      SperreTyp sperreTyp, String reason)
      throws DokumentSperreException;

  default Sperre beschreibungSperrenForEdit(Bearbeiter bearbeiter, String beschreibungsID)
      throws DokumentSperreException {
    return beschreibungSperren(bearbeiter, beschreibungsID, SperreTyp.MANUAL,
        "BeschreibungBearbeiten");
  }

  default Sperre beschreibungSperrenForApproval(Bearbeiter bearbeiter, String beschreibungsID)
      throws DokumentSperreException {
    return beschreibungSperren(bearbeiter, beschreibungsID, SperreTyp.APPROVAL,
        "Publikation");
  }

  Optional<Sperre> findSperreForBeschreibung(String beschreibungsID)
      throws DokumentSperreException;

  void beschreibungEntsperren(String beschreibungsID)
      throws DokumentSperreException;

  default boolean isBeschreibungGesperrt(String beschreibungsID) throws DokumentSperreException {
    return findSperreForBeschreibung(beschreibungsID).isPresent();
  }

  void reindexAllBeschreibungen(WerkzeugBatchJob event);

  void addPURL(String beschreibungId, PURL purl) throws BeschreibungsException;

  List<BeschreibungListDbView> findFilteredBeschreibungListDTOs(int first,
      int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredBeschreibungListDTOs(Map<String, String> filterBy);

  BeschreibungListDbView findBeschreibungListDTOId(String id);

  void addBearbeiterToBeschreibung(String beschreibungID,
      Set<String> bearbeiterID)
      throws BeschreibungsException;

  /**
   * Write-permission check: logged-in Bearbeiter is assigned AND no foreign Sperre blocks writes.
   * Use this to gate write actions (save, delete, withdraw). For read-only visibility checks, use
   * {@link #isBeschreibungAssignedToLoggedInBearbeiter}.
   *
   * <p><b>Fail-closed:</b> if the Sperre lookup throws (DB/state error), returns {@code false}
   * (write denied) so we never bypass locks on inconsistent state. UI hides the action.
   */
  boolean isBeschreibungActionableForLoggedInBearbeiter(String beschreibungID);

  /**
   * Read-only assignment check: logged-in Bearbeiter is assigned to the Beschreibung. Does not
   * consider Sperren — use this for read-only operations like viewing publication history. For
   * write operations, use {@link #isBeschreibungActionableForLoggedInBearbeiter}.
   */
  boolean isBeschreibungAssignedToLoggedInBearbeiter(String beschreibungID);

  /**
   * Checks if the currently logged-in bearbeiter can delete the given Beschreibung. Combines
   * permission check with bearbeiter assignment check.
   */
  boolean isBeschreibungDeletableForLoggedInBearbeiter(String beschreibungId);

  void removeBearbeiterFromBeschreibung(String beschreibungID,
      String bearbeiterID)
      throws BeschreibungsException, DokumentSperreException;

  Set<Bearbeiter> findBearbeiterEineBeschreibung(String beschreibungID)
      throws BeschreibungsException;


  Set<BeschreibungValidationReportItem> findAllInvalidODD();

  Set<BeschreibungValidationReportItem> findAllInvalidALL();

  Stream<? extends XMLDokument> selectAllTeiXml();

  List<Beschreibung> findAllBeschreibungWithActiveApprovalRequest();

  List<BeschreibungListDbView> findMeinePublizierten(String bearbeiter);

  List<BeschreibungListDbView> findLatestPublikationen();

  List<BeschreibungInReviewDbView> findMeineImReview(String bearbeiter);

  List<BeschreibungRejectedDbView> findMeineZurUeberarbeitung(String bearbeiter);
}
