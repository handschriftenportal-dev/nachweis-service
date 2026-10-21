package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Erklaerung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.RespStmt;
import org.tei_c.ns._1.TEI;

@Slf4j
@ApplicationScoped
public class NormdatenErkennungApplyService {

  private final NormdatenErkennungAuftragService auftragService;
  private final BeschreibungsRepository beschreibungsRepository;
  private final NormdatenUpdateService normdatenUpdateService;

  @Inject
  public NormdatenErkennungApplyService(
      NormdatenErkennungAuftragService auftragService,
      BeschreibungsRepository beschreibungsRepository,
      NormdatenUpdateService normdatenUpdateService) {
    this.auftragService = auftragService;
    this.beschreibungsRepository = beschreibungsRepository;
    this.normdatenUpdateService = normdatenUpdateService;
  }

  public void applyAuftrag(String auftragId) {
    var auftragOpt = auftragService.findByIdWithErgebnisse(auftragId);
    auftragOpt.ifPresentOrElse(auftrag -> {
      try {
        var rewrittenXml = resolveAndRewriteTei(auftrag);
        persistRewrittenBeschreibung(auftrag, rewrittenXml);
        completeApplying(auftragId);
      } catch (Exception e) {
        log.error("Error applying auftrag {}", auftragId, e);
        markApplyError(auftragId, e.getMessage());
      }
    }, () -> log.error("applyAuftrag: Could not load auftrag {}", auftragId));
  }

  private String resolveAndRewriteTei(NormdatenErkennungAuftrag auftrag) throws Exception {
    var beschreibungId = auftrag.getBeschreibungsdokumentId();
    var beschreibung = beschreibungsRepository.findById(beschreibungId);
    if (beschreibung == null) {
      throw new IllegalStateException("Beschreibung not found: " + beschreibungId);
    }
    var teiXml = beschreibung.getTeiXML();
    if (StringUtils.isBlank(teiXml)) {
      throw new IllegalStateException("TEI XML is empty for: " + beschreibungId);
    }
    var tei = TEIObjectFactory.unmarshalOne(
            new ByteArrayInputStream(teiXml.getBytes(StandardCharsets.UTF_8)))
        .orElseThrow(() -> new IllegalStateException("Could not parse TEI XML"));

    updateTEI(auftrag, tei);
    return TEIObjectFactory.marshal(tei);
  }

  @Transactional(TxType.REQUIRED)
  void persistRewrittenBeschreibung(NormdatenErkennungAuftrag auftrag, String rewrittenXml) {
    var beschreibung = beschreibungsRepository.findById(auftrag.getBeschreibungsdokumentId());
    if (beschreibung == null) {
      throw new IllegalStateException("Beschreibung not found: " + auftrag.getBeschreibungsdokumentId());
    }
    beschreibung.setTeiXML(rewrittenXml);
    beschreibungsRepository.save(beschreibung);
  }

  @Transactional(TxType.REQUIRED)
  void completeApplying(String auftragId) {
    var auftragOpt = auftragService.findById(auftragId);
    auftragOpt.ifPresent(auftrag -> {
      auftrag.completeApplying();
      auftragService.save(auftrag);
    });
  }

  /** REQUIRES_NEW: error persists even when the surrounding call rolls back. */
  @Transactional(TxType.REQUIRES_NEW)
  void markApplyError(String auftragId, String message) {
    var auftragOpt = auftragService.findById(auftragId);
    auftragOpt.ifPresent(auftrag -> {
      auftrag.markError(message);
      auftragService.save(auftrag);
    });
  }

  private record NormdatenUpdates(Set<RespStmt> respStmts, List<Object> nodes) {
    NormdatenUpdates() {
      this(Set.of(), List.of());
    }

    NormdatenUpdates merge(NormdatenUpdates other) {
      var mergedRespStmts = new HashSet<>(respStmts);
      mergedRespStmts.addAll(other.respStmts());
      var mergedNodes = new ArrayList<>(nodes);
      mergedNodes.addAll(other.nodes());
      return new NormdatenUpdates(mergedRespStmts, mergedNodes);
    }

    Change createTEIChangeElement() {
      var date = new Date();
      date.setWhen(LocalDate.now().format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
      var change = new Change();
      change.getContent().add(date);
      change.getContent().add(" Ergänzung von @key und / oder @ref an ");
      change.getContent().addAll(nodes);
      change.getContent().add(" durch Normdatenerkennungsalgorithmus des HSP.");
      return change;
    }
  }

