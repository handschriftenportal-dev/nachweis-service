package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogBeschreibungViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 13.10.2022
 */

@ViewScoped
@Named
@Slf4j
@LoginCheck
public class KatalogViewController implements Serializable {

  public static final int BESCHREIBUNGEN_LIMIT = 6;
  private static final long serialVersionUID = 5624830444791778457L;
  private final KatalogBoundary katalogBoundary;
  @Getter
  private KatalogViewModel katalogViewModel;
  @Getter
  @Setter
  private String katalogId;

  @Getter
  private boolean showAllBeschreibungen;

  @Getter
  private List<KatalogBeschreibungViewModel> katalogBeschreibungen;

  @Inject
  public KatalogViewController(KatalogBoundary katalogBoundary) {
    this.katalogBoundary = katalogBoundary;
  }

  public void setup() {
    log.debug("Start setup");

    if (Objects.isNull(katalogId) || katalogId.isEmpty()) {
      log.warn("setup: katalogId is null or empty!");
      return;
    }

    katalogViewModel = katalogBoundary.buildKatalogViewModel(katalogId)
        .orElse(null);

    if (Objects.nonNull(katalogViewModel) && Objects.nonNull(
        katalogViewModel.getBeschreibungen())) {
      katalogViewModel.getBeschreibungen()
          .sort(Comparator.comparing(KatalogBeschreibungViewModel::getSignatur));
    }

    limitKatalogBeschreibungen();

    log.debug("Finished setup");
  }

  public void toggleShowAllBeschreibungen() {
    showAllBeschreibungen = !showAllBeschreibungen;
    limitKatalogBeschreibungen();
  }

  private void limitKatalogBeschreibungen() {
    this.katalogBeschreibungen = Optional.ofNullable(katalogViewModel).stream()
        .flatMap(kvm -> kvm.getBeschreibungen().stream())
        .limit(showAllBeschreibungen ? Integer.MAX_VALUE : BESCHREIBUNGEN_LIMIT)
        .collect(Collectors.toList());
  }
}
