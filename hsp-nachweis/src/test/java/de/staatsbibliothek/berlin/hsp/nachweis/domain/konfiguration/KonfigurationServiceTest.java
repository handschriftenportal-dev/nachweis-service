package de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wildfly.common.Assert.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
class KonfigurationServiceTest {

  KonfigurationRepository konfigurationRepositoryMock;

  KonfigurationService konfigurationService;

  @BeforeEach
  void setup() {
    konfigurationRepositoryMock = mock(KonfigurationRepository.class);
    when(konfigurationRepositoryMock.getWert("schluessel")).thenReturn(Optional.of("wert"));
    when(konfigurationRepositoryMock.getWert("does.not.exist")).thenReturn(Optional.empty());

    konfigurationService = new KonfigurationService(konfigurationRepositoryMock);
  }

  @Test
  void testGetWert() {
    Optional<String> wert_1 = konfigurationService.getWert("does.not.exist");
    assertNotNull(wert_1);
    assertFalse(wert_1.isPresent());

    Optional<String> wert_2 = konfigurationService.getWert("schluessel");
    assertNotNull(wert_2);
    assertEquals("wert", wert_2.orElse(null));

    verify(konfigurationRepositoryMock, times(2)).getWert(anyString());
  }

  @Test
  void testSetWert() {
    konfigurationService.setWert("schluessel", "wert");
    verify(konfigurationRepositoryMock, times(1)).setWert("schluessel", "wert");
  }

}
