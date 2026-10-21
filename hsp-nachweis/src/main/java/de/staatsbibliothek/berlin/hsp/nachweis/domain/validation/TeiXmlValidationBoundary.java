package de.staatsbibliothek.berlin.hsp.nachweis.domain.validation;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

public interface TeiXmlValidationBoundary {

  ValidationResult validateOdd(String teiXml, String locale);
  ValidationResult validateTei(String teiXml, String locale);
  ValidationResult validateTeiXml(String teiXml, String locale, boolean odd) throws TeiXmlValidationException;

  boolean validateTeiXml(KulturObjektDokument kod) throws TeiXmlValidationException;

  boolean validateTeiXml(Beschreibung beschreibung, boolean withOdd) throws TeiXmlValidationException;

  boolean validateTeiXml(Katalog katalog) throws TeiXmlValidationException;
}
