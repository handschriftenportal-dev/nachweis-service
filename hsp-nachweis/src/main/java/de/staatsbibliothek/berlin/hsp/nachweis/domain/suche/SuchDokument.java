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

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrDateService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrIndexFields;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 */
@Slf4j
public class SuchDokument extends SolrInputDocument {

  private static final long serialVersionUID = 3527472270628794550L;

  public static SuchDokumentBuilder builder() {
    return new SuchDokumentBuilder();
  }

  public String getId() {
    return getFieldValueAsString(SolrIndexFields.FIELD_ID);
  }

  public String getSuchDokumentTyp() {
    return getFieldValueAsString(SolrIndexFields.FIELD_TYP);
  }

  public String getSignatur() {
    return getFieldValueAsString(SolrIndexFields.FIELD_SIGNATUR);
  }

  public String getTitel() {
    return getFieldValueAsString(SolrIndexFields.FIELD_TITEL);
  }

  public String getSichtbarkeit() {
    return getFieldValueAsString(SolrIndexFields.FIELD_SICHTBARKEIT);
  }

  public String getVerwaltungsTyp() {
    return getFieldValueAsString(SolrIndexFields.FIELD_VERWALTUNGS_TYP);
  }

  public List<String> getBearbeiter() {
    return getFieldValuesAsString(SolrIndexFields.FIELD_BEARBEITER);
  }

  public String getBestandhaltendeInstitutionName() {
    return getFieldValueAsString(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME);
  }

  public String getBestandhaltendeInstitutionOrt() {
    return getFieldValueAsString(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_ORT);
  }

  public String getBestandhaltendeInstitutionNameFacette() {
    return getFieldValueAsString(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE);
  }

  public Integer getJahrDerPublikation() {
    return getTypedFieldValue(SolrIndexFields.FIELD_JAHR_DER_PUBLIKATION, Integer.class);
  }

  public String getLastUpdate() {
    return getFieldValueAsString(SolrIndexFields.FIELD_LAST_UPDATE);
  }

  public String getSearchableValues() {
    return getFieldValueAsString(SolrIndexFields.FIELD_SEARCHABLE_VALUES);
  }

  public Boolean getContainsDigitalisat() {
    return getTypedFieldValue(SolrIndexFields.FIELD_CONTAINS_DIGITALISAT, Boolean.class);
  }

  public Boolean getContainsBuchschmuck() {
    return getTypedFieldValue(SolrIndexFields.FIELD_CONTAINS_BUCHSCHMUCK, Boolean.class);
  }

  public Boolean getContainsBeschreibung() {
    return getTypedFieldValue(SolrIndexFields.FIELD_CONTAINS_BESCHREIBUNG, Boolean.class);
  }

  public Boolean getPubliziert() {
    return getTypedFieldValue(SolrIndexFields.FIELD_PUBLIZIERT, Boolean.class);
  }

  public List<String> getAutoren() {
    return getFieldValuesAsString(SolrIndexFields.FIELD_AUTOREN);
  }

  private <T> T getTypedFieldValue(String fieldName, Class<T> t) {
    return Optional.ofNullable(getFieldValue(fieldName))
        .filter(t::isInstance)
        .map(t::cast)
        .orElse(null);
  }

  private String getFieldValueAsString(String fieldName) {
    return getTypedFieldValue(fieldName, String.class);
  }

  private List<String> getFieldValuesAsString(String fieldName) {
    return Stream.ofNullable(getFieldValues(fieldName))
        .flatMap(Collection::stream)
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "SuchDokument{" + "id='" + getId() + '\''
        + ", suchDokumentTyp=" + getSuchDokumentTyp()
        + ", signatur='" + getSignatur() + '\''
        + ", title='" + getTitel() + '\''
        + ", sichtbarkeit='" + getSichtbarkeit() + '\''
        + ", verwaltungsTyp='" + getVerwaltungsTyp() + '\''
        + ", bearbeiter='" + String.join(";", getBearbeiter()) + '\''
        + ", bestandhaltendeInstitutionName='" + getBestandhaltendeInstitutionName() + '\''
        + ", bestandhaltendeInstitutionOrt='" + getBestandhaltendeInstitutionOrt() + '\''
        + ", bestandhaltendeInstitutionNameFacette='" + getBestandhaltendeInstitutionNameFacette() + '\''
        + ", jahrDerPublikation=" + getJahrDerPublikation()
        + ", lastUpdate=" + getLastUpdate()
        + ", containsDigitalisat=" + getContainsDigitalisat()
        + ", containsBuchschmuck=" + getContainsBuchschmuck()
        + ", containsBeschreibung=" + getContainsBeschreibung()
        + ", publiziert=" + getPubliziert()
        + ", autoren=" + String.join(";", getAutoren())
        + '}';
  }

