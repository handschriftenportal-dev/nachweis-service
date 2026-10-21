package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery.queryForMsDescOfType;
import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.11.2020.
 * @version 1.0
 */
@ApplicationScoped
public class DigitalisatImportService {

  private static final Logger logger = LoggerFactory.getLogger(DigitalisatImportService.class);

  private static final int PROGRESS_LOG_INTERVAL = 50;

  private final String praesentationBaseUrl;
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private final NormdatenReferenzBoundary normdatenReferenzBoundary;
  private final ImportVorgangBoundary importVorgangBoundary;
  private final DokumentSperreBoundary dokumentSperreService;

  @Inject
  public DigitalisatImportService(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      ImportVorgangBoundary importVorgangBoundary,
      DokumentSperreBoundary dokumentSperreService,
      @ConfigProperty(name = "praesentationbaseurl") String praesentationBaseUrl) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.importVorgangBoundary = importVorgangBoundary;
    this.dokumentSperreService = dokumentSperreService;
    this.praesentationBaseUrl = praesentationBaseUrl;
  }

  protected static void setDigitalizationTypesAsConcepts(
      List<Ref> refs, DigitalisatViewModel model) {
    model.setDigitalisatTypKonzepte(new ArrayList<>());
    model
        .getDigitalisatTypKonzepte()
        .addAll(
            refs.stream()
                .filter(r -> !r.getType().equals(TEIValues.REF_TYPE_MANIFEST))
                .filter(r -> !r.getType().equals(TEIValues.REF_TYPE_THUMBNAIL))
                .filter(r -> !r.getType().equals(TEIValues.REF_TYPE_OTHER))
                .map(
                    r ->
                        new NormdatenReferenzBuilder()
                            .withTypeName(r.getType())
                            .withUri(TEICommon.getContentAsString(r.getTargets()))
                            .build())
                .collect(Collectors.toSet()));
  }

  private static void setDatesForModel(List<Date> dates, DigitalisatViewModel model) {
    TEIQuery.findDateByType(dates, TEIValues.DIGITIZED).ifPresent(model::setDigitalisierungsdatum);

    model.setErstellungsdatum(
        TEIQuery.findDateByType(dates, TEIValues.DATE_ISSUED)
            .orElseGet(LocalDate::now));

    TEIQuery.findDateByType(dates, TEIValues.DATE_PUBLISHED).ifPresent(model::setPublikationsDatum);
  }

  private static void setURLsForModel(List<Ref> refs, DigitalisatViewModel model) {
    TEIQuery.findManifestRefs(refs)
        .ifPresent(
            manifest -> model.setManifestURL(String.join(" ", manifest.getTargets())));

    TEIQuery.findOtherURLRefs(refs)
        .ifPresent(
            other -> model.setAlternativeUrl(String.join(" ", other.getTargets())));

    TEIQuery.findThumbnailURLRefs(refs)
        .ifPresent(
            thumbnail -> model.setThumbnail(String.join(" ", thumbnail.getTargets())));
  }

  private static long millis(long fromNanos, long toNanos) {
    return (toNanos - fromNanos) / 1_000_000L;
  }

  public void onImportMessage(@Observes @neueImporte DigitalisatImport importe) {
    logger.info("Neuer Digitalisat Import erhalten {} ", importe.getMessageDTO());
    try {
      doImportMessage(importe);
    } catch (Exception error) {
      logger.error("Error during Digitalisat importieren!", error);
      importVorgangBoundary.sendFailureMessageToImport(importe.getMessageDTO(), error.getMessage());
    }
  }

  @Transactional(
      value = TxType.REQUIRES_NEW,
      rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 1800)
  public void doImportMessage(DigitalisatImport importe) throws Exception {

    ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(importe.getMessageDTO());

    String dateinamen = importVorgang.getImportFiles().stream()
        .map(ImportFile::getDateiName)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));
    logger.info("Starting ImportVorgang {} (Datei: {}) for Digitalisate Import",
        importVorgang.getId(), dateinamen);

    List<TEI> tei = extractTEIFromMessage(importe.getMessageDTO());
    List<MsDesc> msDescObjects = queryForMsDescOfType(tei.getFirst(), TEIValues.HSP_OBJECT);

    List<KodDigitalisat> modelMapList =
        parseMsDescToCreateKODViewModelAndDigitalisatViewModel(msDescObjects);

    if (modelMapList.isEmpty()) {
      throw new DigitalisatImportException(
          format(
              "Importvorgang %s enthielt keine importierbaren Digitalisate!",
              importVorgang.getId()));
    }

    Set<SperreEintrag> sperreEintrage = createSperreEintrage(modelMapList);
    dokumentSperreService.acquireSperre(
        importe.getBearbeiter(),
        SperreTyp.IN_TRANSACTION,
        "Digitalisate importieren",
        sperreEintrage.toArray(new SperreEintrag[0]));

    saveToSystem(importe.getMessageDTO(), modelMapList);
  }

  protected List<KodDigitalisat>
  parseMsDescToCreateKODViewModelAndDigitalisatViewModel(List<MsDesc> msDescObjects) {
    List<KodDigitalisat> modelList = new LinkedList<>();
    long startNanos = System.nanoTime();

    int total = 0;
    for (MsDesc msDesc : msDescObjects) {
      if (msDesc.getMsIdentifier() != null) {
        total += TEIQuery.queryForBiblInMsDesc(msDesc).size();
      }
    }

    int processed = 0;
    for (MsDesc msDesc : msDescObjects) {
      Optional<MsIdentifier> msIdentifier = Optional.ofNullable(msDesc.getMsIdentifier());
      if (msIdentifier.isPresent()) {
        MsIdentifier identifier = msIdentifier.get();
        AtomicReference<String> kodIDorSignatur =
            new AtomicReference<>(TEIQuery.extractSignature(identifier));
        for (Bibl bibl : TEIQuery.queryForBiblInMsDesc(msDesc)) {
          modelList.add(
              parseBiblToCreateDigitalisatViewModel(bibl, kodIDorSignatur, identifier));
          processed++;
          if (processed % PROGRESS_LOG_INTERVAL == 0 || processed == total) {
            logger.info(
                "Digitalisat-Import Fortschritt: {}/{} Bibl(s) verarbeitet in {} ms",
                processed, total, millis(startNanos, System.nanoTime()));
          }
        }
      }
    }

    return modelList;
  }

  protected KodDigitalisat parseBiblToCreateDigitalisatViewModel(
      Bibl bibl, AtomicReference<String> kodIDorSignatur, MsIdentifier msIdentifier) {

    DigitalisatViewModel digitalisatViewModel =
        createDigitalisatModelFromTEIBibl(bibl)
            .orElseThrow(
                () ->
                    new DigitalisatImportException(
                        "Digitalisat konnte nicht erzeugt werden, notwendige Parameter fehlen!"));

    NormdatenReferenz aufbewahrungOrt =
        findAufbewahrungsort(msIdentifier)
            .orElseThrow(
                () ->
                    new DigitalisatImportException(
                        format("Kein AufbewahrungsOrt in Bibl %s", kodIDorSignatur.get())));

    NormdatenReferenz besitzendeEinrichtung =
        findBesitzendeEinrichtung(msIdentifier)
            .orElseThrow(
                () ->
                    new DigitalisatImportException(
                        format("Keine BesitzendeEinrichtung in Bibl %s", kodIDorSignatur.get())));

    updateDigitalisatsTypKonzeptWithNormdatenservice(digitalisatViewModel);

    KulturObjektDokument kod =
        findKulturObjektDokument(kodIDorSignatur.get(), aufbewahrungOrt, besitzendeEinrichtung)
            .orElseThrow(
                () ->
                    new DigitalisatImportException(
                        format(
                            "Kein KOD gefunden zu KOD ID "
                                + "oder Signatur %s, Aufbewahrungsort %s, "
                                + "besitzende Einrichtung %s",
                            kodIDorSignatur.get(),
                            aufbewahrungOrt.getName(),
                            besitzendeEinrichtung.getName())));

    return new KodDigitalisat(kod, digitalisatViewModel);
  }

  protected void updateDigitalisatsTypKonzeptWithNormdatenservice(
      DigitalisatViewModel digitalisatViewModel) {
    List<NormdatenReferenz> normdatenKonzepte = new ArrayList<>();
    digitalisatViewModel
        .getDigitalisatTypKonzepte()
        .forEach(
            c -> {
              NormdatenReferenz concept =
                  normdatenReferenzBoundary
                      .findConceptWithSchemeTypeAndURI(c.getTypeName(), c.getUri())
                      .orElseThrow(
                          () ->
                              new DigitalisatImportException(
                                  format("Digitalisatskonzept nicht gefunden %s, type %s",
                                      c.getUri(),
                                      c.getTypeName())));
              normdatenKonzepte.add(concept);
            });

    digitalisatViewModel.getDigitalisatTypKonzepte().clear();
    digitalisatViewModel.getDigitalisatTypKonzepte().addAll(normdatenKonzepte);
  }

  private Set<SperreEintrag> createSperreEintrage(List<KodDigitalisat> success) {
    Set<SperreEintrag> kodIds = new HashSet<>();
    success.forEach(e -> kodIds.add(new SperreEintrag(e.kod().getId(), SperreDokumentTyp.KOD)));
    return kodIds;
  }

  public void saveToSystem(ActivityStreamMessageDTO message, List<KodDigitalisat> success) {

    ImportVorgang importVorgang;
    try {
      importVorgang = importVorgangBoundary.getImportVorgang(message);
      importDigitalisateHinzufuegen(message, success, importVorgang);
    } catch (Exception e) {
      logger.error("Error during save Digitalisat Import to System!", e);
      throw new DigitalisatImportException(
          "Digitalisat Import gescheitert %s".formatted(e.getMessage()), e);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void importDigitalisateHinzufuegen(
      ActivityStreamMessageDTO message,
      List<KodDigitalisat> success,
      ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException, SperreAlreadyExistException {

    var activityStreamObjectDTO = message.getObjects().stream()
        .filter(o -> ActivityStreamsDokumentTyp.DIGITALISAT.equals(o.getType()))
        .findFirst();
    // Collect all digitalisate per KOD so each KOD is saved/distributed (validated, pushed to Solr,
    // sent to Kafka) only once instead of once per digitalisat.
    Map<String, List<DigitalisatViewModel>> digitalisateProKod = new LinkedHashMap<>();
    for (KodDigitalisat entry : success) {
      String kodId = entry.kod().getId();
      DigitalisatViewModel digitalisat = entry.digitalisat();
      digitalisateProKod.computeIfAbsent(kodId, _ -> new ArrayList<>()).add(digitalisat);
      activityStreamObjectDTO.ifPresent(
          aso -> addDataEntity(aso, importVorgang, kodId, digitalisat));
    }

    digitalisateProKod.forEach((kodId, digitalisate) -> {
      logger.debug("Digitalisate Hinzufügen für KOD {}: {}", kodId, digitalisate.size());
      kulturObjektDokumentBoundary.digitalisateHinzufuegen(kodId, digitalisate);
    });

    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
    importVorgangBoundary.sendImportJobToKafka(importVorgang);
  }

  private void addDataEntity(ActivityStreamObjectDTO activityStreamObjectDTO,
      ImportVorgang importVorgang,
      String kodId, DigitalisatViewModel digitalisat) {
    findImportFileForActivityStreamObject(importVorgang, activityStreamObjectDTO)
        .getDataEntityList()
        .add(
            new DataEntity(
                digitalisat.getId(),
                Optional
                    .ofNullable(digitalisat.getManifestURL())
                    .orElseGet(digitalisat::getAlternativeUrl),
                buildPraesentationUrl(kodId)));
  }

  private String buildPraesentationUrl(String kodId) {
    return praesentationBaseUrl + "discovery?hspobjectid=" + kodId + "?q=%2A";
  }

  ImportFile findImportFileForActivityStreamObject(
      ImportVorgang importVorgang, ActivityStreamObjectDTO activityStreamObject) {

    ImportFile importFile;
    Objects.requireNonNull(activityStreamObject.getName(), "ActivityStreamObject without name");
    importFile =
        importVorgang.getImportFiles().stream()
            .filter(
                searchForImportFile ->
                    activityStreamObject.getName().equals(searchForImportFile.getDateiName()))
            .findFirst()
            .orElse(null);
    return importFile;
  }

  public Optional<DigitalisatViewModel> createDigitalisatModelFromTEIBibl(Bibl bibl)
      throws DigitalisatImportException {

    Optional<DigitalisatViewModel> digitalisatViewModel;

    try {

      digitalisatViewModel = mapTEIToModel(bibl);

      digitalisatViewModel.ifPresent(
          model -> {
            findDigitalisierendeEinrichtung(bibl).ifPresent(model::setEinrichtung);

            findDigitalisierungsOrt(bibl).ifPresent(model::setOrt);
          });

    } catch (Exception e) {
      throw new DigitalisatImportException(e);
    }
    return digitalisatViewModel;
  }

  Optional<DigitalisatViewModel> mapTEIToModel(Bibl bibl) throws Exception {

    Optional<DigitalisatViewModel> digitalisatViewModel = Optional.empty();
    List<Ref> refs = new ArrayList<>();
    List<Date> dates = new ArrayList<>();
    TEICommon.findAll(Ref.class, bibl, refs);
    TEICommon.findAll(Date.class, bibl, dates);

    if (TEIQuery.isManifestOROtherURLInTEI(refs)) {

      DigitalisatViewModel model = DigitalisatViewModel.builder().build();

      setDigitalizationTypesAsConcepts(refs, model);

      setURLsForModel(refs, model);

      setDatesForModel(dates, model);

      model.setId(
          model.getManifestURL() != null
              ? HSP_UUI_Factory.generate(model.getManifestURL().getBytes())
              : HSP_UUI_Factory.generate(model.getAlternativeUrl().getBytes()));

      digitalisatViewModel = Optional.of(model);
    }

    return digitalisatViewModel;
  }

  public Optional<NormdatenReferenz> findAufbewahrungsort(MsIdentifier msIdentifier) {

    final AtomicReference<NormdatenReferenz> ort = new AtomicReference<>();

    Optional.ofNullable(msIdentifier)
        .map(MsIdentifier::getSettlement)
        .ifPresent(
            settlement ->
                ort.set(
                    new NormdatenReferenz(
                        settlement.getKey(),
                        TEICommon.getContentAsString(settlement.getContent()),
                        settlement.getKey())));

    return normdatenReferenzBoundary.findOneByIdOrNameAndType(
        ort.get().getId(), NormdatenReferenz.ORT_TYPE_NAME);
  }

  public Optional<NormdatenReferenz> findBesitzendeEinrichtung(MsIdentifier msIdentifier) {

    final AtomicReference<NormdatenReferenz> koerperschaftAtomicReference = new AtomicReference<>();

    Optional.ofNullable(msIdentifier)
        .map(MsIdentifier::getRepository)
        .ifPresent(
            repository ->
                koerperschaftAtomicReference.set(
                    new NormdatenReferenz(
                        repository.getKey(),
                        TEICommon.getContentAsString(repository.getContent()),
                        repository.getKey()))
        );

    return normdatenReferenzBoundary.findOneByIdOrNameAndType(
        koerperschaftAtomicReference.get().getId(), NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  public Optional<NormdatenReferenz> findDigitalisierungsOrt(Bibl bibl)
      throws DigitalisatImportException {

    try {
      Optional<String> placeId =
          TEIQuery.queryForPlaceNameInBibl(bibl, TEIValues.TYPE_DIGITIZING)
              .flatMap(placeName -> placeName.getReves().stream().findFirst());

      return placeId.flatMap(
          id ->
              normdatenReferenzBoundary.findOneByIdOrNameAndType(
                  id, NormdatenReferenz.ORT_TYPE_NAME));
    } catch (Exception e) {
      throw new DigitalisatImportException(e);
    }
  }

  public Optional<NormdatenReferenz> findDigitalisierendeEinrichtung(Bibl bibl)
      throws DigitalisatImportException {
    try {
      Optional<String> koerperschaftId =
          TEIQuery.queryForOrgNameInBibl(bibl, TEIValues.TYPE_DIGITIZING)
              .flatMap(orgName -> orgName.getReves().stream().findFirst());

      return koerperschaftId.flatMap(
          id ->
              normdatenReferenzBoundary.findOneByIdOrNameAndType(
                  id, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME));

    } catch (Exception e) {
      throw new DigitalisatImportException(e);
    }
  }

  public List<Bibl> findAllTEIDigitalisate(MsDesc msDesc) throws Exception {
    List<Bibl> biblList = new ArrayList<>();
    TEICommon.findAll(Bibl.class, msDesc, biblList);
    return biblList;
  }

  public List<TEI> extractTEIFromMessage(ActivityStreamMessageDTO activityStreamMessage)
      throws ActivityStreamsException, JAXBException {

    List<TEI> teiList = new ArrayList<>();

    if (activityStreamMessage != null && activityStreamMessage.getObjects() != null) {
      for (ActivityStreamObjectDTO o : activityStreamMessage.getObjects()) {
        if (ActivityStreamsDokumentTyp.DIGITALISAT.equals(o.getType())) {
          teiList.addAll(TEIObjectFactory.unmarshal(new ByteArrayInputStream(o.getContent())));
        }
      }
    }

    return teiList;
  }

  public Optional<KulturObjektDokument> findKulturObjektDokument(
      String kodIDorSignatur, NormdatenReferenz ort, NormdatenReferenz einrichtung) {

    if (kodIDorSignatur == null) {
      return Optional.empty();
    }

    return kulturObjektDokumentRepository
        .findByIdOptional(kodIDorSignatur)
        .or(
            () ->
                kulturObjektDokumentRepository.findByIdentifikationIdent(kodIDorSignatur).stream()
                    .filter(
                        kod ->
                            checkIfIdentifikationIsEqual(
                                Set.of(kod.getGueltigeIdentifikation()), ort, einrichtung))
                    .findFirst()
                    .or(
                        () ->
                            kulturObjektDokumentRepository
                                .findByAlternativeIdentifikationIdent(kodIDorSignatur)
                                .stream()
                                .filter(
                                    kod ->
                                        checkIfIdentifikationIsEqual(
                                            kod.getAlternativeIdentifikationen(),
                                            ort,
                                            einrichtung))
                                .findFirst()));
  }

  boolean checkIfIdentifikationIsEqual(
      Set<Identifikation> gueltigeIdentifikationen,
      NormdatenReferenz ort,
      NormdatenReferenz einrichtung) {

    return Objects.nonNull(gueltigeIdentifikationen)
        && gueltigeIdentifikationen.stream()
        .anyMatch(
            identifikation ->
                Objects.nonNull(identifikation)
                    && Objects.nonNull(identifikation.getAufbewahrungsOrt())
                    && Objects.nonNull(identifikation.getBesitzer())
                    && Objects.nonNull(ort)
                    && Objects.nonNull(einrichtung)
                    && identifikation.getAufbewahrungsOrt().equals(ort)
                    && identifikation.getBesitzer().equals(einrichtung));
  }
}
