package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN_EXTERN;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN_INTERN;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN_KATALOG;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.DIGITALISATE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.KATALOGE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.KULTUROBJEKTE;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexJobEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJob;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServiceInfoDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServiceStatsDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BestandInstitutionElementDTO;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */

@Dependent
@Slf4j
public class BestandsBoundaryService implements BestandsBoundary, Serializable {

  @Serial
  private static final long serialVersionUID = -917203372351409139L;

  @Getter
  private final DiscoveryServicePort discoveryServicePort;

  @Getter
  private final BestandsRepository bestandsRepository;
  private final transient ResourceBundle resourceBundle = ResourceBundle
      .getBundle(I18NController.APPLICATION_MESSAGES, I18NController.getLocale());
  Event<IndexJob> indexJobEvent;

  @Inject
  public BestandsBoundaryService(@RestClient DiscoveryServicePort discoveryServicePort,
      BestandsRepository bestandsRepository,
      @sendIndexJobEvent Event<IndexJob> indexJobEvent) {
    this.discoveryServicePort = discoveryServicePort;
    this.bestandsRepository = bestandsRepository;
    this.indexJobEvent = indexJobEvent;
  }

  @Override
  public BestandsView createBestandsView() {
    BestandsView bestandsView = new BestandsView();

    try {
      loadBestaendeOfAllEntities(bestandsView);
    } catch (Exception error) {
      log.error("Error during find Bestaende {}", error.getMessage(), error);
      bestandsView = new BestandsView(error.getMessage());
    }

    return bestandsView;
  }

