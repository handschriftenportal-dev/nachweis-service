package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.KOD_ID_COLLECTION;
import static de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory.unmarshalOne;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_BEARBEITER_ENTZIEHEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIBeschreibungCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BeschreibungsTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexAllBeschreibungenForNachweis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.ServiceNotAvailableException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 */
@Slf4j
@ApplicationScoped
public class BeschreibungsService implements BeschreibungsBoundary,
    Serializable {

  public static final String SCHEMA_ODD = "ODD";
  public static final String PREFIX_MXML = "MXML-";
  public static final String DELETE_BESCHREIBUNG = "Delete Beschreibung ";
  @Serial
  private static final long serialVersionUID = 7896727068740214139L;
  private static final String BESCHREIBUNGSERVICE_NO_BESCHREIBUNG_FOR_ID = "beschreibungservice_no_beschreibung_for_id";
  private static final String MEDIEVAL = "beschreibunginitial_medieval.xml";

  static {
    System.setProperty(
        "javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
  }

  @Inject
  @Setter
  transient PublikationsService publikationsService;
  private BeschreibungsRepository beschreibungsRepository;
  private IdentifikationRepository identifikationRepository;
  private KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private BearbeiterBoundary bearbeiterBoundary;
  private BeschreibungsRechte beschreibungsRechte;
  private transient IndexJobBoundary indexService;
  private transient SuchDokumentService suchDokumentService;
  @Setter
  private transient DokumentSperreBoundary dokumentSperreBoundary;
  private transient PapierkorbBoundary papierkorbBoundary;
  private transient TeiXmlValidationBoundary teiXmlValidationBoundary;
  private SchemaPflegeDateiServicePort schemaPflegeDateiServicePort;
  private transient BeschreibungsSynchronizer beschreibungsSynchronizer;
  private transient KafkaIndexingProducer kafkaIndexingProducer;

  public BeschreibungsService() {
  }

  @Inject
  public BeschreibungsService(
      BeschreibungsRepository beschreibungsRepository,
      IdentifikationRepository identifikationRepository,
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      SuchDokumentService suchDokumentService,
      IndexJobBoundary indexService,
      DokumentSperreBoundary dokumentSperreBoundary,
      PapierkorbBoundary papierkorbBoundary,
      TeiXmlValidationBoundary teiXmlValidationBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      BeschreibungsRechte beschreibungsRechte,
      @RestClient SchemaPflegeDateiServicePort schemaPflegeDateiServicePort,
      BeschreibungsSynchronizer beschreibungsSynchronizer,
      KafkaIndexingProducer kafkaIndexingProducer) {
    this.beschreibungsRepository = beschreibungsRepository;
    this.identifikationRepository = identifikationRepository;
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.suchDokumentService = suchDokumentService;
    this.indexService = indexService;
    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.papierkorbBoundary = papierkorbBoundary;
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.beschreibungsRechte = beschreibungsRechte;
    this.schemaPflegeDateiServicePort = schemaPflegeDateiServicePort;
    this.beschreibungsSynchronizer = beschreibungsSynchronizer;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
  }

  public static void addBeschreibungToActivityStreamMessage(
      ActivityStream message, Beschreibung beschreibung)
      throws ActivityStreamsException {

    Objects.requireNonNull(message, "message is required!");
    Objects.requireNonNull(beschreibung, "beschreibung is required!");

    ActivityStreamObject activityStreamObjectBeschreibung =
        ActivityStreamObject.builder()
            .withId(beschreibung.getId())
            .withContent(beschreibung.getTeiXML())
            .withCompressed(true)
            .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
            .build();
    message.addObject(activityStreamObjectBeschreibung);
  }

  @Override
  public Optional<Beschreibung> findById(String id) {

    if (id != null && !id.isEmpty() && beschreibungsRepository != null) {
      return beschreibungsRepository.findByIdOptional(id);
    }

    return Optional.empty();
  }

  @Override
  public Optional<byte[]> findTeiSnapshotZip(String beschreibungId) {
    if (publikationsService == null) {
      return Optional.empty();
    }
    return publikationsService.findTeiSnapshotZip(beschreibungId);
  }

  @Override
  public Optional<Beschreibung> findByPublikation(Publikation publikation) {
    if (publikation == null || beschreibungsRepository == null) {
      return Optional.empty();
    }
    return beschreibungsRepository.findByPublikation(publikation);
  }

  @Override
  public List<Beschreibung> findAll() {
    return beschreibungsRepository.listAll(true);
  }

  @Override
  public List<BeschreibungListDbView> findLatestModifiedTopTen() {
    return this.findLatestModifiedTopTenByBearbeiter(null);
  }

  @Override
  public List<BeschreibungListDbView> findLatestModifiedTopTenByBearbeiter(
      String bearbeiter) {
    try {
      return beschreibungsRepository.findLatestModifiedByBearbeiter(10,
          bearbeiter);
    } catch (Exception e) {
      log.error("Error finding latest modified Beschreibungen", e);
      return Collections.emptyList();
    }
  }

  @Override
  public long count() {
    return beschreibungsRepository.count();
  }

  @Override
  public SignatureValue getSignatureForBeschreibungId(String beschreibungId) {
    return identifikationRepository.getSignatureByBeschreibungID(
        beschreibungId);
  }

  @Override
  public Map<String, SignatureValue> getAllSignaturesWithBeschreibungIDs() {
    return identifikationRepository.getAllSignaturesWithBeschreibungIDs();
  }

  @Override
  public Optional<BeschreibungsViewModel> buildBeschreibungsViewModel(
      String id) {

    Optional<BeschreibungsViewModel> result = findById(id)
        .map(beschreibung -> {
          Set<String> bearbeiterIDs = beschreibung.getBearbeiterIDs();
          List<Bearbeiter> bearbeiterList = (bearbeiterIDs != null && !bearbeiterIDs.isEmpty())
              ? bearbeiterBoundary.findBearbeiterByNames(bearbeiterIDs)
              : List.of();

          String kodSignatur = Optional.ofNullable(beschreibung.getKodID())
              .flatMap(kulturObjektDokumentRepository::findByIdOptional)
              .map(KulturObjektDokument::getGueltigeSignatur)
              .orElse("");

          return new BeschreibungsViewBuilder(beschreibung)
              .withKODSignatur(kodSignatur)
              .withBearbeiter(bearbeiterList)
              .build();
        });

    result.ifPresent(model -> log.debug("BeschreibunsViewModel loaded {}", model));

    return result;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void delete(Bearbeiter bearbeiter, Beschreibung... beschreibungen)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException {
    Set<KulturObjektDokument> kods = Arrays.stream(beschreibungen)
        .map(b -> kulturObjektDokumentRepository.findById(b.getKodID()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    SperreEintrag[] sperreEintraege = dokumentSperreBoundary
        .createSperreEintraege(kods, Set.of(beschreibungen))
        .toArray(new SperreEintrag[0]);

    dokumentSperreBoundary.acquireSperre(
        bearbeiter, SperreTyp.IN_TRANSACTION, "deleteBeschreibungen",
        sperreEintraege);

    for (Beschreibung beschreibung : beschreibungen) {
      deleteBeschreibung(bearbeiter, beschreibung.getId());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void delete(Bearbeiter bearbeiter, Beschreibung beschreibung)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException {

    KulturObjektDokument kod = kulturObjektDokumentRepository.findById(beschreibung.getKodID());

    SperreEintrag[] sperreEintraege = dokumentSperreBoundary
        .createSperreEintraege(Set.of(kod), Set.of(beschreibung))
        .toArray(new SperreEintrag[0]);

    dokumentSperreBoundary.acquireSperre(
        bearbeiter, SperreTyp.IN_TRANSACTION, "deleteBeschreibungen",
        sperreEintraege);

    deleteBeschreibung(bearbeiter, beschreibung.getId());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void deleteBeschreibung(Bearbeiter bearbeiter, String beschreibungsId)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException {
    Optional<Beschreibung> beschreibungOptional =
        beschreibungsRepository.findByIdOptional(beschreibungsId);

    if (beschreibungOptional.isPresent()) {
      Beschreibung beschreibung = beschreibungOptional.get();
      dokumentSperreBoundary.acquireSperre(
          bearbeiter, SperreTyp.IN_TRANSACTION, "deleteBeschreibung",
          beschreibung);
      try {

        Optional<KulturObjektDokument> kulturObjektDokumentOptional =
            kulturObjektDokumentRepository.findByBeschreibungOptional(
                beschreibungsId);
        if (kulturObjektDokumentOptional.isPresent()) {
          KulturObjektDokument kulturObjektDokument = kulturObjektDokumentOptional.get();

          kulturObjektDokument.removeBeschreibungsdokument(beschreibungsId);
          TEIKulturObjektDokumentCommand.updateKODSourceDescWithRemoveBeschreibungMsDesc(
              kulturObjektDokument, beschreibungsId);
          TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(
              kulturObjektDokument);
          Change change = TEIKODAttributsReferenzCommand.createChangeElement(
              HSP_UUI_Factory.generate(), LocalDateTime.now(),
              AttributsReferenzStatusTyp.MANUELL,
              DELETE_BESCHREIBUNG + beschreibungsId,
              bearbeiterBoundary.getLoggedBearbeiter());
          TEIKODAttributsReferenzCommand.deleteAttributsReferenzenForBeschreibung(
              kulturObjektDokument, beschreibungsId, change);
          teiXmlValidationBoundary.validateTeiXml(kulturObjektDokument);
          kulturObjektDokumentRepository.saveAndFlush(kulturObjektDokument);
          suchDokumentService.kodUebernehmen(kulturObjektDokument);
          if (beschreibung.isPublished()) {
            indexKulturObjektDokument(kulturObjektDokument);
          }
        }

        String loeschId = papierkorbBoundary.erzeugeGeloeschtesDokument(
            beschreibung, bearbeiter);
        log.debug("created GeloeschtesDokument {} for Beschreibung {}",
            loeschId, beschreibungsId);

        beschreibungsRepository.deleteByIdAndFlush(beschreibungsId);
        suchDokumentService.dokumentLoeschen(beschreibungsId);

      } catch (Exception e) {
        log.error(
            "Unable to delete Beschreibung with ID = {} : {}", beschreibungsId,
            e.getMessage(), e);
        throw new BeschreibungsException(
            "Unable to delete Beschreibung with ID = " + beschreibungsId + " : "
                + e.getMessage(),
            e);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {BeschreibungsException.class})
  public void updateBeschreibungMitXML(String id, String xml, String bearbeiterId,
      boolean validateWithOdd,
      Locale locale)
      throws BeschreibungsException, DokumentSperreException {
    log.info("updateBeschreibungMitXML: id={}", id);

    Objects.requireNonNull(id, "id is required!");
    Objects.requireNonNull(xml, "xml is required!");
    Objects.requireNonNull(xml, "locale is required!");

    var beschreibung = beschreibungsRepository.findById(id);
    var bearbeiter = bearbeiterBoundary.findBearbeiterWithName(bearbeiterId);

    if (!beschreibung.hasBearbeiter(bearbeiter)) {
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_updatexml_no_bearbeiter_rights",
              bearbeiterId, id));
    }
    dokumentSperreBoundary.acquireSperre(
        bearbeiter, SperreTyp.MANUAL, "updateBeschreibungREST",
        beschreibung
    );

    Beschreibung originalBeschreibung =
        findById(id)
            .orElseThrow(
                () ->
                    new BeschreibungsException(
                        getMessage(locale,
                            "beschreibungservice_updatexml_no_beschreibung",
                            id)));

    Beschreibung mappedBeschreibung = mapBeschreibungFromXML(xml, locale);

    if (!id.equals(mappedBeschreibung.getId())) {
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_updatexml_wrong_beschreibung",
              id));
    }

    mappedBeschreibung.setBeschreibungsSprache(
        originalBeschreibung.getBeschreibungsSprache());
    mappedBeschreibung.setErstellungsDatum(
        originalBeschreibung.getErstellungsDatum());
    mappedBeschreibung.setBearbeiterIDs(
        originalBeschreibung.getBearbeiterIDs());
    originalBeschreibung
        .getGueltigeIdentifikation()
        .ifPresent(mappedBeschreibung::setGueltigeIdentifikation);
    mappedBeschreibung.getAutoren()
        .removeIf(autor -> autor.getId() == null || autor.getId().isEmpty());

    originalBeschreibung.getPURLs().stream()
        .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
        .forEach(
            purlInternal -> mappedBeschreibung.getPURLs().add(purlInternal));

    try {
      mappedBeschreibung.setAenderungsDatum(LocalDateTime.now());
      mappedBeschreibung.findKopf()
          .ifPresent(
              BeschreibungsKomponenteKopf::removeGrundspracheWithoutNORMUUID);
      TEIBeschreibungCommand.openTEIDokument(mappedBeschreibung);
      TEIBeschreibungCommand.updateAenderungsdatum(mappedBeschreibung);
      TEIBeschreibungCommand.writeXML(mappedBeschreibung);

      teiXmlValidationBoundary.validateTeiXml(mappedBeschreibung, validateWithOdd);

      saveBeschreibung(mappedBeschreibung);
    } catch (Exception error) {
      log.error("Error during reading xml!", error);
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_updatexml_error",
              error.getMessage()), error);
    }
  }

  @Override
  public ValidationResult validateXML(String xmlTEI, String schema,
      String locale) {
    ValidationResult validationResult;

    try {
      validationResult =
          teiXmlValidationBoundary.validateTeiXml(xmlTEI, locale,
              SCHEMA_ODD.equals(schema));
    } catch (Exception e) {
      log.error("Error validating xml!", e);

      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));

      validationResult =
          new ValidationResult(
              false,
              e.getMessage(),
              sw.toString(),
              getMessage(Locale.forLanguageTag(locale),
                  "validation_result_failed"));
    }

    log.debug("validate schema {} ends with {}", schema,
        validationResult.isValid());
    return validationResult;
  }

  @Transactional(rollbackOn = {BeschreibungsException.class})
  @Override
  public Beschreibung beschreibungErstellen(
      Bearbeiter bearbeiter,
      String kodid,
      NormdatenReferenz sprache,
      Set<NormdatenReferenz> autoren,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary)
      throws BeschreibungsException {

    log.info(
        "Beschreibung erstellen with KOD ID {}, Sprache {} , Autoren {}",
        kodid,
        sprache,
        autoren.size()
    );

    KulturObjektDokument kod = kulturObjektDokumentRepository.findById(kodid);

    if (kod == null) {
      throw new BeschreibungsException("Keine KOD zu id vorhanden!");
    }

    Beschreibung beschreibung =
        minimalBeschreibungErstellen(
            bearbeiterBoundary.getLoggedBearbeiter(),
            kodid,
            sprache,
            kod,
            autoren);
    beschreibung.setGueltigeIdentifikation(kod.getGueltigeIdentifikation());

    kod.getBeschreibungenIDs().add(beschreibung.getId());

    try {
      var beschreibungsTemplate = getBeschreibungsTemplate();
      String teiXML =
          TEIObjectFactory.marshal(
              BeschreibungsTemplateMapper.createTEIFromInitialTemplate(
                  beschreibung, beschreibungsTemplate));
      beschreibung.setTeiXML(teiXML);

      teiXmlValidationBoundary.validateTeiXml(beschreibung, true);

      TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);

      saveBeschreibung(beschreibung);

      kulturObjektDokumentBoundary.saveKulturObjektDokument(kod,
          ActivityStreamAction.ADD);

    } catch (Exception e) {
      log.error("Error during create Beschreibung!", e);
      throw new BeschreibungsException(e.getMessage(), e);
    }

    return beschreibung;
  }

  private String getBeschreibungsTemplate() {

    SchemaPflegeDatei template = schemaPflegeDateiServicePort.findById(MEDIEVAL);

    if (template == null) {
      throw new IllegalStateException(
          "Beschreibung template not found in Importservice for id: " + MEDIEVAL);
    }

    return applyTeiVersion(template);
  }

  private String applyTeiVersion(SchemaPflegeDatei template) {

    String xml = template.getDatei();
    String version = template.getVersion();

    if (xml == null || xml.isBlank()) {
      throw new IllegalStateException("Template XML is empty");
    }

    return xml.replace("__tei-odd-version__", version);
  }

  private void saveBeschreibung(Beschreibung beschreibung) throws SolrUebernahmeException {
    beschreibungsRepository.save(beschreibung);
    suchDokumentService.beschreibungUebernehmen(beschreibung);
  }

  @Override
  @Transactional
  public void publizieren(Beschreibung beschreibung) throws PublikationException {
    Objects.requireNonNull(beschreibung, "beschreibung must not be null");

    KulturObjektDokument kod = kulturObjektDokumentRepository.findByIdOptional(
            beschreibung.getKodID())
        .orElseThrow(() -> new PublikationException(
            "publikation_error_kod_not_found", beschreibung.getId(), beschreibung.getKodID()));

    try {
      beschreibungsSynchronizer.updateTEIDocuments(
          kod, beschreibung,
          beschreibungsSynchronizer.findEntstehungsorteAsEntityFacts(beschreibung));
      teiXmlValidationBoundary.validateTeiXml(beschreibung, true);
      kulturObjektDokumentRepository.saveAndFlush(kod);

      suchDokumentService.beschreibungUebernehmen(beschreibung);
      suchDokumentService.kodUebernehmen(kod);

      if (!kod.isIndexable()) {
        throw new IllegalStateException(
            "KOD " + kod.getId() + " is not indexable after publish of Beschreibung "
                + beschreibung.getId() + " — missing TEI or Beschreibungen reference in KOD");
      }
      kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
          kod, ActivityStreamAction.UPDATE, true, BearbeiterBoundary.SYSTEM_USERNAME);
    } catch (PublikationException | IllegalStateException e) {
      throw e;
    } catch (TeiXmlValidationException e) {
      throw new PublikationException(
          "publikation_error_tei_invalid", beschreibung.getId(), e, e.getMessage());
    } catch (ServiceNotAvailableException e) {
      throw new PublikationException(
          "publikation_error_service_not_available", beschreibung.getId(), e);
    } catch (SolrUebernahmeException e) {
      throw new PublikationException(
          "publikation_error_suche_indexer", beschreibung.getId(), e);
    } catch (ActivityStreamsException e) {
      throw new PublikationException(
          "publikation_error_kafka", beschreibung.getId(), e);
    } catch (Exception e) {
      log.error("Unexpected error during publizieren of Beschreibung {} ({})",
          beschreibung.getId(), e.getClass().getSimpleName(), e);
      throw new PublikationException(
          "publikation_error_unexpected", beschreibung.getId(), e,
          e.getClass().getSimpleName());
    }
  }

  @Override
  public Sperre beschreibungSperren(Bearbeiter bearbeiter,
      String beschreibungsID, SperreTyp sperreTyp, String reason)
      throws DokumentSperreException {

    Beschreibung beschreibung = beschreibungsRepository.findById(
        beschreibungsID);

    return dokumentSperreBoundary.acquireSperre(
        bearbeiter, sperreTyp, reason, beschreibung);
  }

  @Override
  public Optional<Sperre> findSperreForBeschreibung(String beschreibungsID)
      throws DokumentSperreException {
    return dokumentSperreBoundary
        .findBySperreEintraege(
            new SperreEintrag(beschreibungsID, SperreDokumentTyp.BESCHREIBUNG))
        .stream()
        .findFirst();
  }

  @Override
  public void beschreibungEntsperren(String beschreibungsID)
      throws DokumentSperreException {
    Optional<Sperre> sperre = findSperreForBeschreibung(beschreibungsID);
    if (sperre.isPresent()) {
      dokumentSperreBoundary.releaseSperre(sperre.get());
    }
  }

  public Beschreibung minimalBeschreibungErstellen(
      Bearbeiter bearbeiter,
      String kodid,
      NormdatenReferenz sprache,
      KulturObjektDokument kod,
      Set<NormdatenReferenz> autoren)
      throws BeschreibungsException {

    if (kod == null
        || kod.getGueltigeIdentifikation() == null
        || kod.getGueltigeIdentifikation().getBesitzer() == null
        || kod.getGueltigeIdentifikation().getAufbewahrungsOrt() == null) {
      throw new BeschreibungsException(
          "KOD ungültig,Beschreibung kann nicht erstellt werden!");
    }

    Identifikation gueltigeSignatur =
        new IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(kod.getGueltigeIdentifikation().getBesitzer())
            .withAufbewahrungsOrt(
                kod.getGueltigeIdentifikation().getAufbewahrungsOrt())
            .withIdent(kod.getGueltigeSignatur())
            .build();

    Beschreibung beschreibung =
        new BeschreibungsBuilder(
            TEIValues.UUID_PREFIX + UUID.randomUUID(), kodid, gueltigeSignatur)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withErstellungsDatum(LocalDateTime.now())
            .withAenderungsDatum(LocalDateTime.now())
            .withBeschreibungssprache(sprache)
            .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
            .withBearbeiterIds(bearbeiter)
            .build();

    Identifikation kodAltIdentifikation =
        new IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withIdentTyp(IdentTyp.HSP_ALT_ID)
            .withIdent(kodid)
            .addSammlungID(KOD_ID_COLLECTION)
            .build();

    BeschreibungsKomponenteKopf kopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withId(UUID.randomUUID().toString())
            .withAlternativeIdentifikationen(Set.of(kodAltIdentifikation))
            .withTitel("")
            .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    Optional.ofNullable(autoren)
        .ifPresent(authors -> beschreibung.getAutoren().addAll(authors));

    return beschreibung;
  }

  @Override
  public void reindexAllBeschreibungen(
      @ObservesAsync @sendIndexAllBeschreibungenForNachweis WerkzeugBatchJob event) {
    try {
      suchDokumentService.reindexAllBeschreibungen();
      event.end("Indexing all manual description successfully completed!");
    } catch (Exception error) {
      log.error("Error during Indexing Beschreibungen", error);
      event.fail(error.getMessage());
    }
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void addPURL(String beschreibungId, PURL purl)
      throws BeschreibungsException {
    log.debug("addPURL: beschreibungId={}, purl={}", beschreibungId, purl);

    Objects.requireNonNull(beschreibungId, "beschreibungId is required");
    Objects.requireNonNull(purl, "purl is required");

    Beschreibung beschreibung =
        findById(beschreibungId)
            .orElseThrow(
                () ->
                    new BeschreibungsException(
                        getMessage(BESCHREIBUNGSERVICE_NO_BESCHREIBUNG_FOR_ID,
                            beschreibungId)));

    try {
      beschreibung.getPURLs().add(purl);
      TEIBeschreibungCommand.updatePURLs(beschreibung);

      teiXmlValidationBoundary.validateTeiXml(beschreibung, true);

      beschreibung = beschreibungsRepository.save(beschreibung);

      suchDokumentService.beschreibungUebernehmen(beschreibung);

    } catch (Exception e) {
      throw new BeschreibungsException(
          getMessage(
              "beschreibungservice_error_create_purl",
              purl.getPurl(),
              beschreibungId,
              e.getMessage()),
          e);
    }

    if (beschreibung.isPublished()) {
      kulturObjektDokumentRepository
          .findByBeschreibungOptional(beschreibung.getId())
          .ifPresent(this::indexKulturObjektDokument);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void indexKulturObjektDokument(KulturObjektDokument kod) {
    log.info("indexKulturObjektDokument for kodId={}", kod.getId());
    indexService.indexDocument(kod, ActivityStreamAction.UPDATE);
  }

  protected Beschreibung mapBeschreibungFromXML(String xml, Locale locale)
      throws BeschreibungsException {
    final Optional<TEI> optTeiElement;
    try (InputStream inputStream = new ByteArrayInputStream(
        xml.getBytes(StandardCharsets.UTF_8))) {
      optTeiElement = unmarshalOne(inputStream);
    } catch (Exception e) {
      log.error("Error unmarshalling xml {}", xml, e);
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_mapxml_unmarshal_error",
              e.getLocalizedMessage()),
          e);
    }

    TEI teiElement =
        optTeiElement.orElseThrow(
            () ->
                new BeschreibungsException(
                    getMessage(locale, "beschreibungservice_mapxml_kein_tei")));

    final List<Beschreibung> beschreibungen;

    try {
      TEI2BeschreibungMapper tei2BeschreibungMapper = new TEI2BeschreibungMapper();
      beschreibungen = tei2BeschreibungMapper.map(teiElement);
    } catch (Exception e) {
      log.error("Error mapping xml to beschreibung: xml={}", xml, e);
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_mapxml_map_error",
              e.getMessage()), e);
    }

    if (1 != beschreibungen.size()) {
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_mapxml_msdesc_error"));
    }

    return beschreibungen.getFirst();
  }

  @Override
  public List<BeschreibungListDbView> findFilteredBeschreibungListDTOs(
      int first, int pageSize, Map<String, String> filterBy,
      Map<String, Boolean> sortByAsc) {
    return beschreibungsRepository.findFilteredBeschreibungListDTOs(
        first, pageSize, filterBy, sortByAsc);
  }

  @Override
  public int countFilteredBeschreibungListDTOs(Map<String, String> filterBy) {
    return beschreibungsRepository.countFilteredBeschreibungListDTOs(filterBy);
  }

  @Override
  public BeschreibungListDbView findBeschreibungListDTOId(String id) {
    return beschreibungsRepository.findBeschreibungListDTOById(id);
  }

  @Override
  @Transactional(rollbackOn = {BeschreibungsException.class})
  public void addBearbeiterToBeschreibung(String beschreibungID,
      Set<String> bearbeiterIDs)
      throws BeschreibungsException {
    Beschreibung beschreibung =
        findById(beschreibungID)
            .orElseThrow(
                () ->
                    new BeschreibungsException(
                        getMessage(BESCHREIBUNGSERVICE_NO_BESCHREIBUNG_FOR_ID,
                            beschreibungID)));

    beschreibung.getBearbeiterIDs().addAll(bearbeiterIDs);

    log.info("Update Beschreibung mit Bearbeitern: bearbeiterIDs={}",
        bearbeiterIDs);

    try {
      saveBeschreibung(beschreibung);
    } catch (Exception error) {
      log.error("Error during save Description!", error);
      throw new BeschreibungsException(
          getMessage("beschreibungservice_updatexml_error", error.getMessage()),
          error);
    }
  }

  @Override
  public boolean isBeschreibungAssignedToLoggedInBearbeiter(String beschreibungID) {
    Beschreibung beschreibung = beschreibungsRepository.findById(beschreibungID);
    if (beschreibung == null) {
      return false;
    }
    Set<String> bearbeiterIds = beschreibung.getBearbeiterIDs();
    if (bearbeiterIds.isEmpty()) {
      return false;
    }
    String loggedBearbeiterId = bearbeiterBoundary.getLoggedBearbeiter().getId();
    return bearbeiterIds.contains(loggedBearbeiterId);
  }

  @Override
  public boolean isBeschreibungActionableForLoggedInBearbeiter(String beschreibungID) {
    if (!isBeschreibungAssignedToLoggedInBearbeiter(beschreibungID)) {
      return false;
    }
    Bearbeiter loggedBearbeiter = bearbeiterBoundary.getLoggedBearbeiter();
    try {
      Optional<Sperre> sperre = findSperreForBeschreibung(beschreibungID);
      return sperre.isEmpty() || loggedBearbeiter.equals(sperre.get().getBearbeiter());
    } catch (DokumentSperreException e) {
      // fail-closed: if there is an error checking the lock, we assume it's locked for the user
      log.error("Sperren-Check fehlgeschlagen für Beschreibung {} — fail-closed (write denied)",
          beschreibungID, e);
      return false;
    }
  }

  @Override
  public boolean isBeschreibungDeletableForLoggedInBearbeiter(String beschreibungId) {
    if (beschreibungsRechte.kannAllerDritterhartLoeschen()) {
      return true;
    }
    if (beschreibungsRechte.kannLoeschen()) {
      return isBeschreibungActionableForLoggedInBearbeiter(beschreibungId);
    }
    return false;
  }

  @Override
  @Transactional(rollbackOn = {BeschreibungsException.class})
  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_BEARBEITER_ENTZIEHEN)
  public void removeBearbeiterFromBeschreibung(String beschreibungID,
      String bearbeiterID)
      throws BeschreibungsException, DokumentSperreException {
    log.info("updateBeschreibungMitBearbeiter: id={}", beschreibungID);

    Beschreibung beschreibung =
        findById(beschreibungID)
            .orElseThrow(
                () ->
                    new BeschreibungsException(
                        getMessage(BESCHREIBUNGSERVICE_NO_BESCHREIBUNG_FOR_ID,
                            beschreibungID)));

    Optional<Sperre> sperre = findSperreForBeschreibung(beschreibungID);
    if (sperre.isEmpty()) {
      try {
        beschreibung.getBearbeiterIDs().remove(bearbeiterID);
        saveBeschreibung(beschreibung);
      } catch (Exception e) {
        log.error("Unable to delete Bearbeiter with ID = {} : {}", bearbeiterID,
            e.getMessage(), e);
        throw new BeschreibungsException(
            "Unable to delete Bearbeiter with ID = " + bearbeiterID + " : "
                + e.getMessage(), e);
      }
    } else {
      throw new BeschreibungsException("Beschreibung ist gesperrt!");
    }
  }

  @Override
  public Set<Bearbeiter> findBearbeiterEineBeschreibung(String beschreibungID)
      throws BeschreibungsException {
    Beschreibung beschreibung =
        findById(beschreibungID)
            .orElseThrow(
                () ->
                    new BeschreibungsException(
                        getMessage(BESCHREIBUNGSERVICE_NO_BESCHREIBUNG_FOR_ID,
                            beschreibungID)));

    return beschreibung.getBearbeiterIDs().stream()
        .map(id -> bearbeiterBoundary.findBearbeiterWithName(id))
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 60000)
  public Set<BeschreibungValidationReportItem> findAllInvalidODD() {
    return findAllInvalidInternal(true);
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 60000)
  public Set<BeschreibungValidationReportItem> findAllInvalidALL() {
    return findAllInvalidInternal(false);
  }

  private Set<BeschreibungValidationReportItem> findAllInvalidInternal(boolean odd) {
    Set<BeschreibungValidationReportItem> report = new HashSet<>();
    AtomicInteger started = new AtomicInteger();
    AtomicInteger succeeded = new AtomicInteger();

    beschreibungsRepository.selectAllTeiXml()
        .forEach(xml -> validateAndCollect(xml, report, started, succeeded, odd));

    log.info(
        "Validation finished (Beschreibungen, ODD={}). totals: started={}, succeeded={}, failed={}",
        odd, started.get(), succeeded.get(), started.get() - succeeded.get());
    return report;
  }

  private void validateAndCollect(XMLDokument xmlDokument,
      Set<BeschreibungValidationReportItem> report,
      AtomicInteger started,
      AtomicInteger succeeded,
      boolean odd) {
    int startedNow = started.incrementAndGet();
    log.info("Start validation of Beschreibung {} (ODD={}, started={}, succeeded={})",
        xmlDokument.getId(), odd, startedNow, succeeded.get());
    try {
      ValidationResult validationResult = teiXmlValidationBoundary.validateTeiXml(
          xmlDokument.getTeiXml(),
          String.valueOf(Locale.GERMAN),
          odd
      );

      int succeededNow = succeeded.incrementAndGet();
      log.debug("Finished validation of {} (ODD={}, started={}, succeeded={})",
          xmlDokument.getId(), odd, started.get(), succeededNow);

      if (!validationResult.isValid()) {
        Beschreibung b = beschreibungsRepository.findById(xmlDokument.getId());
        report.add(buildReportItem(b, validationResult));
      }
    } catch (Exception e) {
      log.error("Validation call failed for Beschreibung {} (ODD={}, started={}, succeeded={}): {}",
          xmlDokument.getId(), odd, started.get(), succeeded.get(), e.getMessage(), e);
      throw new IllegalStateException("Validation service unavailable (IMPORTSERVICE/Eureka).", e);
    }
  }

  private BeschreibungValidationReportItem buildReportItem(Beschreibung beschreibung,
      ValidationResult validationResult) {
    return new BeschreibungValidationReportItem.BeschreibungValidationReportItemBuilder()
        .beschreibungsID(beschreibung.getId())
        .signatur(
            beschreibung.getGueltigeIdentifikation()
                .map(Identifikation::getIdent)
                .orElse("")
        )
        .nameBesitzer(
            beschreibung.getGueltigeIdentifikation()
                .map(Identifikation::getBesitzer)
                .map(NormdatenReferenz::getName)
                .orElse("")
        )
        .nameAufbewahrungsort(
            beschreibung.getGueltigeIdentifikation()
                .map(Identifikation::getAufbewahrungsOrt)
                .map(NormdatenReferenz::getName)
                .orElse("")
        )
        .mxmlID(
            beschreibung.getAltIdentifier().stream()
                .filter(i -> i.contains(PREFIX_MXML))
                .findFirst()
                .orElse("")
        )
        .validationResult(validationResult)
        .build();
  }

  @Override
  public Stream<? extends XMLDokument> selectAllTeiXml() {
    return beschreibungsRepository.selectAllTeiXml();
  }

  @Override
  public List<Beschreibung> findAllBeschreibungWithActiveApprovalRequest() {
    return beschreibungsRepository.findAllBeschreibungWithActiveApprovalRequest();
  }

  @Override
  public List<BeschreibungListDbView> findMeinePublizierten(String bearbeiter) {
    try {
      return beschreibungsRepository.findPublikationenByBearbeiter(bearbeiter);
    } catch (Exception e) {
      log.error("Error finding published Beschreibungen for bearbeiter {}", bearbeiter, e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<BeschreibungListDbView> findLatestPublikationen() {
    try {
      return beschreibungsRepository.findLatestPublikationen(15);
    } catch (Exception e) {
      log.error("Error finding latest Publikationen", e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<BeschreibungInReviewDbView> findMeineImReview(String bearbeiter) {
    try {
      return beschreibungsRepository.findInReviewByBearbeiter(bearbeiter);
    } catch (Exception e) {
      log.error("Error finding in-review Beschreibungen for bearbeiter {}", bearbeiter, e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<BeschreibungRejectedDbView> findMeineZurUeberarbeitung(String bearbeiter) {
    try {
      return beschreibungsRepository.findRejectedByBearbeiter(bearbeiter);
    } catch (Exception e) {
      log.error("Error finding rejected Beschreibungen for bearbeiter {}", bearbeiter, e);
      return Collections.emptyList();
    }
  }
}
