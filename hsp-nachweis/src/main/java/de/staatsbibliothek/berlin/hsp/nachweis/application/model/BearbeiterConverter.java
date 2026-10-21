package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Named;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@FacesConverter(value = "bearbeiterConverter", managed = true)
public class BearbeiterConverter implements Converter<Bearbeiter> {

  public static final String SEPARATOR = "#SEP#";

  @Override
  public Bearbeiter getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) throws ConverterException {
    if (s != null && !s.isEmpty()) {
      String[] fact = s.split(SEPARATOR);

      final Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
              .withId(fact[0])
              .withBearbeitername(fact[1])
              .withVorname(fact[2])
              .withNachname(fact[3])
              .build();

      log.info("Convert to bearbeiter {} ", bearbeiter);

      return bearbeiter;
    }
    return null;
  }

  @Override
  public String getAsString(FacesContext facesContext, UIComponent uiComponent,
      Bearbeiter bearbeiter) throws ConverterException {

    if (Objects.nonNull(bearbeiter)) {
      final String referenzString = bearbeiter.getId() + SEPARATOR
              + bearbeiter.getBearbeitername() + SEPARATOR
              + bearbeiter.getVorname() + SEPARATOR
              + bearbeiter.getNachname();

      log.info("convert to  bearbeiter {} String with  {}", bearbeiter);

      return referenzString;

    } else {
      return "";
    }
  }
}
