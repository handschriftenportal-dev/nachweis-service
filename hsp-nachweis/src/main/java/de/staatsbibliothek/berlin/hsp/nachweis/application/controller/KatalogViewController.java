/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogBeschreibungViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

    if (Objects.nonNull(katalogViewModel) && Objects.nonNull(katalogViewModel.getBeschreibungen())) {
      katalogViewModel.getBeschreibungen().sort(Comparator.comparing(KatalogBeschreibungViewModel::getSignatur));
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
