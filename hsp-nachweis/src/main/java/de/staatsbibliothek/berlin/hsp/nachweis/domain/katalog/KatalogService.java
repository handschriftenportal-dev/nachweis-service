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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DIV_TYPE_BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import de.digitalcollections.iiif.model.MetadataEntry;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIBeschreibungCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKatalogCommand;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http.HttpBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaKatalogProducer;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tei_c.ns._1.Body;
import org.tei_c.ns._1.Div;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.Pb;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 */

@Slf4j
@Dependent
public class KatalogService implements KatalogBoundary, Serializable {

  private static final long serialVersionUID = -6231634553640531082L;

  private static final String LABEL_SIGNATUR = "Signatur";
  private static final String PREFIX_PAGE = "Page";
  private static final String PREFIX_MXML = "MXML-";
  private static final String CRLF = "\r\n";

  private final KatalogRepository katalogRepository;
  private final BeschreibungsRepository beschreibungsRepository;
  private final IdentifikationRepository identifikationRepository;
  private final KafkaKatalogProducer kafkaKatalogProducer;
  private final KafkaIndexingProducer kafkaIndexingProducer;
  private final KulturObjektDokumentService kulturObjektDokumentService;
  private final TeiXmlValidationBoundary teiXmlValidationBoundary;
  private final TeiXmlTransformationBoundary teiXmlTransformationBoundary;
  private final HttpBoundary httpBoundary;

  @Inject
  public KatalogService(
      KatalogRepository katalogRepository,
      BeschreibungsRepository beschreibungsRepository,
      IdentifikationRepository identifikationRepository,
      KafkaKatalogProducer kafkaKatalogProducer,
      KafkaIndexingProducer kafkaIndexingProducer,
      KulturObjektDokumentService kulturObjektDokumentService,
      TeiXmlValidationBoundary teiXmlValidationBoundary,
      TeiXmlTransformationBoundary teiXmlTransformationBoundary,
      HttpBoundary httpBoundary) {
    this.katalogRepository = katalogRepository;
    this.beschreibungsRepository = beschreibungsRepository;
    this.identifikationRepository = identifikationRepository;
    this.kafkaKatalogProducer = kafkaKatalogProducer;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
    this.kulturObjektDokumentService = kulturObjektDokumentService;
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
    this.teiXmlTransformationBoundary = teiXmlTransformationBoundary;
    this.httpBoundary = httpBoundary;
  }

