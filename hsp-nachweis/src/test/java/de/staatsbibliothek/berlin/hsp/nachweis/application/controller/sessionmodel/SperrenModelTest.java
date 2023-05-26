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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperreEintragViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperrenViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 */
class SperrenModelTest {

  public static final String BEARBEITER_ID = "b-jd123";
  DokumentSperreBoundary dokumentSperreBoundary;
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  BeschreibungsBoundary beschreibungsBoundary;

  List<Sperre> sperren;

  @BeforeEach
  void init() throws Exception {
    dokumentSperreBoundary = mock(DokumentSperreBoundary.class);

    sperren = new ArrayList<>();
    sperren.add(Sperre.newBuilder()
        .withId("1")
        .withStartDatum(LocalDateTime.now())
        .withBearbeiter(new Bearbeiter(BEARBEITER_ID, BEARBEITER_ID))
        .withSperreGrund("Test")
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("HSP-aca04d9c-b8da-3906-9346-cf5f15f19357", SperreDokumentTyp.KOD))
        .build());
    sperren.add(Sperre.newBuilder()
        .withId("2")
        .withStartDatum(LocalDateTime.now())
        .withBearbeiter(new Bearbeiter(BEARBEITER_ID, BEARBEITER_ID))
        .withSperreGrund("Test")
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("HSP-5f355105-ab83-3c3d-a03e-4cb0ae49f244", SperreDokumentTyp.KOD))
        .addSperreEintrag(
            new SperreEintrag("HSP-c90eeff6-b8ca-4766-9362-f3006e7817e5", SperreDokumentTyp.BESCHREIBUNG))
        .build()
    );

    when(dokumentSperreBoundary.findAll()).thenReturn(sperren);

    when(dokumentSperreBoundary.releaseSperre(any(Sperre.class)))
        .thenAnswer(invocation -> {
          Sperre sperre = invocation.getArgument(0, Sperre.class);
          return sperren.removeIf(s -> s.getId().equals(sperre.getId()));
        });

    kulturObjektDokumentBoundary = mock(KulturObjektDokumentBoundary.class);
    Map<String, SignatureValue> kodSignaturenMap = new HashMap<>();
    kodSignaturenMap.put("HSP-aca04d9c-b8da-3906-9346-cf5f15f19357",
        new SignatureValue("HSP-aca04d9c-b8da-3906-9346-cf5f15f19357",
            "Kl. L. 121 X", "Berlin", "Staatsbibliothek zu Berlin"));
    kodSignaturenMap.put("HSP-5f355105-ab83-3c3d-a03e-4cb0ae49f244",
        new SignatureValue("HSP-5f355105-ab83-3c3d-a03e-4cb0ae49f244",
            "Kl. L. 122 Y", "Berlin", "Staatsbibliothek zu Berlin"));

    when(kulturObjektDokumentBoundary.getAllSignaturesWithKodIDs()).thenReturn(kodSignaturenMap);

    beschreibungsBoundary = mock(BeschreibungsBoundary.class);
    Map<String, SignatureValue> beschreibungSignaturenMap = new HashMap<>();
    beschreibungSignaturenMap.put("HSP-c90eeff6-b8ca-4766-9362-f3006e7817e5",
        new SignatureValue("HSP-c90eeff6-b8ca-4766-9362-f3006e7817e5",
            "Kl. L. 123 Z", "Berlin", "Staatsbibliothek zu Berlin"));
    when(beschreibungsBoundary.getAllSignaturesWithBeschreibungIDs()).thenReturn(beschreibungSignaturenMap);
  }

  @Test
  void testGetAllSperren() {
    SperrenModel sperrenModel = new SperrenModel(dokumentSperreBoundary, kulturObjektDokumentBoundary,
        beschreibungsBoundary);
    assertNotNull(sperrenModel.getAllSperren());
    assertEquals(2, sperrenModel.getAllSperren().size());
  }

  @Test
  void testSperreLoeschen() {
    SperrenModel sperrenModel = new SperrenModel(dokumentSperreBoundary, kulturObjektDokumentBoundary,
        beschreibungsBoundary);
    int initialSize = sperrenModel.getAllSperren().size();
    Sperre sperre = sperren.get(1);
    sperrenModel.sperreLoeschen(sperre.getId());
    sperrenModel.alleSperrenAnzeigen();

    assertEquals(--initialSize, sperrenModel.getAllSperren().size());
  }

  @Test
  void testMapSperrenViewModel() {
    SperrenModel sperrenModel = new SperrenModel(dokumentSperreBoundary, kulturObjektDokumentBoundary,
        beschreibungsBoundary);
    Map<String, SignatureValue> kodSignaturen = kulturObjektDokumentBoundary.getAllSignaturesWithKodIDs();
    Map<String, SignatureValue> beschreibungSignaturen = beschreibungsBoundary.getAllSignaturesWithBeschreibungIDs();

    sperren.forEach(sperre -> {
      SperrenViewModel sperrenViewModel = sperrenModel.mapSperre(sperre, kodSignaturen, beschreibungSignaturen);

      assertNotNull(sperrenViewModel);
      assertEquals(sperre.getId(), sperrenViewModel.getId());
      assertEquals(sperre.getBearbeiter().getBearbeitername(), sperrenViewModel.getBearbeitername());
      assertEquals(sperre.getStartDatum(), sperrenViewModel.getStartDatum());
      assertEquals(sperre.getSperreTyp(), sperrenViewModel.getSperreTyp());
      assertEquals(sperre.getSperreGrund(), sperrenViewModel.getSperreGrund());
      assertEquals(sperre.getTxId(), sperrenViewModel.getTxId());
      assertNotNull(sperrenViewModel.getSperreEintraege());
      assertEquals(sperre.getSperreEintraege().size(), sperrenViewModel.getSperreEintraege().size());
    });
  }

  @Test
  void testMapSignatures() {
    SperrenModel sperrenModel = new SperrenModel(dokumentSperreBoundary, kulturObjektDokumentBoundary,
        beschreibungsBoundary);

    Sperre sperre = sperren.get(1);
    Map<String, SignatureValue> kodSignaturenMap = kulturObjektDokumentBoundary.getAllSignaturesWithKodIDs();
    Map<String, SignatureValue> beschreibungSignaturenMap = beschreibungsBoundary.getAllSignaturesWithBeschreibungIDs();

    List<SperreEintragViewModel> sperreEintragViewModels =
        sperrenModel.mapSperreEintraege(sperre.getSperreEintraege(), kodSignaturenMap, beschreibungSignaturenMap);

    assertNotNull(sperreEintragViewModels);
    assertEquals(2, sperreEintragViewModels.size());

    assertTrue(sperreEintragViewModels.stream()
        .anyMatch(se -> "Kl. L. 122 Y".equals(se.getSignature())
            && SperreDokumentTyp.KOD == se.getTargetType()
            && "HSP-5f355105-ab83-3c3d-a03e-4cb0ae49f244".equals(se.getTargetId())));

    assertTrue(sperreEintragViewModels.stream()
        .anyMatch(se -> "Kl. L. 123 Z".equals(se.getSignature())
            && SperreDokumentTyp.BESCHREIBUNG == se.getTargetType()
            && "HSP-c90eeff6-b8ca-4766-9362-f3006e7817e5".equals(se.getTargetId())));
  }


}
