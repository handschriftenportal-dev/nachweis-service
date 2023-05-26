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
  private XMLFormate xmlFormat;

  @JsonProperty(SRD_SCHEMARESOURCE_TYPE_FIELD)
  private SchemaResourceTyp schemaResourceTyp;

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

  public enum SchemaResourceTyp {
    XSLT,
    XSD,
    RNG,
    ISOSCH
  }

  public enum XMLFormate {
    MARC21,
    TEI_ALL,
    TEI_ODD,
    TEI_HSP,
    MXML,
    UNBEKANNT
  }
}
