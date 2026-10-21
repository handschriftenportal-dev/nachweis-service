package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
class BeschreibungsRepositoryTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @Test
  void testsave() {
    BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("123", "KOD-1",
        IDENTIFIKATION)
        .build();

    Mockito.when((beschreibungsRepository.save(beschreibung))).thenReturn(beschreibung);
    Mockito.when((beschreibungsRepository.findById("123"))).thenReturn(beschreibung);
    beschreibungsRepository.save(beschreibung);

    assertNotNull(beschreibungsRepository.findById("123"));
  }

  @Test
  void testfindById() {
    BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("123", "KOD-1",
        IDENTIFIKATION)
        .build();

    Mockito.when((beschreibungsRepository.save(beschreibung))).thenReturn(beschreibung);
    beschreibungsRepository.save(beschreibung);

    Mockito.when((beschreibungsRepository.findByIdOptional("123")))
        .thenReturn(Optional.of(beschreibung));

    Assertions.assertEquals(Optional.of(beschreibung),
        beschreibungsRepository.findByIdOptional("123"));
  }

}
