package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import java.util.Objects;
import java.util.Set;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 29.06.2022
 */

@Slf4j
@Named
@FacesConverter(value = "normdatenReferenzConverter", managed = true)
public class NormdatenReferenzConverter implements Converter<NormdatenReferenz> {

  public static final String SEPARATOR = "#SEP#";

  public static final String NOT_SET = "NOTSET";

  @Override
  public NormdatenReferenz getAsObject(FacesContext facesContext, UIComponent uiComponent, String s)
      throws ConverterException {

    String language = getLanguage(facesContext);

    if (s != null && !s.isEmpty()) {
      String[] fact = s.split(SEPARATOR);

      final NormdatenReferenz normdatenReferenz = new NormdatenReferenzBuilder().withId(fact[0])
          .withName(fact[1])
          .withGndID(fact[2].equals(NOT_SET) ? null : fact[2]).withTypeName(fact[3])
          .withUri(fact[4].equals(NOT_SET) ? null : fact[4])
          .withVarianterName(
              !fact[5].equals(NOT_SET) ? Set.of(new VarianterName(fact[5], language)) : Set.of())
          .build();

      log.info("Convert to normdatenreferenz {} with language {}", normdatenReferenz, language);

      return normdatenReferenz;
    }

    return null;

  }

  private static String getLanguage(FacesContext facesContext) {
    return facesContext != null ? facesContext.getViewRoot().getLocale().getLanguage()
        : "de";
  }

  @Override
  public String getAsString(FacesContext facesContext, UIComponent uiComponent,
      NormdatenReferenz normdatenReferenz) throws ConverterException {

    String language = getLanguage(facesContext);

    if (Objects.nonNull(normdatenReferenz)) {
      final String referenzString = normdatenReferenz.getId() + SEPARATOR
          + normdatenReferenz.getName() + SEPARATOR
          + (Objects.nonNull(normdatenReferenz.getGndID()) ? normdatenReferenz.getGndID() : NOT_SET)
          + SEPARATOR
          + normdatenReferenz.getTypeName()
          + SEPARATOR
          + (Objects.nonNull(normdatenReferenz.getUri()) ? normdatenReferenz.getUri() : NOT_SET)
          + SEPARATOR
          + (Objects.nonNull(normdatenReferenz.getVarianterName())
          && !normdatenReferenz.getVarianterName().isEmpty() && !normdatenReferenz.getVariantName(
          language).isEmpty()
          ? normdatenReferenz.getVariantName(language) : NOT_SET);

      log.info("convert to normdatenreferenz {} String with language {}", normdatenReferenz,
          language);

      return referenzString;

    } else {
      return "";
    }
  }
}
