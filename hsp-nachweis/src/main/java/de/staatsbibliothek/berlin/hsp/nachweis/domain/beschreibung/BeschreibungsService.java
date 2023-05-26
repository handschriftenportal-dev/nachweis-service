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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.KOD_ID_COLLECTION;
import static de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory.unmarshalOne;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIBeschreibungCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.BeschreibungsTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsAutorView;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.LizenzViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 */
@Slf4j
@Dependent
public class BeschreibungsService implements BeschreibungsBoundary, Serializable {

  public static final String SCHEMA_ODD = "ODD";

  private static final long serialVersionUID = 7896727068740214139L;

  static {
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
  }

  private BeschreibungsRepository beschreibungsRepository;
  private IdentifikationRepository identifikationRepository;
  private KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private transient IndexService indexService;
  private transient SuchDokumentService suchDokumentService;
  private transient DokumentSperreBoundary dokumentSperreBoundary;
  private transient PapierkorbBoundary papierkorbBoundary;
  private transient TeiXmlValidationBoundary teiXmlValidationBoundary;
  private transient TeiXmlTransformationBoundary teiXmlTransformationBoundary;

  public BeschreibungsService() {
  }

  @Inject
  public BeschreibungsService(
      BeschreibungsRepository beschreibungsRepository,
      IdentifikationRepository identifikationRepository,
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      SuchDokumentService suchDokumentService,
      IndexService indexService,
      DokumentSperreBoundary dokumentSperreBoundary,
      PapierkorbBoundary papierkorbBoundary,
      TeiXmlValidationBoundary teiXmlValidationBoundary,
      TeiXmlTransformationBoundary teiXmlTransformationBoundary) {
    this.beschreibungsRepository = beschreibungsRepository;
    this.identifikationRepository = identifikationRepository;
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.suchDokumentService = suchDokumentService;
    this.indexService = indexService;
    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.papierkorbBoundary = papierkorbBoundary;
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
    this.teiXmlTransformationBoundary = teiXmlTransformationBoundary;
  }

