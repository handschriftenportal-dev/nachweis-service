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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ABMESSUNG;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.BESCHREIBSTOFF;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.BUCHSCHMUCK;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ENTSTEHUNGSORT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.ENTSTEHUNGSZEIT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.FORMAT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.GRUNDSPRACHE;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.HANDSCHRIFTENTYP;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.MUSIKNOTATION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.STATUS;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.TITEL;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp.UMFANG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.wildfly.common.Assert.assertFalse;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsAutorView;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.faces.event.AjaxBehaviorEvent;
import org.junit.jupiter.api.Test;

class KODKerndatenModelTest {

  private static final Path BESCHREIBUNG_ATTRIBUTSREFERENZEN = Paths.get("src", "test", "resources",
      "tei-beschreibung-attributsreferenzen.xml");

  @Test
  void testInit() throws IOException {
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        createKulturObjektDokumentViewModel();

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();
    kodKerndatenModel.init(kulturObjektDokumentViewModel);
    assertFalse(kodKerndatenModel.isEditable());

    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    kulturObjektDokumentViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    kodKerndatenModel = new KODKerndatenModel();
    kodKerndatenModel.init(kulturObjektDokumentViewModel);

    assertTrue(kodKerndatenModel.isEditable());

    assertNull(kodKerndatenModel.getSelectedBeschreibungId());

    checkMapsInitialized(kodKerndatenModel);

    assertNotNull(kodKerndatenModel.getBeschreibungen());
    assertEquals(3, kodKerndatenModel.getBeschreibungen().size());

    assertTrue(kodKerndatenModel.getBeschreibungen().containsKey(beschreibung_1.getId()));
    assertEquals(beschreibung_1, kodKerndatenModel.getBeschreibungen().get(beschreibung_1.getId()));
    assertTrue(kodKerndatenModel.getBeschreibungen().containsKey(beschreibung_2.getId()));
    assertEquals(beschreibung_2, kodKerndatenModel.getBeschreibungen().get(beschreibung_2.getId()));

    checkSelectedValuesMatch(kodKerndatenModel, kulturObjektDokumentViewModel);
  }

