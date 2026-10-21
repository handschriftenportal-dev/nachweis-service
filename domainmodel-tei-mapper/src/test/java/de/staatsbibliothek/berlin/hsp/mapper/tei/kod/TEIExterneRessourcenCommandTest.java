package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TEIExterneRessourcenCommandTest {

  private static final Path TEI_FILE_PATH =
      Paths.get("src", "test", "resources", "tei", "tei-HSP-kodms1.xml");

  private static final ExterneRessource HSC =
      new ExterneRessource(ExterneRessourceTyp.HSC, "https://hsc.example.org/objekt/1");
  private static final ExterneRessource ECODICES =
      new ExterneRessource(ExterneRessourceTyp.ECODICES, "https://e-codices.example.org/objekt/1");

  private String teiXML;

  @BeforeEach
  void setUp() throws IOException {
    teiXML = Files.readString(TEI_FILE_PATH, StandardCharsets.UTF_8);
  }

  @Test
  void shouldAddRessourceAndReturnNewTeiXml() {
    Optional<String> updated = TEIExterneRessourcenCommand.addExterneRessource(teiXML, HSC);

    assertTrue(updated.isPresent());
    assertEquals(List.of(HSC),
        TEIExterneRessourcenCommand.readExterneRessourcen(updated.get()));
    assertTrue(updated.get().contains(
            "<ref target=\"" + HSC.url() + "\">" + ExterneRessourceTyp.HSC.getTeiCaption() + "</ref>"),
        updated.get());
  }

  @Test
  void shouldRejectAddingToAKodWithoutTeiXml() {
    TEICommandException nullXml = assertThrows(TEICommandException.class,
        () -> TEIExterneRessourcenCommand.addExterneRessource(null, HSC));
    TEICommandException blankXml = assertThrows(TEICommandException.class,
        () -> TEIExterneRessourcenCommand.addExterneRessource("  ", HSC));

    assertTrue(nullXml.getMessage().contains("TEI-XML"), nullXml.getMessage());
    assertTrue(blankXml.getMessage().contains("TEI-XML"), blankXml.getMessage());
  }

  @Test
  void shouldReportNothingToRemoveForAKodWithoutTeiXml() {
    assertTrue(TEIExterneRessourcenCommand.removeExterneRessource(null, HSC).isEmpty());
    assertTrue(TEIExterneRessourcenCommand.removeExterneRessource("  ", HSC).isEmpty());
  }

  @Test
  void shouldNotMutateTheInputTeiXml() {
    TEIExterneRessourcenCommand.addExterneRessource(teiXML, HSC);

    assertTrue(TEIExterneRessourcenCommand.readExterneRessourcen(teiXML).isEmpty(),
        "the input string must be untouched");
  }

  @Test
  void shouldReturnEmptyWhenAddingDuplicate() {
    String updated = TEIExterneRessourcenCommand.addExterneRessource(teiXML, HSC).orElseThrow();

    assertTrue(TEIExterneRessourcenCommand.addExterneRessource(updated, HSC).isEmpty());
  }

  @Test
  void shouldRemoveRessource() {
    String withRessourcen = TEIExterneRessourcenCommand
        .addExterneRessource(teiXML, HSC).orElseThrow();
    withRessourcen = TEIExterneRessourcenCommand
        .addExterneRessource(withRessourcen, ECODICES).orElseThrow();

    String updated = TEIExterneRessourcenCommand
        .removeExterneRessource(withRessourcen, HSC).orElseThrow();

    assertEquals(List.of(ECODICES), TEIExterneRessourcenCommand.readExterneRessourcen(updated));
  }

  @Test
  void shouldReturnEmptyWhenRemovingAbsentRessource() {
    assertTrue(TEIExterneRessourcenCommand.removeExterneRessource(teiXML, HSC).isEmpty());
  }

  @Test
  void shouldRoundTripAddAndRemoveBackToTheOriginalProjection() {
    String added = TEIExterneRessourcenCommand.addExterneRessource(teiXML, HSC).orElseThrow();
    String removed = TEIExterneRessourcenCommand.removeExterneRessource(added, HSC).orElseThrow();

    assertEquals(
        TEIExterneRessourcenCommand.readExterneRessourcen(teiXML),
        TEIExterneRessourcenCommand.readExterneRessourcen(removed));
  }

  @Test
  void shouldReadEmptyListForMissingTeiXml() {
    assertTrue(TEIExterneRessourcenCommand.readExterneRessourcen(null).isEmpty());
    assertTrue(TEIExterneRessourcenCommand.readExterneRessourcen("  ").isEmpty());
  }

  @Test
  void shouldRejectUnparsableTeiXml() {
    assertThrows(TEICommandException.class,
        () -> TEIExterneRessourcenCommand.addExterneRessource("Kein TEI XML", HSC));
  }
}
