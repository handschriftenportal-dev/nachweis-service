package de.staatsbibliothek.berlin.hsp.nachweis.domain.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class PersonTest {

  @Test
  void getVariantName_returnsEmptyString_whenVariantNameIsNull() {
    Person person = Person.builder().variantName(null).build();

    assertEquals("", person.getVariantName());
  }

  @Test
  void getVariantName_returnsEmptyString_whenVariantNameIsEmpty() {
    Person person = Person.builder().variantName(List.of()).build();

    assertEquals("", person.getVariantName());
  }

  @Test
  void getVariantName_returnsSingleVariantName() {
    Person person = Person.builder()
        .variantName(List.of(new PersonVariantName("beatus")))
        .build();

    assertEquals("beatus", person.getVariantName());
  }

  @Test
  void getVariantName_returnsMultipleVariantNamesSeparatedByComma() {
    Person person = Person.builder()
        .variantName(List.of(
            new PersonVariantName("beatus"),
            new PersonVariantName("augustinus")
        ))
        .build();

    assertEquals("beatus, augustinus", person.getVariantName());
  }

  @Test
  void getVariantName_returnsEmptyString_whenAllNamesAreBlank() {
    Person person = Person.builder()
        .variantName(List.of(
            new PersonVariantName(null),
            new PersonVariantName(" "),
            new PersonVariantName("")
        ))
        .build();

    assertEquals("", person.getVariantName());
  }

  @Test
  void getVariantName_ignoresNullAndBlankNames() {
    Person person = Person.builder()
        .variantName(List.of(
            new PersonVariantName("beatus"),
            new PersonVariantName(null),
            new PersonVariantName(" "),
            new PersonVariantName("augustinus")
        ))
        .build();

    assertEquals("beatus, augustinus", person.getVariantName());
  }

  @Test
  void getGndUrl_returnsNull_whenGndIdentifierIsNull() {
    Person person = Person.builder().gndIdentifier(null).build();

    assertNull(person.getGndUrl());
  }

  @Test
  void getGndUrl_returnsNull_whenGndIdentifierIsBlank() {
    Person person = Person.builder().gndIdentifier(" ").build();

    assertNull(person.getGndUrl());
  }

  @Test
  void getGndUrl_returnsCorrectUrl_whenGndIdentifierIsPresent() {
    Person person = Person.builder().gndIdentifier("123456").build();

    assertEquals("https://d-nb.info/gnd/123456", person.getGndUrl());
  }
}