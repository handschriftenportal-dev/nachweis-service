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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 09.03.2023
 */
class WerkzeugeControllerTest {

  @Test
  void testSetup() throws DokumentSperreException {
    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(KulturObjektDokumentBoundary.class);
    DokumentSperreBoundary dokumentSperreBoundaryMock = mock(DokumentSperreBoundary.class);

    WerkzeugeController werkzeugeController = new WerkzeugeController(kulturObjektDokumentBoundaryMock,
        dokumentSperreBoundaryMock);

    werkzeugeController.setup();

    assertFalse(werkzeugeController.isSpeereExists());

    verify(dokumentSperreBoundaryMock, times(1)).findAll();
  }

  @Test
  void testSetup_withSperre() throws DokumentSperreException {
    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(KulturObjektDokumentBoundary.class);
    DokumentSperreBoundary dokumentSperreBoundaryMock = mock(DokumentSperreBoundary.class);

    when(dokumentSperreBoundaryMock.findAll()).thenReturn(List.of(createSperre()));

    WerkzeugeController werkzeugeController = new WerkzeugeController(kulturObjektDokumentBoundaryMock,
        dokumentSperreBoundaryMock);

    werkzeugeController.setup();

    assertTrue(werkzeugeController.isSpeereExists());

    verify(dokumentSperreBoundaryMock, times(1)).findAll();
  }

  @Test
  void testMigrateAttributsReferenzen() throws DokumentSperreException {

    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(KulturObjektDokumentBoundary.class);
    DokumentSperreBoundary dokumentSperreBoundaryMock = mock(DokumentSperreBoundary.class);

    OAUTH2Config oauth2ConfigMock = mock(OAUTH2Config.class);
    when(oauth2ConfigMock.isLoginCheckEnabled()).thenReturn(false);

    WerkzeugeController werkzeugeController = new WerkzeugeController(kulturObjektDokumentBoundaryMock,
        dokumentSperreBoundaryMock);

    werkzeugeController.setup();

    werkzeugeController.migrateAttributsReferenzen();

    verify(kulturObjektDokumentBoundaryMock, times(1)).migrateAttributsReferenzen();
  }

  private Sperre createSperre() {
    Bearbeiter bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ml123")
        .withVorname("Martin")
        .withNachname("Luther")
        .withEmail("martin@luther.de")
        .withRolle("Redakteur")
        .build();

    return Sperre.newBuilder()
        .withSperreTyp(SperreTyp.IN_TRANSACTION)
        .withSperreGrund("KerndatenBearbeiten")
        .withBearbeiter(bearbeiter)
        .addSperreEintrag(new SperreEintrag("HSP-123", SperreDokumentTyp.KOD))
        .withTxId("T-123").build();
  }

}
