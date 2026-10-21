package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 15.01.21
 */
public interface SolrIndexFields {

  String FIELD_ID = "id";
  String FIELD_TITEL = "title_t";
  String FIELD_SIGNATUR = "signatur_t";
  String FIELD_TYP = "typ_s";
  String FIELD_SICHTBARKEIT = "sichtbarkeit_s";
  String FIELD_VERWALTUNGS_TYP = "verwaltungsTyp_s";
  String FIELD_BEARBEITER = "bearbeiter_ss";
  String FIELD_BESTANDHALTENDE_INSTITUTION_NAME = "bestandhaltendeInstitutionName_t";
  String FIELD_BESTANDHALTENDE_INSTITUTION_ORT = "bestandhaltendeInstitutionOrt_t";
  String FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE = "bestandhaltendeInstitutionName_s";
  String FIELD_JAHR_DER_PUBLIKATION = "jahrDerPublikation_i";
  String FIELD_LAST_UPDATE = "lastUpdate_dt";
  String FIELD_SEARCHABLE_VALUES = "searchableValues_t";
  String FIELD_CONTAINS_DIGITALISAT = "containsDigitalisat_b";
  String FIELD_CONTAINS_BUCHSCHMUCK = "containsBuchschmuck_b";
  String FIELD_CONTAINS_BESCHREIBUNG = "containsBeschreibung_b";
  String FIELD_PUBLIZIERT = "publiziert_b";
  String FIELD_VERSION = "_version_";
  String FIELD_AUTOREN = "autoren";
}
