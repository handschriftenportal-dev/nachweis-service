package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 09.11.2021.
 * @version 1.0
 */

@QuarkusTest
@TestTransaction
public class BearbeiterRepositoryAdapterTest {

  @Inject
  BearbeiterRepository bearbeiterRepository;

  @Test
  void testfindByBearbeiterName() {

    Bearbeiter tester = new Bearbeiter("1", "tester");

    bearbeiterRepository.saveAndFlush(tester);

    Bearbeiter bearbeiter = bearbeiterRepository.findByBearbeiterName("tester");

    assertNotNull(bearbeiter);

    assertEquals("tester", bearbeiter.getBearbeitername());

    bearbeiterRepository.deleteByIdAndFlush(tester.getId());
  }

  @Test
  void testFindByBearbeiterNameCollection() {

    Bearbeiter tester1 = new Bearbeiter("10", "tester1");
    Bearbeiter tester2 = new Bearbeiter("11", "tester2");

    bearbeiterRepository.saveAndFlush(tester1);
    bearbeiterRepository.saveAndFlush(tester2);

    List<Bearbeiter> result = bearbeiterRepository.findByBearbeiterName(
        List.of("tester1", "tester2"));

    assertNotNull(result);
    assertEquals(2, result.size());

    List<Bearbeiter> singleResult = bearbeiterRepository.findByBearbeiterName(List.of("tester1"));

    assertNotNull(singleResult);
    assertEquals(1, singleResult.size());
    assertEquals("tester1", singleResult.getFirst().getBearbeitername());

    bearbeiterRepository.deleteByIdAndFlush(tester1.getId());
    bearbeiterRepository.deleteByIdAndFlush(tester2.getId());
  }

  @Test
  void testFindByBearbeiterNameSet() {

    Bearbeiter tester1 = new Bearbeiter("20", "testerA");
    Bearbeiter tester2 = new Bearbeiter("21", "testerB");

    bearbeiterRepository.saveAndFlush(tester1);
    bearbeiterRepository.saveAndFlush(tester2);

    List<Bearbeiter> result = bearbeiterRepository.findByBearbeiterName(
        Set.of("testerA", "testerB"));

    assertNotNull(result);
    assertEquals(2, result.size());

    List<Bearbeiter> singleResult = bearbeiterRepository.findByBearbeiterName(Set.of("testerA"));

    assertNotNull(singleResult);
    assertEquals(1, singleResult.size());
    assertEquals("testerA", singleResult.getFirst().getBearbeitername());

    bearbeiterRepository.deleteByIdAndFlush(tester1.getId());
    bearbeiterRepository.deleteByIdAndFlush(tester2.getId());
  }
}
