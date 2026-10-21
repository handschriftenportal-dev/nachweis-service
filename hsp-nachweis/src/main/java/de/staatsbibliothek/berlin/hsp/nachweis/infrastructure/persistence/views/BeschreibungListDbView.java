package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.11.22
 */

@Entity
@Immutable
@Table(name = "beschreibung_dashboard_view")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class BeschreibungListDbView implements Serializable {

  public static final String NULL_DATE = "30.12.1899";
  public static final String NULL_TIMESTAMP = "00:00:00";
  @Serial
  private static final long serialVersionUID = 400089542487229655L;
  private static final Pattern SPLITTER = Pattern.compile("\\|");
  @Id
  @Column(name = "hsp_id")
  String hspId;

  @Column(name = "kod_id")
  String kodId;

  @Column(name = "katalog_id")
  String katalogId;

  @Column(name = "gueltige_signatur")
  String gueltigeSignatur;

  @Column(name = "titel")
  String titel;

  @Column(name = "verwaltungstyp")
  String verwaltungsTyp;

  @Column(name = "dokumentobjekttyp")
  String dokumentObjektTyp;

  @Column(name = "dokumentobjekttypForFilter")
  String dokumentobjekttypForFilter;

  @Column(name = "autoren")
  String autoren;

  @Column(name = "aufbewahrungsort")
  String aufbewahrungsort;

  @Column(name = "besitzer")
  String besitzer;

  @Column(name = "erstellungsdatum")
  LocalDateTime erstellungsdatum;

  @Column(name = "erstellungsdatum_string")
  String erstellungsdatumString;

  @Transient
  String erstellungsdatumStringInt;

  @Column(name = "aenderungsdatum")
  LocalDateTime aenderungsdatum;

  @Column(name = "aenderungsdatum_string")
  String aenderungsdatumString;

  @Transient
  String aenderungsdatumStringInt;

  @Column(name = "bearbeiter")
  String bearbeiter;

  @Column(name = "publikations_datum_hsp")
  LocalDateTime publikationsDatumHsp;

  @Enumerated(EnumType.STRING)
  @Column(name = "publikation_status")
  PublikationStatus publikationStatus;

  @Column(name = "hsp_purl")
  String hspPurl;

  protected BeschreibungListDbView() {
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
    return Stream.ofNullable(autoren)
        .flatMap(SPLITTER::splitAsStream)
        .toList();
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

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateproxy
        ? hibernateproxy.getHibernateLazyInitializer().getPersistentClass()
        : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateproxy
        ? hibernateproxy.getHibernateLazyInitializer().getPersistentClass()
        : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    BeschreibungListDbView that = (BeschreibungListDbView) o;
    return getHspId() != null && Objects.equals(getHspId(), that.getHspId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateproxy
        ? hibernateproxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
