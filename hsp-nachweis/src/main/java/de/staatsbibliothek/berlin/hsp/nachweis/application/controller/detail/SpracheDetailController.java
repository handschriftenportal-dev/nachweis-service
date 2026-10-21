package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISO_639_1_TYPE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator.ISO_639_2_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konstantin.goerlitz@sbb.spk-berlin.de on 16.07.2021.
 */

@ViewScoped
@Slf4j
@Named
public class SpracheDetailController implements Serializable {

  @Serial
  private static final long serialVersionUID = -3160714450958901126L;
  private NormdatenReferenz model;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Inject
  public SpracheDetailController(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      ExternalContext externalContext = facesContext.getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String id = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(id) && !id.isEmpty()) {
          normdatenReferenzBoundary.findSpracheById(id)
              .ifPresent(sprache -> model = sprache);
        }
      }
    }
  }

  public NormdatenReferenz getModel() {
    return model;
  }

  void setModel(NormdatenReferenz model) {
    this.model = model;
  }

  public Set<Identifikator> getIso6391Identifikatoren() {
    return Optional.ofNullable(model)
        .map(sprache -> sprache.getIdentifikatorByTypeName(ISO_639_1_TYPE))
        .orElse(Collections.emptySet());
  }

  public Set<Identifikator> getIso6392Identifikatoren() {
    return Optional.ofNullable(model)
        .map(sprache -> sprache.getIdentifikatorByTypeName(ISO_639_2_TYPE))
        .orElse(Collections.emptySet());
  }

}
