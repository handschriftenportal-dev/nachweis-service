package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Help to convert date
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 05.06.18
 */
public class SolrDateService {

  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
  private static final DateTimeFormatter CUSTOM_FORMAT = DateTimeFormatter.ofPattern(dateFormat, Locale.GERMANY)
      .withZone(offset);

  public static String formatLocalDateTime(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }

    return localDateTime.format(CUSTOM_FORMAT);
  }

}