  private void checkSelectedValuesMatch(KODKerndatenModel kodKerndatenModel,
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel) {

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(TITEL),
        kulturObjektDokumentViewModel.getTitel());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(BESCHREIBSTOFF),
        kulturObjektDokumentViewModel.getBeschreibstoff());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(UMFANG),
        kulturObjektDokumentViewModel.getUmfang());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(ABMESSUNG),
        kulturObjektDokumentViewModel.getAbmessung());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(FORMAT),
        kulturObjektDokumentViewModel.getFormat());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(ENTSTEHUNGSORT),
        kulturObjektDokumentViewModel.getEntstehungsort());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(ENTSTEHUNGSZEIT),
        kulturObjektDokumentViewModel.getEntstehungszeit());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(GRUNDSPRACHE),
        kulturObjektDokumentViewModel.getGrundsprache());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(BUCHSCHMUCK),
        kulturObjektDokumentViewModel.getBuchschmuck());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(HANDSCHRIFTENTYP),
        kulturObjektDokumentViewModel.getHandschriftentyp());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(MUSIKNOTATION),
        kulturObjektDokumentViewModel.getMusiknotation());

    assertSelectedIdMatchesValue(kodKerndatenModel.getSelectedIds().get(STATUS),
        kulturObjektDokumentViewModel.getStatus());
  }

  private <T extends AttributsReferenz> void assertSelectedIdMatchesValue(String selectedId, T attributsReferenz) {
    assertNotNull(selectedId);
    assertNotNull(attributsReferenz);
    assertEquals(attributsReferenz.getId(), selectedId);
  }

  @Test
  void testBearbeiten() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    kodKerndatenModel.init(kulturObjektDokumentViewModel);

    checkMapsInitialized(kodKerndatenModel);

    kodKerndatenModel.bearbeiten();

    checkMapsFilled(kodKerndatenModel);
  }

  private void checkMapsInitialized(KODKerndatenModel kodKerndatenModel) {
    assertNotNullAndEmpty(kodKerndatenModel.getTitel());
    assertNotNullAndEmpty(kodKerndatenModel.getBeschreibstoffe());
    assertNotNullAndEmpty(kodKerndatenModel.getUmfange());
    assertNotNullAndEmpty(kodKerndatenModel.getAbmessungen());
    assertNotNullAndEmpty(kodKerndatenModel.getFormate());
    assertNotNullAndEmpty(kodKerndatenModel.getEntstehungsorte());
    assertNotNullAndEmpty(kodKerndatenModel.getEntstehungszeiten());
    assertNotNullAndEmpty(kodKerndatenModel.getGrundsprachen());
    assertNotNullAndEmpty(kodKerndatenModel.getBuchschmuck());
    assertNotNullAndEmpty(kodKerndatenModel.getHandschriftentypen());
    assertNotNullAndEmpty(kodKerndatenModel.getMusiknotationen());
    assertNotNullAndEmpty(kodKerndatenModel.getStatus());
  }

  private void checkMapsFilled(KODKerndatenModel kodKerndatenModel) {
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getTitel());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getBeschreibstoffe());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getUmfange());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getAbmessungen());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getFormate());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getEntstehungsorte());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getEntstehungszeiten());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getGrundsprachen());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getBuchschmuck());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getHandschriftentypen());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getMusiknotationen());
    assertNotNullAndSizeOfTwo(kodKerndatenModel.getStatus());
  }

  @Test
  void testBeschreibungswerteUebernehmen() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    kodKerndatenModel.init(kulturObjektDokumentViewModel);
    kodKerndatenModel.bearbeiten();

    assertEquals("HSP-51c69ef8-b5a2-3cb1-80e9-2ea2fc26e3f2", kodKerndatenModel.getSelectedIds().get(TITEL));
    assertEquals("HSP-7a9ad7b9-a6b0-3479-a6ae-b5e67f482eb8", kodKerndatenModel.getSelectedIds().get(BESCHREIBSTOFF));
    assertEquals("HSP-3214d290-638c-3519-ba97-d99fbe8787e3", kodKerndatenModel.getSelectedIds().get(UMFANG));
    assertEquals("HSP-a492b67e-78f7-3121-803a-ebadc1357ecf", kodKerndatenModel.getSelectedIds().get(ABMESSUNG));
    assertEquals("HSP-a44d2fd1-3ce6-34cf-8501-2e8833ba964f", kodKerndatenModel.getSelectedIds().get(FORMAT));
    assertEquals("HSP-f12c2125-44b7-3050-b37e-4e71da8ab10f", kodKerndatenModel.getSelectedIds().get(ENTSTEHUNGSORT));
    assertEquals("HSP-15ade495-213e-393c-a3c1-f82bd70c2de2", kodKerndatenModel.getSelectedIds().get(ENTSTEHUNGSZEIT));
    assertEquals("HSP-c78baa8d-9f4a-3aa7-a8f9-b429eea63244", kodKerndatenModel.getSelectedIds().get(GRUNDSPRACHE));
    assertEquals("HSP-5d51f2f0-7762-3166-aeaf-f3d752cf1220", kodKerndatenModel.getSelectedIds().get(BUCHSCHMUCK));
    assertEquals("HSP-2fc83d8b-bebb-3852-a293-70520150fd7e", kodKerndatenModel.getSelectedIds().get(HANDSCHRIFTENTYP));
    assertEquals("HSP-b3dcaa16-e06f-3f4b-8353-3f6e3b836b54", kodKerndatenModel.getSelectedIds().get(MUSIKNOTATION));
    assertEquals("HSP-2596d5ec-0b2b-3302-b85f-cb7e2568c359", kodKerndatenModel.getSelectedIds().get(STATUS));

    kodKerndatenModel.setSelectedBeschreibungId(beschreibung_2.getId());
    kodKerndatenModel.beschreibungswerteUebernehmen(mock(AjaxBehaviorEvent.class));

    assertEquals("HSP-55ee9198-0c95-310c-8495-b9d4417cfd28", kodKerndatenModel.getSelectedIds().get(TITEL));
    assertEquals("HSP-bdc27dc6-cb54-338d-8541-10db7db85399", kodKerndatenModel.getSelectedIds().get(BESCHREIBSTOFF));
    assertEquals("HSP-64c24e78-7790-30ac-b094-9d4defdc3966", kodKerndatenModel.getSelectedIds().get(UMFANG));
    assertEquals("HSP-f23f5c7c-8a82-39aa-9d68-18e3ef71f6a9", kodKerndatenModel.getSelectedIds().get(ABMESSUNG));
    assertEquals("HSP-b7ce1f15-54e1-3bc6-b8ca-aab1c234f427", kodKerndatenModel.getSelectedIds().get(FORMAT));
    assertEquals("HSP-05f1d4be-a359-39e0-a4c8-215e044a075f", kodKerndatenModel.getSelectedIds().get(ENTSTEHUNGSORT));
    assertEquals("HSP-c75eaa95-865f-386e-a2df-71e54bf50bca", kodKerndatenModel.getSelectedIds().get(ENTSTEHUNGSZEIT));
    assertEquals("HSP-e49b07b8-db17-31f1-88c6-82454b1a0985", kodKerndatenModel.getSelectedIds().get(GRUNDSPRACHE));
    assertEquals("HSP-1a1d38bc-7e2c-3d03-9477-d1c04db50c24", kodKerndatenModel.getSelectedIds().get(BUCHSCHMUCK));
    assertEquals("HSP-51f027a8-6099-3d88-81de-7e5e792478d6", kodKerndatenModel.getSelectedIds().get(HANDSCHRIFTENTYP));
    assertEquals("HSP-b1c89052-4d4b-3efe-a4e5-a1e0c5d6657d", kodKerndatenModel.getSelectedIds().get(MUSIKNOTATION));
    assertEquals("HSP-952b01fe-aad1-32b1-aeb0-6d318423d757", kodKerndatenModel.getSelectedIds().get(STATUS));
  }

  @Test
  void testKerndatenUebernehmen() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    KulturObjektDokumentViewModel kodViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    kodKerndatenModel.init(kodViewModel);
    kodKerndatenModel.bearbeiten();

    kodKerndatenModel.getSelectedIds().put(TITEL, "HSP-55ee9198-0c95-310c-8495-b9d4417cfd28");
    kodKerndatenModel.getSelectedIds().put(BESCHREIBSTOFF, "HSP-bdc27dc6-cb54-338d-8541-10db7db85399");
    kodKerndatenModel.getSelectedIds().put(UMFANG, "HSP-64c24e78-7790-30ac-b094-9d4defdc3966");
    kodKerndatenModel.getSelectedIds().put(ABMESSUNG, "HSP-f23f5c7c-8a82-39aa-9d68-18e3ef71f6a9");
    kodKerndatenModel.getSelectedIds().put(FORMAT, "HSP-b7ce1f15-54e1-3bc6-b8ca-aab1c234f427");
    kodKerndatenModel.getSelectedIds().put(ENTSTEHUNGSORT, "HSP-05f1d4be-a359-39e0-a4c8-215e044a075f");
    kodKerndatenModel.getSelectedIds().put(ENTSTEHUNGSZEIT, "HSP-c75eaa95-865f-386e-a2df-71e54bf50bca");
    kodKerndatenModel.getSelectedIds().put(GRUNDSPRACHE, "HSP-e49b07b8-db17-31f1-88c6-82454b1a0985");
    kodKerndatenModel.getSelectedIds().put(BUCHSCHMUCK, "HSP-1a1d38bc-7e2c-3d03-9477-d1c04db50c24");
    kodKerndatenModel.getSelectedIds().put(HANDSCHRIFTENTYP, "HSP-51f027a8-6099-3d88-81de-7e5e792478d6");
    kodKerndatenModel.getSelectedIds().put(MUSIKNOTATION, "HSP-b1c89052-4d4b-3efe-a4e5-a1e0c5d6657d");
    kodKerndatenModel.getSelectedIds().put(STATUS, "HSP-952b01fe-aad1-32b1-aeb0-6d318423d757");

    kodKerndatenModel.kerndatenUebernehmen(kodViewModel);

    assertAttributsReferenz(kodViewModel.getTitel(),
        "HSP-55ee9198-0c95-310c-8495-b9d4417cfd28", "Johannes Cassianus: Collationes, pars II, III");

    assertAttributsReferenz(kodViewModel.getBeschreibstoff(),
        "HSP-bdc27dc6-cb54-338d-8541-10db7db85399", "Pergament");

    assertAttributsReferenz(kodViewModel.getUmfang(),
        "HSP-64c24e78-7790-30ac-b094-9d4defdc3966", "I + 208 Bl.");

    assertAttributsReferenz(kodViewModel.getAbmessung(),
        "HSP-f23f5c7c-8a82-39aa-9d68-18e3ef71f6a9", "24 × 18,5");

    assertAttributsReferenz(kodViewModel.getFormat(),
        "HSP-b7ce1f15-54e1-3bc6-b8ca-aab1c234f427", "quarto");

    assertAttributsReferenz(kodViewModel.getEntstehungsort(),
        "HSP-05f1d4be-a359-39e0-a4c8-215e044a075f", "Regensburg");

    assertAttributsReferenz(kodViewModel.getEntstehungszeit(),
        "HSP-c75eaa95-865f-386e-a2df-71e54bf50bca",
        "11. Jh., Mitte; um 11. Jh., Mitte; bald nach 11. Jh., Mitte");

    assertAttributsReferenz(kodViewModel.getGrundsprache(),
        "HSP-e49b07b8-db17-31f1-88c6-82454b1a0985", "lateinisch");

    assertAttributsReferenz(kodViewModel.getBuchschmuck(),
        "HSP-1a1d38bc-7e2c-3d03-9477-d1c04db50c24", "yes");

    assertAttributsReferenz(kodViewModel.getHandschriftentyp(),
        "HSP-51f027a8-6099-3d88-81de-7e5e792478d6", "codex");

    assertAttributsReferenz(kodViewModel.getMusiknotation(),
        "HSP-b1c89052-4d4b-3efe-a4e5-a1e0c5d6657d", "no");

    assertAttributsReferenz(kodViewModel.getStatus(),
        "HSP-952b01fe-aad1-32b1-aeb0-6d318423d757", "displaced");
  }

  @Test
  void testIsSelectionChanged() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    KulturObjektDokumentViewModel kodViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    kodKerndatenModel.init(kodViewModel);

    assertNotNull(kodKerndatenModel.getBestaetigungsMeldungen());

    kodKerndatenModel.bearbeiten();

    assertFalse(kodKerndatenModel.isSelectionChanged());

    kodKerndatenModel.getSelectedIds().put(TITEL, "HSP-55ee9198-0c95-310c-8495-b9d4417cfd28");
    assertTrue(kodKerndatenModel.isSelectionChanged());

    kodKerndatenModel.getSelectedIds().put(TITEL, "");
    assertTrue(kodKerndatenModel.isSelectionChanged());

    kodKerndatenModel.getSelectedIds().put(TITEL, "HSP-51c69ef8-b5a2-3cb1-80e9-2ea2fc26e3f2");
    assertFalse(kodKerndatenModel.isSelectionChanged());
  }

  @Test
  void testKerndatenBestaetigen() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    KulturObjektDokumentViewModel kodViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    kodKerndatenModel.init(kodViewModel);

    assertNotNull(kodKerndatenModel.getBestaetigungsMeldungen());

    kodKerndatenModel.bearbeiten();

    kodKerndatenModel.kerndatenBestaetigen();

    assertEquals(0, kodKerndatenModel.getBestaetigungsMeldungen().size());

    kodKerndatenModel.getSelectedIds().put(TITEL, "HSP-55ee9198-0c95-310c-8495-b9d4417cfd28");
    kodKerndatenModel.getSelectedIds().put(BESCHREIBSTOFF, "HSP-bdc27dc6-cb54-338d-8541-10db7db85399");
    kodKerndatenModel.getSelectedIds().put(UMFANG, "HSP-64c24e78-7790-30ac-b094-9d4defdc3966");
    kodKerndatenModel.getSelectedIds().put(ABMESSUNG, "HSP-f23f5c7c-8a82-39aa-9d68-18e3ef71f6a9");
    kodKerndatenModel.getSelectedIds().put(FORMAT, "HSP-b7ce1f15-54e1-3bc6-b8ca-aab1c234f427");
    kodKerndatenModel.getSelectedIds().put(ENTSTEHUNGSORT, "HSP-05f1d4be-a359-39e0-a4c8-215e044a075f");
    kodKerndatenModel.getSelectedIds().put(ENTSTEHUNGSZEIT, "HSP-c75eaa95-865f-386e-a2df-71e54bf50bca");
    kodKerndatenModel.getSelectedIds().put(GRUNDSPRACHE, "HSP-e49b07b8-db17-31f1-88c6-82454b1a0985");
    kodKerndatenModel.getSelectedIds().put(BUCHSCHMUCK, "HSP-1a1d38bc-7e2c-3d03-9477-d1c04db50c24");
    kodKerndatenModel.getSelectedIds().put(HANDSCHRIFTENTYP, "HSP-51f027a8-6099-3d88-81de-7e5e792478d6");
    kodKerndatenModel.getSelectedIds().put(MUSIKNOTATION, "HSP-b1c89052-4d4b-3efe-a4e5-a1e0c5d6657d");
    kodKerndatenModel.getSelectedIds().put(STATUS, "HSP-952b01fe-aad1-32b1-aeb0-6d318423d757");

    kodKerndatenModel.kerndatenBestaetigen();

    assertEquals(12, kodKerndatenModel.getBestaetigungsMeldungen().size());

    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Status: \"disloziert\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Titel: \"Johannes Cassianus: Collationes, pars II, III\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Material, Beschreibstoff: \"Pergament\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Umfang: \"I + 208 Bl.\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Abmessungen: \"24 × 18,5\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Format: \"Quart\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Entstehungsort: \"Regensburg\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Entstehungszeit: \"11. Jh., Mitte; um 11. Jh., Mitte; bald nach 11. Jh., Mitte\" "
            + "aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Grundsprache: \"lateinisch\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Handschriftentyp: \"Codex\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Buchschmuck: \"Ja\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Musiknotation: \"Nein\" aus \"31.12.2004, Elisabeth Klemm\" übernehmen"));

    kodKerndatenModel.kerndatenUebernehmen(kodViewModel);
    kodKerndatenModel.init(kodViewModel);
    kodKerndatenModel.bearbeiten();

    kodKerndatenModel.getSelectedIds().put(TITEL, "");
    kodKerndatenModel.getSelectedIds().put(BESCHREIBSTOFF, "HSP-66fff085-f8c3-3c30-8c85-e3444767c676");

    kodKerndatenModel.kerndatenBestaetigen();

    assertEquals(2, kodKerndatenModel.getBestaetigungsMeldungen().size());

    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Titel: \"Johannes Cassianus: Collationes, pars II, III\" aus \"31.12.2004, Elisabeth Klemm\" entfernen"));
    assertTrue(kodKerndatenModel.getBestaetigungsMeldungen().contains(
        "Material, Beschreibstoff: \"Pergament\" aus \"31.12.2004, Elisabeth Klemm\" ersetzen durch "
            + "\"Pergament\" aus \"31.12.2004, Elisabeth Klemm\""));
  }

  @Test
  void testInitBeschreibungen() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    Map<String, BeschreibungsViewModel> result = kodKerndatenModel.initBeschreibungen(
        List.of(beschreibung_1, beschreibung_2));
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.containsKey(beschreibung_1.getId()));
    assertEquals(beschreibung_1, result.get(beschreibung_1.getId()));
    assertTrue(result.containsKey(beschreibung_2.getId()));
    assertEquals(beschreibung_2, result.get(beschreibung_2.getId()));
  }

  @Test
  void testMapAttributsReferenzen() throws IOException {
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();
    Map<AttributsTyp, AttributsReferenz> result = kodKerndatenModel.mapAttributsReferenzen(beschreibung_2);

    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());

    checkMappedAttributsReferenz(result, TITEL, Titel.class,
        "HSP-55ee9198-0c95-310c-8495-b9d4417cfd28", "Johannes Cassianus: Collationes, pars II, III");

    checkMappedAttributsReferenz(result, BESCHREIBSTOFF, Beschreibstoff.class,
        "HSP-bdc27dc6-cb54-338d-8541-10db7db85399", "Pergament");

    checkMappedAttributsReferenz(result, UMFANG, Umfang.class,
        "HSP-64c24e78-7790-30ac-b094-9d4defdc3966", "I + 208 Bl.");

    checkMappedAttributsReferenz(result, ABMESSUNG, Abmessung.class,
        "HSP-f23f5c7c-8a82-39aa-9d68-18e3ef71f6a9", "24 × 18,5");

    checkMappedAttributsReferenz(result, FORMAT, Format.class,
        "HSP-b7ce1f15-54e1-3bc6-b8ca-aab1c234f427", "quarto");

    checkMappedAttributsReferenz(result, ENTSTEHUNGSORT, Entstehungsort.class,
        "HSP-05f1d4be-a359-39e0-a4c8-215e044a075f", "Regensburg");

    checkMappedAttributsReferenz(result, ENTSTEHUNGSZEIT, Entstehungszeit.class,
        "HSP-c75eaa95-865f-386e-a2df-71e54bf50bca",
        "11. Jh., Mitte; um 11. Jh., Mitte; bald nach 11. Jh., Mitte");

    checkMappedAttributsReferenz(result, BUCHSCHMUCK, Buchschmuck.class,
        "HSP-1a1d38bc-7e2c-3d03-9477-d1c04db50c24", "yes");

    checkMappedAttributsReferenz(result, HANDSCHRIFTENTYP, Handschriftentyp.class,
        "HSP-51f027a8-6099-3d88-81de-7e5e792478d6", "codex");

    checkMappedAttributsReferenz(result, MUSIKNOTATION, Musiknotation.class,
        "HSP-b1c89052-4d4b-3efe-a4e5-a1e0c5d6657d", "no");

    checkMappedAttributsReferenz(result, STATUS, Status.class,
        "HSP-952b01fe-aad1-32b1-aeb0-6d318423d757", "displaced");
  }

  private void checkMappedAttributsReferenz(Map<AttributsTyp, AttributsReferenz> result, AttributsTyp attributsTyp,
      Class<? extends AttributsReferenz> clazz, String id, String text) {
    assertTrue(result.containsKey(attributsTyp));
    assertTrue(clazz.isInstance(result.get(attributsTyp)));
    assertEquals(id, result.get(attributsTyp).getId());
    assertEquals(text, result.get(attributsTyp).getText());
  }

  @Test
  void testFindReferenzIdForBeschreibungId() throws IOException {
    BeschreibungsViewModel beschreibung_1 = createBeschreibung_1();
    BeschreibungsViewModel beschreibung_2 = createBeschreibung_2();
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        createKulturObjektDokumentViewModel(beschreibung_1, beschreibung_2);

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();
    kodKerndatenModel.init(kulturObjektDokumentViewModel);
    kodKerndatenModel.bearbeiten();

    assertEquals("", kodKerndatenModel.findReferenzIdForBeschreibungId(
        "doesNotExists",
        kodKerndatenModel.getEntstehungsorte()));

    assertEquals("HSP-2072f2f2-5248-308e-a586-7437e0944ccf",
        kodKerndatenModel.findReferenzIdForBeschreibungId(
            beschreibung_1.getId(),
            kodKerndatenModel.getEntstehungsorte()));

    assertEquals("HSP-05f1d4be-a359-39e0-a4c8-215e044a075f",
        kodKerndatenModel.findReferenzIdForBeschreibungId(
            beschreibung_2.getId(),
            kodKerndatenModel.getEntstehungsorte()));
  }

  @Test
  void testGetAttributsReferenzId() {
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = createKulturObjektDokumentViewModel();

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    assertEquals("", kodKerndatenModel.getAttributsReferenzId(null));

    assertEquals("HSP-51c69ef8-b5a2-3cb1-80e9-2ea2fc26e3f2",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getTitel()));

    assertEquals("HSP-7a9ad7b9-a6b0-3479-a6ae-b5e67f482eb8",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getBeschreibstoff()));

    assertEquals("HSP-3214d290-638c-3519-ba97-d99fbe8787e3",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getUmfang()));

    assertEquals("HSP-a492b67e-78f7-3121-803a-ebadc1357ecf",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getAbmessung()));

    assertEquals("HSP-a44d2fd1-3ce6-34cf-8501-2e8833ba964f",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getFormat()));

    assertEquals("HSP-f12c2125-44b7-3050-b37e-4e71da8ab10f",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getEntstehungsort()));

    assertEquals("HSP-15ade495-213e-393c-a3c1-f82bd70c2de2",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getEntstehungszeit()));

    assertEquals("HSP-c78baa8d-9f4a-3aa7-a8f9-b429eea63244",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getGrundsprache()));

    assertEquals("HSP-5d51f2f0-7762-3166-aeaf-f3d752cf1220",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getBuchschmuck()));

    assertEquals("HSP-2fc83d8b-bebb-3852-a293-70520150fd7e",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getHandschriftentyp()));

    assertEquals("HSP-b3dcaa16-e06f-3f4b-8353-3f6e3b836b54",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getMusiknotation()));

    assertEquals("HSP-2596d5ec-0b2b-3302-b85f-cb7e2568c359",
        kodKerndatenModel.getAttributsReferenzId(kulturObjektDokumentViewModel.getStatus()));
  }

  @Test
  void testAddAttributsReferenzenToWerte() {
    KulturObjektDokumentViewModel kod = createKulturObjektDokumentViewModel();

    KODKerndatenModel kodKerndatenModel = new KODKerndatenModel();

    assertNotNull(kodKerndatenModel.getEntstehungsorte());
    assertTrue(kodKerndatenModel.getEntstehungsorte().isEmpty());
    assertNotNull(kodKerndatenModel.getEntstehungszeiten());
    assertTrue(kodKerndatenModel.getEntstehungszeiten().isEmpty());

    Map<AttributsTyp, AttributsReferenz> attributsReferenzen = new EnumMap<>(AttributsTyp.class);

    attributsReferenzen.put(kod.getEntstehungsort().getAttributsTyp(), kod.getEntstehungsort());
    attributsReferenzen.put(kod.getEntstehungszeit().getAttributsTyp(), kod.getEntstehungszeit());

    kodKerndatenModel.addAttributsReferenzenToWerte(attributsReferenzen);

    assertEquals(1, kodKerndatenModel.getEntstehungsorte().size());
    assertTrue(kodKerndatenModel.getEntstehungsorte().containsKey(kod.getEntstehungsort().getId()));
    assertEquals(kod.getEntstehungsort(),
        kodKerndatenModel.getEntstehungsorte().get(kod.getEntstehungsort().getId()));

    assertEquals(1, kodKerndatenModel.getEntstehungszeiten().size());
    assertTrue(kodKerndatenModel.getEntstehungszeiten().containsKey(kod.getEntstehungszeit().getId()));
    assertEquals(kod.getEntstehungszeit(),
        kodKerndatenModel.getEntstehungszeiten().get(kod.getEntstehungszeit().getId()));
  }

  private void assertNotNullAndEmpty(Map<String, ? extends AttributsReferenz> values) {
    assertNotNull(values);
    assertTrue(values.isEmpty());
  }

  private void assertNotNullAndSizeOfTwo(Map<String, ? extends AttributsReferenz> values) {
    assertNotNull(values);
    assertEquals(2, values.size());
  }

  private void assertAttributsReferenz(AttributsReferenz attributsReferenz, String id, String text) {
    assertNotNull(attributsReferenz);
    assertEquals(id, attributsReferenz.getId());
    assertEquals(text, attributsReferenz.getText());
  }

  private KulturObjektDokumentViewModel createKulturObjektDokumentViewModel(
      BeschreibungsViewModel... beschreibungsViewModels) {
    KulturObjektDokumentViewModelBuilder builder =
        KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
            .withTitel(createTitel())
            .withBeschreibstoff(createBeschreibstoff())
            .withUmfang(createUmfang())
            .withAbmessung(createAbmessung())
            .withFormat(createFormat())
            .withEntstehungsort(createEntstehungsort())
            .withEntstehungszeit(createEntstehungszeit())
            .withGrundSprache(createGrundsprache())
            .withBuchschmuck(createBuchschmuck())
            .withHandschriftentyp(createHandschriftentyp())
            .withMusiknotation(createMusiknotation())
            .withStatus(createStatus());
    Arrays.stream(beschreibungsViewModels).forEach(builder::addBeschreibung);
    return builder.build();
  }

  private BeschreibungsViewModel createBeschreibung_1() throws IOException {
    return createBeschreibungsViewModel("HSP-7a2b79aa-b80e-3b7c-9e7f-c7e76a7c1941",
        "Johannes Cassianus: Collationes, pars I");
  }

  private BeschreibungsViewModel createBeschreibung_2() throws IOException {
    return createBeschreibungsViewModel("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
        "Johannes Cassianus: Collationes, pars II, III");
  }

  private BeschreibungsViewModel createBeschreibungsViewModel(String id, String titel) throws IOException {
    return new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID(id)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDatumErstpublikation(LocalDateTime.of(LocalDate.of(2004, 12, 31), LocalTime.MAX))
        .withAutoren(List.of(new BeschreibungsAutorView("Elisabeth Klemm", null)))
        .withTitel(titel)
        .withTEIXML(Files.readString(BESCHREIBUNG_ATTRIBUTSREFERENZEN, StandardCharsets.UTF_8)
            .replace("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", id))
        .build();
  }

  private Titel createTitel() {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("Titel der Veschreibung")
        .build();
  }

  private Beschreibstoff createBeschreibstoff() {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("Pergament, Papyrus")
        .withTypen(List.of("parchment", "papyrus"))
        .build();
  }

  private Umfang createUmfang() {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .build();
  }

  private Abmessung createAbmessung() {
    return Abmessung.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addAbmessungWert(AbmessungWert.builder()
            .withText("23,5 × 16,5 x 2,5")
            .withHoehe("23,5")
            .withBreite("16,5")
            .withTiefe("2,5")
            .withTyp("factual")
            .build())
        .build();
  }

  private Format createFormat() {
    return Format.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addFormatWert(FormatWert.builder()
            .withText("long and narrow")
            .withTyp("factual")
            .build())
        .build();
  }

  private Entstehungsort createEntstehungsort() {
    return Entstehungsort.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("Weißenburg")
        .addNormdatenReferenz(
            new NormdatenReferenz("NORM-f5c6758c-3c33-3785-933a-999b0fa2a4a0",
                "Weißenburg (Elsass)",
                "https://d-nb.info/gnd/4079134-8",
                NormdatenReferenz.ORT_TYPE_NAME)
        ).build();
  }

  private Entstehungszeit createEntstehungszeit() {
    return Entstehungszeit.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .addEntstehungszeitWert(EntstehungszeitWert.builder()
            .withText("1446 oder vorher (Fasz. I)")
            .withNichtVor("1426")
            .withNichtNach("1446")
            .withTyp("datable")
            .build())
        .addEntstehungszeitWert(
            EntstehungszeitWert.builder()
                .withText("1446 (Fasz. II)")
                .withNichtVor("1446")
                .withNichtNach("1446")
                .withTyp("dated")
                .build())
        .build();
  }

  private Grundsprache createGrundsprache() {
    return Grundsprache.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Althochdeutsch")
        .addNormdatenId("4001523-3")
        .build();
  }

  private Buchschmuck createBuchschmuck() {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("yes")
        .build();
  }

  private Handschriftentyp createHandschriftentyp() {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("composite")
        .build();
  }

  private Musiknotation createMusiknotation() {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("yes")
        .build();
  }

  private Status createStatus() {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("displaced")
        .build();
  }
}