  public static class SuchDokumentBuilder {

    private final SuchDokument suchDokument;

    public SuchDokumentBuilder() {
      suchDokument = new SuchDokument();
      suchDokument.setField(SolrIndexFields.FIELD_ID, null);
    }

    private SuchDokumentBuilder setField(String field, Object value) {
      Optional.ofNullable(value).ifPresent(v -> suchDokument.setField(field, value));
      return this;
    }

    private SuchDokumentBuilder addField(String field, Object value) {
      Optional.ofNullable(value).ifPresent(v -> suchDokument.addField(field, value));
      return this;
    }

    public SuchDokumentBuilder withId(String id) {
      return setField(SolrIndexFields.FIELD_ID, id);
    }

    public SuchDokumentBuilder withSignatur(String signatur) {
      return setField(SolrIndexFields.FIELD_SIGNATUR, signatur);
    }

    public SuchDokumentBuilder withSuchDokumentTyp(SuchDokumentTyp typ) {
      return setField(SolrIndexFields.FIELD_TYP, Optional.of(typ).map(SuchDokumentTyp::name).orElse(""));
    }

    public SuchDokumentBuilder withTitel(String titel) {
      return setField(SolrIndexFields.FIELD_TITEL, titel);
    }

    public SuchDokumentBuilder withSichtbarkeit(String sichtbarkeit) {
      return setField(SolrIndexFields.FIELD_SICHTBARKEIT, sichtbarkeit);
    }

    public SuchDokumentBuilder withVerwaltungsTyp(String verwaltungsTyp) {
      return setField(SolrIndexFields.FIELD_VERWALTUNGS_TYP, verwaltungsTyp);
    }

    public SuchDokumentBuilder withBearbeiter(List<String> bearbeiter) {
      Stream.ofNullable(bearbeiter).flatMap(Collection::stream)
          .forEach(b -> addField(SolrIndexFields.FIELD_BEARBEITER, b));
      return this;
    }

    public SuchDokumentBuilder addBearbeiter(String bearbeiter) {
      return addField(SolrIndexFields.FIELD_BEARBEITER, bearbeiter);
    }

    public SuchDokumentBuilder withBestandhaltendeInstitutionName(String bestandhaltendeInstitutionName) {
      setField(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE, bestandhaltendeInstitutionName);
      return setField(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME, bestandhaltendeInstitutionName);
    }

    public SuchDokumentBuilder withBestandhaltendeInstitutionOrt(String bestandhaltendeInstitutionOrt) {
      return setField(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_ORT, bestandhaltendeInstitutionOrt);
    }

    public SuchDokumentBuilder withJahrDerPublikation(Integer jahrDerPublikation) {
      return setField(SolrIndexFields.FIELD_JAHR_DER_PUBLIKATION, jahrDerPublikation);
    }

    public SuchDokumentBuilder withLastUpdate(LocalDateTime lastUpdate) {
      return setField(SolrIndexFields.FIELD_LAST_UPDATE, SolrDateService.formatLocalDateTime(lastUpdate));
    }

    public SuchDokumentBuilder withContainsDigitalisat(Boolean containsDigitalisat) {
      return setField(SolrIndexFields.FIELD_CONTAINS_DIGITALISAT, containsDigitalisat);
    }

    public SuchDokumentBuilder withContainsBuchschmuck(Boolean containsBuchschmuck) {
      return setField(SolrIndexFields.FIELD_CONTAINS_BUCHSCHMUCK, containsBuchschmuck);
    }

    public SuchDokumentBuilder withSearchableValues(String searchableValues) {
      return setField(SolrIndexFields.FIELD_SEARCHABLE_VALUES, searchableValues);
    }

    public SuchDokumentBuilder withAutoren(List<String> autoren) {
      Stream.ofNullable(autoren).flatMap(Collection::stream)
          .forEach(autor -> addField(SolrIndexFields.FIELD_AUTOREN, autor));
      return this;
    }

    public SuchDokumentBuilder addAutor(String autor) {
      return addField(SolrIndexFields.FIELD_AUTOREN, autor);
    }

    public SuchDokumentBuilder withContainsBeschreibung(Boolean containsBeschreibung) {
      return setField(SolrIndexFields.FIELD_CONTAINS_BESCHREIBUNG, containsBeschreibung);
    }

    public SuchDokumentBuilder withPubliziert(Boolean publiziert) {
      return setField(SolrIndexFields.FIELD_PUBLIZIERT, publiziert);
    }

    public SuchDokument build() {
      return suchDokument;
    }

  }

}
