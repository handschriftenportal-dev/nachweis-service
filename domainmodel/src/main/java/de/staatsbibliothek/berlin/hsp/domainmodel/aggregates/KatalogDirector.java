package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */
public class KatalogDirector {

  private KatalogBuilder builder;

  public KatalogDirector(
      KatalogBuilder builder) {
    this.builder = builder;
  }

  public Katalog createSimpleHSKKatalog(String title, String teiXML, String publikationsJahr,
      String hskID, NormdatenReferenz verlag) {

    String id = UUID.randomUUID().toString();

    if (hskID != null && !hskID.isEmpty()) {
      id = UUID.nameUUIDFromBytes(hskID.getBytes()).toString();
    }

    return this.builder.withId(id)
        .withHSKID(hskID)
        .withTitle(title).withTEIXML(teiXML)
        .withErstelldatum(LocalDateTime.now())
        .withAenderungsdatum(LocalDateTime.now())
        .withPublikationsJahr(publikationsJahr)
        .withVerlag(verlag)
        .build();
  }
}
