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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertNotNull;
import static org.wildfly.common.Assert.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogBeschreibungViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogBoundary;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 13.10.2022
 */
class KatalogViewControllerTest {

  KatalogBoundary katalogBoundary;

  @BeforeEach
  void beforeEach() {
    katalogBoundary = mock(KatalogBoundary.class);
  }

  @Test
  void testSetup() {
    String katalogId = "HSP-123";
    KatalogViewModel katalogViewModel = createKatalogViewModel(katalogId);

    when(katalogBoundary.buildKatalogViewModel(katalogId))
        .thenReturn(Optional.of(katalogViewModel));

    KatalogViewController katalogViewController = new KatalogViewController(katalogBoundary);
    katalogViewController.setKatalogId(katalogId);
    katalogViewController.setup();

    assertNotNull(katalogViewController.getKatalogViewModel());
    assertEquals(katalogId, katalogViewController.getKatalogViewModel().getId());
    assertNotNull(katalogViewController.getKatalogBeschreibungen());
    assertEquals(KatalogViewController.BESCHREIBUNGEN_LIMIT, katalogViewController.getKatalogBeschreibungen().size());

    katalogViewController.toggleShowAllBeschreibungen();

    assertNotNull(katalogViewController.getKatalogBeschreibungen());
    assertEquals(katalogViewController.getKatalogViewModel().getBeschreibungen().size(),
        katalogViewController.getKatalogBeschreibungen().size());
  }

  @Test
  void testToggleShowAllBeschreibungen() {
    String katalogId = "HSP-123";
    KatalogViewModel katalogViewModel = createKatalogViewModel(katalogId);

    when(katalogBoundary.buildKatalogViewModel(katalogId))
        .thenReturn(Optional.of(katalogViewModel));

    KatalogViewController katalogViewController = new KatalogViewController(katalogBoundary);
    katalogViewController.setKatalogId(katalogId);
    katalogViewController.setup();

    assertFalse(katalogViewController.isShowAllBeschreibungen());
    assertNotNull(katalogViewController.getKatalogBeschreibungen());
    assertEquals(KatalogViewController.BESCHREIBUNGEN_LIMIT, katalogViewController.getKatalogBeschreibungen().size());

    katalogViewController.toggleShowAllBeschreibungen();

    assertTrue(katalogViewController.isShowAllBeschreibungen());
    assertNotNull(katalogViewController.getKatalogBeschreibungen());
    assertEquals(katalogViewController.getKatalogViewModel().getBeschreibungen().size(),
        katalogViewController.getKatalogBeschreibungen().size());
  }

  KatalogViewModel createKatalogViewModel(String katalogId) {
    KatalogViewModel katalogViewModel = KatalogViewModel.builder()
        .withId(katalogId)
        .withBeschreibungen(new ArrayList<>())
        .build();
    for (int x = 0; x < 10; x++) {
      katalogViewModel.getBeschreibungen().add(
          KatalogBeschreibungViewModel.builder()
              .withId(katalogId + "_" + x)
              .withSignatur("Sig. " + x)
              .build());
    }
    return katalogViewModel;
  }
}