  @Override
  public Collection<Katalog> findAll() {

    return katalogRepository.findAll().stream().collect(Collectors.toSet());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Katalog importieren(Bearbeiter bearbeiter, Katalog katalog)
      throws KatalogException {

    Katalog katalogWork;

    try {
      Optional<Katalog> katalogOptional = katalogRepository.findByIdOptional(katalog.getId());
      if (katalogOptional.isPresent()) {
        katalogWork = katalogOptional.get();

        katalogWork.setTitle(katalog.getTitle());
        katalogWork.getAutoren().clear();
        katalogWork.getAutoren().addAll(katalog.getAutoren());
        katalogWork.setVerlag(katalog.getVerlag());
        katalogWork.setDigitalisat(katalog.getDigitalisat());
        katalogWork.setPublikationsJahr(katalog.getPublikationsJahr());
        katalogWork.setLizenzUri(katalog.getLizenzUri());
        katalogWork.setTeiXML(katalog.getTeiXML());
        katalogWork.setAenderungsDatum(katalog.getAenderungsDatum());

        updateKatalogAndBeschreibungen(katalogWork, ActivityStreamAction.UPDATE);
      } else {
        katalogWork = katalog;

        updateKatalogAndBeschreibungen(katalogWork, ActivityStreamAction.ADD);
      }
    } catch (KatalogException katalogException) {
      throw katalogException;
    } catch (Exception e) {
      throw new KatalogException(getMessage("katalog_service_fehler_import", e.getMessage()), e);
    }

    return katalogWork;
  }

  public Optional<KatalogViewModel> buildKatalogViewModel(String katalogId) {
    log.debug("buildKatalogViewModel: katalogId={}", katalogId);

    return katalogRepository.findByIdOptional(katalogId)
        .map(katalog -> KatalogViewModel.map(katalog, beschreibungsRepository.findByKatalogId(katalogId)));
  }

  @Transactional(rollbackOn = {Exception.class})
  Katalog updateKatalogAndBeschreibungen(Katalog katalog, ActivityStreamAction addOrUpdate)
      throws Exception {

    Objects.requireNonNull(katalog, getMessage("validierung_fehler_null", "Katalog"));
    Objects.requireNonNull(addOrUpdate, getMessage("validierung_fehler_null", "ActivityStreamAction"));

    String kID = "Katalog " + katalog.getId() + ": ";
    Objects.requireNonNull(katalog.getTeiXML(),
        getMessage("validierung_fehler_null", kID + "TEI-XML"));
    Objects.requireNonNull(katalog.getDigitalisat(),
        getMessage("validierung_fehler_null", kID + "Digitalisat"));
    Objects.requireNonNull(katalog.getDigitalisat().getManifestURL(),
        getMessage("validierung_fehler_null", kID + "Digitalisat.manifestURL"));

    TEIKatalogCommand.updateId(katalog);

    Set<Beschreibung> updatedBeschreibungen = volltexteUmhaengen(katalog);

    speichern(katalog, updatedBeschreibungen);

    sendMessages(katalog, addOrUpdate, updatedBeschreibungen);

    return katalog;
  }

  Optional<URI> findRangeURIForBeschreibung(Beschreibung beschreibung, Map<String, URI> allRanges) {
    Optional<URI> rangeURI = beschreibung.getGueltigeIdentifikation()
        .map(gueltigeIdentifikation -> allRanges.get(gueltigeIdentifikation.getIdent()));

    if (rangeURI.isEmpty()) {
      log.info("Found no rangeURI for signature of beschreibung {}, trying kod-signatures.",
          beschreibung.getId());

      rangeURI = identifikationRepository.getGueltigeSignatureByKodID(beschreibung.getKodID())
          .map(signature -> allRanges.get(signature.getSignature()));

      if (rangeURI.isEmpty()) {
        rangeURI = identifikationRepository.getAlternativeSignaturesByKodID(beschreibung.getKodID())
            .stream()
            .filter(altSignatur -> allRanges.containsKey(altSignatur.getSignature()))
            .findFirst()
            .map(altSignatur -> allRanges.get(altSignatur.getSignature()));
      }
    }
    return rangeURI;
  }

  Map<String, URI> getAllRangeURIsWithSignatureAsKey(URI manifestURL) throws KatalogException {
    log.info("getAllRangeURIsWithSignatureAsKey for manifestURL {}", manifestURL);

    Manifest manifest = loadManifest(manifestURL);
    List<Range> ranges = manifest.getRanges();
    if (ranges == null || ranges.isEmpty()) {
      log.info("No ranges found in manifestID = '{}'", manifest.getIdentifier().toASCIIString());
      return Collections.emptyMap();
    }
    Map<String, URI> allRanges = new HashMap<>(ranges.size());
    for (Range range : ranges) {
      List<MetadataEntry> metadata = range.getMetadata();
      if (metadata == null || metadata.isEmpty()) {
        continue;
      }
      for (MetadataEntry metadataEntry : metadata) {
        if (LABEL_SIGNATUR.equals(metadataEntry.getLabelString())) {
          String signature = metadataEntry.getValueString();
          String normalizedSignature = StringUtils.normalizeSpace(signature);

          allRanges.put(normalizedSignature, range.getIdentifier());

          log.debug("Found range {} for Signatur '{}', normalized '{}'",
              range.getIdentifier().toASCIIString(), signature, normalizedSignature);
          break;
        }
      }
    }

    return allRanges;
  }

  Manifest loadManifest(URI manifestURL) throws KatalogException {
    if (Objects.isNull(manifestURL)) {
      throw new KatalogException(getMessage("validierung_fehler_null", "manifestURL"));
    }

    try {
      Optional<String> manifestBody = httpBoundary.loadBodyFromURL(manifestURL);
      if (manifestBody.isEmpty() || manifestBody.get().isEmpty()) {
        throw new KatalogException(getMessage("katalog_service_fehler_manifestURL", manifestURL));
      }

      ObjectMapper iiifMapper = getIIIFObjectMapper();
      return iiifMapper.readValue(manifestBody.get(), Manifest.class);
    } catch (Exception e) {
      String message = getMessage("katalog_service_fehler_manifest", manifestURL, e.getMessage());
      log.warn(message, e);
      throw new KatalogException(message, e);
    }
  }

  ObjectMapper getIIIFObjectMapper() {
    final ObjectMapper iiifMapper = new IiifObjectMapper();
    iiifMapper.coercionConfigFor(LogicalType.Array)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    iiifMapper.coercionConfigFor(LogicalType.Collection)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    iiifMapper.coercionConfigFor(LogicalType.Map)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    return iiifMapper;
  }

  Set<Beschreibung> volltexteUmhaengen(Katalog katalog) throws Exception {

    Set<Beschreibung> updatedBeschreibungen = new LinkedHashSet<>();

    Set<Beschreibung> katalogBeschreibungen = new LinkedHashSet<>();
    katalogBeschreibungen.addAll(beschreibungsRepository.findByKatalogId(katalog.getHskID()));
    katalogBeschreibungen.addAll(beschreibungsRepository.findByKatalogId(katalog.getId()));

    Map<String, URI> allRanges = getAllRangeURIsWithSignatureAsKey(katalog.getDigitalisat().getManifestURL());

    LocalDateTime aenderungsDatum = LocalDateTime.now();
    katalog.setAenderungsDatum(aenderungsDatum);

    katalog.getBeschreibungen().clear();

    Map<String, Div> volltexte = getVolltextBeschreibungen(katalog);
    Map<String, KatalogBeschreibungReferenz> referenzen = new LinkedHashMap<>();

    for (Entry<String, Div> entry : volltexte.entrySet()) {
      KatalogBeschreibungReferenz referenz = createKatalogBeschreibungReferenz(katalog.getId(), entry.getValue());
      referenzen.put(entry.getKey(), referenz);
    }

    for (Beschreibung beschreibung : katalogBeschreibungen) {
      Optional<String> mxmlId = beschreibung.getAltIdentifier()
          .stream()
          .filter(altIdentifier -> volltexte.containsKey(altIdentifier) && referenzen.containsKey(altIdentifier))
          .findFirst();

      if (mxmlId.isEmpty()) {
        log.warn("No Volltext found for Beschreibung {}", beschreibung.getId());
        continue;
      }

      Div volltext = volltexte.get(mxmlId.get());
      KatalogBeschreibungReferenz referenz = referenzen.get(mxmlId.get());
      referenz.setBeschreibungsID(beschreibung.getId());

      Optional<URI> rangeURI = findRangeURIForBeschreibung(beschreibung, allRanges);
      if (rangeURI.isPresent()) {
        referenz.setManifestRangeURL(rangeURI.get());
      } else {
        log.warn("No ManifestRangeUrl found for Beschreibung {}", beschreibung.getId());
        continue;
      }

      beschreibung.setAenderungsDatum(aenderungsDatum);

      TEIBeschreibungCommand.updateAenderungsdatum(beschreibung);
      TEIBeschreibungCommand.updateVolltext(beschreibung, volltext);
      TEIBeschreibungCommand.updateKatalogId(beschreibung, katalog.getId());
      TEIBeschreibungCommand.updateKatalogReferences(referenz.getManifestRangeURL(),
          beschreibung, katalog.getDigitalisat().getManifestURL());

      updatedBeschreibungen.add(beschreibung);
    }

    katalog.getBeschreibungen().addAll(referenzen.values());

    return updatedBeschreibungen;
  }

  Map<String, Div> getVolltextBeschreibungen(Katalog katalog) throws Exception {
    if (Objects.isNull(katalog)) {
      throw new KatalogException(getMessage("validierung_fehler_null", "katalog"));
    }
    if (Objects.isNull(katalog.getTeiXML()) || katalog.getTeiXML().isEmpty()) {
      throw new KatalogException(getMessage("katalog_service_fehler_kein_teixml", katalog.getId()));
    }

    TEI tei = TEIObjectFactory.unmarshalOne(
            new ByteArrayInputStream(katalog.getTeiXML().getBytes(StandardCharsets.UTF_8)))
        .orElseThrow(() ->
            new KatalogException(getMessage("katalog_service_fehler_invalides_teixml", katalog.getId())));

    Body body = TEICommon.findFirst(Body.class, tei)
        .orElseThrow(() ->
            new KatalogException(getMessage("katalog_service_fehler_invalides_teixml", katalog.getId())));

    Map<String, Div> volltexte = new LinkedHashMap<>();
    body.getIndicesAndSpenAndSpanGrps().stream()
        .filter(elem -> Div.class.isAssignableFrom(elem.getClass()))
        .map(elem -> (Div) elem)
        .filter(div -> DIV_TYPE_BESCHREIBUNG.equals(div.getType()) && Objects.nonNull(div.getN()))
        .forEachOrdered(div -> volltexte.put(createMxmlId(div.getN()), div));
    return volltexte;
  }

  String createMxmlId(String value) {
    return Optional.ofNullable(value)
        .map(v -> v.replace(",", "-"))
        .map(v -> PREFIX_MXML + v)
        .orElse(null);
  }

  KatalogBeschreibungReferenz createKatalogBeschreibungReferenz(String katalogId, Div div) throws Exception {
    String beschreibungsID = Optional.ofNullable(div)
        .map(Div::getN)
        .map(n -> n.split(",")[0])
        .orElseThrow(() -> new KatalogException(getMessage("katalog_service_fehler_beschreibungId", katalogId)));

    List<Pb> pbs = new ArrayList<>();
    TEICommon.findAll(Pb.class, div, pbs);

    String seiteVon = "";
    String seiteBis = "";
    if (!pbs.isEmpty()) {
      seiteVon = getSeite(pbs.get(0));
      seiteBis = getSeite(pbs.get(pbs.size() - 1));
    }

    String ocrVolltext = getOcrVolltext(div);

    return new KatalogBeschreibungReferenz(beschreibungsID, seiteVon,
        seiteBis, ocrVolltext, null);
  }

  String getSeite(Pb pb) {
    return Optional.ofNullable(pb)
        .map(Pb::getN)
        .map(n -> n.replace(PREFIX_PAGE, ""))
        .orElse("");
  }

  String getOcrVolltext(Div div) throws Exception {
    List<P> blocks = new ArrayList<>();
    TEICommon.findAll(P.class, div, blocks);

    return blocks.stream()
        .map(p -> TEICommon.getContentAsString(p.getContent()))
        .map(StringUtils::normalizeSpace)
        .filter(text -> !text.trim().isEmpty())
        .collect(Collectors.joining(CRLF));
  }

  @Transactional
  void sendMessages(Katalog katalog, ActivityStreamAction katalogAction, Set<Beschreibung> beschreibungen)
      throws ActivityStreamsException {

    kafkaKatalogProducer.sendKatalogAsActivityStreamMessage(katalog,katalogAction,true,SYSTEM_USERNAME);

    for (Beschreibung beschreibung : beschreibungen) {
      kulturObjektDokumentService.findById(beschreibung.getKodID()).ifPresent(kod -> {
        try {
          kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(kod,katalogAction,
              true,SYSTEM_USERNAME);
        } catch (ActivityStreamsException e) {
          log.error("Error during send KOD Update", e);
        }
      });
    }
  }

  @Transactional(rollbackOn = Exception.class)
  void speichern(Katalog katalog, Set<Beschreibung> beschreibungen)
      throws TeiXmlTransformationException, TeiXmlValidationException {

    teiXmlValidationBoundary.validateTeiXml(katalog);
    katalogRepository.saveAndFlush(katalog);

    for (Beschreibung beschreibung : beschreibungen) {
      teiXmlTransformationBoundary.transformTei2Hsp(beschreibung);
      teiXmlValidationBoundary.validateTeiXml(beschreibung);
      beschreibungsRepository.save(beschreibung);
    }
  }
}
