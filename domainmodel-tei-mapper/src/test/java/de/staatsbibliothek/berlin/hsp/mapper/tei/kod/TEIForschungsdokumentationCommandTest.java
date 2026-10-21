package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TEIForschungsdokumentationCommandTest {

  private static final Path TEI_FILE_PATH =
      Paths.get("src", "test", "resources", "tei", "tei-HSP-kodms1.xml");
  private static final String RESSOURCE = "Handschriftenforschung und ihr Weg ins Digitale";
  private static final String LINK = "https://example.org/forschungsdokumentation/1";
  private static final Forschungsdokumentation FORSCHUNGSDOKUMENTATION =
      new Forschungsdokumentation(RESSOURCE, LINK);

  private String teiXML;

  @BeforeEach
  void setUp() throws IOException {
    teiXML = Files.readString(TEI_FILE_PATH, StandardCharsets.UTF_8);
  }

  @Test
  void shouldAddForschungsdokumentationAndReturnNewTeiXml() {
    String updated = TEIForschungsdokumentationCommand
        .addForschungsdokumentation(teiXML, FORSCHUNGSDOKUMENTATION)
        .orElseThrow();

    assertTrue(updated.contains("<listBibl type=\"bibliography\">"), updated);
    assertTrue(updated.contains("<bibl default=\"true\">"), updated);
    assertTrue(updated.contains("<ref target=\"" + LINK + "\">" + RESSOURCE + "</ref>"),
        updated);
  }

  @Test
  void shouldAddForschungsdokumentationWithoutLink() {
    String updated = TEIForschungsdokumentationCommand.addForschungsdokumentation(
        teiXML,
        new Forschungsdokumentation(RESSOURCE, null)).orElseThrow();

    assertTrue(updated.contains("<bibl default=\"true\">" + RESSOURCE + "</bibl>"), updated);
  }

  @Test
  void shouldEncodeRessourceAsPlainText() {
    String ressource = "Forschung <Digital> & Handschriften";

    String updated = TEIForschungsdokumentationCommand.addForschungsdokumentation(
        teiXML,
        new Forschungsdokumentation(ressource, null)).orElseThrow();

    assertTrue(
        updated.contains("<bibl default=\"true\">Forschung &lt;Digital&gt; &amp; Handschriften</bibl>"),
        updated);
  }

  @Test
  void shouldReturnEmptyWhenAddingDuplicateLink() {
    String updated = TEIForschungsdokumentationCommand
        .addForschungsdokumentation(teiXML, FORSCHUNGSDOKUMENTATION)
        .orElseThrow();

    Optional<String> duplicate = TEIForschungsdokumentationCommand.addForschungsdokumentation(
        updated,
        new Forschungsdokumentation("Anderer Text", LINK));

    assertTrue(duplicate.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenAddingDuplicateRessourceWithoutLink() {
    String updated = TEIForschungsdokumentationCommand.addForschungsdokumentation(
        teiXML,
        new Forschungsdokumentation(RESSOURCE, null)).orElseThrow();

    Optional<String> duplicate = TEIForschungsdokumentationCommand.addForschungsdokumentation(
        updated,
        new Forschungsdokumentation(
            "  Handschriftenforschung  und ihr Weg\nins Digitale  ",
            null));

    assertTrue(duplicate.isEmpty());
  }

  @Test
  void shouldRemoveForschungsdokumentationAndReturnUpdatedTeiXml() {
    String updated = TEIForschungsdokumentationCommand
        .addForschungsdokumentation(teiXML, FORSCHUNGSDOKUMENTATION)
        .orElseThrow();

    Optional<String> removed = TEIForschungsdokumentationCommand
        .removeForschungsdokumentation(updated, FORSCHUNGSDOKUMENTATION);

    assertTrue(removed.isPresent());
    assertEquals(List.of(),
        TEIForschungsdokumentationCommand.readForschungsdokumentationen(removed.orElseThrow()));
  }

  @Test
  void shouldRemoveForschungsdokumentationWithoutLink() {
    Forschungsdokumentation withoutLink = new Forschungsdokumentation(RESSOURCE, null);
    String updated = TEIForschungsdokumentationCommand
        .addForschungsdokumentation(teiXML, withoutLink)
        .orElseThrow();

    Optional<String> removed = TEIForschungsdokumentationCommand
        .removeForschungsdokumentation(updated, withoutLink);

    assertTrue(removed.isPresent());
    assertEquals(List.of(),
        TEIForschungsdokumentationCommand.readForschungsdokumentationen(removed.orElseThrow()));
  }

  @Test
  void shouldReturnEmptyWhenRemovingAbsentForschungsdokumentation() {
    assertFalse(TEIForschungsdokumentationCommand
        .removeForschungsdokumentation(teiXML, FORSCHUNGSDOKUMENTATION)
        .isPresent());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\n\t"})
  void shouldReturnEmptyWhenRemovingFromMissingTeiXml(String xml) {
    assertTrue(TEIForschungsdokumentationCommand
        .removeForschungsdokumentation(xml, FORSCHUNGSDOKUMENTATION)
        .isEmpty());
  }

  @Test
  void shouldRejectUnparsableTeiXmlWhenRemoving() {
    assertThrows(
        TEICommandException.class,
        () -> TEIForschungsdokumentationCommand.removeForschungsdokumentation(
            "Kein TEI XML",
            FORSCHUNGSDOKUMENTATION));
  }

  @Test
  void shouldRejectMissingTeiXml() {
    TEICommandException nullXml = assertThrows(
        TEICommandException.class,
        () -> TEIForschungsdokumentationCommand.addForschungsdokumentation(
            null,
            FORSCHUNGSDOKUMENTATION));
    TEICommandException blankXml = assertThrows(
        TEICommandException.class,
        () -> TEIForschungsdokumentationCommand.addForschungsdokumentation(
            "  ",
            FORSCHUNGSDOKUMENTATION));

    assertTrue(nullXml.getMessage().contains("TEI-XML"), nullXml.getMessage());
    assertTrue(blankXml.getMessage().contains("TEI-XML"), blankXml.getMessage());
  }

  @Test
  void shouldRejectUnparsableTeiXml() {
    assertThrows(
        TEICommandException.class,
        () -> TEIForschungsdokumentationCommand.addForschungsdokumentation(
            "Kein TEI XML",
            FORSCHUNGSDOKUMENTATION));
  }

  @Test
  void shouldReadMixedEntriesFromTeiXmlInDocumentOrder() {
    String xml = teiXML.replaceAll("(?s)<listBibl>.*?</listBibl>", """
        <listBibl type="root">
          <listBibl type="resources">
            <bibl type="hsc"><ref target="https://example.org/external">External</ref></bibl>
          </listBibl>
          <listBibl type="bibliography">
            <bibl><ref target="https://example.org/research">Z Forschung</ref></bibl>
            <bibl>A Forschung &lt;Digital&gt; &amp; Handschriften</bibl>
          </listBibl>
        </listBibl>
        """);

    assertEquals(List.of(
            new Forschungsdokumentation("Z Forschung", "https://example.org/research"),
            new Forschungsdokumentation("A Forschung <Digital> & Handschriften", null)),
        TEIForschungsdokumentationCommand.readForschungsdokumentationen(xml));
  }

  @Test
  void shouldReadEntriesWrittenByAddCommand() {
    Forschungsdokumentation withoutLink = new Forschungsdokumentation("Weitere Forschung", null);
    String xml = TEIForschungsdokumentationCommand
        .addForschungsdokumentation(teiXML, FORSCHUNGSDOKUMENTATION).orElseThrow();
    xml = TEIForschungsdokumentationCommand
        .addForschungsdokumentation(xml, withoutLink).orElseThrow();

    assertEquals(List.of(FORSCHUNGSDOKUMENTATION, withoutLink),
        TEIForschungsdokumentationCommand.readForschungsdokumentationen(xml));
  }

  @Test
  void shouldReadEmptyListWhenBibliographyIsMissing() {
    assertEquals(List.of(),
        TEIForschungsdokumentationCommand.readForschungsdokumentationen(teiXML));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\n\t"})
  void shouldReadEmptyListWhenTeiXmlIsMissing(String xml) {
    assertEquals(List.of(),
        TEIForschungsdokumentationCommand.readForschungsdokumentationen(xml));
  }

  @Test
  void shouldRejectUnparsableTeiXmlWhenReading() {
    assertThrows(TEICommandException.class,
        () -> TEIForschungsdokumentationCommand.readForschungsdokumentationen("Kein TEI XML"));
  }
}
