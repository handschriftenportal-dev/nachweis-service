/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper.mapAttributsReferenz;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import com.google.common.collect.Lists;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException.ERROR_TYPE;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistry;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mozilla.universalchardet.UniversalDetector;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

@Slf4j
@Dependent
public class KulturObjektDokumentService implements KulturObjektDokumentBoundary, Serializable {

  public static final Pattern PATTERN_END_CSV_LINE = Pattern.compile("(?<=\")\\v");
  public static final String KEY_MANIFEST = "manifest";
  public static final String KEY_ALTERNATIVE = "alternative";
  private static final long serialVersionUID = 7896727068740214139L;
  private transient KulturObjektDokumentRepository kulturObjektDokumentRepository;

  private transient IdentifikationRepository identifikationRepository;

  private transient BeschreibungsRepository beschreibungsRepository;

  private transient SuchDokumentBoundary suchDokumentService;

  private transient KafkaIndexingProducer kafkaIndexingProducer;

  private transient BeschreibungsBoundary beschreibungsService;

  private transient DokumentSperreBoundary dokumentSperreService;

  private transient PapierkorbBoundary papierkorbService;

  private transient PURLBoundary purlService;
  private boolean purlAutogenerateEnabled;

  private transient TeiXmlTransformationBoundary teiXmlTransformationService;

  private transient TeiXmlValidationBoundary teiXmlValidationService;

  public KulturObjektDokumentService() {
  }

  @Inject
  public KulturObjektDokumentService(
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      IdentifikationRepository identifikationRepository,
      SuchDokumentBoundary suchDokumentService,
      BeschreibungsBoundary beschreibungsService,
      DokumentSperreBoundary dokumentSperreService,
      KafkaIndexingProducer kafkaIndexingProducer,
      BeschreibungsRepository beschreibungsRepository,
      PURLBoundary purlService,
      PapierkorbBoundary papierkorbService,
      @ConfigProperty(name = "purl.autogenerate.enabled") boolean purlAutogenerateEnabled,
      TeiXmlTransformationBoundary teiXmlTransformationService,
      TeiXmlValidationBoundary teiXmlValidationService) {

    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.identifikationRepository = identifikationRepository;
    this.suchDokumentService = suchDokumentService;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
    this.beschreibungsService = beschreibungsService;
    this.beschreibungsRepository = beschreibungsRepository;
    this.dokumentSperreService = dokumentSperreService;
    this.purlService = purlService;
    this.papierkorbService = papierkorbService;
    this.purlAutogenerateEnabled = purlAutogenerateEnabled;
    this.teiXmlTransformationService = teiXmlTransformationService;
    this.teiXmlValidationService = teiXmlValidationService;
  }

