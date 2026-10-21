package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.AUTOR;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.BEARBEITER;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.BESTANDSHALTENDE_INSTITUTION;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.CONTAINS_BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.CONTAINS_BUCHSCHMUCK;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.CONTAINS_DIGITALISAT;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.LAST_UPDATE_FROM;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.LAST_UPDATE_TO;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.PUBLIZIERT;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.TYP;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria.VERWALTUNGS_TYP;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 19.01.23
 */
class FilterCriteriaTest {

  @Test
  void testCreateFilter() {
    assertEquals("containsBuchschmuck_b:true", CONTAINS_BUCHSCHMUCK.createFilter("true"));
    assertEquals("containsDigitalisat_b:true", CONTAINS_DIGITALISAT.createFilter("true"));
    assertEquals("containsBeschreibung_b:true", CONTAINS_BESCHREIBUNG.createFilter("true"));
    assertEquals("typ_s:KOD", TYP.createFilter("KOD"));
    assertEquals("publiziert_b:false", PUBLIZIERT.createFilter("false"));
    assertEquals("bestandhaltendeInstitutionName_s:(\"SBB\")",
        BESTANDSHALTENDE_INSTITUTION.createFilter("(\"SBB\")"));
    assertEquals("verwaltungsTyp_s:extern", VERWALTUNGS_TYP.createFilter("extern"));
    assertEquals("autoren:(\"Autor\")", AUTOR.createFilter("(\"Autor\")"));
    assertEquals("lastUpdate_dt:[datumVon TO *]", LAST_UPDATE_FROM.createFilter("datumVon"));
    assertEquals("lastUpdate_dt:[* TO datumBis]", LAST_UPDATE_TO.createFilter("datumBis"));
    assertEquals("bearbeiter_ss:(\"Bearbeiter\")", BEARBEITER.createFilter("(\"Bearbeiter\")"));
  }

}
