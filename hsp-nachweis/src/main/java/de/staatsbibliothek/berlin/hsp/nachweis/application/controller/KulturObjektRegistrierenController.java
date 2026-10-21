package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjektRegistrierenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.08.2020.
 * @version 1.0
 */

@Named
@ViewScoped
public class KulturObjektRegistrierenController implements Serializable {

  @Serial
  private static final long serialVersionUID = 8965795879732550037L;

  private static final Logger logger = LoggerFactory.getLogger(
      KulturObjektRegistrierenController.class);

  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Delegate
  private final KulturObjektRegistrierenModel dataModel;

  @Inject
  public KulturObjektRegistrierenController(
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      KulturObjektRegistrierenModel dataModel) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.dataModel = dataModel;
  }

  public void placeChangedListener() {
    logger.info("Change Ort and active Koerperschaft");

    dataModel.getKoerperschaftViewModelMap().clear();

    if (Objects.isNull(dataModel.getOrteViewModel())) {
      return;
    }

    normdatenReferenzBoundary.findKoerperschaftenByOrtId(dataModel.getOrteViewModel().getId(),
            false)
        .forEach(normdatenKoerperschaftViewModel -> dataModel.getKoerperschaftViewModelMap()
            .put(normdatenKoerperschaftViewModel.getName(), normdatenKoerperschaftViewModel));
  }
}