  private TEINormdatenErkennungRespStmts createRspStmts(NormdatenErkennungAuftrag auftrag) {
    var appliedBy = auftrag.getAppliedBy();
    var bearbeiterName = appliedBy != null ? appliedBy.getName() : null;
    return StringUtils.isBlank(bearbeiterName)
        ? new TEINormdatenErkennungRespStmts()
        : new TEINormdatenErkennungRespStmts(bearbeiterName);
  }

  private void updateTEI(NormdatenErkennungAuftrag auftrag, TEI tei) {
    var respStmts = createRspStmts(auftrag);

    var updates = applyResponsesToPersNames(auftrag, TEIQuery.findAllPersNamesWithPath(tei), respStmts);
    // when updating the TEI for <corpName> and <placeName>, extend this code via:
    //    .merge(applyResponsesToCorpNames(auftrag, ..., respStmts))
    //    .merge(applyResponsesToPlaceNames(auftrag, ..., respStmts))

    updateTEIHeader(tei, updates);
  }

  private NormdatenUpdates applyResponsesToPersNames(
      NormdatenErkennungAuftrag auftrag,
      Map<String, PersName> persNamesMap,
      TEINormdatenErkennungRespStmts respStmts) {
    var updates = new NormdatenUpdates();
    for (var response : auftrag.getErgebnisse()) {
      updates = updates.merge(applyResponseToNode(response, persNamesMap, respStmts));
    }
    return updates;
  }

  private NormdatenUpdates applyResponseToNode(NormdatenErkennungEintrag response,
      Map<String, PersName> persNamesMap, TEINormdatenErkennungRespStmts respStmts) {
    if (response.getSelectionType() == NormdatenErkennungAuswahlTyp.NONE) {
      return new NormdatenUpdates();
    }
    var fact = resolveEntityFact(response);
    if (fact == null) {
      log.warn("Could not resolve entity fact for response {}, skipping", response.getId());
      return new NormdatenUpdates();
    }
    var persName = persNamesMap.get(response.getXpath());
    if (persName == null) {
      log.warn("PersName not found at xpath {} for response {}, skipping",
          response.getXpath(), response.getId());
      return new NormdatenUpdates();
    }

    var anfrage = response.getAnfrage();
    var resp = (anfrage != null && !StringUtils.isBlank(anfrage.id()))
        ? respStmts.idReplacement() : respStmts.nameReplacement();
    applyFactToPersName(persName, fact, resp);
    return new NormdatenUpdates(Set.of(resp), List.of(persName));
  }

  private void applyFactToPersName(PersName persName, GNDEntityFact fact, RespStmt resp) {
    if (fact.getId() != null) {
      persName.setKey(fact.getId());
    }
    String ref = resolveRef(fact);
    if (ref != null) {
      persName.getReves().clear();
      persName.getReves().add(ref);
    }
    persName.getResps().add("#" + resp.getId());
  }

  private void updateTEIHeader(TEI tei, NormdatenUpdates updates) {
    if (updates.respStmts.isEmpty()) {
      return;
    }
    var titleStmt = TEIQuery.queryForTitleStmt(tei);
    var revisionDesc = TEIQuery.queryForRevisionDesc(tei);

    titleStmt.ifPresent(ts ->
        ts.getAuthorsAndEditorsAndRespStmts().addAll(updates.respStmts));

    revisionDesc.ifPresent(rd ->
      rd.getRevisionChangeAttribute().addFirst(updates.createTEIChangeElement())
    );
  }

  private GNDEntityFact resolveEntityFact(NormdatenErkennungEintrag response) {
    if (response.getSelectionType() == NormdatenErkennungAuswahlTyp.MANUAL) {
      return normdatenUpdateService.ensureInGraph(
          response.getManualIdentifier(), response.getNormdatenTyp()).orElse(null);
    }

    var selectedTreffer = response.getSelectedTreffer();
    if (selectedTreffer == null) {
      return null;
    }

    var fact = selectedTreffer.getGndEntityFact();
    if (fact == null) {
      return null;
    }

    var quelle = selectedTreffer.getErklaerung() != null
        ? selectedTreffer.getErklaerung().quelle() : null;

    if (Erklaerung.QUELLE_LOBID.equals(quelle) && fact.getGndIdentifier() != null) {
      return normdatenUpdateService.ensureInGraph(
          fact.getGndIdentifier(), response.getNormdatenTyp()).orElse(null);
    }

    return fact;
  }

  private String resolveRef(GNDEntityFact fact) {
    if (!StringUtils.isBlank(fact.getGndIdentifier())) {
      return NormdatenGraphQLBoundary.DNB_URL_BASE + fact.getGndIdentifier();
    }
    return fact.getUri();
  }
}
