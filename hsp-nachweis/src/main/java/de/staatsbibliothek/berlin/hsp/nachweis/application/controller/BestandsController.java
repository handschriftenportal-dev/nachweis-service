package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandInstitutionElement;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsElement;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */

@SessionScoped
@Named
@LoginCheck
@Slf4j
public class BestandsController implements Serializable {

  public static final String CLIENTID_BESTAENDE_TAB_KOD_INSTITUTIONS = "bestaendeTabKODInstitutions";
  public static final String CLIENT_ID_BESTAENDE_TAB_BESCHREIBUNGEN_INSTITUTIONS = "bestaendeTabBeschreibungenInstitutions";

  public static final String CLIENT_ID_BESTAENDE_TAB_DIGITALISATE_INSTITUTIONS = "bestaendeTabDigitalisateInstitutions";
  public static final String CLIENTID_BESTAENDE_TAB = "bestaendeTabAll";
  @Serial
  private static final long serialVersionUID = 8856279690329953292L;
  private final transient BestandsBoundary bestandsBoundary;
  private final transient Authentication authentication;
  @Getter
  private List<SortMeta> sortBy;
  private List<BestandsElement> bestandsElemente;
  private BestandsView bestandsView;

  @Inject
  public BestandsController(BestandsBoundary bestandsBoundary, Authentication authentication) {
    this.bestandsBoundary = bestandsBoundary;
    this.authentication = authentication;
  }

  @PostConstruct
  public void setup() {
    sortBy = new ArrayList<>();

    sortBy.add(SortMeta.builder()
        .field("label")
        .order(SortOrder.ASCENDING)
        .priority(1)
        .build());
    sortBy.add(SortMeta.builder()
        .field("bestandNachweis")
        .order(SortOrder.ASCENDING)
        .build());
    sortBy.add(SortMeta.builder()
        .field("bestandPraesentation")
        .order(SortOrder.ASCENDING)
        .build());

    sortBy.add(SortMeta.builder()
        .field("bestandDifferenz")
        .order(SortOrder.ASCENDING)
        .build());

    bestandsView = bestandsBoundary.createBestandsView();

    bestandsElemente = bestandsView.getBestandsElemente();

  }


  public BestandsView getBestandsView() {
    return bestandsBoundary.createBestandsView();
  }

  public List<BestandsElement> getBestandsElemente() {
    return bestandsElemente;
  }

  public List<BestandInstitutionElement> getBestandInstitutionKODElements() {
    return bestandsView.getBestandInstitutionElementeForKODs();
  }

  public List<BestandInstitutionElement> getBestandInstitutionBeschreibungElements() {
    return bestandsView.getBestandInstitutionElementeForBeschreibungen();
  }

  public List<BestandInstitutionElement> getBestandInstitutionDigitalisateElements() {
    return bestandsView.getBestandInstitutionElementeForDigitalisate();
  }

  public void onTabChanged(TabChangeEvent<?> tabChangeEvent) {

    final Tab activeTab = tabChangeEvent.getTab();

    log.info("On Tab Changed Event recieved {}", activeTab.getClientId());

    if (activeTab.getClientId().contains(CLIENTID_BESTAENDE_TAB)
        && activeTab.isRendered()) {
      log.info("Open Alll Bestaende Tab");
      bestandsView.getBestandsElemente().clear();
      bestandsBoundary.loadBestaendeOfAllEntities(bestandsView);
    } else if (activeTab.getClientId().contains(CLIENTID_BESTAENDE_TAB_KOD_INSTITUTIONS)
        && activeTab.isRendered()) {
      log.info("Open Bestaende KOD Institution Tab");
      bestandsView.getBestandInstitutionElementeForKODs().clear();
      bestandsView.getBestandInstitutionElementeForKODs()
          .addAll(bestandsBoundary.loadBestandsInstitutionDataForKulturobjektdokument());
    } else if (activeTab.getClientId().contains(
        CLIENT_ID_BESTAENDE_TAB_BESCHREIBUNGEN_INSTITUTIONS)
        && activeTab.isRendered()) {
      log.info("Open Bestaende Beschreibung Institution Tab");
      bestandsView.getBestandInstitutionElementeForBeschreibungen().clear();
      bestandsView.getBestandInstitutionElementeForBeschreibungen()
          .addAll(bestandsBoundary.loadBestandsInstitutionDataForBeschreibungen());
    } else if (activeTab.getClientId().contains(
        CLIENT_ID_BESTAENDE_TAB_DIGITALISATE_INSTITUTIONS)
        && activeTab.isRendered()) {
      log.info("Open Bestaende Digitalisate Institution Tab");
      bestandsView.getBestandInstitutionElementeForDigitalisate().clear();
      bestandsView.getBestandInstitutionElementeForDigitalisate()
          .addAll(bestandsBoundary.loadBestandsInstitutionDataForDigitalisate());
    }
  }
}
