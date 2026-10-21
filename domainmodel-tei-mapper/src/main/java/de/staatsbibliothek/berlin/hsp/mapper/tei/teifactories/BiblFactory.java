package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import java.util.Objects;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.Ref;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public final class BiblFactory {

  private BiblFactory() {
  }

  public static Bibl build(String referenzID) {

    Bibl bibl = new Bibl();
    bibl.setId(referenzID);

    Ref ref = new Ref();
    ref.getTargets().add(referenzID);
    bibl.getContent().add(ref);

    return bibl;
  }

  public static Bibl build(ExterneRessource externeRessource) {
    Objects.requireNonNull(externeRessource, "externeRessource is required");

    Bibl bibl = new Bibl();
    bibl.setType(externeRessource.typ().getTeiType());

    Ref ref = new Ref();
    ref.getTargets().add(externeRessource.url());
    ref.getContent().add(externeRessource.typ().getTeiCaption());
    bibl.getContent().add(ref);

    return bibl;
  }

  public static Bibl build(Forschungsdokumentation forschungsdokumentation) {
    Objects.requireNonNull(forschungsdokumentation, "forschungsdokumentation is required");

    Bibl bibl = new Bibl();
    if (forschungsdokumentation.hasLink()) {
      Ref ref = new Ref();
      ref.getContent().add(forschungsdokumentation.ressource());
      ref.getTargets().add(forschungsdokumentation.link());
      bibl.getContent().add(ref);
    } else {
      bibl.getContent().add(forschungsdokumentation.ressource());
    }

    return bibl;
  }
}
