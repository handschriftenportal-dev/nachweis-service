package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.RevisionDesc;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class RevisionDescFactoryTest {

  @Test
  void testbuildWithModificationDate() {
    LocalDateTime modificationDate = LocalDateTime.of(2022, 4, 7, 9, 27, 0);
    Optional<RevisionDesc> revisionDesc = Optional.of(new RevisionDesc());
    RevisionDescFactory.buildWithModificationDate(revisionDesc, modificationDate);

    assertEquals(1, revisionDesc.get().getRevisionChangeAttribute().size());

    revisionDesc.get().getRevisionChangeAttribute().stream().findFirst().ifPresent(c -> {
      assertEquals(1, c.getContent().size());

      c.getContent().stream().findFirst()
          .ifPresent(d -> assertEquals("2022-04-07", ((Date) d).getWhen()));
    });
  }
}