  public static void addKulturObjektDokumentToActivityStreamMessage(ActivityStream message, KulturObjektDokument kod)
      throws ActivityStreamsException {

    Objects.requireNonNull(message, "message is required!");
    Objects.requireNonNull(kod, "kod is required!");

    ActivityStreamObject activityStreamObjectKOD = ActivityStreamObject.builder()
        .withId(kod.getId())
        .withContent(kod.getTeiXML())
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.KOD)
        .build();
    message.addObject(activityStreamObjectKOD);
  }

  @Override
  public Optional<KulturObjektDokument> findById(String id) {
    if (id == null || id.isEmpty()) {
      return Optional.empty();
    }
    return kulturObjektDokumentRepository.findByIdOptional(id);
  }

  @Override
  public Map<String, List<String>> getAllBeschreibungsIdsWithKodIDs() {
    return kulturObjektDokumentRepository.getAllBeschreibungsIdsWithKodIDs();
  }

  @Override
  public List<KulturObjektDokument> findAll() {
    return kulturObjektDokumentRepository.listAll(true);
  }

  @Override
  public long count() {
    return kulturObjektDokumentRepository.count();
  }

  @Override
  @TransactionConfiguration(timeout = 7200)
  @Transactional(rollbackOn = {Exception.class})
  public void delete(Bearbeiter bearbeiter, String... kulturObjektDokumentIds)
      throws Exception {

    Set<KulturObjektDokument> kulturObjektDokumente = Arrays.stream(kulturObjektDokumentIds)
        .map(kodId -> kulturObjektDokumentRepository.findById(kodId))
        .collect(Collectors.toSet());

    Set<Beschreibung> beschreibungen = kulturObjektDokumente.stream()
        .flatMap(kulturObjektDokument -> kulturObjektDokument.getBeschreibungenIDs().stream())
        .map(id -> beschreibungsRepository.findById(id)).filter(Objects::nonNull)
        .collect(Collectors.toSet());

    Set<SperreEintrag> sperreEintraege = dokumentSperreService.createSperreEintraege(
        kulturObjektDokumente,
        beschreibungen);

    dokumentSperreService.acquireSperre(bearbeiter, SperreTyp.IN_TRANSACTION,
        "Löschen via GUI", sperreEintraege.toArray(new SperreEintrag[0]));

    for (KulturObjektDokument kulturObjektDokument : kulturObjektDokumente) {
      deleteKulturObjektDokument(bearbeiter, kulturObjektDokument.getId());
    }
  }

  @Override
  public void reindexAllKulturObjektDokumente(Bearbeiter bearbeiter) throws Exception {
    suchDokumentService.reindexAllKulturObjektDokumente();
  }

  @Override
  public Map<String, SignatureValue> getAllSignaturesWithKodIDs() {
    return identifikationRepository.getAllSignaturesWithKodIDs();
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<KulturObjektDokument> registrieren(Bearbeiter bearbeiter,
      List<KulturObjektDokument> kods)
      throws KulturObjektDokumentRegistrierenException {

    if (Objects.isNull(kods)) {
      throw new KulturObjektDokumentRegistrierenException(
          "Fehler bei der Verarbeitung der Signaturdatei! KOD-Liste ist null.",
          ERROR_TYPE.TECHNICAL, null);
    }

    try {
      log.info("Starting filter kods without gueltige signatur number of kod's {}", kods.size());

      List<KulturObjektDokument> kodsWithGueltigeSignatur = kods.stream()
          .filter(kod -> Objects.nonNull(kod.getGueltigeIdentifikation()))
          .collect(Collectors.toList());

      log.info("Starting check if already in file for number of kod's {}", kodsWithGueltigeSignatur.size());

      List<KulturObjektDokument> gueltigeSignaturInDatei = checkIfKulturObjektAlreadyInDatei(kods);
      if (!gueltigeSignaturInDatei.isEmpty()) {
        throw new KulturObjektDokumentRegistrierenException(
            "Gültige Signatur mehrfach in Datei vorhanden: " +
                gueltigeSignaturInDatei.stream()
                    .map(KulturObjektDokument::getGueltigeSignatur)
                    .collect(Collectors.joining(",")),
            gueltigeSignaturInDatei,
            ERROR_TYPE.GUELTIGE_SIGNATUR_IN_DATEI);
      }

      log.info("Starting check if already exists for number of kod's {}", kodsWithGueltigeSignatur.size());

      List<KulturObjektDokument> gueltigeSignaturInSystem = checkIfKulturObjektAlreadyExists(kodsWithGueltigeSignatur);
      if (!gueltigeSignaturInSystem.isEmpty()) {
        throw new KulturObjektDokumentRegistrierenException(
            "Kulturobjekt mit gültiger Signatur bereits im System: " +
                gueltigeSignaturInSystem.stream()
                    .map(KulturObjektDokument::getGueltigeSignatur)
                    .collect(Collectors.joining(",")),
            gueltigeSignaturInSystem, ERROR_TYPE.GUELTIGE_SIGNATUR_IN_SYSTEM);
      }

      createAndAddInternalPURLs(kodsWithGueltigeSignatur);

      log.info("Saving KOD's successfull {} ", kodsWithGueltigeSignatur.size());

      saveKulturObjektDokumentInSystem(kodsWithGueltigeSignatur);

      return kodsWithGueltigeSignatur;

    } catch (KulturObjektDokumentRegistrierenException kodrex) {
      throw kodrex;
    } catch (Exception error) {
      throw new KulturObjektDokumentRegistrierenException(
          "Fehler bei der Verarbeitung der Signaturdatei !,  " + error.getMessage(),
          ERROR_TYPE.TECHNICAL, error);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<KulturObjektDokument> registrieren(Bearbeiter bearbeiter, NormdatenReferenz ort,
      NormdatenReferenz koerperschaft, byte[] signaturen)
      throws KulturObjektDokumentRegistrierenException {

    checkEncoding(signaturen);

    List<KulturObjektDokument> kods = new ArrayList<>();

    String signaturLine;

    try (Scanner scanner = new Scanner(
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(signaturen))))) {
      scanner.useDelimiter(PATTERN_END_CSV_LINE);
      while (scanner.hasNext()) {
        signaturLine = scanner.next();

        // don't process a line with newline control elements at all.
        if (findAnEmptyRow(signaturLine)) {
          break;
        }

        kods.add(KulturObjektDokumentRegistry
            .create(ort, koerperschaft,
                KulturObjektDokumentRegistry.splitSignaturen(signaturLine)));
      }

      return registrieren(bearbeiter, kods);

    } catch (KulturObjektDokumentRegistrierenException kodrex) {
      throw kodrex;
    } catch (Exception error) {
      throw new KulturObjektDokumentRegistrierenException(
          "Fehler bei der Verarbeitung der Signaturdatei!,  " + error.getMessage(),
          ERROR_TYPE.TECHNICAL, error);
    }
  }

  protected void checkEncoding(byte[] data) throws KulturObjektDokumentRegistrierenException {

    String encoding;
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
      encoding = UniversalDetector.detectCharset(inputStream);

      log.info("Check CSV Encoding: {}", encoding);

      if (encoding != null && !"UTF-8|US-ASCII".contains(encoding)) {
        throw new KulturObjektDokumentRegistrierenException(
            I18NController.getMessage("kodservice_error_wrong_encoding", encoding));
      }
    } catch (IOException e) {
      log.error("Error during determine CSV Encoding", e);
    }
  }

  private boolean findAnEmptyRow(String signaturLine) {
    return signaturLine.trim().isEmpty();
  }

  @Override
  public Optional<KulturObjektDokumentViewModel> buildKulturObjektDokumentViewModel(String id) {
    KulturObjektDokumentViewModel model = null;
    Optional<KulturObjektDokument> byId = this.findById(id);

    if (Objects.nonNull(byId) && byId.isPresent()) {
      KulturObjektDokument kulturObjektDokument = byId.get();

      KulturObjektDokumentViewModelBuilder builder = KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
          .withId(kulturObjektDokument.getId())
          .withSignatur(kulturObjektDokument.getGueltigeIdentifikation().getIdent())
          .withTeiXML(kulturObjektDokument.getTeiXML())
          .withBeschreibungen(map(kulturObjektDokument.getBeschreibungenIDs()))
          .withAlternativeSignaturen(kulturObjektDokument.getAlternativeIdentifikationen().stream()
              .map(Identifikation::getIdent)
              .collect(Collectors.toList()))
          .withDigitalisats(mapDigitalisate(kulturObjektDokument))
          .withHspPurl(mapHspUrl(kulturObjektDokument))
          .withExterneLinks(mapExterneLinks(kulturObjektDokument));

      mapAttributsReferenzen(builder, kulturObjektDokument.getTeiXML());

      model = builder.build();
      mapIdentifikationWerte(Set.of(kulturObjektDokument.getGueltigeIdentifikation()), model);
    }

    return Optional.ofNullable(model);
  }

  private List<BeschreibungsViewModel> map(Set<String> beschreibungenIDs) {
    return beschreibungenIDs.stream().map(
            beschreibungsID -> {
              log.info("Map Beschreibung {}", beschreibungsID);
              return beschreibungsService.buildBeschreibungsViewModel(beschreibungsID).orElse(null);
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private void mapAttributsReferenzen(KulturObjektDokumentViewModelBuilder builder, String teiXml) {
    try {
      TEI tei = TEI2AttributsReferenzMapper.unmarshalTEI(teiXml);
      MsDesc msDesc = TEI2AttributsReferenzMapper.findFirstMsDesc(tei);

      builder.withTitel(mapAttributsReferenz(msDesc, tei, Titel.class));
      builder.withBeschreibstoff(mapAttributsReferenz(msDesc, tei, Beschreibstoff.class));
      builder.withUmfang(mapAttributsReferenz(msDesc, tei, Umfang.class));
      builder.withAbmessung(mapAttributsReferenz(msDesc, tei, Abmessung.class));
      builder.withFormat(mapAttributsReferenz(msDesc, tei, Format.class));
      builder.withEntstehungsort(mapAttributsReferenz(msDesc, tei, Entstehungsort.class));
      builder.withEntstehungszeit(mapAttributsReferenz(msDesc, tei, Entstehungszeit.class));
      builder.withGrundSprache(mapAttributsReferenz(msDesc, tei, Grundsprache.class));
      builder.withBuchschmuck(mapAttributsReferenz(msDesc, tei, Buchschmuck.class));
      builder.withHandschriftentyp(mapAttributsReferenz(msDesc, tei, Handschriftentyp.class));
      builder.withMusiknotation(mapAttributsReferenz(msDesc, tei, Musiknotation.class));
      builder.withStatus(mapAttributsReferenz(msDesc, tei, Status.class));
    } catch (Exception e) {
      log.error("Error mapping attributsReferenzen!", e);
    }
  }

  private String mapHspUrl(KulturObjektDokument kulturObjektDokument) {
    return kulturObjektDokument.getPURLs().stream()
        .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
        .findFirst()
        .map(purl -> purl.getPurl().toASCIIString())
        .orElse(null);
  }

  private List<String> mapExterneLinks(KulturObjektDokument kulturObjektDokument) {
    return kulturObjektDokument.getPURLs().stream()
        .filter(purl -> PURLTyp.INTERNAL != purl.getTyp())
        .map(purl -> purl.getPurl().toASCIIString())
        .collect(Collectors.toList());
  }

  private List<DigitalisatViewModel> mapDigitalisate(KulturObjektDokument kulturObjektDokument) {
    return kulturObjektDokument.getDigitalisate().stream()
        .map(DigitalisatViewModel::map)
        .collect(Collectors.toList());
  }

  private void mapIdentifikationWerte(Set<Identifikation> identifikationen,
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel) {
    for (Identifikation identifikation : identifikationen) {
      if (identifikation.getIdentTyp() == IdentTyp.GUELTIGE_SIGNATUR) {
        kulturObjektDokumentViewModel.setSignatur(identifikation.getIdent());

        Optional.of(identifikation).map(Identifikation::getBesitzer)
            .map(NormdatenReferenz::getName).ifPresent(
                kulturObjektDokumentViewModel::setBestandhaltendeInstitutionName);

        Optional.of(identifikation).map(Identifikation::getAufbewahrungsOrt)
            .map(NormdatenReferenz::getName)
            .ifPresent(
                kulturObjektDokumentViewModel::setBestandhaltendeInstitutionOrt);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void saveKulturObjektDokumentInSystem(List<KulturObjektDokument> kodsSucess)
      throws KulturObjektDokumentRegistrierenException {
    for (KulturObjektDokument kod : kodsSucess) {
      try {

        KulturObjektDokumentRegistry.addTEI(kod);

        saveKulturObjektDokument(kod, ActivityStreamAction.ADD);

      } catch (Exception e) {
        throw new KulturObjektDokumentRegistrierenException(
            "Kulturobjektdokument " + kod.getGueltigeSignatur()
                + " konnte nicht gespeichert werden! " + e.getMessage(),
            ERROR_TYPE.TECHNICAL, e);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void deleteKulturObjektDokument(
      Bearbeiter bearbeiter, String kodId) throws Exception {
    log.debug("deleteKulturObjektDokument: bearbeiter={},kodId={}", bearbeiter, kodId);

    Optional<KulturObjektDokument> kodOptional = kulturObjektDokumentRepository
        .findByIdOptional(kodId);

    if (kodOptional.isPresent()) {
      KulturObjektDokument kod = kodOptional.get();
      for (String beschreibungId : kod.getBeschreibungenIDs()) {
        Optional<Beschreibung> optBeschreibung = beschreibungsRepository.findByIdOptional(beschreibungId);
        if (optBeschreibung.isPresent()) {
          String loeschId = papierkorbService.erzeugeGeloeschtesDokument(optBeschreibung.get(), bearbeiter);
          log.debug("created GeloeschtesDokument {} for Beschreibung {}", loeschId, beschreibungId);
          beschreibungsRepository.deleteById(beschreibungId);
        }

        suchDokumentService.dokumentLoeschen(beschreibungId);
      }

      String loeschId = papierkorbService.erzeugeGeloeschtesDokument(kod, bearbeiter);
      log.debug("created GeloeschtesDokument {} for KOD {}", loeschId, kodId);

      kulturObjektDokumentRepository.deleteById(kodId);
      kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(kod, ActivityStreamAction.REMOVE, true,
          SYSTEM_USERNAME);
      suchDokumentService.dokumentLoeschen(kodId);
    }
  }

  @Transactional
  public void saveKulturObjektDokument(KulturObjektDokument kod, ActivityStreamAction action)
      throws TeiXmlTransformationException, TeiXmlValidationException, SolrUebernahmeException,
      ActivityStreamsException {

    teiXmlTransformationService.transformTei2Hsp(kod);
    teiXmlValidationService.validateTeiXml(kod);
    kulturObjektDokumentRepository.save(kod);
    suchDokumentService.kodUebernehmen(kod);
    kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(kod, action, true, SYSTEM_USERNAME);
  }

  protected List<KulturObjektDokument> checkIfKulturObjektAlreadyInDatei(List<KulturObjektDokument> kods) {
    Set<Identifikation> gueltigeSignaturenInDatei = new HashSet<>();
    List<KulturObjektDokument> gueltigeSignaturMehrfachInDatei = new ArrayList<>();

    for (KulturObjektDokument kod : kods) {
      if (Objects.nonNull(kod.getGueltigeIdentifikation())) {
        if (gueltigeSignaturenInDatei.contains(kod.getGueltigeIdentifikation())) {
          gueltigeSignaturMehrfachInDatei.add(kod);
        } else {
          gueltigeSignaturenInDatei.add(kod.getGueltigeIdentifikation());
        }
      }
    }

    return gueltigeSignaturMehrfachInDatei;
  }

  protected List<KulturObjektDokument> checkIfKulturObjektAlreadyExists(List<KulturObjektDokument> kods) {
    List<KulturObjektDokument> kodsFailureWithGueltigeSignature = new ArrayList<>();

    for (KulturObjektDokument kod : kods) {
      kulturObjektDokumentRepository.findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
              kod.getGueltigeIdentifikation().getIdent(),
              kod.getGueltigeIdentifikation().getBesitzer().getId(),
              kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getId())
          .stream()
          .findAny()
          .ifPresent(kodsFailureWithGueltigeSignature::add);
    }

    return kodsFailureWithGueltigeSignature;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<KulturObjektDokument> digitalisatHinzufuegen(
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException, SperreAlreadyExistException {

    final Optional<KulturObjektDokument> kulturObjektDokument = kulturObjektDokumentRepository
        .findByIdOptional(kulturObjektDokumentViewModel.getId());

    if (kulturObjektDokument.isPresent() && digitalisatViewModel != null) {

      KulturObjektDokument kod = kulturObjektDokument.get();
      Digitalisat digitalisat;

      Map<String, String> imageMap = new HashMap<>();
      imageMap.put(KEY_MANIFEST, digitalisatViewModel.getManifestURL());
      imageMap.put(KEY_ALTERNATIVE, digitalisatViewModel.getAlternativeUrl());

      if (digitalisatViewModel.getDigitalisatTyp() == null
          || (imageMap.get(KEY_MANIFEST) == null
          && imageMap.get(KEY_ALTERNATIVE) == null)) {
        throw new KulturObjektDokumentException("Es sind nicht alle Angaben gemacht worden!");
      }
      digitalisat = Digitalisat.DigitalisatBuilder()
          .withID(digitalisatViewModel.getId())
          .withManifest(imageMap.get(KEY_MANIFEST))
          .withAlternativeUrl(imageMap.get(KEY_ALTERNATIVE))
          .withThumbnail(digitalisatViewModel.getThumbnail())
          .withEntstehungsort(digitalisatViewModel.getOrt())
          .withDigitalisierendeEinrichtung(digitalisatViewModel.getEinrichtung())
          .withDigitalisierungsdatum(digitalisatViewModel.getDigitalisierungsdatum())
          .withErstellungsdatum(LocalDate.now())
          .withTyp(digitalisatViewModel.getDigitalisatTyp())
          .build();

      kod.getDigitalisate().remove(digitalisat);
      kod.getDigitalisate().add(digitalisat);

      try {

        TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kod);

        saveKulturObjektDokument(kod, ActivityStreamAction.UPDATE);

      } catch (Exception error) {
        log.error("Error during Digiatlisate hinzufuegen to kod!", error);
        throw new KulturObjektDokumentException("Digitalisate konnten nicht gespeichert werden!",
            error);
      }
    }

    return kulturObjektDokument;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<KulturObjektDokument> digitalisatLoeschen(
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException {
    final Optional<KulturObjektDokument> kulturObjektDokument = kulturObjektDokumentRepository
        .findByIdOptional(kulturObjektDokumentViewModel.getId());

    if (kulturObjektDokument.isPresent()) {
      KulturObjektDokument kod = kulturObjektDokument.get();

      Optional<Digitalisat> digitalisatToRemove = kod.getDigitalisate().stream()
          .filter(d -> d.getId().equals(digitalisatViewModel.getId())).findFirst();

      if (digitalisatToRemove.isPresent()) {

        kod.getDigitalisate().remove(digitalisatToRemove.get());

        try {

          TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kod);

          saveKulturObjektDokument(kod, ActivityStreamAction.UPDATE);

        } catch (Exception error) {
          log.error("Error during Digiatlisate loeschen to kod!", error);
          throw new KulturObjektDokumentException("Digitalisate konnten nicht gelöscht werden!",
              error);
        }

      }


    }

    return kulturObjektDokument;
  }

  @Override
  public Sperre kulturObjektDokumentSperren(Bearbeiter bearbeiter, String sperreGrund,
      String kulturObjektDokumentID)
      throws DokumentSperreException, SperreAlreadyExistException {

    KulturObjektDokument kulturObjektDokument = kulturObjektDokumentRepository.findById(
        kulturObjektDokumentID);

    return dokumentSperreService.acquireSperre(bearbeiter,
        SperreTyp.MANUAL,
        sperreGrund,
        kulturObjektDokument
    );
  }

  @Override
  public Optional<Sperre> findSperreForKulturObjektDokument(String kulturObjektDokumentID)
      throws DokumentSperreException {
    return dokumentSperreService.findBySperreEintraege(
            new SperreEintrag(kulturObjektDokumentID, SperreDokumentTyp.KOD))
        .stream()
        .findFirst();
  }

  @Override
  public void kulturObjektDokumentEntsperren(String kulturObjektDokumentID)
      throws DokumentSperreException {
    Optional<Sperre> sperre = findSperreForKulturObjektDokument(kulturObjektDokumentID);
    if (sperre.isPresent()) {
      dokumentSperreService.releaseSperre(sperre.get());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void addPURL(String kodId, PURL purl) throws KulturObjektDokumentException {
    log.debug("addPURL: kodId={}, purl={}", kodId, purl);

    Objects.requireNonNull(kodId, "kodId is required");
    Objects.requireNonNull(purl, "purl is required");

    KulturObjektDokument kod = findById(kodId)
        .orElseThrow(() -> new KulturObjektDokumentException(getMessage("kodservice_no_kod_for_id", kodId)));

    try {
      kod.getPURLs().add(purl);
      TEIKulturObjektDokumentCommand.updatePURLs(kod);
      saveKulturObjektDokument(kod, ActivityStreamAction.UPDATE);
    } catch (Exception e) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_create_purl", purl.getPurl(), kodId, e.getMessage()), e);
    }
  }

  @Override
  public List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {
    return kulturObjektDokumentRepository.findFilteredKulturObjektDokumentListDTOs(first, pageSize, filterBy,
        sortByAsc);
  }

  @Override
  public int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy) {
    return kulturObjektDokumentRepository.countFilteredKulturObjektDokumentListDTOs(filterBy);
  }

  @Override
  public KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id) {
    return kulturObjektDokumentRepository.findKulturObjektDokumentListDTOById(id);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public KulturObjektDokument kulturObjektDokumentAktualisieren(KulturObjektDokumentViewModel kodViewModel)
      throws KulturObjektDokumentException {
    Objects.requireNonNull(kodViewModel, "kodViewModel is required!");

    log.info("kulturObjektDokumentAktualisieren: kodId={}", kodViewModel.getId());

    KulturObjektDokument kod = findById(kodViewModel.getId())
        .orElseThrow(
            () -> new KulturObjektDokumentException(getMessage("kodservice_no_kod_for_id", kodViewModel.getId())));

    Set<AttributsReferenz> attributsReferenzen = getAttributsReferenzenFromKODViewModel(kodViewModel);

    try {
      TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, attributsReferenzen,
          purlService.createPURLMapWithBeschreibungsID(kod.getBeschreibungenIDs()));
      saveKulturObjektDokument(kod, ActivityStreamAction.UPDATE);
      return kod;
    } catch (Exception e) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_update_kod", kodViewModel.getId(), e.getMessage()), e);
    }
  }

  public void setSuchDokumentService(SuchDokumentService suchDokumentService) {
    this.suchDokumentService = suchDokumentService;
  }

  @Override
  public void migrateAttributsReferenzen() throws KulturObjektDokumentException {
    log.info("start migrateAttributsReferenzen");

    List<String> kodIds = kulturObjektDokumentRepository.listAll()
        .stream().map(KulturObjektDokument::getId)
        .collect(Collectors.toList());

    List<List<String>> sublists = Lists.partition(kodIds, 1000);
    log.info("Migrate AttributsReferenzen of {} KODs in {} sublists.", kodIds.size(), sublists.size());

    int sublistCounter = 0;
    for (List<String> sublist : sublists) {
      log.info("Indexing sublist {}.", ++sublistCounter);
      migrateAttributsReferenzen(sublist);
    }

    log.info("finished migrateAttributsReferenzen");
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 7200)
  void migrateAttributsReferenzen(List<String> kodIds)
      throws KulturObjektDokumentException {

    List<KulturObjektDokument> kods = new ArrayList<>();

    for (String kodId : kodIds) {
      kods.add(kulturObjektDokumentRepository.findByIdOptional(kodId)
          .orElseThrow(() -> new KulturObjektDokumentException("Found no KulturObjektDokument for kodId" + kodId)));
    }

    for (KulturObjektDokument kod : kods) {
      try {

        log.info("migrate AttributsReferenzen for KOD {}", kod.getId());

        TEIKODAttributsReferenzCommand.migrateAttributsReferenzen(kod,
            purlService.createPURLMapWithBeschreibungsID(kod.getBeschreibungenIDs()));

        saveKulturObjektDokument(kod, ActivityStreamAction.UPDATE);


      } catch (Exception e) {
        throw new KulturObjektDokumentException(
            "Error migrating attributsReferenzen for KOD " + kod.getId() + ": " + e.getMessage(), e);
      }
    }
  }

  void createAndAddInternalPURLs(List<KulturObjektDokument> kods) {
    log.info("createAndAddInternalPURLs: purlAutogenerateEnabled={}, kods={}", purlAutogenerateEnabled, kods.size());
    if (purlAutogenerateEnabled) {
      for (KulturObjektDokument kod : kods) {
        try {
          PURL purl = purlService.createNewPURL(kod.getId(), DokumentObjektTyp.HSP_OBJECT);
          kod.getPURLs().add(purl);
        } catch (Exception e) {
          // just catch exception and register KODs anyway:
          log.error("createAndAddInternalPURLs: error registering PURL for kod " + kod.getId(), e);
          return;
        }
      }
    }
  }

  Set<AttributsReferenz> getAttributsReferenzenFromKODViewModel(KulturObjektDokumentViewModel kodViewModel) {
    if (Objects.nonNull(kodViewModel)) {
      return Stream.of(
              kodViewModel.getTitel(),
              kodViewModel.getBeschreibstoff(),
              kodViewModel.getUmfang(),
              kodViewModel.getAbmessung(),
              kodViewModel.getFormat(),
              kodViewModel.getEntstehungsort(),
              kodViewModel.getEntstehungszeit(),
              kodViewModel.getGrundsprache(),
              kodViewModel.getBuchschmuck(),
              kodViewModel.getHandschriftentyp(),
              kodViewModel.getMusiknotation(),
              kodViewModel.getStatus())
          .filter(Objects::nonNull)
          .collect(Collectors.toCollection(LinkedHashSet::new));
    } else {
      return Collections.emptySet();
    }
  }
}
