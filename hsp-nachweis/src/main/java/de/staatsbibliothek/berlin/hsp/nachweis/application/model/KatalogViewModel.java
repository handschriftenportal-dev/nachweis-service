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
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 13.10.22
 */
@Getter
@Builder(setterPrefix = "with")
@EqualsAndHashCode(of = {"id"})
public class KatalogViewModel implements Serializable {

  private static final long serialVersionUID = 5380656152355918342L;
  private String id;
  private String titel;
  private String autoren;
  private String publikationsJahr;
  private String verlag;
  private String lizenz;
  private LocalDateTime erstellDatum;
  private LocalDateTime aenderungsDatum;
  private String teiXML;
  private int anzahlBeschreibungen;
  private int anzahlReferenzen;
  private DigitalisatViewModel digitalisat;
  private List<KatalogBeschreibungViewModel> beschreibungen;


  public static KatalogViewModel map(Katalog katalog, List<Beschreibung> katalogBeschreibungen) {
    Objects.requireNonNull(katalog, "katalog is required");
    Objects.requireNonNull(katalogBeschreibungen, "katalogBeschreibungen is required");

    return KatalogViewModel.builder()
        .withId(katalog.getId())
        .withTitel(katalog.getTitle())
        .withTeiXML(katalog.getTeiXML())
        .withAenderungsDatum(katalog.getAenderungsDatum())
        .withErstellDatum(katalog.getErstellDatum())
        .withPublikationsJahr(katalog.getPublikationsJahr())
        .withLizenz(katalog.getLizenzUri())
        .withVerlag(Optional.ofNullable(katalog.getVerlag()).map(NormdatenReferenz::getName).orElse(""))
        .withAutoren(katalog.getAutoren().stream()
            .map(NormdatenReferenz::getName)
            .collect(Collectors.joining("; ")))
        .withAnzahlBeschreibungen(katalogBeschreibungen.size())
        .withAnzahlReferenzen(katalog.getBeschreibungen().size())
        .withBeschreibungen(katalogBeschreibungen.stream()
            .map(KatalogBeschreibungViewModel::map)
            .collect(Collectors.toList()))
        .withDigitalisat(Optional.ofNullable(katalog.getDigitalisat())
            .map(DigitalisatViewModel::map)
            .orElse(null))
        .build();
  }

  public StreamedContent getTeiFile() {
    if (Objects.nonNull(this.teiXML) && !this.teiXML.isEmpty() && Objects.nonNull(this.id)) {
      return DefaultStreamedContent.builder()
          .name("tei-" + this.id + ".xml")
          .contentType("text/xml")
          .stream(() -> new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8)))
          .build();
    } else {
      return null;
    }
  }

}
