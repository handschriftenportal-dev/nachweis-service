package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.inject.Qualifier;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.10.23.
 * @version 1.0
 */
public class MMRedirectStrategies {

  @Qualifier
  @Retention(RUNTIME)
  @Target({PARAMETER, TYPE})
  public @interface Beschreibungen {

  }

  @Qualifier
  @Retention(RUNTIME)
  @Target({PARAMETER, TYPE})
  public @interface Kataloge {

  }

  @Qualifier
  @Retention(RUNTIME)
  @Target({PARAMETER, TYPE})
  public @interface Sozietaeten {

  }

  @Qualifier
  @Retention(RUNTIME)
  @Target({PARAMETER, TYPE})
  public @interface Signaturen {

  }

}
