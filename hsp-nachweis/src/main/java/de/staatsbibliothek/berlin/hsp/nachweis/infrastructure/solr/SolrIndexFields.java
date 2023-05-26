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
  String FIELD_BEARBEITER = "bearbeiter_s";
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
