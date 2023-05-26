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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.12.22
 */

class SuchDokumentMapper {

  private SuchDokumentMapper() {
  }

  static SuchDokument map(final KulturObjektDokument kod, String kodAsJSON) {
    Objects.requireNonNull(kod, "KulturObjektDokument is required");
    Objects.requireNonNull(kodAsJSON, "kodAsJSON is required");

    return SuchDokument.builder()
        .withId(kod.getId())
        .withSuchDokumentTyp(SuchDokumentTyp.KOD)
        .withSignatur(kod.getGueltigeSignatur())
        .withLastUpdate(kod.getRegistrierungsDatum())
        .withContainsDigitalisat(!kod.getDigitalisate().isEmpty())
        .withSearchableValues(kodAsJSON)
        .withBestandhaltendeInstitutionName(mapBesitzer(kod.getGueltigeIdentifikation()))
        .withBestandhaltendeInstitutionOrt(mapAufbewahrungsOrt(kod.getGueltigeIdentifikation()))
        .withContainsBeschreibung(mapContainsBeschreibung(kod))
        .build();
  }

  static SuchDokument map(final Beschreibung beschreibung, final String beschreibungAsJson) {
    Objects.requireNonNull(beschreibung, "beschreibung is required");
    Objects.requireNonNull(beschreibungAsJson, "beschreibungAsJson is required");

    Identifikation gueltigeIdentifikation = beschreibung.getGueltigeIdentifikation().orElse(null);

    return SuchDokument.builder()
        .withId(beschreibung.getId())
        .withSignatur(beschreibung.getGueltigeIdentifikation().map(Identifikation::getIdent).orElse(null))
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withTitel(mapBeschreibungTitel(beschreibung))
        .withPubliziert(mapPubliziert(beschreibung))
        .withVerwaltungsTyp(mapVerwaltungsTyp(beschreibung))
        .withBearbeiter(mapBeschreibungsBeteiligte(beschreibung))
        .withBestandhaltendeInstitutionName(mapBesitzer(gueltigeIdentifikation))
        .withBestandhaltendeInstitutionOrt(mapAufbewahrungsOrt(gueltigeIdentifikation))
        .withJahrDerPublikation(mapBeschreibungJahrDerPublikation(beschreibung))
        .withLastUpdate(mapBeschreibungLastUpdate(beschreibung))
        .withSearchableValues(beschreibungAsJson)
        .withAutoren(mapAutoren(beschreibung))
        .withSichtbarkeit("NICHT_GESPERRT")
        .build();
  }

  static Boolean mapPubliziert(Beschreibung beschreibung) {
    return Objects.nonNull(beschreibung.getPublikationen()) && !beschreibung.getPublikationen().isEmpty();
  }

  static LocalDateTime mapBeschreibungLastUpdate(Beschreibung beschreibung) {
    return Optional.ofNullable(beschreibung.getAenderungsDatum())
        .orElse(beschreibung.getErstellungsDatum());
  }

  static List<String> mapBeschreibungsBeteiligte(Beschreibung beschreibung) {
    return beschreibung.getBeschreibungsBeteiligte().stream()
        .map(NormdatenReferenz::getName)
        .collect(Collectors.toList());
  }

  static String mapBeschreibungTitel(Beschreibung beschreibung) {
    return Stream.ofNullable(beschreibung.getBeschreibungsStruktur())
        .flatMap(Collection::stream)
        .filter(BeschreibungsKomponenteKopf.class::isInstance)
        .findFirst()
        .map(BeschreibungsKomponenteKopf.class::cast)
        .map(BeschreibungsKomponenteKopf::getTitel)
        .orElse(null);
  }

  static String mapBesitzer(Identifikation gueltigeIdentifikation) {
    return Optional.ofNullable(gueltigeIdentifikation)
        .map(Identifikation::getBesitzer)
        .map(NormdatenReferenz::getName)
        .orElse(null);
  }

  static String mapAufbewahrungsOrt(Identifikation gueltigeIdentifikation) {
    return Optional.ofNullable(gueltigeIdentifikation)
        .map(Identifikation::getAufbewahrungsOrt)
        .map(NormdatenReferenz::getName)
        .orElse(null);
  }

  static Integer mapBeschreibungJahrDerPublikation(Beschreibung beschreibung) {
    return Stream.ofNullable(beschreibung.getPublikationen())
        .flatMap(Collection::stream)
        .filter(publikation -> PublikationsTyp.ERSTPUBLIKATION == publikation.getPublikationsTyp())
        .findFirst()
        .map(Publikation::getDatumDerVeroeffentlichung)
        .map(LocalDateTime::getYear)
        .orElse(null);
  }

  static List<String> mapAutoren(Beschreibung beschreibung) {
    return Stream.ofNullable(beschreibung.getAutoren())
        .flatMap(Collection::stream)
        .map(NormdatenReferenz::getName)
        .collect(Collectors.toList());
  }

  static Boolean mapContainsBeschreibung(KulturObjektDokument kod) {
    return Objects.nonNull(kod.getBeschreibungenIDs()) && !kod.getBeschreibungenIDs().isEmpty();
  }

  static String mapVerwaltungsTyp(Beschreibung beschreibung) {
    return Optional.ofNullable(beschreibung.getVerwaltungsTyp()).map(VerwaltungsTyp::toString).orElse("");
  }
}
