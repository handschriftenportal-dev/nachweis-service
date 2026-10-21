package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DATE_TIME_PATTERN;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.tei_c.ns._1.Creation;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.ProfileDesc;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class ProfilDescFactory {

  static void buildWithCreationDate(Optional<ProfileDesc> profileDesc,
      LocalDateTime creationDateTime) {
    if (profileDesc.isPresent() && creationDateTime != null) {
      profileDesc.get().getAbstractsAndCreationsAndLangUsages().stream()
          .filter(e -> e instanceof Creation)
          .map(c -> (Creation) c).findFirst().ifPresentOrElse(creation -> {
            creation.getContent().clear();
            Date creationDate = createCreationDate(creationDateTime);
            creation.getContent().add(creationDate);
          }, () -> {
            Creation creation = new Creation();
            Date creationDate = createCreationDate(creationDateTime);
            creation.getContent().add(creationDate);
            profileDesc.get().getAbstractsAndCreationsAndLangUsages().add(creation);
          });
    }
  }

  private static Date createCreationDate(LocalDateTime creationDateTime) {
    Date creationDate = new Date();
    creationDate.setWhen(creationDateTime
        .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
    creationDate.getContent().add(creationDateTime
        .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    return creationDate;
  }
}
