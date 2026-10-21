package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ExterneRessourceTypTest {

  @ParameterizedTest
  @EnumSource(ExterneRessourceTyp.class)
  void shouldResolveEveryTypeByTeiType(ExterneRessourceTyp typ) {
    Optional<ExterneRessourceTyp> result =
        ExterneRessourceTyp.fromTeiType(typ.getTeiType());

    assertEquals(Optional.of(typ), result);
  }

  @ParameterizedTest
  @EnumSource(ExterneRessourceTyp.class)
  void shouldResolveTeiTypeCaseInsensitiveAndTrimmed(ExterneRessourceTyp typ) {
    Optional<ExterneRessourceTyp> result =
        ExterneRessourceTyp.fromTeiType(
            "  " + typ.getTeiType().toUpperCase(Locale.ROOT) + "  ");

    assertEquals(Optional.of(typ), result);
  }

  @Test
  void shouldResolveMixedCaseTeiType() {
    assertEquals(
        Optional.of(ExterneRessourceTyp.HSC),
        ExterneRessourceTyp.fromTeiType("HsC"));
  }

  @ParameterizedTest
  @EnumSource(ExterneRessourceTyp.class)
  void shouldProvideMessageKeyForEveryType(ExterneRessourceTyp typ) {
    assertEquals(
        "externe_ressource_typ_" + typ.name(),
        typ.getMessageKey());
  }

  @Test
  void shouldReturnEmptyForUnknownTeiType() {
    Optional<ExterneRessourceTyp> result =
        ExterneRessourceTyp.fromTeiType("unknown");

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyForNullTeiType() {
    Optional<ExterneRessourceTyp> result =
        ExterneRessourceTyp.fromTeiType(null);

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyForBlankTeiType() {
    Optional<ExterneRessourceTyp> result =
        ExterneRessourceTyp.fromTeiType("   ");

    assertTrue(result.isEmpty());
  }
}
