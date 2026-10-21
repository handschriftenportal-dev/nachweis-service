package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: Christoph Marten on 10.03.2021 at 14:50
 */
@ViewScoped
@Named
@Slf4j
public class PersonDetailController implements Serializable {

  @Serial
  private static final long serialVersionUID = 5348351518003799626L;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient NormdatenReferenz viewModel;

  @Inject
  public PersonDetailController(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance()) && Objects.nonNull(
        FacesContext.getCurrentInstance().getExternalContext())) {

      FacesContext facesContext = FacesContext.getCurrentInstance();
      ExternalContext externalContext = facesContext.getExternalContext();

      if (Objects.nonNull(externalContext)) {
        final String personID = externalContext.getRequestParameterMap().get("id");
        if (Objects.nonNull(personID) && !personID.isEmpty()) {
          normdatenReferenzBoundary.findOneByIdOrNameAndType(personID, NormdatenReferenz.PERSON_TYPE_NAME)
              .ifPresent(normDatenPersonView -> viewModel = normDatenPersonView);
        }
      }
    }
  }

  public NormdatenReferenz getViewModel() {
    return viewModel;
  }

}
