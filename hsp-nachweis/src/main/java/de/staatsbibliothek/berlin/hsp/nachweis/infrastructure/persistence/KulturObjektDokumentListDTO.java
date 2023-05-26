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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 03.11.22
 */

@Entity
@Immutable
@Subselect("select kod.id as hsp_id,"
    + " kod.gndidentifier as gnd_id,"
    + " identifikation.ident as gueltige_signatur,"
    + " aufbewahrungsort.name as aufbewahrungsort,"
    + " besitzer.name as besitzer,"
    + " kod.registrierungsdatum as registrierungsdatum,"
    + " TO_CHAR(kod.registrierungsdatum, 'DD.MM.YYYY HH24:MI:SS') as registrierungsdatum_string,"
    + " beschreibungen.beschreibungen_ids as beschreibungen_ids"
    + " from kulturobjektdokument kod"
    + " left join Identifikation identifikation on kod.gueltigeidentifikation_id = identifikation.id"
    + " join normdatenreferenz aufbewahrungsort on identifikation.aufbewahrungsort_id = aufbewahrungsort.id"
    + " join normdatenreferenz besitzer on identifikation.besitzer_id = besitzer.id"
    + " left join (select kod_beschreibungenids.kulturobjektdokument_id,"
    + " string_agg(kod_beschreibungenids.beschreibungenids, ' ') as beschreibungen_ids"
    + " from kulturobjektdokument_beschreibungenids kod_beschreibungenids"
    + " group by kod_beschreibungenids.kulturobjektdokument_id) beschreibungen"
    + " on beschreibungen.kulturobjektdokument_id = kod.id")
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"hspId"})
public class KulturObjektDokumentListDTO implements Serializable {

  private static final long serialVersionUID = 8118664717869142155L;

  private static final Pattern SPLITTER = Pattern.compile(" ");

  @Id
  @Column(name = "hsp_id")
  private String hspId;
  @Column(name = "gnd_id")
  private String gndId;
  @Column(name = "gueltige_signatur")
  private String gueltigeSignatur;
  @Column(name = "aufbewahrungsort")
  private String aufbewahrungsort;
  @Column(name = "besitzer")
  private String besitzer;
  @Column(name = "registrierungsdatum")
  private LocalDateTime registrierungsdatum;
  @Column(name = "registrierungsdatum_string")
  private String registrierungsdatumString;
  @Column(name = "beschreibungen_ids")
  private String beschreibungenIds;

  protected KulturObjektDokumentListDTO() {
    // do not use
  }

  public List<String> getBeschreibungenIdsAsList() {
    return Stream.ofNullable(beschreibungenIds)
        .flatMap(SPLITTER::splitAsStream)
        .collect(Collectors.toList());
  }
}
