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
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.11.22
 */

@Entity
@Immutable
@Subselect("select beschreibung.id as hsp_id,"
    + " beschreibung.kodid as kod_id,"
    + " beschreibung.katalogid as katalog_id,"
    + " identifikation.ident as gueltige_signatur,"
    + " beschreibungskomponentekopf.titel as titel,"
    + " beschreibung.verwaltungstyp as verwaltungstyp,"
    + " beschreibung.dokumentobjekttyp as dokumentobjekttyp,"
    + " author.autoren as autoren,"
    + " aufbewahrungsort.name as aufbewahrungsort,"
    + " besitzer.name as besitzer,"
    + " beschreibung.erstellungsdatum as erstellungsdatum,"
    + " TO_CHAR(beschreibung.erstellungsdatum, 'DD.MM.YYYY HH24:MI:SS') as erstellungsdatum_string,"
    + " beschreibung.aenderungsdatum as aenderungsdatum,"
    + " TO_CHAR(beschreibung.aenderungsdatum, 'DD.MM.YYYY HH24:MI:SS') as aenderungsdatum_string"
    + " from beschreibungsdokument beschreibung"
    + " left join beschreibungsdokument_globalebeschreibungskomponente bd_gbk"
    + " on beschreibung.id = bd_gbk.beschreibung_id"
    + " left join globalebeschreibungskomponente globalebeschreibungskomponente"
    + " on bd_gbk.beschreibungsstruktur_id = globalebeschreibungskomponente.id"
    + " left join beschreibungskomponentekopf beschreibungskomponentekopf"
    + " on globalebeschreibungskomponente.id = beschreibungskomponentekopf.id"
    + " left join globalebeschreibungskomponente_identifikation gbk_i"
    + " on globalebeschreibungskomponente.id = gbk_i.globalebeschreibungskomponente_id"
    + " left join identifikation identifikation"
    + " on gbk_i.identifikationen_id = identifikation.id and identifikation.identtyp = 'GUELTIGE_SIGNATUR'"
    + " join normdatenreferenz besitzer on identifikation.besitzer_id = besitzer.id"
    + " join normdatenreferenz aufbewahrungsort on identifikation.aufbewahrungsort_id = aufbewahrungsort.id"
    + " left join (select ba.beschreibung_id as bsId,  string_agg(aut.name , '|') as autoren"
    + "                from beschreibungsdokument_autoren ba"
    + "                join normdatenreferenz aut"
    + "                on ba.autoren_id=aut.id group by bsId) author"
    + "    on beschreibung.id= author.bsId")
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"hspId"})
public class BeschreibungListDTO implements Serializable {

  private static final long serialVersionUID = 400089542487229655L;

  private static final Pattern SPLITTER = Pattern.compile("\\|");
  public static final String NULL_DATE = "30.12.1899";
  public static final String NULL_TIMESTAMP = "00:00:00";

  @Id
  @Column(name = "hsp_id")
  private String hspId;

  @Column(name = "kod_id")
  private String kodId;

  @Column(name = "katalog_id")
  private String katalogId;

  @Column(name = "gueltige_signatur")
  private String gueltigeSignatur;

  @Column(name = "titel")
  private String titel;

  @Column(name = "verwaltungstyp")
  private String verwaltungsTyp;

  @Column(name = "dokumentobjekttyp")
  private String dokumentObjektTyp;

  @Column(name = "autoren")
  private String autoren;

  @Transient
  private List<String> autorenListInt = null;

  @Column(name = "aufbewahrungsort")
  private String aufbewahrungsort;

  @Column(name = "besitzer")
  private String besitzer;

  @Column(name = "erstellungsdatum")
  private LocalDateTime erstellungsdatum;

  @Column(name = "erstellungsdatum_string")
  private String erstellungsdatumString;

  @Transient
  private String erstellungsdatumStringInt;

  @Column(name = "aenderungsdatum")
  private LocalDateTime aenderungsdatum;

  @Column(name = "aenderungsdatum_string")
  private String aenderungsdatumString;

  @Transient
  private String aenderungsdatumStringInt;


  protected BeschreibungListDTO() {
    // do not use
  }

  public String getErstellungsdatumString() {
    if (erstellungsdatumStringInt == null) {
      erstellungsdatumStringInt = getDatumString(erstellungsdatumString);
    }
    return erstellungsdatumStringInt;
  }

  public String getAenderungsdatumString() {
    if (aenderungsdatumStringInt == null) {
      aenderungsdatumStringInt = getDatumString(aenderungsdatumString);
    }
    return aenderungsdatumStringInt;
  }

  public List<String> getAutorenAsList() {
    if (autorenListInt == null) {
      autorenListInt = Stream.ofNullable(autoren)
          .flatMap(SPLITTER::splitAsStream)
          .collect(Collectors.toList());
    }
    return autorenListInt;
  }

  public String getDatumString(String formattedDatum) {
    if (formattedDatum == null || formattedDatum.startsWith(NULL_DATE)) {
      return "";
    }

    if (formattedDatum.contains(NULL_TIMESTAMP)) {
      return formattedDatum.substring(0, 10);
    }

    return formattedDatum;
  }
}
