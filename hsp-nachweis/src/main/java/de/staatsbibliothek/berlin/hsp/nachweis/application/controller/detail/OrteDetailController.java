package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.GEONAMES_TYPE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.GETTY_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ViewScoped
@Named
public class OrteDetailController implements Serializable {

  private static final long serialVersionUID = 875395979591212095L;
  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private NormdatenReferenz viewModel;

  @Inject
  public OrteDetailController(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance())
        && Objects.nonNull(FacesContext.getCurrentInstance().getExternalContext())) {

      FacesContext facesContext = FacesContext.getCurrentInstance();
      ExternalContext externalContext = facesContext.getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String ortID = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(ortID) && !ortID.isEmpty()) {
          normdatenReferenzBoundary.findOneByIdOrNameAndType(ortID, NormdatenReferenz.ORT_TYPE_NAME)
              .ifPresent(normDatenPersonView -> viewModel = normDatenPersonView);
        }
      }
    }
  }

  public NormdatenReferenz getViewModel() {
    return viewModel;
  }

  public Identifikator findGeonamesIdentifikator() {
    return Optional.ofNullable(this.viewModel)
        .stream()
        .map(ort -> ort.getIdentifikatorByTypeName(GEONAMES_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }

  public Identifikator findGettyIdentifikator() {
    return Optional.ofNullable(this.viewModel)
        .stream()
        .map(ort -> ort.getIdentifikatorByTypeName(GETTY_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }

}
