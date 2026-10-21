package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_UMZIEHEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjekteModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturobjektDokumentUmziehenErgebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.02.24.
 * @version 1.0
 */

@ViewScoped
@Named
@Slf4j
@LoginCheck
public class KulturObjekteUmziehenController {

  @Delegate
  private final KulturObjekteModel kulturObjekteModel;
  @Getter
  private final Map<String, NormdatenReferenz> koerperschaftViewModelMap = new HashMap<>();
  @Getter(AccessLevel.PACKAGE)
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  @Getter(AccessLevel.PACKAGE)
  private final NormdatenReferenzBoundary normdatenReferenzBoundary;
  private final BearbeiterBoundary bearbeiterBoundary;
  @Getter
  @Setter
  private NormdatenReferenz neuerOrt;
  @Getter
  @Setter
  private NormdatenReferenz neuerBesitzer;
  @Getter
  @Setter(AccessLevel.PACKAGE)
  private List<KulturobjektDokumentUmziehenErgebnis> kodUmziehenErgebnis;

  @Inject
  public KulturObjekteUmziehenController(KulturObjekteModel kulturObjekteModel,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      NormdatenReferenzBoundary normdatenReferenzBoundary, BearbeiterBoundary bearbeiterBoundary) {
    this.kulturObjekteModel = kulturObjekteModel;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @CheckPermission(permission = PERMISSION_KOD_UMZIEHEN)
  public void executeKODUmziehenPruefung() {

    log.info("Execute KOD Umziehen Prüfung");

    kodUmziehenErgebnis = kulturObjektDokumentBoundary.pruefeKODUmziehen(
        kulturObjekteModel.getSelectedKulturObjektDokumente().stream()
            .collect(Collectors.toMap(KulturObjektDokumentListDTO::getHspId,
                KulturObjektDokumentListDTO::getGueltigeSignatur)),
        neuerBesitzer, neuerOrt);

    log.info("Umziehen Ergebnisse {} ", kodUmziehenErgebnis.size());
  }

  @CheckPermission(permission = PERMISSION_KOD_UMZIEHEN)
  public void kodUmziehen() {
    log.info("Execute KOD Umziehen!");

    try {
      kulturObjektDokumentBoundary.umziehen(kodUmziehenErgebnis,
          bearbeiterBoundary.getLoggedBearbeiter());

      kulturObjekteModel.reloadTableRecords();

      if (Objects.nonNull(FacesContext.getCurrentInstance())) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Kulturobjekte erfolgreich umgezogen",
            kodUmziehenErgebnis.stream().map(KulturobjektDokumentUmziehenErgebnis::signatur)
                .collect(Collectors.joining(";")));
        FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
      }
    } catch (Exception e) {
      log.error("Error during Kulturobjekte umziehen!", e);
      if (Objects.nonNull(FacesContext.getCurrentInstance())) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Kulturobjekte konnten nicht umgezogen werden!", e.getMessage());
        FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
      }
    }
  }

  public String umzuziehendeSignaturen() {

    if (kodUmziehenErgebnis == null) {
      return "";
    }

    return kodUmziehenErgebnis.stream().map(KulturobjektDokumentUmziehenErgebnis::signatur)
        .collect(Collectors.joining(","));
  }

  public boolean kodKoennenUmziehen() {
    if (kodUmziehenErgebnis == null) {
      return false;
    }
    return kodUmziehenErgebnis.stream()
        .allMatch(KulturobjektDokumentUmziehenErgebnis::isSuccessful);
  }

  public void placeChangedListener() {
    log.info("Change Ort and active Koerperschaft");

    this.getKoerperschaftViewModelMap().clear();

    if (Objects.isNull(neuerOrt)) {
      return;
    }

    normdatenReferenzBoundary.findKoerperschaftenByOrtId(neuerOrt.getId(),
            false)
        .forEach(normdatenKoerperschaftViewModel -> getKoerperschaftViewModelMap()
            .put(normdatenKoerperschaftViewModel.getName(), normdatenKoerperschaftViewModel));
  }
}
