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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konradigitalisat.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Slf4j
public class DigitalisatViewModel implements Serializable {

  public static final String DATE_TIME_FORMAT = "dd.MM.yyyy";
  private static final long serialVersionUID = -2958547584046678919L;
  private String id;
  private String manifestURL;
  private String alternativeUrl;
  private String thumbnail;
  private DigitalisatTyp digitalisatTyp;
  private NormdatenReferenz ort;
  private NormdatenReferenz einrichtung;
  private LocalDate digitalisierungsdatum;
  private LocalDate erstellungsdatum;

  public static DigitalisatViewModel map(Digitalisat digitalisat) {
    log.info("Map Digitalisat {}", digitalisat);
    if (Objects.isNull(digitalisat)) {
      return null;
    }
    return DigitalisatViewModel.builder()
        .id(digitalisat.getId())
        .manifestURL(Optional.ofNullable(digitalisat.getManifestURL()).map(URI::toASCIIString).orElse(null))
        .thumbnail(Optional.ofNullable(digitalisat.getThumbnailURL()).map(URI::toASCIIString).orElse(null))
        .alternativeUrl(Optional.ofNullable(digitalisat.getAlternativeURL()).map(URI::toASCIIString).orElse(null))
        .digitalisatTyp(digitalisat.getTyp())
        .erstellungsdatum(digitalisat.getErstellungsDatum())
        .digitalisierungsdatum(digitalisat.getDigitalisierungsDatum())
        .ort(digitalisat.getEntstehungsOrt())
        .einrichtung(digitalisat.getDigitalisierendeEinrichtung())
        .build();
  }
}
