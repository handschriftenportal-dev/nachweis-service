package de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = {"id"})
public class SchemaPflegeDatei {

  public static final String SRD_ID_FIELD = "id";
  public static final String SRD_XML_FORMAT_FIELD = "xmlFormat";
  public static final String SRD_SCHEMARESOURCE_TYPE_FIELD = "schemaResourceTyp";
  public static final String SRD_DATEI_NAME_FIELD = "dateiName";
  public static final String SRD_BEARBEITERNAME_FIELD = "bearbeitername";
  public static final String SRD_VERSION_FIELD = "version";
  public static final String SRD_ERSTELLUNGS_DATUM_FIELD = "erstellungsDatum";
  public static final String SRD_AENDERUNGS_DATUM_FIELD = "aenderungsDatum";
  public static final String SRD_DATEI_FIELD = "datei";

  @JsonProperty(SRD_ID_FIELD)
  private String id;

  @JsonProperty(SRD_XML_FORMAT_FIELD)
  private String xmlFormat;

  @JsonProperty(SRD_SCHEMARESOURCE_TYPE_FIELD)
  private String schemaResourceTyp;

  @JsonProperty(SRD_DATEI_NAME_FIELD)
  private String dateiName;

  @JsonProperty(SRD_BEARBEITERNAME_FIELD)
  private String bearbeitername;

  @JsonProperty(SRD_VERSION_FIELD)
  private String version;

  @JsonProperty(SRD_ERSTELLUNGS_DATUM_FIELD)
  private LocalDateTime erstellungsDatum;

  @JsonProperty(SRD_AENDERUNGS_DATUM_FIELD)
  private LocalDateTime aenderungsDatum;

  @JsonProperty(SRD_DATEI_FIELD)
  private String datei;
}
