/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
