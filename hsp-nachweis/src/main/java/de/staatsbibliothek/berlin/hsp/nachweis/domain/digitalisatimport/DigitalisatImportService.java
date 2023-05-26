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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.LOCAL_DATE_OPT_MONTH_OPT_DAY_FORMAT;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.xml.bind.JAXBException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.OrgName;
import org.tei_c.ns._1.PlaceName;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.11.2020.
 * @version 1.0
 */

@ApplicationScoped
public class DigitalisatImportService {

  private static final Logger logger = LoggerFactory.getLogger(DigitalisatImportService.class);
  private static final DateTimeFormatter OPTIONAL_DATE_PARSER =
      DateTimeFormatter.ofPattern(LOCAL_DATE_OPT_MONTH_OPT_DAY_FORMAT);

  private final String praesentationBaseUrl;
  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private NormdatenReferenzBoundary normdatenReferenzBoundary;
  private ImportVorgangBoundary importVorgangBoundary;
  private DokumentSperreBoundary dokumentSperreService;

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

  public void onImportMessage(@Observes @neueImporte DigitalisatImport importe) {
    logger.info("Neuer Digitalisat Import erhalten {} ", importe.getMessageDTO());
    try {
      doImportMessage(importe);
    } catch (Exception error) {
      logger.error("Error during Digitalisat importieren!", error);
      sendFailureMessageToImport(importe.getMessageDTO(), error.getMessage());
    }
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 1800)
  public void doImportMessage(DigitalisatImport importe) throws Exception {

    List<Map<KulturObjektDokumentViewModel, DigitalisatViewModel>> success = new LinkedList<>();

    ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(importe.getMessageDTO());

    logger.info("Starting ImportVorgang {} for Digitalisate Import", importVorgang.getId());

    AtomicReference<Boolean> error = new AtomicReference<>();
    error.set(false);

    for (Bibl bibl : findAllTEIDigitalisate(extractTEIFromMessage(importe.getMessageDTO()))) {

      String kodIDorSignatur = extraxtKODIDFromBibl(bibl);

      DigitalisatViewModel digitalisatViewModel = createDigitalisatModelFromTEIBibl(bibl)
          .orElseThrow(() -> {
            error.set(true);
            throw new DigitalisatImportException(
                "Digitalisat konnte nicht erzeugt werden, notwendige Parameter fehlen!");
          });

      NormdatenReferenz aufbewahrungOrt = findNormDatenOrt(bibl, null)
          .orElseThrow(() -> new DigitalisatImportException(
              "Kein AufbewahrungsOrt in Bibl " + kodIDorSignatur));

      NormdatenReferenz besitzendeEinrichtung = findNormdatenKoerperschaft(bibl, null)
          .orElseThrow(
              () -> new DigitalisatImportException(
                  "Keine BesitzendeEinrichtung in Bibl " + kodIDorSignatur));

      findKODIDOrSignature(kodIDorSignatur, aufbewahrungOrt, besitzendeEinrichtung)
          .ifPresentOrElse(
              kodID -> kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel(kodID)
                  .ifPresentOrElse(
                      kod -> success.add(Map.of(kod, digitalisatViewModel)),
                      () -> {
                        error.set(true);
                        throw new DigitalisatImportException("KOD konnte nicht erstellt werden");
                      }),
              () -> {
                error.set(true);
                throw new DigitalisatImportException(
                    "Kein KOD gefunden zu KOD ID oder Signatur " + kodIDorSignatur
                        + ", Aufbewahrungsort " + aufbewahrungOrt.getName()
                        + ", besitzende Einrichtung " + besitzendeEinrichtung.getName());
              });
    }

    if (success.isEmpty()) {
      throw new DigitalisatImportException(
          "Importvorgang " + importVorgang.getId() + " enthielt keine importierbaren Digitalisate!");
    }

    Set<SperreEintrag> sperreEintrage = createSperreEintrage(success);
    dokumentSperreService.acquireSperre(importe.getBearbeiter(),
        SperreTyp.IN_TRANSACTION,
        "Digitalisate importieren",
        sperreEintrage.toArray(new SperreEintrag[sperreEintrage.size()]));

    saveToSystem(importe.getMessageDTO(), success);

  }

