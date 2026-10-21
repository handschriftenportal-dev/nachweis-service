package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Creation;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.ProfileDesc;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class ProfilDescFactoryTest {

  @Test
  void buildWithCreationDate() {
    LocalDateTime creatioNDate = LocalDateTime.of(2022, 4, 7, 9, 27, 0);
    Optional<ProfileDesc> profileDesc = Optional.of(new ProfileDesc());
    ProfilDescFactory.buildWithCreationDate(profileDesc, creatioNDate);

    assertEquals(1,
        profileDesc.get().getAbstractsAndCreationsAndLangUsages().size());

    profileDesc.get().getAbstractsAndCreationsAndLangUsages().stream().findFirst()
        .flatMap(e -> ((Creation) e).getContent().stream().findFirst())
        .ifPresent(d -> Assertions.assertEquals("2022-04-07", ((Date) d).getWhen()));
  }
}