  public static void addBeschreibungToActivityStreamMessage(ActivityStream message, Beschreibung beschreibung)
      throws ActivityStreamsException {

    Objects.requireNonNull(message, "message is required!");
    Objects.requireNonNull(beschreibung, "beschreibung is required!");

    ActivityStreamObject activityStreamObjectBeschreibung = ActivityStreamObject.builder()
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
  public List<Beschreibung> findAll() {
    return beschreibungsRepository.listAll(true);
  }

  @Override
  public List<BeschreibungListDTO> findLatestModified() {
    try {
      return beschreibungsRepository.findLatestModified();
    } catch (Exception e) {
      log.error("Error in findLatestEdited", e);
      return Collections.emptyList();
    }
  }

  @Override
  public long count() {
    return beschreibungsRepository.count();
  }

  @Override
  public SignatureValue getSignatureForBeschreibungId(String beschreibungId) {
    return identifikationRepository.getSignatureByBeschreibungID(beschreibungId);
  }

  @Override
  public Map<String, SignatureValue> getAllSignaturesWithBeschreibungIDs() {
    return identifikationRepository.getAllSignaturesWithBeschreibungIDs();
  }

  @Override
  public Optional<BeschreibungsViewModel> buildBeschreibungsViewModel(String id) {

    BeschreibungsViewModel model = null;
    AtomicReference<String> kodSignatur = new AtomicReference<>("");

    Optional<Beschreibung> byId = this.findById(id);
    if (byId.isPresent()) {
      Beschreibung beschreibung = byId.get();
      String titel = beschreibung.getTitel().orElse("");

      AtomicReference<String> signatur = new AtomicReference<>("");
      AtomicReference<String> aufbewahrendeInstitution = new AtomicReference<>("");
      AtomicReference<String> aufbewahrungsort = new AtomicReference<>("");

      beschreibung.getGueltigeIdentifikation().ifPresent(identifikation -> {
        signatur.set(identifikation.getIdent());
        Optional.ofNullable(identifikation.getBesitzer())
            .map(NormdatenReferenz::getName)
            .ifPresent(aufbewahrendeInstitution::set);
        Optional.ofNullable(identifikation.getAufbewahrungsOrt())
            .map(NormdatenReferenz::getName)
            .ifPresent(aufbewahrungsort::set);
      });

      if (Objects.nonNull(beschreibung.getKodID())) {
        kulturObjektDokumentRepository.findByIdOptional(beschreibung.getKodID()).ifPresent(
            kod -> kodSignatur.set(kod.getGueltigeSignatur()));
      }

      model = new BeschreibungsViewModel.BeschreibungsViewBuilder()
          .withID(id)
          .withTitel(titel)
          .withTEIXML(beschreibung.getTeiXML())
          .withSignatur(signatur.get())
          .withKODID(beschreibung.getKodID())
          .withKODSignatur(kodSignatur.get())
          .withErstellungsDatum(beschreibung.getErstellungsDatum())
          .withAenderungsDatum(beschreibung.getAenderungsDatum())
          .withBestandhaltendeInstitutionName(aufbewahrendeInstitution.get())
          .withBestandhaltendeInstitutionOrt(aufbewahrungsort.get())
          .withPublikationen(List.copyOf(beschreibung.getPublikationen()))
          .withBeteiligte(List.copyOf(beschreibung.getBeschreibungsBeteiligte()))
          .withVerwaltungsTyp(beschreibung.getVerwaltungsTyp())
          .withBeschreibungsSprache(beschreibung.getBeschreibungsSprache())
          .withLizenz(Optional.ofNullable(beschreibung.getLizenz())
              .map(l -> new LizenzViewModel(
                  beschreibung.getLizenz().getUris() != null ?
                      new LinkedHashSet<>(beschreibung.getLizenz().getUris()) : null,
                  beschreibung.getLizenz().getBeschreibungsText()))
              .orElse(null))
          .withAutoren(beschreibung.getAutoren().stream().map(
              normdatenReferenz -> new BeschreibungsAutorView(normdatenReferenz.getName(),
                  normdatenReferenz.getGndID())).collect(
              Collectors.toList()))
          .withGedrucktemKatalog(
              DokumentObjektTyp.HSP_DESCRIPTION_RETRO.equals(beschreibung.getDokumentObjektTyp()))
          .withHspPurl(beschreibung.getPURLs().stream()
              .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
              .findFirst()
              .map(purl -> purl.getPurl().toASCIIString()).orElse(null))
          .withExterneLinks(beschreibung.getPURLs().stream()
              .filter(purl -> PURLTyp.INTERNAL != purl.getTyp())
              .map(purl -> purl.getPurl().toASCIIString())
              .collect(Collectors.toList()))
          .withDatumErstpublikation(mapDatumErstpublikation(beschreibung.getPublikationen()))
          .build();
    }

    log.debug("BeschreibunsViewModel loaded {}", model);

    return Optional.ofNullable(model);
  }

  private LocalDateTime mapDatumErstpublikation(Set<Publikation> publikationen) {
    return Stream.ofNullable(publikationen)
        .flatMap(Collection::stream)
        .filter(publikation -> PublikationsTyp.ERSTPUBLIKATION == publikation.getPublikationsTyp())
        .findFirst()
        .map(Publikation::getDatumDerVeroeffentlichung)
        .orElse(null);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void delete(Bearbeiter bearbeiter, Beschreibung... beschreibungen)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException {
    dokumentSperreBoundary.acquireSperre(bearbeiter,
        SperreTyp.IN_TRANSACTION,
        "deleteBeschreibungen",
        beschreibungen
    );

    for (Beschreibung beschreibung : beschreibungen) {
      deleteBeschreibung(bearbeiter, beschreibung.getId());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void deleteBeschreibung(Bearbeiter bearbeiter, String beschreibungsId)
      throws BeschreibungsException, DokumentSperreException, SperreAlreadyExistException {
    Optional<Beschreibung> beschreibungOptional = beschreibungsRepository
        .findByIdOptional(beschreibungsId);

    if (beschreibungOptional.isPresent()) {
      Beschreibung beschreibung = beschreibungOptional.get();
      dokumentSperreBoundary.acquireSperre(bearbeiter,
          SperreTyp.IN_TRANSACTION,
          "deleteBeschreibung",
          beschreibung);
      try {

        Optional<KulturObjektDokument> kulturObjektDokumentOptional = kulturObjektDokumentRepository
            .findByBeschreibungOptional(beschreibungsId);
        if (kulturObjektDokumentOptional.isPresent()) {
          KulturObjektDokument kulturObjektDokument = kulturObjektDokumentOptional.get();

          kulturObjektDokument.removeBeschreibungsdokument(beschreibungsId);
          TEIKulturObjektDokumentCommand
              .updateKODSourceDescWithRemoveBeschreibungMsDesc(kulturObjektDokument, beschreibungsId);
          TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kulturObjektDokument);
          TEIKODAttributsReferenzCommand.deleteAttributsReferenzenForBeschreibung(kulturObjektDokument, beschreibungsId);
          teiXmlValidationBoundary.validateTeiXml(kulturObjektDokument);

          suchDokumentService.kodUebernehmen(kulturObjektDokument);
          if (VerwaltungsTyp.EXTERN == beschreibung.getVerwaltungsTyp()) {
            indexKulturObjektDokument(kulturObjektDokument);
          }
        }

        String loeschId = papierkorbBoundary.erzeugeGeloeschtesDokument(beschreibung, bearbeiter);
        log.debug("created GeloeschtesDokument {} for Beschreibung {}", loeschId, beschreibungsId);

        beschreibungsRepository.deleteById(beschreibungsId);
        suchDokumentService.dokumentLoeschen(beschreibungsId);
      } catch (Exception e) {
        log.error("Unable to delete Beschreibung with ID = {} : {}", beschreibungsId,
            e.getMessage(), e);
        throw new BeschreibungsException(
            "Unable to delete Beschreibung with ID = " + beschreibungsId + " : " + e.getMessage(),
            e);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {BeschreibungsException.class})
  public void updateBeschreibungMitXML(String id, String xml, Locale locale) throws BeschreibungsException {
    log.info("updateBeschreibungMitXML: id={}", id);

    Objects.requireNonNull(id, "id is required!");
    Objects.requireNonNull(xml, "xml is required!");
    Objects.requireNonNull(xml, "locale is required!");

    Beschreibung originalBeschreibung = findById(id).orElseThrow(
        () -> new BeschreibungsException(getMessage(locale, "beschreibungservice_updatexml_no_beschreibung", id)));

    Beschreibung mappedBeschreibung = mapBeschreibungFromXML(xml, locale);

    if (!id.equals(mappedBeschreibung.getId())) {
      throw new BeschreibungsException(getMessage(locale, "beschreibungservice_updatexml_wrong_beschreibung", id));
    }

    mappedBeschreibung.setBeschreibungsSprache(originalBeschreibung.getBeschreibungsSprache());
    mappedBeschreibung.setErstellungsDatum(originalBeschreibung.getErstellungsDatum());

    originalBeschreibung.getPURLs().stream()
        .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
        .forEach(purlInternal -> mappedBeschreibung.getPURLs().add(purlInternal));

    try {
      mappedBeschreibung.setAenderungsDatum(LocalDateTime.now());
      TEIBeschreibungCommand.updateAenderungsdatum(mappedBeschreibung);

      teiXmlTransformationBoundary.transformTei2Hsp(mappedBeschreibung);
      teiXmlValidationBoundary.validateTeiXml(mappedBeschreibung);

      beschreibungsRepository.save(mappedBeschreibung);

      suchDokumentService.beschreibungUebernehmen(mappedBeschreibung);
    } catch (Exception error) {
      log.error("Error during reading xml!", error);
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_updatexml_error", error.getMessage()), error);
    }
  }

  @Override
  public ValidationResult validateXML(String xmlTEI, String schema, String locale) {
    ValidationResult validationResult;

    try {
      validationResult = teiXmlValidationBoundary.validateTeiXml(xmlTEI, locale, SCHEMA_ODD.equals(schema));
    } catch (Exception e) {
      log.error("Error validating xml!", e);

      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));

      validationResult = new ValidationResult(false, e.getMessage(), sw.toString(),
          getMessage(Locale.forLanguageTag(locale), "validation_result_failed"));
    }

    log.debug("validate schema {} ends with {}", schema, validationResult.isValid);
    return validationResult;
  }

  @Transactional(rollbackOn = {BeschreibungsException.class})
  @Override
  public Beschreibung beschreibungErstellen(Bearbeiter bearbeiter, String kodid,
      NormdatenReferenz sprache,
      Set<NormdatenReferenz> autoren, Lizenz lizenz,
      String template, KulturObjektDokumentBoundary kulturObjektDokumentBoundary)
      throws BeschreibungsException {

    log.info(
        "Beschreibung erstellen with KOD ID {}, Sprache {} , Autoren {}, lizenz {}, Template {}",
        kodid, sprache, autoren.size(), lizenz, template);
    KulturObjektDokument kod = kulturObjektDokumentRepository.findById(kodid);

    if (kod == null) {
      throw new BeschreibungsException("Keine KOD zu id vorhanden!");
    }

    Beschreibung beschreibung = minimalBeschreibungErstellen(kodid, sprache, kod, autoren, lizenz,
        template);

    kod.getBeschreibungenIDs().add(beschreibung.getId());

    try {
      String teiXML = TEIObjectFactory.marshal(
          BeschreibungsTemplateMapper.createTEIFromInitialTemplate(beschreibung));
      beschreibung.setTeiXML(teiXML);

      teiXmlTransformationBoundary.transformTei2Hsp(beschreibung);
      teiXmlValidationBoundary.validateTeiXml(beschreibung);

      TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);

      beschreibungsRepository.save(beschreibung);

      suchDokumentService.beschreibungUebernehmen(beschreibung);

      kulturObjektDokumentBoundary.saveKulturObjektDokument(kod, ActivityStreamAction.ADD);

    } catch (Exception e) {
      log.error("Error during create Beschreibung!", e);
      throw new BeschreibungsException(e.getMessage(), e);
    }

    return beschreibung;
  }

  @Override
  public Sperre beschreibungSperren(Bearbeiter bearbeiter, String beschreibungsID)
      throws DokumentSperreException {

    Beschreibung beschreibung = beschreibungsRepository.findById(beschreibungsID);

    return dokumentSperreBoundary.acquireSperre(bearbeiter,
        SperreTyp.MANUAL,
        "BeschreibungBearbeiten",
        beschreibung
    );
  }

  @Override
  public Optional<Sperre> findSperreForBeschreibung(String beschreibungsID)
      throws DokumentSperreException {
    return dokumentSperreBoundary.findBySperreEintraege(
            new SperreEintrag(beschreibungsID, SperreDokumentTyp.BESCHREIBUNG))
        .stream()
        .findFirst();
  }

  @Override
  public void beschreibungEntsperren(String beschreibungsID) throws DokumentSperreException {
    Optional<Sperre> sperre = findSperreForBeschreibung(beschreibungsID);
    if (sperre.isPresent()) {
      dokumentSperreBoundary.releaseSperre(sperre.get());
    }
  }

  public Beschreibung minimalBeschreibungErstellen(String kodid, NormdatenReferenz sprache,
      KulturObjektDokument kod, Set<NormdatenReferenz> autoren, Lizenz lizenz, String template)
      throws BeschreibungsException {

    if (kod == null || kod.getGueltigeIdentifikation() == null
        || kod.getGueltigeIdentifikation().getBesitzer() == null ||
        kod.getGueltigeIdentifikation().getAufbewahrungsOrt() == null) {
      throw new BeschreibungsException("KOD ungültig,Beschreibung kann nicht erstellt werden!");
    }

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withKodID(kodid)
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withErstellungsDatum(LocalDateTime.now())
        .withAenderungsDatum(LocalDateTime.now())
        .withBeschreibungssprache(sprache)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withBeschreibungsTyp(BeschreibungsTyp.valueOf(template.toUpperCase()))
        .withLizenz(lizenz)
        .build();

    Set<Identifikation> kopfIdentifikation = new HashSet<>();
    Identifikation beschreibungIdentifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(kod.getGueltigeIdentifikation().getBesitzer())
        .withAufbewahrungsOrt(kod.getGueltigeIdentifikation().getAufbewahrungsOrt())
        .withIdent(kod.getGueltigeSignatur())
        .build();

    Identifikation kodAltIdentifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.ALTSIGNATUR).withIdent(kodid)
        .addSammlungID(KOD_ID_COLLECTION)
        .build();

    kopfIdentifikation.add(beschreibungIdentifikation);
    kopfIdentifikation.add(kodAltIdentifikation);

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withIndentifikationen(kopfIdentifikation)
        .withTitel("")
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    Optional.ofNullable(autoren).ifPresent(authors -> beschreibung.getAutoren().addAll(authors));

    return beschreibung;
  }

  @Override
  public void reindexAllBeschreibungen(Bearbeiter bearbeiter) throws Exception {
    suchDokumentService.reindexAllBeschreibungen();
  }

  public void setDokumentSperreBoundary(DokumentSperreBoundary dokumentSperreBoundary) {
    this.dokumentSperreBoundary = dokumentSperreBoundary;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void addPURL(String beschreibungId, PURL purl) throws BeschreibungsException {
    log.debug("addPURL: beschreibungId={}, purl={}", beschreibungId, purl);

    Objects.requireNonNull(beschreibungId, "beschreibungId is required");
    Objects.requireNonNull(purl, "purl is required");

    Beschreibung beschreibung = findById(beschreibungId).orElseThrow(() ->
        new BeschreibungsException(getMessage("beschreibungservice_no_beschreibung_for_id", beschreibungId)));

    try {
      beschreibung.getPURLs().add(purl);
      TEIBeschreibungCommand.updatePURLs(beschreibung);

      teiXmlTransformationBoundary.transformTei2Hsp(beschreibung);
      teiXmlValidationBoundary.validateTeiXml(beschreibung);

      beschreibung = beschreibungsRepository.save(beschreibung);

      suchDokumentService.beschreibungUebernehmen(beschreibung);

    } catch (Exception e) {
      throw new BeschreibungsException(getMessage("beschreibungservice_error_create_purl",
          purl.getPurl(), beschreibungId, e.getMessage()), e);
    }

    if (VerwaltungsTyp.EXTERN == beschreibung.getVerwaltungsTyp()) {
      kulturObjektDokumentRepository.findByBeschreibungOptional(beschreibung.getId())
          .ifPresent(this::indexKulturObjektDokument);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void indexKulturObjektDokument(KulturObjektDokument kod) {
    log.info("indexKulturObjektDokument for kodId={}", kod.getId());
    indexService.indexKulturObjektDokumentWithKafkaTransaction(kod, ActivityStreamAction.UPDATE);
  }

  protected Beschreibung mapBeschreibungFromXML(String xml, Locale locale) throws BeschreibungsException {
    final Optional<TEI> optTeiElement;
    try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
      optTeiElement = unmarshalOne(inputStream);
    } catch (Exception e) {
      log.error("Error unmarshalling xml " + xml, e);
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_mapxml_unmarshal_error", e.getMessage()), e);
    }

    TEI teiElement = optTeiElement.orElseThrow(
        () -> new BeschreibungsException(getMessage(locale, "beschreibungservice_mapxml_kein_tei")));

    final List<Beschreibung> beschreibungen;

    try {
      TEI2BeschreibungMapper tei2BeschreibungMapper = new TEI2BeschreibungMapper();
      beschreibungen = tei2BeschreibungMapper.map(teiElement);
    } catch (Exception e) {
      log.error("Error mapping xml to beschreibung: xml=" + xml, e);
      throw new BeschreibungsException(
          getMessage(locale, "beschreibungservice_mapxml_map_error", e.getMessage()), e);
    }

    if (1 != beschreibungen.size()) {
      throw new BeschreibungsException(getMessage(locale, "beschreibungservice_mapxml_msdesc_error"));
    }

    return beschreibungen.get(0);
  }

  @Override
  public List<BeschreibungListDTO> findFilteredBeschreibungListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {
    return beschreibungsRepository.findFilteredBeschreibungListDTOs(first, pageSize, filterBy, sortByAsc);
  }

  @Override
  public int countFilteredBeschreibungListDTOs(Map<String, String> filterBy) {
    return beschreibungsRepository.countFilteredBeschreibungListDTOs(filterBy);
  }

  @Override
  public BeschreibungListDTO findBeschreibungListDTOId(String id) {
    return beschreibungsRepository.findBeschreibungListDTOById(id);
  }

}
