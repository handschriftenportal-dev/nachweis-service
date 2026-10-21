package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NormdatumComponentTest {

  @Test
  void testFormatText() {
    NormdatumComponent normdatumComponent = new NormdatumComponent();

    assertNull(normdatumComponent.formatText(null, null));
    assertEquals("text", normdatumComponent.formatText(null, "text"));
    assertEquals("text", normdatumComponent.formatText("", "text"));
    assertNull(normdatumComponent.formatText("textKey", null));

    assertEquals("größer als Folio",
        normdatumComponent.formatText("kod_kerndaten_format_", "larger_than_folio"));

    assertEquals("größer als Folio; kleiner als Oktav",
        normdatumComponent.formatText("kod_kerndaten_format_", "larger_than_folio;smaller_than_octavo"));
  }

}
