package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 24.05.2022
 */
class SolrDateServiceTest {

  @Test
  void testFormatLocalDateTime() {
    LocalDateTime dateTime = LocalDateTime.of(2022, 5, 4, 11, 12, 13);
    assertEquals("2022-05-04T11:12:13Z", SolrDateService.formatLocalDateTime(dateTime));
  }

}
