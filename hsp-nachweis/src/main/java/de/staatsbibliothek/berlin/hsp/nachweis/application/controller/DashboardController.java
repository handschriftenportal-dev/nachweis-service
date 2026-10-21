package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;


import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 11.04.2022
 */
@ViewScoped
@Named
@Slf4j
public class DashboardController implements Serializable {

  @Serial
  private static final long serialVersionUID = 3856646111600383437L;
  private final BeschreibungsBoundary beschreibungsBoundary;
  private final BearbeiterBoundary bearbeiterService;

  @Getter
  private String bearbeiterName;

  private List<BeschreibungListDbView> beschreibungen;
  private List<BeschreibungListDbView> meineBeschreibungen;
  private List<BeschreibungListDbView> meinePublizierten;
  private List<BeschreibungInReviewDbView> meineImReview;
  private List<BeschreibungRejectedDbView> meineZurUeberarbeitung;
  private List<BeschreibungListDbView> latestPublikationen;

  @Inject
  public DashboardController(BeschreibungsBoundary beschreibungsBoundary,
      BearbeiterBoundary bearbeiterService) {
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.bearbeiterService = bearbeiterService;
  }

  @PostConstruct
  public void setup() {
    bearbeiterName = bearbeiterService.getLoggedBearbeiter()
        .getFullName();
  }

  public void anmelden() {
    bearbeiterService.startLogin();
  }

  public List<BeschreibungListDbView> getBeschreibungen() {
    if (beschreibungen == null) {
      beschreibungen = beschreibungsBoundary.findLatestModifiedTopTen();
    }
    return beschreibungen;
  }

  public List<BeschreibungListDbView> getMeineBeschreibungen() {
    if (meineBeschreibungen == null) {
      meineBeschreibungen = beschreibungsBoundary.findLatestModifiedTopTenByBearbeiter(
          bearbeiterName);
    }
    return meineBeschreibungen;
  }

  public List<BeschreibungListDbView> getMeinePublizierten() {
    if (meinePublizierten == null) {
      meinePublizierten = beschreibungsBoundary.findMeinePublizierten(bearbeiterName);
    }
    return meinePublizierten;
  }

  public List<BeschreibungInReviewDbView> getMeineImReview() {
    if (meineImReview == null) {
      meineImReview = beschreibungsBoundary.findMeineImReview(bearbeiterName);
    }
    return meineImReview;
  }

  public List<BeschreibungRejectedDbView> getMeineZurUeberarbeitung() {
    if (meineZurUeberarbeitung == null) {
      meineZurUeberarbeitung = beschreibungsBoundary.findMeineZurUeberarbeitung(bearbeiterName);
    }
    return meineZurUeberarbeitung;
  }

  public List<BeschreibungListDbView> getLatestPublikationen() {
    if (latestPublikationen == null) {
      latestPublikationen = beschreibungsBoundary.findLatestPublikationen();
    }
    return latestPublikationen;
  }
}
