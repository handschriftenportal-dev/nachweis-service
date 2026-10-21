package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 14.02.2020.
 */
@Data
@NoArgsConstructor
public class SolrErgebnisEintrag {

  @Field(SolrIndexFields.FIELD_ID)
  protected String id;

  @Field(SolrIndexFields.FIELD_TYP)
  protected String typ;
  @Field(SolrIndexFields.FIELD_SIGNATUR)
  protected String signatur;
  @Field(SolrIndexFields.FIELD_TITEL)
  protected String title;
  @Field(SolrIndexFields.FIELD_PUBLIZIERT)
  protected Boolean publiziert;
  @Field(SolrIndexFields.FIELD_SICHTBARKEIT)
  protected String sichtbarkeit;
  @Field(SolrIndexFields.FIELD_VERWALTUNGS_TYP)
  protected String verwaltungsTyp;
  @Field(SolrIndexFields.FIELD_BEARBEITER)
  protected List<String> bearbeiter;
  @Field(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME)
  protected String bestandhaltendeInstitutionName;
  @Field(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE)
  protected String bestandhaltendeInstitutionNameFacette;
  @Field(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_ORT)
  protected String bestandhaltendeInstitutionOrt;
  @Field(SolrIndexFields.FIELD_JAHR_DER_PUBLIKATION)
  protected Integer jahrDerPublikation;
  @Field(SolrIndexFields.FIELD_LAST_UPDATE)
  protected Date lastUpdate;
  @Field(SolrIndexFields.FIELD_CONTAINS_DIGITALISAT)
  protected Boolean containsDigitalisat;
  @Field(SolrIndexFields.FIELD_CONTAINS_BUCHSCHMUCK)
  protected Boolean containsBuchschmuck;
  @Field(SolrIndexFields.FIELD_CONTAINS_BESCHREIBUNG)
  protected Boolean containsBeschreibung;
  @Field(SolrIndexFields.FIELD_AUTOREN)
  protected List<String> autoren;

}

