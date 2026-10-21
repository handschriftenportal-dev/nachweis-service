package de.staatsbibliothek.berlin.hsp.nachweis.domain.event;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 20.02.2020.
 */
public class DomainEvents {

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface neueImporte {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ImportUpdate {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface sendIndexMessage {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface sendIndexJobEvent {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface sendRemoveMsDescFromKOD {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface sendIndexAllKODForNachweis {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface sendIndexAllBeschreibungenForNachweis {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface executeNormdatenErkennungForBeschreibungen {

  }

  @Qualifier
  @Target({FIELD, PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface sendIndexAllForPraesentation {

  }

}
