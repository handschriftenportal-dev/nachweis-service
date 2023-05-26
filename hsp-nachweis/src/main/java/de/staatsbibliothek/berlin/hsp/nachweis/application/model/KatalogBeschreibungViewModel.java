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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import java.io.Serializable;
import java.time.temporal.ChronoField;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 13.10.22
 */
@Getter
@Builder(setterPrefix = "with")
@EqualsAndHashCode(of = {"id"})
public class KatalogBeschreibungViewModel implements Serializable {

  private static final long serialVersionUID = -4013682818747728693L;
  private String id;
  private String katalogId;
  private String signatur;
  private String titel;
  private String bestandhaltendeInstitutionName;
  private String bestandhaltendeInstitutionOrt;
  private String autoren;
  private Integer jahrDerErstpublikation;

  public static KatalogBeschreibungViewModel map(Beschreibung katalogBeschreibung) {
    Objects.requireNonNull(katalogBeschreibung, "katalogBeschreibung is required");

    return KatalogBeschreibungViewModel.builder()
        .withId(katalogBeschreibung.getId())
        .withKatalogId(katalogBeschreibung.getKatalogID())
        .withSignatur(katalogBeschreibung.getGueltigeIdentifikation()
            .map(Identifikation::getIdent)
            .orElse(""))
        .withTitel(mapTitel(katalogBeschreibung))
        .withJahrDerErstpublikation(mapJahrDerErstpublikation(katalogBeschreibung))
        .withAutoren(mapAutoren(katalogBeschreibung))
        .withBestandhaltendeInstitutionName(mapBestandhaltendeInstitutionName(katalogBeschreibung))
        .withBestandhaltendeInstitutionOrt(mapBestandhaltendeInstitutionOrt(katalogBeschreibung))
        .build();
  }

  static String mapTitel(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getBeschreibungsStruktur().stream()
        .filter(BeschreibungsKomponenteKopf.class::isInstance)
        .findFirst()
        .map(BeschreibungsKomponenteKopf.class::cast)
        .map(BeschreibungsKomponenteKopf::getTitel)
        .orElse("");
  }

  static Integer mapJahrDerErstpublikation(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getPublikationen().stream()
        .filter(publikation -> PublikationsTyp.ERSTPUBLIKATION == publikation.getPublikationsTyp())
        .findFirst()
        .map(publikation -> publikation.getDatumDerVeroeffentlichung().get(ChronoField.YEAR))
        .orElse(null);
  }

  static String mapAutoren(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getAutoren().stream()
        .map(NormdatenReferenz::getName)
        .collect(Collectors.joining("; "));
  }

  static String mapBestandhaltendeInstitutionName(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getGueltigeIdentifikation()
        .map(Identifikation::getBesitzer)
        .map(NormdatenReferenz::getName)
        .orElse("");
  }

  static String mapBestandhaltendeInstitutionOrt(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getGueltigeIdentifikation()
        .map(Identifikation::getAufbewahrungsOrt)
        .map(NormdatenReferenz::getName)
        .orElse("");
  }
}
