package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = LizenzTyp.class)
public class LizenzTypConverter implements Converter<LizenzTyp> {

  @Override
  public LizenzTyp getAsObject(FacesContext context, UIComponent component, String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    return LizenzTyp.valueOf(value);
  }

  @Override
  public String getAsString(FacesContext context, UIComponent component, LizenzTyp value) {
    if (value == null) {
      return "";
    }
    return value.name();
  }
}