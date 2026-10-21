package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.xml.bind.UnmarshalException;
import org.junit.jupiter.api.Test;

class NormdatenErkennungRequestParamExtractorTest {

  private final NormdatenErkennungRequestParamExtractor extractor =
      new NormdatenErkennungRequestParamExtractor();

  @Test
  void extract_returnsEmpty_whenXmlIsBlank() throws Exception {
    assertTrue(extractor.extract("").isEmpty());
    assertTrue(extractor.extract("   ").isEmpty());
  }

  @Test
  void extract_returnsEmpty_whenXmlIsUnparseable() throws Exception {
    assertThrows(UnmarshalException.class, () -> extractor.extract("<not valid xml"));
  }

  @Test
  void extract_returnsPersNameParams_forPersNamesWithoutKey() throws Exception {
    var teiXml = Files.readString(Path.of("src/test/resources/beschreibung_freiburg_1988.xml"));

    var result = extractor.extract(teiXml);

    assertFalse(result.isEmpty());
    result.forEach(p -> {
      assertFalse(p.xpath().isBlank(), "xpath should be set");
      assertFalse(p.displayText().isBlank(), "displayText should be extracted from element content");
    });
  }

  @Test
  void extract_filtersOut_persNamesWithExistingKey() throws Exception {
    var teiXml = Files.readString(Path.of("src/test/resources/tei-msDesc_Westphal.xml"));

    var result = extractor.extract(teiXml);
    assertEquals(1, result.size());
  }
}
