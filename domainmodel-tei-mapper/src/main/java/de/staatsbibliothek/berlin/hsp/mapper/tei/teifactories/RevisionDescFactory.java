package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DATE_TIME_PATTERN;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.RevisionDesc;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class RevisionDescFactory {

  static void buildWithModificationDate(Optional<RevisionDesc> revisionDesc,
      LocalDateTime modificationDateTime) {
    if (revisionDesc.isPresent() && modificationDateTime != null) {
      revisionDesc.get().getRevisionChangeAttribute().clear();

      Change change = new Change();
      Date modifikationDate = new Date();
      modifikationDate.setWhen(modificationDateTime
          .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
      modifikationDate.getContent().add(modificationDateTime
          .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
      change.getContent().add(modifikationDate);
      revisionDesc.get().getRevisionChangeAttribute().add(change);
    }
  }
}
