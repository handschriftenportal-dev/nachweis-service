package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    + "   kod.gndidentifier as gnd_id,"
    + "   identifikation.ident as gueltige_signatur,"
    + "   aufbewahrungsort.name as aufbewahrungsort,"
    + "   identifikation.sort_key as signatur_sort_key,"
    + "   besitzer.name as besitzer,"
    + "   kod.registrierungsdatum as registrierungsdatum,"
    + "   TO_CHAR(kod.registrierungsdatum, 'DD.MM.YYYY HH24:MI:SS') as registrierungsdatum_string,"
    + "   beschreibungen.beschreibungen_ids as beschreibungen_ids,"
    + "   p.purl as purl,"
    + "   kod.attributsstatustyp as kerndaten,"
    + "   CASE WHEN TRIM(COALESCE(beschreibungen.beschreibungen_ids, '')) <> ''"
    + "        THEN 'JA' ELSE 'NEIN' END as mit_beschreibungen,"
    + "   CASE WHEN EXISTS ("
    + "          SELECT 1 FROM kulturobjektdokument_digitalisate kd"
    + "          WHERE kd.kulturobjektdokument_id = kod.id"
    + "        ) THEN 'JA' ELSE 'NEIN' END as mit_digitalisat"
    + "   from kulturobjektdokument kod"
    + "   left join Identifikation identifikation on kod.gueltigeidentifikation_id = identifikation.id"
    + "   left join normdatenreferenz aufbewahrungsort on identifikation.aufbewahrungsort_id = aufbewahrungsort.id"
    + "   left join normdatenreferenz besitzer on identifikation.besitzer_id = besitzer.id"
    + "   left join purl p on p.kulturobjektdokument_id = kod.id"
    + "   left join (select kod_beschreibungenids.kulturobjektdokument_id,"
    + "                    string_agg(kod_beschreibungenids.beschreibungenids, ' ') as beschreibungen_ids"
    + "               from kulturobjektdokument_beschreibungenids kod_beschreibungenids"
    + "              group by kod_beschreibungenids.kulturobjektdokument_id) beschreibungen"
    + "   on beschreibungen.kulturobjektdokument_id = kod.id")

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"hspId"})
public class KulturObjektDokumentListDTO implements Serializable {

  @Serial
  private static final long serialVersionUID = 8118664717869142155L;

  private static final Pattern SPLITTER = Pattern.compile(" ");

  @Id
  @Column(name = "hsp_id")
  String hspId;
  @Column(name = "gnd_id")
  String gndId;
  @Column(name = "gueltige_signatur")
  String gueltigeSignatur;
  @Column(name = "signatur_sort_key")
  String sortKey;
  @Column(name = "aufbewahrungsort")
  String aufbewahrungsort;
  @Column(name = "besitzer")
  String besitzer;
  @Column(name = "registrierungsdatum")
  LocalDateTime registrierungsdatum;
  @Column(name = "registrierungsdatum_string")
  String registrierungsdatumString;
  @Column(name = "beschreibungen_ids")
  String beschreibungenIds;
  @Column(name = "purl")
  String purl;
  @Column(name = "kerndaten")
  String kerndaten;
  @Column(name = "mit_beschreibungen")
  String mitBeschreibungen;
  @Column(name = "mit_digitalisat")
  String mitDigitalisat;

  protected KulturObjektDokumentListDTO() {
    // do not use
  }

  public List<String> getBeschreibungenIdsAsList() {
    return Stream.ofNullable(beschreibungenIds)
        .flatMap(SPLITTER::splitAsStream)
        .collect(Collectors.toList());
  }
}
