package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import java.util.Objects;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Named;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 04.05.2021.
 * @version 1.0
 */

@Named
@FacesConverter(value = "gndEntityConverter", managed = true)
public class NormdatenGNDEntityConverter implements Converter<GNDEntityFact> {

  public static final String SEPARATOR = "#SEP#";

  @Override
  public GNDEntityFact getAsObject(FacesContext facesContext, UIComponent uiComponent, String s)
      throws ConverterException {

    String[] fact = s.split(SEPARATOR);
    if (fact.length == 4) {
      return new GNDEntityFact(fact[0], fact[1], fact[2], fact[3]);
    } else if (fact.length == 3) {
      return new GNDEntityFact(fact[0], fact[1], fact[2]);
    }
    return null;
  }

  @Override
  public String getAsString(FacesContext facesContext, UIComponent uiComponent,
      GNDEntityFact gndEntityFact) throws ConverterException {
    return gndEntityFact.getId() + SEPARATOR
        + gndEntityFact.getGndIdentifier() + SEPARATOR
        + gndEntityFact.getPreferredName()
        + (Objects.nonNull(gndEntityFact.getTypeName()) ? SEPARATOR + gndEntityFact.getTypeName() : "");
  }
}