  @Override
  public void loadBestaendeOfAllEntities(BestandsView bestandsView) {
    BestandNachweisDTO bestandNachweisDTO = bestandsRepository.findAllBestaende();
    DiscoveryServiceInfoDTO discoveryServiceInfoDTO = discoveryServicePort.findInfo();

    bestandsView.getBestandsElemente().add(new BestandsElement(KULTUROBJEKTE,
        resourceBundle.getString("document_titel_kulturobjektdokumente"),
        Math.toIntExact(bestandNachweisDTO.getAnzahlKOD()),
        discoveryServiceInfoDTO.getItems().getHspObjects()));
    final String documentTitelBeschreibungen = resourceBundle.getString(
        "document_titel_beschreibungen");
    bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN,
        documentTitelBeschreibungen,
        Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungen()),
        discoveryServiceInfoDTO.getItems().getHspDescriptions()
            + discoveryServiceInfoDTO.getItems().getHspDescriptionsRetro()));
    bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN_INTERN,
        documentTitelBeschreibungen + " (Intern)",
        Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungenIntern()),
        -1));
    bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN_EXTERN,
        documentTitelBeschreibungen + " (Extern)",
        Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungenExtern()),
        discoveryServiceInfoDTO.getItems().getHspDescriptions()
            + discoveryServiceInfoDTO.getItems().getHspDescriptionsRetro()));
    bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN_KATALOG,
        documentTitelBeschreibungen + " (Katalog)",
        Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungenKatalog()),
        discoveryServiceInfoDTO.getItems().getHspDescriptionsRetro()));
    bestandsView.getBestandsElemente().add(
        new BestandsElement(DIGITALISATE, resourceBundle.getString("kod_detail_digitalisate"),
            Math.toIntExact(bestandNachweisDTO.getAnzahlDigitalisate()),
            discoveryServiceInfoDTO.getItems().getHspDigitized()));
    bestandsView.getBestandsElemente()
        .add(new BestandsElement(KATALOGE, resourceBundle.getString("document_titel_kataloge"),
            Math.toIntExact(bestandNachweisDTO.getAnzahlKataloge()), -1));

    bestandsView.getBestandsElemente()
        .sort(Comparator.comparingLong(BestandsElement::getBestandNachweis));
  }

  @Override
  public List<BestandInstitutionElement> loadBestandsInstitutionDataForKulturobjektdokument() {
    List<BestandInstitutionElementDTO> bestandInstitutionElementDTOS = bestandsRepository.findAllKulturObjekteForInstitution();
    DiscoveryServiceStatsDTO discoveryServiceInfoDTOS = discoveryServicePort.findStats();

    List<BestandInstitutionElement> bestand = bestandInstitutionElementDTOS.stream().map(dto -> {
      BestandInstitutionElement element = new BestandInstitutionElement(dto, KULTUROBJEKTE);
      element.setBestandPraesentation(
          discoveryServiceInfoDTOS.getKods().getItems()
              .getOrDefault(dto.getBesitzendeEinrichtungID(), 0));
      return element;
    }).collect(Collectors.toList());

    discoveryServiceInfoDTOS.getKods().getItems().forEach((key, value) -> {
      if (bestandInstitutionElementDTOS.stream()
          .filter(b -> b.getBesitzendeEinrichtungID().equals(key)).findFirst()
          .isEmpty()) {
        bestand.add(new BestandInstitutionElement(key, "", value
            , 0, KULTUROBJEKTE));
      }
    });

    return bestand;
  }

  @Override
  public List<BestandInstitutionElement> loadBestandsInstitutionDataForBeschreibungen() {
    List<BestandInstitutionElementDTO> bestandInstitutionElementDTOS = bestandsRepository.findAllBeschreibungenForInstitution();
    log.info("Loading Beschreibungs Bestaende {}", bestandInstitutionElementDTOS.stream()
        .map(BestandInstitutionElementDTO::toString).collect(Collectors.joining(",")));

    DiscoveryServiceStatsDTO discoveryServiceInfoDTOS = discoveryServicePort.findStats();

    final Map<String, Integer> webDescriptions = discoveryServiceInfoDTOS.getDescription()
        .getItems();

    List<BestandInstitutionElement> bestand = bestandInstitutionElementDTOS.stream()
        .map(dto -> {
          BestandInstitutionElement element = new BestandInstitutionElement(dto, BESCHREIBUNGEN);
          element.setBestandPraesentation(
              webDescriptions
                  .getOrDefault(dto.getBesitzendeEinrichtungID(), 0));

          return element;
        }).collect(Collectors.toList());

    discoveryServiceInfoDTOS.getDescription().getItems().forEach((key, value) -> {
      if (bestandInstitutionElementDTOS.stream()
          .filter(b -> b.getBesitzendeEinrichtungID().equals(key)).findFirst()
          .isEmpty()) {
        bestand.add(new BestandInstitutionElement(key, "", value
            , 0, DIGITALISATE));
      }
    });

    return bestand;
  }

  @Override
  public List<BestandInstitutionElement> loadBestandsInstitutionDataForDigitalisate() {
    List<BestandInstitutionElementDTO> bestandInstitutionElementDTOS = bestandsRepository.findAllDigitalisateForKOD();
    DiscoveryServiceStatsDTO discoveryServiceInfoDTOS = discoveryServicePort.findStats();

    List<BestandInstitutionElement> bestand = bestandInstitutionElementDTOS.stream().map(dto -> {
      BestandInstitutionElement element = new BestandInstitutionElement(dto, DIGITALISATE);
      element.setBestandPraesentation(
          discoveryServiceInfoDTOS.getDigitizedItems().getItems()
              .getOrDefault(dto.getBesitzendeEinrichtungID(), 0));
      return element;
    }).collect(Collectors.toList());

    discoveryServiceInfoDTOS.getDigitizedItems().getItems().forEach((key, value) -> {
      if (bestandInstitutionElementDTOS.stream()
          .filter(b -> b.getBesitzendeEinrichtungID().equals(key)).findFirst()
          .isEmpty()) {
        bestand.add(new BestandInstitutionElement(key, "", value
            , 0, DIGITALISATE));
      }
    });

    return bestand;
  }

}