  private Set<SperreEintrag> createSperreEintrage(
      List<Map<KulturObjektDokumentViewModel, DigitalisatViewModel>> success) {
    Set<SperreEintrag> kodIds = new HashSet<>();
    for (Map<KulturObjektDokumentViewModel, DigitalisatViewModel> entry : success) {
      for (KulturObjektDokumentViewModel dokumentViewModel : entry.keySet()) {
        kodIds.add(new SperreEintrag(dokumentViewModel.getId(), SperreDokumentTyp.KOD));
      }
    }
    return kodIds;
  }

  public void sendFailureMessageToImport(ActivityStreamMessageDTO message, String errorMessage) {
    ImportVorgang importVorgang;
    try {

      importVorgang = importVorgangBoundary.getImportVorgang(message);
      importVorgang.setFehler(errorMessage);
      importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);

    } catch (Exception e) {
      logger.error("Error during Send Import Message!", e);
    }
  }

  public void saveToSystem(ActivityStreamMessageDTO message,
      List<Map<KulturObjektDokumentViewModel, DigitalisatViewModel>> success) {

    ImportVorgang importVorgang;
    try {

      importVorgang = importVorgangBoundary.getImportVorgang(message);

      importDigitalisateHinzufuegen(message, success, importVorgang);

    } catch (Exception e) {
      logger.error("Error during save Digitalisat Import to System!", e);
    }

  }

  @Transactional(rollbackOn = {Exception.class})
  void importDigitalisateHinzufuegen(ActivityStreamMessageDTO message,
      List<Map<KulturObjektDokumentViewModel, DigitalisatViewModel>> success,
      ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException, SperreAlreadyExistException {
    for (Map<KulturObjektDokumentViewModel, DigitalisatViewModel> map : success) {

      for (Map.Entry<KulturObjektDokumentViewModel, DigitalisatViewModel> entry : map.entrySet()) {

        logger.debug("Digitalisat Hinzufügen {} ", entry.getValue());

        digitalisatLoeschenIfExist(entry.getKey(), entry.getValue());
        kulturObjektDokumentBoundary.digitalisatHinzufuegen(entry.getKey(), entry.getValue());

        message.getObjects().stream()
            .filter(o -> ActivityStreamsDokumentTyp.DIGITALISAT.equals(o.getType()))
            .findFirst().ifPresent(o -> {

              ImportFile importFile = findImportFileForActivityStreamObject(importVorgang, o);

              importFile.getDataEntityList()
                  .add(new DataEntity(entry.getValue().getId(),
                      entry.getValue().getManifestURL() != null ? entry.getValue().getManifestURL()
                          : entry.getValue().getAlternativeUrl(),
                      praesentationBaseUrl + "discovery?hspobjectid=" + entry.getKey().getId()
                          + "?q=%2A"));
            });
      }
    }

    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
    importVorgangBoundary.sendImportJobToKafka(importVorgang);
  }

  ImportFile findImportFileForActivityStreamObject(ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject) {

    ImportFile importFile;
    Objects.requireNonNull(activityStreamObject.getName(), "ActivityStreamObject without name");
    importFile = importVorgang.getImportFiles().stream()
        .filter(searchForImportFile -> activityStreamObject.getName()
            .equals(searchForImportFile.getDateiName()))
        .findFirst().orElse(null);
    return importFile;
  }

  void digitalisatLoeschenIfExist(KulturObjektDokumentViewModel kod,
      DigitalisatViewModel digitalisat) throws SperreAlreadyExistException {

    kod.getDigitalisate().stream()
        .filter(d -> ((Objects.nonNull(d.getManifestURL()) && d.getManifestURL().equals(digitalisat.getManifestURL()))
            || (Objects.nonNull(d.getAlternativeUrl()) && d.getAlternativeUrl()
            .equals(digitalisat.getAlternativeUrl())))
            && d.getDigitalisatTyp().equals(digitalisat.getDigitalisatTyp()))
        .forEach(d -> kulturObjektDokumentBoundary.digitalisatLoeschen(kod, d));
  }

  public Optional<DigitalisatViewModel> createDigitalisatModelFromTEIBibl(Bibl bibl)
      throws DigitalisatImportException {

    Optional<DigitalisatViewModel> digitalisatViewModel;

    try {

        digitalisatViewModel = mapTEIToModel(bibl);

        digitalisatViewModel.ifPresent(model -> {

          findNormdatenKoerperschaft(bibl, TEIValues.TYPE_DIGITALISING)
              .or(() -> findNormdatenKoerperschaft(bibl, null))
              .ifPresent(model::setEinrichtung);

          findNormDatenOrt(bibl, TEIValues.TYPE_DIGITALISING)
              .or(() -> findNormDatenOrt(bibl, null))
              .ifPresent(model::setOrt);
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

    if (isAnyURLInTEI(refs)) {

      DigitalisatViewModel model = DigitalisatViewModel.builder()
          .id(UUID.randomUUID().toString())
          .build();

      extractManifest(refs, model);

      extractOtherURL(refs, model);

      extractThumbnail(refs, model);

      extractedDigitizedDate(dates, model);

      extractedIssuedDate(dates, model);

      digitalisatViewModel = Optional.of(model);
    }

    return digitalisatViewModel;
  }

  void extractedIssuedDate(List<Date> dates, DigitalisatViewModel model) {
    dates.stream()
        .filter(d -> TEIValues.DATE_ISSUED.equals(d.getType()))
        .findFirst()
        .map(date -> parseDate(date.getWhen()))
        .ifPresentOrElse(model::setErstellungsdatum,
            () -> model.setErstellungsdatum(LocalDate.now()));
  }

  void extractedDigitizedDate(List<Date> dates, DigitalisatViewModel model) {
    dates.stream()
        .filter(d -> TEIValues.DATE_DIGITIZED.equals(d.getType()))
        .findFirst()
        .map(date -> parseDate(date.getWhen()))
        .ifPresent(model::setDigitalisierungsdatum);
  }

  void extractThumbnail(List<Ref> refs, DigitalisatViewModel model) {
    refs.stream()
        .filter(r -> TEIValues.REF_TYPE_THUMBNAIL.equals(r.getType())).findFirst()
        .ifPresent(r -> model.setThumbnail(String.join(" ", r.getTargets())));
  }

  void extractOtherURL(List<Ref> refs, DigitalisatViewModel model) {
    refs.stream()
        .filter(r -> TEIValues.REF_TYPE_OTHER.equals(r.getType())).findFirst()
        .ifPresent(r -> {
          model.setAlternativeUrl(String.join(" ", r.getTargets()));
          if (model.getDigitalisatTyp() == null) {
            model.setDigitalisatTyp(DigitalisatTyp.fromString(r.getSubtype()));
          }
        });
  }

  void extractManifest(List<Ref> refs, DigitalisatViewModel model) {
    refs.stream()
        .filter(r -> TEIValues.REF_TYPE_MANIFEST.equals(r.getType())).findFirst()
        .ifPresent(r -> {
          model.setManifestURL(String.join(" ", r.getTargets()));
          model.setDigitalisatTyp(DigitalisatTyp.fromString(r.getSubtype()));
        });
  }

  boolean isAnyURLInTEI(List<Ref> refs) {
    return refs.stream()
        .anyMatch(
            r -> TEIValues.REF_TYPE_MANIFEST.equals(r.getType()) || TEIValues.REF_TYPE_OTHER
                .equals(r.getType()));
  }

  public Optional<NormdatenReferenz> findNormDatenOrt(Bibl bibl, String ortType)
      throws DigitalisatImportException {
    List<PlaceName> placeNames = new ArrayList<>();
    try {
      TEICommon.findAll(PlaceName.class, bibl, placeNames);

      Optional<String> placeId = placeNames.stream()
          .filter(placeName -> (Objects.isNull(ortType) && Objects.isNull(placeName.getType()))
              || (Objects.nonNull(ortType) && ortType.equals(placeName.getType())))
          .findFirst()
          .flatMap(placeName -> placeName.getReves().stream().findFirst());

      if (placeId.isPresent()) {
        return normdatenReferenzBoundary.findOneByIdOrNameAndType(placeId.get(), NormdatenReferenz.ORT_TYPE_NAME);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      throw new DigitalisatImportException(e);
    }
  }

  public Optional<NormdatenReferenz> findNormdatenKoerperschaft(Bibl bibl,
      String einrichtungType)
      throws DigitalisatImportException {
    List<OrgName> orgNames = new ArrayList<>();
    try {
      TEICommon.findAll(OrgName.class, bibl, orgNames);
      Optional<String> koerperschaftId = orgNames.stream()
          .filter(orgName -> (Objects.isNull(einrichtungType) && Objects.isNull(orgName.getType()))
              || (Objects.nonNull(einrichtungType) && einrichtungType.equals(orgName.getType())))
          .findFirst()
          .flatMap(orgName -> orgName.getReves().stream().findFirst());
      if (koerperschaftId.isPresent()) {
        return normdatenReferenzBoundary.findOneByIdOrNameAndType(koerperschaftId.get(),
            NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      throw new DigitalisatImportException(e);
    }
  }

  public List<Bibl> findAllTEIDigitalisate(List<TEI> teiList) throws Exception {
    List<Bibl> biblList = new ArrayList<>();

    for (TEI tei : teiList) {
      TEICommon.findAll(Bibl.class, tei, biblList);
    }

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

  public Optional<String> findKODIDOrSignature(String kodIDorSignatur, NormdatenReferenz ort,
      NormdatenReferenz einrichtung) {

    AtomicReference<String> kodID = new AtomicReference<>();

    if (kodIDorSignatur != null) {

      kulturObjektDokumentRepository.findByIdOptional(kodIDorSignatur)
          .ifPresentOrElse(kod -> kodID.set(kod.getId()),
              () -> kulturObjektDokumentRepository.findByIdentifikationIdent(kodIDorSignatur)
                  .stream()
                  .filter(
                      kod -> checkIfIdentifikationIsEqual(Set.of(kod.getGueltigeIdentifikation()),
                          ort, einrichtung))
                  .findFirst()
                  .or(() -> kulturObjektDokumentRepository.findByAlternativeIdentifikationIdent(
                          kodIDorSignatur)
                      .stream()
                      .filter(
                          kod -> checkIfIdentifikationIsEqual(kod.getAlternativeIdentifikationen(),
                              ort, einrichtung))
                      .findFirst()
                  ).ifPresent(kod -> kodID.set(kod.getId())));
    }
    return Optional.ofNullable(kodID.get());
  }

  protected String extraxtKODIDFromBibl(Bibl bibl) throws Exception {
    List<Ref> refs = new ArrayList<>();

    TEICommon.findAll(Ref.class, bibl, refs);

    Optional<List<String>> ids = refs.stream()
        .filter(r -> DokumentObjektTyp.HSP_OBJECT.toString().equals(r.getType()))
        .findFirst()
        .map(Ref::getTargets);

    return ids.map(strings -> String.join(" ", strings)).orElse("");
  }

  boolean checkIfIdentifikationIsEqual(Set<Identifikation> gueltigeIdentifikationen,
      NormdatenReferenz ort, NormdatenReferenz einrichtung) {

    return Objects.nonNull(gueltigeIdentifikationen)
        && gueltigeIdentifikationen
        .stream()
        .anyMatch(identifikation ->
            Objects.nonNull(identifikation)
                && Objects.nonNull(identifikation.getAufbewahrungsOrt())
                && Objects.nonNull(identifikation.getBesitzer())
                && Objects.nonNull(ort)
                && Objects.nonNull(einrichtung)

                // compare AufbewahrungsOrt
                && identifikation.getAufbewahrungsOrt().equals(ort)
                // compare Besitzer
                && identifikation.getBesitzer().equals(einrichtung)
        );
  }

  LocalDate parseDate(String value) {
    if (Objects.isNull(value) || value.trim().isEmpty()) {
      return null;
    }

    TemporalAccessor temporalAccessor;
    try {
      temporalAccessor = OPTIONAL_DATE_PARSER.parseBest(value, LocalDate::from, YearMonth::from, Year::from);
    } catch (Exception e) {
      logger.warn("Error parsing date {} : {} ", value, e.getMessage());
      return null;
    }

    if (temporalAccessor instanceof LocalDate) {
      return (LocalDate) temporalAccessor;
    } else if (temporalAccessor instanceof YearMonth) {
      return ((YearMonth) temporalAccessor).atDay(1);
    } else if (temporalAccessor instanceof Year) {
      return ((Year) temporalAccessor).atMonth(Month.JANUARY).atDay(1);
    } else {
      return null;
    }
  }
}
