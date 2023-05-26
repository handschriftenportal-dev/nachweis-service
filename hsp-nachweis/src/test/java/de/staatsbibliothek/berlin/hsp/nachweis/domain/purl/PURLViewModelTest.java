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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 12.08.22
 */

class PURLViewModelTest {

  static final String DOKUMENT_ID = "HSP-123";
  static final DokumentObjektTyp DOKUMENT_OBJEKT_TYP = DokumentObjektTyp.HSP_DESCRIPTION_RETRO;

  static final PURL PURL_INTERNAL = new PURL(URI.create("https://resolver.url/HSP-123"),
      URI.create("https://target.url/HSP-123"), PURLTyp.INTERNAL);

  static final PURL PURL_EXTERNAL = new PURL(URI.create("https://external.url/HSP-123"),
      URI.create("https://external.url/HSP-123"), PURLTyp.EXTERNAL);

  @Test
  void testConstruct() {
    PURLViewModel purlViewModel = new PURLViewModel(DOKUMENT_ID, DOKUMENT_OBJEKT_TYP,
        new HashSet<>(List.of(PURL_INTERNAL, PURL_EXTERNAL)));

    assertNotNull(purlViewModel);
    assertEquals(DOKUMENT_ID, purlViewModel.getDokumentId());
    assertEquals(DOKUMENT_OBJEKT_TYP, purlViewModel.getDokumentObjektTyp());
    assertNotNull(purlViewModel.getInternalPURLs());
    assertEquals(1, purlViewModel.getInternalPURLs().size());
    assertTrue(purlViewModel.getInternalPURLs().contains(PURL_INTERNAL));
    assertNotNull(purlViewModel.getExternalPURLs());
    assertEquals(1, purlViewModel.getExternalPURLs().size());
    assertTrue(purlViewModel.getExternalPURLs().contains(PURL_EXTERNAL));

    assertEquals("PURLViewModel(dokumentId=HSP-123, dokumentObjektTyp=hsp:description_retro, "
            + "internalPURLs=[PURL{id='HSP-f0ddaf97-7830-321d-8337-b1d44173ccfd', purl='https://resolver.url/HSP-123', target='https://target.url/HSP-123', typ=INTERNAL}], "
            + "externalPURLs=[PURL{id='HSP-d500450f-9013-3e04-a607-18383919102b', purl='https://external.url/HSP-123', target='https://external.url/HSP-123', typ=EXTERNAL}])",
        purlViewModel.toString());

    assertNotEquals(null, purlViewModel);
    assertEquals(purlViewModel, new PURLViewModel(DOKUMENT_ID, DOKUMENT_OBJEKT_TYP, Collections.emptySet()));
    assertNotEquals(purlViewModel, new PURLViewModel("HSP-456", DOKUMENT_OBJEKT_TYP, Collections.emptySet()));
  }

}
