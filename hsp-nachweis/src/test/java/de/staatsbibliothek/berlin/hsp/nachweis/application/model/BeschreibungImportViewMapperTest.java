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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp.BESCHREIBUNG;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.KulturObjektDokumentNotFoundException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 07.06.2019.
 */

public class BeschreibungImportViewMapperTest {

  static String TEI_FILENAME = "tei-msDesc_Westphal_head.xml";
  private static TEI tei = null;
  private BeschreibungImportService importBoundary = mock(BeschreibungImportService.class);

  @BeforeEach
  public void init() throws IOException, JAXBException {
    if (Objects.isNull(tei)) {
      Path teiFilePath = Paths
          .get("../domainmodel-tei-mapper/src", "test", "resources", "tei", TEI_FILENAME);

      try (InputStream is = newInputStream(teiFilePath)) {
        List<TEI> allTEI = TEIObjectFactory.unmarshal(is);

        tei = allTEI.get(0);
      }
    }
  }

  @Test
  public void test_GivenImport_MapToView() throws Exception {

    NormdatenReferenz besitzer = new NormdatenReferenz("","Staatsbibliothek zu Berlin","");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(Arrays.asList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("40170459")
        .withKodID(kod.getId())
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(BESCHREIBUNG)
        .withUrl("http://importservice:9296/import/job/123")
        .build();

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withActorName("Konrad Eichstädt")
        .withType(ActivityStreamAction.ADD)
        .withPublished(LocalDateTime.now())
        .withTargetName("Berlin_test_4_190405.fmt.xml")
        .addObject(activityStreamObject)
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);

    when(importBoundary.ermittleKOD(any())).thenReturn(Optional.of(kod));

    when(importBoundary.convertTEI2Beschreibung(new ActivityStreamObjectDTO(activityStreamObject))).thenCallRealMethod();

    ImportViewModel importViewModel = ImportViewMapper.map(beschreibungImportJob, importBoundary,true);

    assertNotNull(importViewModel);

    assertNotNull(importViewModel.getId());

    assertEquals("Berlin_test_4_190405.fmt.xml", importViewModel.getDateiName());

    assertEquals("Konrad Eichstädt", importViewModel.getBenutzer());

    assertEquals("http://importservice:9296/import/job/123", importViewModel.getImportUrl());

    assertNotNull(importViewModel.getBeschreibung());

    assertEquals("mss_36-23-aug-2f_tei-msDesc_Westphal",importViewModel.getBeschreibung().get(0).getId());

    assertEquals(beschreibungImportJob.getId(),importViewModel.getBeschreibung().get(0).getImportJobID());

    assertEquals("Index term title",
        importViewModel.getBeschreibung().get(0).getTitel());

    assertEquals("Cod. Guelf. 36.23 Aug. 2°", importViewModel.getBeschreibung().get(0).getSignatur());

    assertNotNull(importViewModel.getBeschreibung().get(0).getTeiXML());

    assertEquals(importViewModel.getBeschreibung().get(0).getPreconditionResult(),"Kulturobjektdokument 1234<br/>Mscr.Dresd.A.111<br/>Staatsbibliothek zu Berlin");
  }

  @Test
  void testcheckBeschreibungsImportPreCondition() throws KulturObjektDokumentNotFoundException {

    NormdatenReferenz besitzer = new NormdatenReferenz("","Staatsbibliothek zu Berlin","");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(Arrays.asList(identifikation)))
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("40170459")
        .withKodID("1")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withID((beschreibung).getId()).build();

    when(importBoundary.ermittleKOD(beschreibung)).thenThrow(new KulturObjektDokumentNotFoundException("KOD nicht gefunden"));

    ImportViewMapper.checkBeschreibungsImportPreCondition(importBoundary,beschreibung,view);

    assertFalse(view.isPreconditionState());

    assertEquals("KOD nicht gefunden",view.getPreconditionResult());

    when(importBoundary.checkIfBeschreibungAlreadyExists(beschreibung)).thenReturn(Optional.of(beschreibung));

    ImportViewMapper.checkBeschreibungsImportPreCondition(importBoundary,beschreibung,view);

    assertFalse(view.isPreconditionState());

    assertEquals("Beschreibung (intern) existiert bereits 40170459",view.getPreconditionResult());
  }
}
