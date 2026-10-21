package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISIL_IDENTIFIER_TYPE;

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

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.08.2020.
 * @version 1.0
 */

@ViewScoped
@Named
public class KoerperschaftsDetailController implements Serializable {

  private static final long serialVersionUID = -2472185105889279485L;

  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient NormdatenReferenz model;

  @Inject
  public KoerperschaftsDetailController(
      NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String id = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(id) && !id.isEmpty()) {

          final Optional<NormdatenReferenz> koerperschaftView = normdatenReferenzBoundary
              .findOneByIdOrNameAndType(id, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

          koerperschaftView.ifPresent(normdatenKoerperschaftViewModel -> this.model = normdatenKoerperschaftViewModel);
        }
      }
    }
  }

  public Identifikator findIsil() {
    return Optional.ofNullable(this.model)
        .stream()
        .map(koerperschaft -> koerperschaft.getIdentifikatorByTypeName(ISIL_IDENTIFIER_TYPE))
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }

  public NormdatenReferenz getModel() {
    return model;
  }

  void setModel(NormdatenReferenz model) {
    this.model = model;
  }
}
