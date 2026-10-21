package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.TEIDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface KulturObjektDokumentBoundary {

  Optional<KulturObjektDokument> findById(String id);

  Optional<KulturObjektDokument> findByPurl(String purl);

  KulturObjektDokument findKulturObjektDokumentBySignaturForExterneRessourcenImport(
      String signatur,
      String besitzendeEinrichtung,
      String aufbewahrungsort)
      throws ExterneRessourcenImportException;

  Optional<KulturObjektDokument> findKodByBeschreibung(Beschreibung beschreibung)
      throws KulturObjektDokumentNotFoundException;

  List<KulturObjektDokument> findAll();

  long count();

  void delete(Bearbeiter bearbeiter, String... kulturObjektDokumentIds) throws Exception;

  void reindexAllKulturObjektDokumente(WerkzeugBatchJob event) throws Exception;

  Map<String, SignatureValue> getAllSignaturesWithKodIDs();

  List<KulturObjektDokument> registrieren(Bearbeiter bearbeiter, NormdatenReferenz ort,
      NormdatenReferenz koerperschaft,
      byte[] signaturen)
      throws KulturObjektDokumentRegistrierenException, SperreAlreadyExistException;

  List<KulturObjektDokument> registrieren(Bearbeiter bearbeiter, List<KulturObjektDokument> kods)
      throws KulturObjektDokumentRegistrierenException;

  KulturObjektSortkeyUpdateReport updateSortkeys(byte[] content, String originalFilename)
      throws KulturObjektSortkeyUpdateException;

  Optional<KulturObjektDokumentViewModel> buildKulturObjektDokumentViewModel(String id);

  Optional<KulturObjektDokument> digitalisatHinzufuegen(
      String kulturObjektDokumentId,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException, SperreAlreadyExistException;

  /**
   * Adds all given Digitalisate to a KOD (existing ones with the same URL are replaced) and
   * saves+indexes the KOD once. Used by the Digitalisat import to trigger only one distributed save
   * (validation, Solr, Kafka) per KOD instead of one per Digitalisat.
   */
  Optional<KulturObjektDokument> digitalisateHinzufuegen(
      String kulturObjektDokumentId,
      Collection<DigitalisatViewModel> digitalisate)
      throws KulturObjektDokumentException;

  Optional<KulturObjektDokument> digitalisatLoeschen(
      String kulturObjektDokumentId,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException, SperreAlreadyExistException;

  void saveKulturObjektDokument(KulturObjektDokument kod, ActivityStreamAction action)
      throws Exception;

  Sperre kulturObjektDokumentSperren(Bearbeiter bearbeiter, String sperreGrund,
      String kulturObjektDokumentID)
      throws DokumentSperreException, SperreAlreadyExistException;

  Optional<Sperre> findSperreForKulturObjektDokument(String kulturObjektDokumentID)
      throws DokumentSperreException;

  void kulturObjektDokumentEntsperren(String kulturObjektDokumentID) throws DokumentSperreException;

  void addPURL(String kodId, PURL purl) throws KulturObjektDokumentException;

  /**
   * The Sperre is caller-owned: it must already be held, and the caller releases it.
   *
   * @throws KulturObjektDokumentException when the url is invalid or the ressource already exists
   */
  Optional<KulturObjektDokument> externeRessourceHinzufuegen(
      String kulturObjektDokumentId,
      ExterneRessourceTyp typ,
      String url,
      Sperre sperre)
      throws KulturObjektDokumentException;

  void externeRessourceImportieren(String kulturObjektDokumentId, ExterneRessource externeRessource)
      throws ExterneRessourcenImportException;

  /**
   * Does nothing when the ressource is already absent. Sperre is caller-owned, see
   * {@link #externeRessourceHinzufuegen}.
   */
  void externeRessourceLoeschen(
      String kulturObjektDokumentId,
      ExterneRessourceTyp typ,
      String url,
      Sperre sperre)
      throws KulturObjektDokumentException;

  /**
   * Adds research documentation to a KulturObjektDokument. The Sperre is caller-owned: it must
   * already be held, and the caller releases it.
   *
   * @throws KulturObjektDokumentException when the resource or link is invalid, a duplicate
   *     exists, or the document cannot be saved
   */
  void forschungsdokumentationHinzufuegen(
      String kulturObjektDokumentId,
      String ressource,
      String link,
      Sperre sperre)
      throws KulturObjektDokumentException;

  /**
   * Deletes research documentation from a KulturObjektDokument. The Sperre is caller-owned and
   * must already be held.
   */
  void forschungsdokumentationLoeschen(
      String kulturObjektDokumentId,
      String ressource,
      String link,
      Sperre sperre)
      throws KulturObjektDokumentException;

  List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(int first,
      int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc);

  int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy);

  KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id);

  KulturObjektDokument kulturObjektDokumentAttributsreferenzenAktualisieren(
      KulturObjektDokumentViewModel kodViewModel)
      throws KulturObjektDokumentException;

  Optional<KulturObjektDokument> gueltigeSignaturVeraendern(String neueGueltigeSignatur,
      String kodID, Sperre sperre) throws Exception;

  Optional<KulturObjektDokument> alternativeSignaturHinzufuegen(String neueAlternativeSignatur,
      String kodID, Sperre sperre) throws Exception;

  void checkIfSignaturAlreadyExists(String neueGueltigeSignatur, String besitzerID)
      throws KulturObjektDokumentException;

  List<KulturobjektDokumentUmziehenErgebnis> pruefeKODUmziehen(Map<String, String> signaturenMap,
      NormdatenReferenz neuerBesitzer, NormdatenReferenz neuerOrt);

  void umziehen(List<KulturobjektDokumentUmziehenErgebnis> pruefungsErgebnis, Bearbeiter bearbeiter)
      throws Exception;

  boolean isAltIdentifikationDeletable(String altIdentifikation);

  void deleteAltIdentifikation(Bearbeiter bearbeiter, String kodId, String altIdentifikationId)
      throws Exception;

  void attributsReferenzenAusBeschreibungenAktualisieren(KulturObjektDokument kod,
      AttributsReferenzStatusTyp attributsReferenzStatusTyp, String changeID)
      throws HSPMapperException;

  Map<AttributsTyp, AttributsReferenz> loadAttributsReferenzenFromTei(
      TEIDokument teiDokument) throws HSPMapperException;

  Set<KulturObjekteValidationReportItem> findAllInvalid();

  KulturObjektDokument updateKODXML(String id) throws Exception;
}
