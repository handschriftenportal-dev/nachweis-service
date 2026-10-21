package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;


/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.06.2019.
 */
public class BeschreibungImportStatusTest {

  @Test
  public void testToString() {

    assertThat(ImportStatus.OFFEN.toString(), is("offen"));

    assertThat(ImportStatus.UEBERNOMMEN.toString(), is("übernommen"));

    assertThat(ImportStatus.ABGELEHNT.toString(), is("abgelehnt"));

    assertThat(ImportStatus.FEHLGESCHLAGEN.toString(), is("fehlgeschlagen"));

  }
}
