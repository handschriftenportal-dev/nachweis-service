package de.staatsbibliothek.berlin.hsp.nachweis.domain.initium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class InitiumTest {

  @Test
  void getLanguageLabelsCapitalized_returnsEmptyString_whenLanguagesIsNull() {
    Initium initium = Initium.builder().id("ID1").text("text").languages(null).build();

    assertEquals("", initium.getLanguageLabelsCapitalized());
  }

  @Test
  void getLanguageLabelsCapitalized_returnsEmptyString_whenLanguagesIsEmpty() {
    Initium initium = Initium.builder().id("ID1").text("text").languages(List.of()).build();

    assertEquals("", initium.getLanguageLabelsCapitalized());
  }

  @Test
  void getLanguageLabelsCapitalized_returnsSingleLanguage() {
    Initium initium = Initium.builder().id("ID1").text("text")
        .languages(List.of(new InitiumLanguage("L1", "deutsch"))).build();

    assertEquals("Deutsch", initium.getLanguageLabelsCapitalized());
  }

  @Test
  void getLanguageLabelsCapitalized_returnsMultipleLanguagesSeparatedByComma() {
    Initium initium = Initium.builder().id("ID1").text("text")
        .languages(List.of(
            new InitiumLanguage("L1", "deutsch"),
            new InitiumLanguage("L2", "lateinisch")
        )).build();

    assertEquals("Deutsch, Lateinisch", initium.getLanguageLabelsCapitalized());
  }

  @Test
  void getLanguageLabelsCapitalized_ignoresNullAndBlankLanguageNames() {
    Initium initium = Initium.builder().id("ID1").text("text")
        .languages(List.of(
            new InitiumLanguage("L1", "deutsch"),
            new InitiumLanguage("L2", null),
            new InitiumLanguage("L3", " "),
            new InitiumLanguage("L4", "lateinisch")
        )).build();

    assertEquals("Deutsch, Lateinisch", initium.getLanguageLabelsCapitalized());
  }

  @Test
  void getLanguageLabelsCapitalized_returnsSameValueAsGetLanguage() {
    Initium initium = Initium.builder().id("ID1").text("text")
        .languages(List.of(new InitiumLanguage("L1", "deutsch"))).build();

    assertEquals(initium.getLanguageLabelsCapitalized(), initium.getLanguage());
  }
}